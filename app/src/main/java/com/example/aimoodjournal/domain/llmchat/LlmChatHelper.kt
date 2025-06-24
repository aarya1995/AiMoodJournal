package com.example.aimoodjournal.domain.llmchat

import android.content.Context
import android.graphics.Bitmap
import com.example.aimoodjournal.domain.model.LlmConfigOptions

/**
 * Callback function type for receiving LLM inference results.
 * 
 * This function is called during inference to provide partial results
 * as they become available. The callback receives:
 * - partialResult: The current partial text output from the model
 * - done: Boolean indicating whether inference is complete
 * 
 * @param partialResult The current partial result from the LLM
 * @param done True if inference is complete, false if more results are expected
 */
typealias ResultListener = (partialResult: String, done: Boolean) -> Unit

/**
 * Helper interface for managing inference operations.
 * 
 * This interface provides a high-level abstraction for LLM interactions,
 * handling model initialization, inference execution, and token counting.
 * It supports both text-only and multimodal (text + images) inference
 * operations using MediaPipe's LLM inference framework.
 * 
 * The interface is designed to work with local LLM models (such as Gemma)
 * and provides streaming results through callback mechanisms for real-time
 * user experience.
 * 
 * Key features:
 * - Model initialization and configuration management
 * - Asynchronous inference with streaming results
 * - Multimodal input support (text + images)
 * - Token counting for input validation
 * - Performance optimization through backend selection
 * 
 * @see LlmConfigOptions for configuration options
 */
interface LlmChatHelper {

    /**
     * Initializes the LLM model with the specified configuration.
     * 
     * This method sets up the LLM inference session with the provided
     * configuration options. It must be called before any inference
     * operations can be performed. The initialization process includes:
     * - Loading the model from the specified path
     * - Configuring inference parameters (topK, topP, temperature)
     * - Setting up the preferred backend (CPU/GPU)
     * - Enabling vision modality for multimodal inference
     * 
     * @param context Android application context for resource access
     * @param llmConfigOptions Configuration options including model path,
     *                        inference parameters, and backend preferences.
     */
    fun initialize(context: Context, llmConfigOptions: LlmConfigOptions)

    /**
     * Runs inference on the provided input text and images.
     * 
     * This method performs asynchronous inference using the initialized
     * LLM model. It supports both text-only and multimodal inputs.
     * Results are delivered incrementally through the provided listener,
     * allowing for real-time streaming of generated text.
     * 
     * The inference process:
     * - Processes the input text and any provided images
     * - Generates text output token by token
     * - Calls the result listener with partial results as they become available
     * - Signals completion when inference is finished
     * 
     * @param input The text input to process (e.g., prompt, question, or context)
     * @param resultListener Callback function that receives partial results
     *                       during inference. Called multiple times with
     *                       incremental text output until done = true
     * @param images Optional list of bitmap images for multimodal analysis.
     *               Can be empty for text-only inference. Images are processed
     *               alongside the text input for comprehensive understanding
     */
    fun runInference(
        input: String,
        resultListener: ResultListener,
        images: List<Bitmap>,
    )

    /**
     * Calculates the number of tokens in the given input text.
     *
     * @param input The text input to tokenize
     * @return The estimated number of tokens in the input text
     */
    fun getNumTokens(input: String): Int
}