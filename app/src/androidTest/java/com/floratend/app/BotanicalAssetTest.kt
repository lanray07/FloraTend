package com.floratend.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class BotanicalAssetTest {
    @Test fun originalVectorsRenderOnFireGraphics() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val images=listOf("Indoor" to R.drawable.art_indoor,"Outdoor" to R.drawable.art_outdoor,
            "Succulent" to R.drawable.art_succulent,"Cactus" to R.drawable.art_cactus,
            "Herb" to R.drawable.art_herb,"Flowering" to R.drawable.art_flowering,
            "Tree" to R.drawable.art_tree,"Other" to R.drawable.art_other,
            "Splash" to R.drawable.art_splash,"Premium" to R.drawable.art_premium)
        val bitmap=Bitmap.createBitmap(1200,520,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap);canvas.drawColor(Color.rgb(250,248,242))
        val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(49,92,69);textSize=22f }
        images.forEachIndexed { index,(name,id) ->
            val x=(index%5)*240;val y=(index/5)*260
            val drawable=context.getDrawable(id)!!
            drawable.setBounds(x+24,y+12,x+216,y+204);drawable.draw(canvas)
            canvas.drawText(name,(x+24).toFloat(),(y+240).toFloat(),paint)
            assertNotEquals("$name pot must be drawn",Color.rgb(250,248,242),bitmap.getPixel(x+120,y+174))
        }
        val folder=File(context.getExternalFilesDir(null),"botanical-assets").apply { mkdirs() }
        File(folder,"native-vector-contact-sheet.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }
}
