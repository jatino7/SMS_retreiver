package com.o7solutions.sms_retreiver.model

import com.o7solutions.sms_retreiver.data_classes.QuestionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {
    @GET("api.php")
    fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String
    ): Call<QuestionsResponse>
}
