package com.o7solutions.sms_retreiver.data_classes

data class User(
    val id: String,
    val email: String
)
{
    constructor(): this("","")
}