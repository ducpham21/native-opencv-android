package com.example.nativeopencvandroidtemplate

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.nativeopencvandroidtemplate.databinding.ActivityCameraBinding
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class CameraActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        const val CAMERA_PERMISSION_REQUEST = 1
        const val TAG = "CameraActivity"

        fun startCameraActivity(context: Context) {
            context.startActivity(Intent(context, CameraActivity::class.java))
        }
    }

    private var _binding: ActivityCameraBinding? = null

    private var mRgba: Mat? = null
    private var mGray: Mat? = null
    private var mTemplate: Mat? = null
    private var mResult: Mat? = null

    private val mMethod = Imgproc.TM_CCOEFF_NORMED

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("native-lib")
                    _binding?.cameraOpenCv?.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _binding = ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(_binding?.root)
        _binding?.cameraOpenCv?.setCvCameraViewListener(this)
        _binding?.cameraOpenCv?.visibility = SurfaceView.VISIBLE
        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST
        )

    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                _binding?.cameraOpenCv?.setCameraPermissionGranted()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (_binding?.cameraOpenCv != null) {
            _binding?.cameraOpenCv?.disableView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_binding?.cameraOpenCv != null) {
            _binding?.cameraOpenCv?.disableView()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat()
        mGray = Mat()
        mTemplate = Mat()
        mResult = Mat()
    }

    override fun onCameraViewStopped() {
        mRgba?.release()
        mGray?.release()
        mTemplate?.release()
        mResult?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame?.rgba()
        mGray = inputFrame?.gray()
        mTemplate = Utils.loadResource(this, R.drawable.heart_sample)
        Imgproc.resize(mTemplate, mTemplate, Size(100.0, 100.0))

        Imgproc.cvtColor(mTemplate, mTemplate, Imgproc.COLOR_BGR2GRAY)

        Imgproc.matchTemplate(mGray, mTemplate, mResult, mMethod)

        Log.d(TAG, "result: $mResult")
        Log.d(TAG, "screen: $mRgba")

        val mmr = Core.minMaxLoc(mResult)

        Log.d(TAG, "mmr: {min: ${mmr.minLoc}, max: ${mmr.maxLoc}}")

        val topLeft = mmr.maxLoc
        val bottomRight = Point(topLeft.x + mTemplate!!.cols(), topLeft.y + mTemplate!!.rows())

        Imgproc.rectangle(
            mRgba, topLeft, bottomRight, Scalar(0.0, 255.0, 0.0), 2
        )

        val result = Mat()
        Imgproc.cvtColor(mRgba, result, Imgproc.COLOR_RGBA2BGRA)
        return result
    }
}