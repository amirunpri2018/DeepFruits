package com.nullpointerbay.deepfruits.camera

import android.Manifest
import android.content.pm.PackageManager
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Size
import android.view.KeyEvent
import android.widget.Toast
import com.nullpointerbay.deepfruits.R
import timber.log.Timber

class CameraActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(p0: ImageReader?) {

    }

    var isDebug = false
        private set

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    protected val desiredPreviewFrameSize: Size = Size(640, 480);

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate " + this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        if (hasPermission()) {
            setFragment()
        } else {
            requestPermission()
        }
    }

    @Synchronized public override fun onResume() {
        Timber.d("onResume " + this)
        super.onResume()

        handlerThread = HandlerThread("inference")
        handlerThread?.start()
        handler = Handler(handlerThread?.looper)
    }

    @Synchronized public override fun onPause() {
        Timber.d("onPause " + this)

        if (!isFinishing) {
            Timber.d("Requesting finish")
            finish()
        }

        handlerThread!!.quitSafely()
        try {
            handlerThread!!.join()
            handlerThread = null
            handler = null
        } catch (e: InterruptedException) {
            Timber.e(e, "Exception!")
        }

        super.onPause()
    }

    @Synchronized public override fun onStop() {
        Timber.d("onStop " + this)
        super.onStop()
    }

    @Synchronized public override fun onDestroy() {
        Timber.d("onDestroy " + this)
        super.onDestroy()
    }

    @Synchronized protected fun runInBackground(r: Runnable) {
        if (handler != null) {
            handler!!.post(r)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment()
                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(this@CameraActivity, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show()
            }
            requestPermissions(arrayOf(PERMISSION_CAMERA, PERMISSION_STORAGE), PERMISSIONS_REQUEST)
        }
    }

    protected fun setFragment() {
        val fragment = CameraConnectionFragment.newInstance(
                object : CameraConnectionFragment.ConnectionCallback {
                    override fun onPreviewSizeChosen(size: Size, cameraRotation: Int) {

                    }
//                    override fun onPreviewSizeChosen(size: Size, rotation: Int) {
//                        onPreviewSizeChosen(size, rotation)
//                    }
                },
                this,
                desiredPreviewFrameSize)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    protected fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray>) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                Timber.d("Initializing buffer %d at size %d", i, buffer.capacity())
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i])
        }
    }

    fun requestRender() {
        val overlay = findViewById<OverlayView>(R.id.debug_overlay)
        if (overlay != null) {
            overlay.postInvalidate()
        }
    }

    fun addCallback(callback: OverlayView.DrawCallback) {
        val overlay = findViewById<OverlayView>(R.id.debug_overlay)
        if (overlay != null) {
            overlay.addCallback(callback)
        }
    }

    open fun onSetDebug(debug: Boolean) {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            isDebug = !isDebug
            requestRender()
            onSetDebug(isDebug)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    protected fun onPreviewSizeChosen(size: Size, rotation: Int) {}

    companion object {

        private val PERMISSIONS_REQUEST = 1

        private val PERMISSION_CAMERA = Manifest.permission.CAMERA
        private val PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
