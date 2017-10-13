package com.nullpointerbay.deepfruits

import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.Operation
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by rafal.wachol on 13/10/2017.
 */
class TensorFlowImageClassifier(val inputName: String, val outputName: String,
                                val tensorFlowInferenceInterface: TensorFlowInferenceInterface,
                                val inputSize: Int, val imageMean: Int, val imageStd: Float)
    : Classifier {

    override fun recognizeImage(bitmap: Bitmap): List<Classifier.Recognition> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableStatLogging(debug: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStatString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val TAG = "TensorFlowImageClassifier"

    // Only return this many results with at least this confidence.
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.1f

    // Pre-allocated buffers.
    private val labels = ArrayList<String>()
    private val intValues: IntArray? = null
    private val floatValues: FloatArray? = null
    private val outputs: FloatArray? = null
    private val outputNames: Array<String>? = null

    private val logStats = false


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
                    tensorFlowInferenceInterface, inputSize, imageMean, imageStd)

        }
    }
}