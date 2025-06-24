package com.example.aimoodjournal.domain.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.aimoodjournal.domain.llmchat.LlmPerfMetrics
import com.example.aimoodjournal.domain.model.AIReport
import com.example.aimoodjournal.domain.model.LlmConfigOptions
import com.example.aimoodjournal.domain.model.UserData

/**
 * Repository interface for configuring the model and handling inference operations.
 * 
 * This repository encapsulates all model functionality including:
 * - Model initialization and configuration
 * - Inference execution with performance metrics
 * - AI report generation from journal entries
 * - Multimodal analysis (text + images)
 */
interface LlmRepository {
    
    /**
     * Initializes the LLM with the provided configuration.
     * 
     * @param context Android application context
     * @param llmConfigOptions Configuration options for the language model
     * @throws IllegalStateException if initialization fails
     */
    suspend fun initialize(context: Context, llmConfigOptions: LlmConfigOptions)
    
    /**
     * Checks if the LLM model is properly installed and available.
     * 
     * @param context Android application context
     * @return true if the model is installed and ready for use
     */
    suspend fun isModelInstalled(context: Context): Boolean
    
    /**
     * Generates an AI report from journal text and optional images.
     * 
     * This method performs inference on the provided input and returns both
     * the AI report and performance metrics for the inference operation.
     * 
     * @param journalText The journal text to analyze
     * @param images Optional list of images for multimodal analysis
     * @param userData User data for context-aware analysis
     * @param llmConfigOptions Current LLM configuration options to use for inference
     * @return Pair containing the AI report and performance metrics
     * @throws IllegalStateException if the report generation fails
     */
    suspend fun generateAIReport(
        journalText: String,
        images: List<Bitmap>,
        userData: UserData,
        llmConfigOptions: LlmConfigOptions
    ): Pair<AIReport, LlmPerfMetrics>
    
    /**
     * Calculates the number of tokens in the given input.
     * 
     * @param input The text input to tokenize
     * @return The estimated number of tokens
     */
    suspend fun getNumTokens(input: String): Int
    
    /**
     * Updates the LLM configuration and re-initializes the model.
     * 
     * @param context Android application context
     * @param llmConfigOptions New configuration options
     */
    suspend fun updateConfig(context: Context, llmConfigOptions: LlmConfigOptions)
} 