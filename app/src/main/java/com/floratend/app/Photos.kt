package com.floratend.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.util.UUID

object Photos {
    // Downsample before decoding; copy into private storage, never retain external grants.
    fun import(context: Context, uri: Uri): String {
        val orientation=runCatching {
            context.contentResolver.openInputStream(uri).use { stream ->
                requireNotNull(stream)
                ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
        require(options.outWidth > 0 && options.outHeight > 0)
        var sample = 1
        while (options.outWidth / sample > 1600 || options.outHeight / sample > 1600) sample *= 2
        val bitmap = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: error("Invalid image")
        val transform=Matrix().apply {
            if(orientation in listOf(2,4,5,7)) postScale(-1f,1f)
            val degrees=when(orientation) { 3,4 -> 180f; 5,8 -> 270f; 6,7 -> 90f; else -> 0f }
            if(degrees!=0f) postRotate(degrees)
        }
        var upright:Bitmap?=null
        try {
            upright=if(transform.isIdentity) bitmap else Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,transform,true)
            return save(context,upright)
        } finally {
            if(upright!==bitmap) upright?.recycle()
            bitmap.recycle()
        }
    }
    fun save(context: Context, bitmap: Bitmap): String {
        val folder = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)) }
        return file.absolutePath
    }
}
