package com.stucs17.stockai.sql

import android.content.ContentValues
import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.get
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HttpRequestHelper {
    private val client: HttpClient = HttpClient(CIO)

    suspend fun requestKtorIo(): String = withContext(Dispatchers.IO) {
        val url = "https://kdt-frontend.cat-api.programmers.co.kr/1"
        val response: HttpResponse = client.get(url)
        val responseStatus = response.status
        Log.d(ContentValues.TAG, "requestKtorIo: $responseStatus")

        if (responseStatus == HttpStatusCode.OK) {
            response.readText()
        } else {
            "error: $responseStatus"
        }
    }
}
