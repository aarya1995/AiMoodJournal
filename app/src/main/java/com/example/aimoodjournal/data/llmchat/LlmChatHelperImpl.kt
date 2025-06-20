package com.example.aimoodjournal.data.llmchat

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.aimoodjournal.domain.llmchat.LlmChatHelper
import com.example.aimoodjournal.domain.llmchat.ResultListener
import com.example.aimoodjournal.domain.model.Accelerator
import com.example.aimoodjournal.domain.model.LlmConfigOptions
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import java.io.File
import javax.inject.Inject

private const val TAG = "LlmChatHelperImpl"

class LlmChatHelperImpl @Inject constructor(
    private val context: Context,
) : LlmChatHelper {

    private var llmInferenceSession: LlmInferenceSession? = null

    override fun initialize(context: Context, llmConfigOptions: LlmConfigOptions) {
        val preferredBackend = when (llmConfigOptions.accelerator) {
            Accelerator.CPU -> LlmInference.Backend.CPU
            Accelerator.GPU -> LlmInference.Backend.GPU
        }

        val taskOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(
                File(
                    context.filesDir,
                    llmConfigOptions.modelPath,
                ).absolutePath
            )
            .setMaxTokens(llmConfigOptions.maxTokens)
            .setPreferredBackend(preferredBackend)
            .setMaxNumImages(llmConfigOptions.setMaxNumImages)
            .build()

        try {
            val llmInferenceTask = LlmInference.createFromOptions(context, taskOptions)
            llmInferenceSession = LlmInferenceSession.createFromOptions(
                llmInferenceTask,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(llmConfigOptions.topK)
                    .setTopP(llmConfigOptions.topP)
                    .setTemperature(llmConfigOptions.temperature)
                    .setGraphOptions(GraphOptions.builder().setEnableVisionModality(true).build())
                    .build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing LLM inference task", e)
            return
        }
    }

    override fun resetSession(llmConfigOptions: LlmConfigOptions) {
        TODO("Not yet implemented")
    }

    override fun cleanupSession() {
        TODO("Not yet implemented")
    }

    override fun runInference(input: String, resultListener: ResultListener, images: List<Bitmap>) {
        val inferenceSession =
            llmInferenceSession ?: return // todo: throw exception if session is not initialized

        inferenceSession.addQueryChunk(input)
        for (image in images) {
            inferenceSession.addImage(BitmapImageBuilder(image).build())
        }
        inferenceSession.generateResponseAsync(resultListener)
    }

    override fun getNumTokens(input: String): Int = llmInferenceSession?.sizeInTokens(input) ?: 0
}