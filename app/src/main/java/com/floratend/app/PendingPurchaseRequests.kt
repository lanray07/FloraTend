package com.floratend.app

/** Confined to the billing coroutine's main thread, including suspended verification. */
internal class PendingPurchaseRequests<K> {
    private val owners = mutableMapOf<K, String>()
    operator fun set(request: K, owner: String) { owners[request] = owner }
    fun clear() = owners.clear()
    fun isNotEmpty() = owners.isNotEmpty()
    fun mayRefreshHistory(historyInFlight: Boolean) = !historyInFlight && owners.isEmpty()

    suspend fun process(request: K, handler: suspend (String) -> Unit) {
        val owner = owners[request] ?: return
        try { handler(owner) }
        finally { owners.remove(request) }
    }
}
