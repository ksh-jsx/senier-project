package com.stucs17.stockai.data

data class NotSignedStockData (
    val id : Int,
    val stockName : String,
    val stockQty : Int,
    val orderPrice: Int,
    val tradeType: String,
    val strOrderNumberOri: String,
)
