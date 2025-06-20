package com.example.aimoodjournal.domain.model

/**
 * @param modelPath: The path to where the model is stored within the project directory.
 * @param topK: The number of tokens the model considers at each step of generation. Limits predictions to the top k most-probable tokens.
 * @param topP: The cumulative probability of tokens the model considers at each step of generation. Limits predictions to tokens whose cumulative probability exceeds topP.
 * @param maxTokens: The maximum number of tokens (input tokens + output tokens) the model handles.
 * @param temperature: The amount of randomness introduced during generation. A higher temperature results in more creativity in the generated text, while a lower temperature produces more predictable generation.
 * @param setMaxNumImages: The maximum number of images the model can accept per session
 * @param accelerator: The type of accelerator to use for inference.
 *
 * See this reference: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android#configuration-options
 */
data class LlmConfigOptions(
    val modelPath: String,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxTokens: Int = 512,
    val temperature: Float = 0.8f,
    val setMaxNumImages: Int = 1,
    val accelerator: Accelerator = Accelerator.CPU,
)

enum class Accelerator { CPU, GPU }