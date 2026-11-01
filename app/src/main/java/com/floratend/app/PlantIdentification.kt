package com.floratend.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class SpeciesSuggestion(val scientificName:String,val commonNames:List<String>,val score:Double)
sealed interface IdentificationResult {
    data class Suggestions(val items:List<SpeciesSuggestion>):IdentificationResult
    data object NoMatch:IdentificationResult
    data object DailyLimit:IdentificationResult
    data object Unavailable:IdentificationResult
}
interface PlantIdentifier {
    val available:Boolean
    suspend fun identify(photo:String,language:String):IdentificationResult
}

/** A public server address only. Provider keys, receipt identifiers and notes never enter this transport. */
class ServerPlantIdentifier(private val context:Context,private val endpoint:String=BuildConfig.PLANT_IDENTIFIER_URL):PlantIdentifier {
    override val available:Boolean get()=configuredIdentifier(endpoint)
    private val client=OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .callTimeout(22,TimeUnit.SECONDS).connectTimeout(8,TimeUnit.SECONDS)
        .readTimeout(18,TimeUnit.SECONDS).writeTimeout(10,TimeUnit.SECONDS).build()
    override suspend fun identify(photo:String,language:String):IdentificationResult {
        if(!available)return IdentificationResult.Unavailable
        return try {
            val bytes=withContext(Dispatchers.IO) { privatePhoto(context,photo) }
            val lang=language.lowercase(Locale.ROOT).takeIf { it.matches(Regex("[a-z]{2}")) } ?: "en"
            val request=Request.Builder().url("$endpoint?lang=$lang")
                .header("X-FloraTend-Photo-Consent","plantnet-photo-v1")
                .post(bytes.toRequestBody("image/jpeg".toMediaType())).build()
            suspendCancellableCoroutine { continuation ->
                val call=client.newCall(request)
                continuation.invokeOnCancellation { call.cancel() }
                call.enqueue(object:Callback {
                    override fun onFailure(call:Call,e:IOException) { if(continuation.isActive)continuation.resume(IdentificationResult.Unavailable) }
                    override fun onResponse(call:Call,response:Response) {
                        val result=runCatching { response.use {
                            when(it.code) {
                                429 -> IdentificationResult.DailyLimit
                                200 -> {
                                    val stream=it.body?.byteStream() ?: return@use IdentificationResult.Unavailable
                                    val bytes=stream.readBounded(65536)
                                    require(bytes.size<=65536)
                                    parseIdentification(bytes.toString(Charsets.UTF_8))
                                }
                                else -> IdentificationResult.Unavailable
                            }
                        } }.getOrDefault(IdentificationResult.Unavailable)
                        if(continuation.isActive)continuation.resume(result)
                    }
                })
            }
        } catch(cancelled:CancellationException) { throw cancelled }
        catch(failure:Exception) { IdentificationResult.Unavailable }
    }
    companion object {
        internal fun privatePhoto(context:Context,path:String):ByteArray {
            val folder=File(context.filesDir,"photos").canonicalFile
            val file=File(path).canonicalFile
            require(file.parentFile==folder && file.isFile && file.length() in 4..(2L*1024*1024))
            // Bounded read also protects against a file changing between stat and read.
            val bytes=file.inputStream().use { it.readBounded(2*1024*1024) }
            require(bytes.size in 4..(2*1024*1024) && bytes[0]==(-1).toByte() && bytes[1]==(-40).toByte() && bytes[bytes.size-2]==(-1).toByte() && bytes.last()==(-39).toByte())
            return bytes
        }
    }
}

internal fun configuredIdentifier(endpoint:String):Boolean=runCatching { URI(endpoint).let { it.scheme=="https" && !it.host.isNullOrBlank() && it.path=="/identify" && it.userInfo==null && it.query==null && it.fragment==null } }.getOrDefault(false)

internal fun java.io.InputStream.readBounded(maximum:Int):ByteArray {
    val output=java.io.ByteArrayOutputStream()
    val chunk=ByteArray(8192)
    while(true) {
        val count=read(chunk,0,minOf(chunk.size,maximum-output.size()+1))
        if(count<0)break
        require(output.size()+count<=maximum)
        output.write(chunk,0,count)
    }
    return output.toByteArray()
}

internal fun parseIdentification(text:String):IdentificationResult {
    val body=JSONObject(text)
    require(body.getString("provider")=="plantnet")
    val rows=body.getJSONArray("results")
    require(rows.length()<=3)
    if(body.getString("state")=="no_match") { require(rows.length()==0);return IdentificationResult.NoMatch }
    require(body.getString("state")=="identified" && rows.length()>0)
    fun safe(value:String):String { require(value.isNotBlank() && value==value.trim() && value.length<=180 && !value.any { it.code<32 || it.code==127 || it.code in 0x202a..0x202e || it.code in 0x2066..0x2069 } && !value.contains(Regex("https?://",RegexOption.IGNORE_CASE)));return value }
    val suggestions=(0 until rows.length()).map { index ->
        val row=rows.getJSONObject(index)
        require(row.get("score") is Number)
        val score=row.getDouble("score");require(score.isFinite() && score in 0.0..1.0)
        val names=row.getJSONArray("commonNames");require(names.length()<=3)
        SpeciesSuggestion(safe(row.getString("scientificName")),(0 until names.length()).map { safe(names.getString(it)) },score)
    }
    require(suggestions.map { it.scientificName.lowercase(Locale.ROOT) }.distinct().size==suggestions.size)
    return IdentificationResult.Suggestions(suggestions)
}

data class IdentificationState(val loading:Boolean=false,val result:IdentificationResult?=null)
class IdentificationViewModel(private val gateway:PlantIdentifier):ViewModel() {
    private val mutable=MutableStateFlow(IdentificationState())
    val state=mutable.asStateFlow()
    val available get()=gateway.available
    private var job:Job?=null
    private var generation=0
    private var boundPhoto:String?=null
    fun selectedSpecies(photo:String?,name:String):String? = name.takeIf {
        photo!=null && photo==boundPhoto && (mutable.value.result as? IdentificationResult.Suggestions)?.items?.any { item -> item.scientificName==name }==true
    }
    fun bind(photo:String?) { if(photo!=boundPhoto) {cancel();boundPhoto=photo} }
    fun cancel() { generation++;job?.cancel();job=null;mutable.value=IdentificationState() }
    // Called only by the per-request Send photo consent action.
    fun send(photo:String,language:String) {
        if(mutable.value.loading || !available || photo!=boundPhoto)return
        val requestGeneration=++generation
        mutable.value=IdentificationState(loading=true)
        job=viewModelScope.launch {
            val result=try { gateway.identify(photo,language) } catch(cancelled:CancellationException) { throw cancelled } catch(failure:Exception) { IdentificationResult.Unavailable }
            if(generation==requestGeneration && boundPhoto==photo)mutable.value=IdentificationState(result=result)
        }
    }
}
