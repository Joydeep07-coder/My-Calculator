package com.example.data

import com.example.model.CurrencyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LiveExchangeRatesResult(
    val ratesFromUsd: Map<String, Double>,
    val sourceName: String,
    val lastUpdatedTimestamp: String,
)

interface ExchangeRateProvider {
    suspend fun fetchUsdRates(): Result<LiveExchangeRatesResult>
}

sealed interface ExchangeRateUiState {
    data object Loading : ExchangeRateUiState
    data class Success(
        val currencies: List<CurrencyInfo>,
        val sourceName: String,
        val lastUpdated: String,
    ) : ExchangeRateUiState

    data class Error(
        val message: String,
    ) : ExchangeRateUiState
}

/**
 * Fetches live USD-based exchange rates from the public ExchangeRate-API v6 open endpoint.
 */
class OpenExchangeRateProvider(
    private val endpointUrl: String = "https://open.er-api.com/v6/latest/USD",
) : ExchangeRateProvider {

    override suspend fun fetchUsdRates(): Result<LiveExchangeRatesResult> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }
            try {
                val statusCode = connection.responseCode
                if (statusCode !in 200..299) {
                    throw IllegalStateException("Exchange rate service returned HTTP $statusCode.")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseOpenExchangeRateJson(body)
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        fun parseOpenExchangeRateJson(jsonBody: String): LiveExchangeRatesResult {
            val root = JSONObject(jsonBody)
            val resultStatus = root.optString("result", "")
            if (resultStatus.isNotEmpty() && resultStatus != "success") {
                val errType = root.optString("error-type", "Unknown API error")
                throw IllegalStateException("Exchange rate error: $errType")
            }
            val ratesJson = root.optJSONObject("rates")
                ?: throw IllegalStateException("Exchange rates missing from response.")

            val rates = mutableMapOf<String, Double>()
            val keys = ratesJson.keys()
            while (keys.hasNext()) {
                val code = keys.next()
                val value = ratesJson.optDouble(code, Double.NaN)
                if (value.isFinite() && value > 0.0) {
                    rates[code.uppercase()] = value
                }
            }
            if (rates.isEmpty()) {
                throw IllegalStateException("No valid exchange rates returned.")
            }

            val rawTimestamp = root.optString("time_last_update_utc", "").trim()
            val timestamp = rawTimestamp.ifEmpty { "Unknown update time" }
            val sourceName = "ExchangeRate-API (open.er-api.com)"

            return LiveExchangeRatesResult(
                ratesFromUsd = rates,
                sourceName = sourceName,
                lastUpdatedTimestamp = timestamp,
            )
        }
    }
}
