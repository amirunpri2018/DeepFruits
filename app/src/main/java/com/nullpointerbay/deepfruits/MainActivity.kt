package com.nullpointerbay.deepfruits

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        TensorFlowImageClassifier(inputName, outputName, tensorFlowInferenceInterface, inputSize, imageMean, imageStd)
    }
}
