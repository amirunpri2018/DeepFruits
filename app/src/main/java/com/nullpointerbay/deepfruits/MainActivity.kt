package com.nullpointerbay.deepfruits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Button
import com.nullpointerbay.deepfruits.recognizer.Classifier
import com.nullpointerbay.deepfruits.recognizer.TensorFlowImageClassifier
import com.nullpointerbay.deepfruits.tensor.ClassifierActivity


class MainActivity : AppCompatActivity() {

    private val INPUT_SIZE = 224
    private val IMAGE_MEAN = 128
    private val IMAGE_STD = 128.0f
    private val INPUT_NAME = "input"
    private val OUTPUT_NAME = "final_result"

    private val MODEL_FILE = "file:///android_asset/graph.pb"
    private val LABEL_FILE = "file:///android_asset/labels.txt"

    private lateinit var classifier: Classifier

    private lateinit var recognitionScoreView: RecognitionScoreView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Deep fruits"

        recognitionScoreView = findViewById(R.id.recognition_view)
        val btnClassify = findViewById<Button>(R.id.btn_classify)
        btnClassify.setOnClickListener {
            //            classifySampleImage()

            val intent = Intent(this, ClassifierActivity::class.java)
            startActivity(intent)

        }

        classifier = TensorFlowImageClassifier.create(assets, MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME)


    }

    private fun classifySampleImage() {
        val flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower_sample)
        val resized = Bitmap.createScaledBitmap(flowerBitmap, INPUT_SIZE, INPUT_SIZE, true)

        val recognizeImage = classifier.recognizeImage(resized)
        recognitionScoreView.setResults(recognizeImage)
    }
}
