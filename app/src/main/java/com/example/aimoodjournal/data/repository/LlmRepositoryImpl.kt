package com.example.aimoodjournal.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.aimoodjournal.data.llmchat.getPromptContext
import com.example.aimoodjournal.domain.llmchat.LlmChatHelper
import com.example.aimoodjournal.domain.llmchat.LlmPerfMetrics
import com.example.aimoodjournal.domain.model.AIReport
import com.example.aimoodjournal.domain.model.LlmConfigOptions
import com.example.aimoodjournal.domain.model.UserData
import com.example.aimoodjournal.domain.repository.LlmRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import javax.inject.Inject
import javax.inject.Singleton

private const val MODEL_NAME = "gemma-3n-E4B-it-int4.task"
private const val MODEL_DOWNLOAD_DIRECTORY = "models"

/**
 * Implementation of LlmRepository that handles LLM operations using LlmChatHelper.
 * 
 * This implementation encapsulates all LLM-related logic including:
 * - Model initialization and management
 * - Inference execution with performance tracking
 * - AI report generation and parsing
 * - Multimodal analysis capabilities
 */
@Singleton
class LlmRepositoryImpl @Inject constructor(
    private val llmChatHelper: LlmChatHelper
) : LlmRepository {

    companion object {
        private const val TAG = "LlmRepositoryImpl"
    }

    override suspend fun initialize(context: Context, llmConfigOptions: LlmConfigOptions) {
        withContext(Dispatchers.IO) {
            try {
                llmChatHelper.initialize(context, llmConfigOptions)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize LLM", e)
                throw IllegalStateException("Failed to initialize LLM: ${e.message}")
            }
        }
    }

    override suspend fun isModelInstalled(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val modelDir = File(context.filesDir, MODEL_DOWNLOAD_DIRECTORY)
                val modelFile = File(modelDir, MODEL_NAME)
                modelFile.exists() && modelFile.length() > 0
            } catch (e: Exception) {
                Log.e(TAG, "Error checking model installation", e)
                false
            }
        }
    }

    override suspend fun generateAIReport(
        journalText: String,
        images: List<Bitmap>,
        userData: UserData,
        llmConfigOptions: LlmConfigOptions
    ): Pair<AIReport, LlmPerfMetrics> {
        return withContext(Dispatchers.IO) {
            try {
                val promptContext = buildPromptContext(userData)
                val input = promptContext + journalText

                // Initialize performance metrics
                var timeToFirstToken = 0f
                var prefillSpeed = 0f
                var decodeSpeed = 0f
                var latencySecs = 0f

                var prefillTokens = llmChatHelper.getNumTokens(input)
                prefillTokens += images.size * 257 // Approximate tokens per image

                var firstRun = true
                var firstTokenTs = 0L
                var decodeTokens = 0
                val start = System.currentTimeMillis()
                val output = StringBuilder()

                // Run inference with suspendCoroutine to make it blocking
                suspendCoroutine { continuation ->
                    llmChatHelper.runInference(
                        input,
                        images = images,
                        resultListener = { partialResult, done ->
                            output.append(partialResult)
                            val curTs = System.currentTimeMillis()

                            if (firstRun) {
                                firstTokenTs = System.currentTimeMillis()
                                timeToFirstToken = (firstTokenTs - start) / 1000f
                                prefillSpeed = prefillTokens / timeToFirstToken
                                firstRun = false
                            } else {
                                decodeTokens++
                            }

                            if (done) {
                                latencySecs = (curTs - start).toFloat() / 1000f
                                decodeSpeed = decodeTokens / ((curTs - firstTokenTs) / 1000f)
                                if (decodeSpeed.isNaN()) {
                                    decodeSpeed = 0f
                                }

                                // Resume the coroutine when inference is complete
                                continuation.resume(Unit)
                            }
                        }
                    )
                }

                // Parse the AI response
                val finalResponse = output.toString()
                val aiReport = parseAIResponse(finalResponse)
                    ?: throw IllegalStateException("Failed to parse AI response")

                // Create performance metrics using the current config
                val llmPerfMetrics = LlmPerfMetrics(
                    timeToFirstToken = timeToFirstToken,
                    prefillSpeed = prefillSpeed,
                    decodeSpeed = decodeSpeed,
                    latencySeconds = latencySecs,
                    accelerator = llmConfigOptions.accelerator.name,
                )

                Pair(aiReport, llmPerfMetrics)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating AI report", e)
                throw IllegalStateException("Failed to generate AI report: ${e.message}")
            }
        }
    }

    override suspend fun getNumTokens(input: String): Int {
        return withContext(Dispatchers.IO) {
            llmChatHelper.getNumTokens(input)
        }
    }

    override suspend fun updateConfig(context: Context, llmConfigOptions: LlmConfigOptions) {
        withContext(Dispatchers.IO) {
            try {
                llmChatHelper.initialize(context, llmConfigOptions)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update LLM config", e)
                throw IllegalStateException("Failed to update LLM config: ${e.message}")
            }
        }
    }

    /**
     * Builds the prompt context using user data for personalized analysis.
     */
    private fun buildPromptContext(userData: UserData): String {
        return getPromptContext(userData)
    }

    /**
     * Parses the AI response and converts it to an AIReport object.
     */
    private fun parseAIResponse(response: String): AIReport? {
        return try {
            // Clean the response - remove any extra text before or after the JSON
            val jsonStart = response.indexOf('{')
            val jsonEnd = response.lastIndexOf('}') + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = response.substring(jsonStart, jsonEnd)
                Gson().fromJson(jsonString, AIReport::class.java)
            } else {
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing AI response JSON", e)
            null
        }
    }
} 