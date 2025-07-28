package com.samikhan.draven.data.api

import com.samikhan.draven.data.model.ApiRequest
import com.samikhan.draven.data.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    
    @POST("v1/chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ApiRequest
    ): ApiResponse
} 