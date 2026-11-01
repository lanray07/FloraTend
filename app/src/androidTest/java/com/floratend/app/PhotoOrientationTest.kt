package com.floratend.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/** Eight controlled EXIF transforms through ContentResolver; no user photographs. */
class PhotoOrientationTest {
    @Test fun importsEveryExifOrientationAsUprightPrivatePixels() {
        val app=ApplicationProvider.getApplicationContext<FloraApp>()
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val colors=listOf(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW)
        val expected=listOf(listOf(0,1,2,3),listOf(1,0,3,2),listOf(3,2,1,0),listOf(2,3,0,1),
            listOf(0,2,1,3),listOf(2,0,3,1),listOf(3,1,2,0),listOf(1,3,0,2))
        for(orientation in 1..8) {
            val source=File(app.cacheDir,"qa-photos/orientation-$orientation.jpg").apply { parentFile!!.mkdirs() }
            var imported:File?=null
            try {
                val image=Bitmap.createBitmap(80,40,Bitmap.Config.ARGB_8888)
                for(y in 0 until 40) for(x in 0 until 80) image.setPixel(x,y,colors[(if(y>=20) 2 else 0)+(if(x>=40) 1 else 0)])
                source.outputStream().use { image.compress(Bitmap.CompressFormat.JPEG,100,it) };image.recycle()
                ExifInterface(source.path).apply { setAttribute(ExifInterface.TAG_ORIENTATION,orientation.toString());saveAttributes() }
                val uri=FileProvider.getUriForFile(app,BuildConfig.APPLICATION_ID+".qa.fixtures",source)
                imported=File(Photos.import(app,uri))
                val decoded=BitmapFactory.decodeFile(imported.path)
                try {
                    assertEquals("Width for EXIF $orientation",if(orientation>=5) 40 else 80,decoded.width)
                    assertEquals("Height for EXIF $orientation",if(orientation>=5) 80 else 40,decoded.height)
                    val points=listOf(5 to 5,decoded.width-6 to 5,5 to decoded.height-6,decoded.width-6 to decoded.height-6)
                    points.forEachIndexed { index,(x,y) ->
                        val actual=decoded.getPixel(x,y);val target=colors[expected[orientation-1][index]]
                        assertTrue("EXIF $orientation corner $index",kotlin.math.abs(Color.red(actual)-Color.red(target))<35 && kotlin.math.abs(Color.green(actual)-Color.green(target))<35 && kotlin.math.abs(Color.blue(actual)-Color.blue(target))<35)
                    }
                } finally { decoded.recycle() }
                assertEquals(ExifInterface.ORIENTATION_UNDEFINED,ExifInterface(imported.path).getAttributeInt(ExifInterface.TAG_ORIENTATION,0))
            } finally { source.delete();imported?.delete() }
        }
    }
}
