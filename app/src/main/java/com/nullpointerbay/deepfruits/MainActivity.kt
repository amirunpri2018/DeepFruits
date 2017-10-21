package com.nullpointerbay.deepfruits

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.graphics.Bitmap



class MainActivity : AppCompatActivity() {

    private val INPUT_SIZE = 224
    private val IMAGE_MEAN = 128
    private val IMAGE_STD = 128.0f
    private val INPUT_NAME = "input"
    private val OUTPUT_NAME = "final_result"

    private val MODEL_FILE = "file:///android_asset/graph.pb"
    private val LABEL_FILE = "file:///android_asset/labels.txt"

    lateinit var classifier: Classifier


    lateinit var recognitionScoreView: RecognitionScoreView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recognitionScoreView = findViewById(R.id.recognition_view)
        val btnClassify = findViewById<Button>(R.id.btn_classify)
        btnClassify.setOnClickListener {
            val flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower_sample)
            val resized = Bitmap.createScaledBitmap(flowerBitmap, INPUT_SIZE, INPUT_SIZE, true)

            val recognizeImage = classifier.recognizeImage(resized)
            recognitionScoreView.setResults(recognizeImage)

        }


        classifier = TensorFlowImageClassifier.create(
                assets,
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME
        )

//        TensorFlowImageClassifier(inputName, outputName, tensorFlowInferenceInterface, inputSize, imageMean, imageStd)
    }
}
