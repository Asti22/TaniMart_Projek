package com.example.tanimart.data.model
data class OrderModel(
    val orderId: String = "",
    val consumerId: String = "",
    val consumerEmail: String = "",
    val consumerAddress: String = "",
    val items: List<ProductModel> = emptyList(),
    val totalPrice: Int = 0,
    val paymentMethod: String = "COD",
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)