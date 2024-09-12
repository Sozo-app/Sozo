package com.animestudios.animeapp.script
import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.renderscript.Element

object RenderScriptBlur {
    fun blur(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val renderScript = RenderScript.create(context)
        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val output = Allocation.createTyped(renderScript, input.type)

        val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blurScript.setRadius(radius)
        blurScript.setInput(input)
        blurScript.forEach(output)
        output.copyTo(bitmap)

        renderScript.destroy()
        return bitmap
    }
}
