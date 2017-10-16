package com.nullpointerbay.deepfruits

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Generic interface for interacting with different recognition engines.
 */
interface Classifier {

    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    fun enableStatLogging(debug: Boolean)

    fun getStatString(): String

    fun close()

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    data class Recognition(val id: String, val title: String, val confidence: Float?, val location: RectF?) {


        override fun toString(): String {
            var resultString = ""
            resultString += "[$id] "

            resultString += title + " "

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f)
            }

            resultString += location!!.toString() + " "

            return resultString.trim { it <= ' ' }
        }
    }

}
