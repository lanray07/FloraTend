package com.floratend.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

internal enum class Verification { VERIFIED, REVOKED, INVALID, UNAVAILABLE }

internal fun interface ReceiptVerifier {
    suspend fun verify(owner: String, receipt: String): Verification
}

/** Only the public HTTPS verifier address is bundled. Amazon's shared key stays on the server. */
internal class ServerReceiptVerifier(private val endpoint: String = BuildConfig.RECEIPT_VERIFIER_URL) : ReceiptVerifier {
    companion object {
        fun configured(endpoint: String = BuildConfig.RECEIPT_VERIFIER_URL): Boolean {
            val uri = runCatching { URI(endpoint) }.getOrNull() ?: return false
            return uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null && uri.query == null && uri.fragment == null
        }
    }
    override suspend fun verify(owner: String, receipt: String): Verification = withContext(Dispatchers.IO) {
        if (!configured(endpoint) || owner.isBlank() || receipt.isBlank()) return@withContext Verification.UNAVAILABLE
        var connection: HttpURLConnection? = null
        try {
            connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 12_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.outputStream.use { it.write(JSONObject().put("userId", owner).put("receiptId", receipt).toString().toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode != 200) return@withContext Verification.UNAVAILABLE
            val bytes = connection.inputStream.use { it.readBytesLimited(4096) }
            when (JSONObject(String(bytes, Charsets.UTF_8)).getString("state")) {
                "verified" -> Verification.VERIFIED
                "revoked" -> Verification.REVOKED
                "invalid" -> Verification.INVALID
                else -> Verification.UNAVAILABLE
            }
        } catch (_: Exception) { Verification.UNAVAILABLE }
        finally { connection?.disconnect() }
    }

    private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (true) {
            val size = read(buffer)
            if (size < 0) return output.toByteArray()
            check(output.size() + size <= limit)
            output.write(buffer, 0, size)
        }
    }
}

internal object ReceiptVerificationPolicy {
    fun grant(mode: String, debug: Boolean, verification: Verification): Boolean =
        (mode == "SANDBOX" && debug) || (mode == "PRODUCTION" && verification == Verification.VERIFIED)
}
