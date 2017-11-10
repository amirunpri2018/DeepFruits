package com.nullpointerbay.deepfruits.camera

import com.nullpointerbay.deepfruits.recognizer.Classifier

interface ResultsView {
    fun setResults(results: List<Classifier.Recognition>)
}
