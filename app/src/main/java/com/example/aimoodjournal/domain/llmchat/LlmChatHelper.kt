package com.example.aimoodjournal.domain.llmchat

import android.content.Context
import android.graphics.Bitmap
import com.example.aimoodjournal.domain.model.LlmConfigOptions

typealias ResultListener = (partialResult: String, done: Boolean) -> Unit

interface LlmChatHelper {

    fun initialize(context: Context, llmConfigOptions: LlmConfigOptions)

    fun resetSession(llmConfigOptions: LlmConfigOptions)

    fun cleanupSession()

    fun runInference(
        input: String,
        resultListener: ResultListener,
        images: List<Bitmap>,
    )

    fun getNumTokens(input: String): Int
}