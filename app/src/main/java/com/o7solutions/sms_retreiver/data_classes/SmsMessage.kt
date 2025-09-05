package com.o7solutions.sms_retreiver.data_classes

data class SmsMessage(
    val id: String = "",
    val from: String = "",
    val message: String = "",
    val type: String = "",
    val date: String = ""
)
