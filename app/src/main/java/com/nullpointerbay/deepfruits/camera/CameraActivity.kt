package com.nullpointerbay.deepfruits.camera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nullpointerbay.deepfruits.R

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit()
        }
    }
}