package com.nullpointerbay.deepfruits

import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Trace
import android.util.Log
import org.tensorflow.Operation
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by rafal.wachol on 13/10/2017.
 */
class TensorFlowImageClassifier(val inputName: String, val outputName: String,
                                val tensorFlowInferenceInterface: TensorFlowInferenceInterface,
                                val inputSize: Int, val imageMean: Int, val imageStd: Float, numClasses: Int)
    : Classifier {


    // Only return this many results with at least this confidence.
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.1f

    private val TAG = "TensorFlowImageClassifier"
    private var logStats = false

    // Pre-allocated buffers.
    private val labels = ArrayList<String>()
    private lateinit var intValues: IntArray
    private lateinit var floatValues: FloatArray
    private val outputs: ArrayList<Float> = ArrayList(numClasses)
    private val outputNames: ArrayList<String> = ArrayList()

    override fun recognizeImage(bitmap: Bitmap): List<Classifier.Recognition> {
        outputNames.add(outputName)
        Trace.beginSection("recognizeImage")

        Trace.beginSection("preprocessBitmap")

        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        intValues = kotlin.IntArray(bitmap.width * bitmap.height)
        floatValues = FloatArray(bitmap.width * bitmap.height * 3)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)


        for ((index, value) in intValues.withIndex()) {
            floatValues[index * 3 + 0] = ((value shr 16 and 0xFF) - imageMean) / imageStd
            floatValues[index * 3 + 1] = ((value shr 8 and 0xFF) - imageMean) / imageStd
            floatValues[index * 3 + 2] = ((value and 0xFF) - imageMean) / imageStd
        }

        Trace.endSection()

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        tensorFlowInferenceInterface.feed(inputName, floatValues, 1, inputSize.toLong(), inputSize.toLong(), 3)
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("run")
        tensorFlowInferenceInterface.run(outputNames.toTypedArray(), logStats)
        Trace.endSection()

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch")
        tensorFlowInferenceInterface.fetch(outputName, outputs.toFloatArray())
        Trace.endSection()

        // Find the best classifications.
        val pq = PriorityQueue<Classifier.Recognition>(3, kotlin.Comparator { lhs, rhs ->
            if (lhs.confidence == null || rhs.confidence == null) {
                0
            } else {
                (lhs.confidence - rhs.confidence).toInt()
            }
        })

        for ((index, value) in outputs.withIndex()) {
            if (value > THRESHOLD) {
                pq.add(Classifier.Recognition(
                        index.toString(),
                        if (labels.size > index) labels[index] else "unknown",
                        value,
                        null
                ))
            }
        }

        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionSize = Math.min(pq.size, MAX_RESULTS)
        (0..recognitionSize).forEach { recognitions.add(pq.poll()) }

        Trace.endSection()

        return recognitions

    }

    override fun enableStatLogging(debug: Boolean) {
        this.logStats = debug

    }

    override fun getStatString(): String = tensorFlowInferenceInterface.statString

    override fun close() {
        tensorFlowInferenceInterface.close()
    }

    companion object {
        fun create(assetManager: AssetManager,
                   modelFilename: String,
                   labelFilename: String,
                   inputSize: Int,
                   imageMean: Int,
                   imageStd: Float,
                   inputName: String,
                   outputName: String): Classifier {


            val labels = arrayListOf<String>()

            val actualFilename = labelFilename.split("file:///android_asset/")[1]
            Log.i(TAG, "Reading labels from: " + actualFilename)
            BufferedReader(InputStreamReader(assetManager.open(actualFilename))).useLines { sequence ->
                sequence.forEach {
                    labels.add(it)
                }
            }

            val tensorFlowInferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)
            val operation: Operation = tensorFlowInferenceInterface.graphOperation(outputName)
            val numOfClasses = operation.output(0).shape().size(1)

            Timber.i("Read ${labels.size} labels, output layer size is $numOfClasses")


            return TensorFlowImageClassifier(inputName, outputName,
                    tensorFlowInferenceInterface, inputSize, imageMean, imageStd, numOfClasses.toInt())

        }
    }
}