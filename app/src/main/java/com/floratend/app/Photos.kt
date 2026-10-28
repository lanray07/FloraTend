package com.floratend.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

object Photos {
    // Downsample before decoding; copy into private storage, never retain external grants.
    fun import(context: Context, uri: Uri): String {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
        require(options.outWidth > 0 && options.outHeight > 0)
        var sample = 1
        while (options.outWidth / sample > 1600 || options.outHeight / sample > 1600) sample *= 2
        val bitmap = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: error("Invalid image")
        return save(context, bitmap).also { bitmap.recycle() }
    }
    fun save(context: Context, bitmap: Bitmap): String {
        val folder = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)) }
        return file.absolutePath
    }
}
