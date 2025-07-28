package com.samikhan.draven.data.model

data class ApiRequest(
    val messages: List<Message>,
    val model: String = "nvidia/llama-3.1-nemotron-ultra-253b-v1",
    val max_tokens: Int = 4096,
    val temperature: Double = 0.0,
    val top_p: Double = 0.95,
    val frequency_penalty: Int = 0,
    val presence_penalty: Int = 0,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ApiResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: Message,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
) 