package com.o7solutions.sms_retreiver.data_classes


data class QuestionsResponse(
    val response_code: Int,
    val results: List<Question>
)