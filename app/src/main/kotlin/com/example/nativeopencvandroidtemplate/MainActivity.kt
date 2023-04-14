package com.example.nativeopencvandroidtemplate

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.nativeopencvandroidtemplate.databinding.ActivityMainBinding
import com.example.nativeopencvandroidtemplate.modal.BlurSettingDialogFragment
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("native-lib")
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
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setSupportActionBar(_binding?.myToolbar)
        setContentView(_binding!!.root)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_main_action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_gaussian_blur -> {
                val modalBlur = BlurSettingDialogFragment.newInstance()
                modalBlur.applyBlur = { blur ->
                    changeGaussianBlurImage(blur.sigmaX, blur.sigmaY)
                }
                modalBlur
                    .show(supportFragmentManager, BlurSettingDialogFragment::class.java.simpleName)
                true
            }
            R.id.action_smooth_box_filter -> {
                val modalBlur = BlurSettingDialogFragment.newInstance()
                modalBlur.applyBlur = { blur ->
                    changeBoxFilterImage(blur.sigmaX, blur.sigmaY)
                }
                modalBlur
                    .show(supportFragmentManager, BlurSettingDialogFragment::class.java.simpleName)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun changeGaussianBlurImage(sigmaX: Double, sigmaY: Double) {
        try {
            val inputBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
            val requireWidth = inputBitmap.width
            val requireHeight = inputBitmap.height
            val matFromBitmap = Mat(requireWidth, requireHeight, CvType.CV_8UC1)
            Utils.bitmapToMat(inputBitmap, matFromBitmap)
            val matAfterChangeFilter = Mat(requireWidth, requireHeight, CvType.CV_8UC1)

            Imgproc.GaussianBlur(
                matFromBitmap,
                matAfterChangeFilter,
                Size(0.0, 0.0),
                sigmaX,
                sigmaY
            )

            matFromBitmap.release()
            val outputBitmap =
                Bitmap.createBitmap(requireWidth, requireHeight, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(matAfterChangeFilter, outputBitmap)
            matAfterChangeFilter.release()

            _binding?.ivImgSample?.setImageBitmap(outputBitmap)
        } catch (ex: Exception) {
            Log.e(TAG, "${ex.message}")
        }
    }

    private fun changeBoxFilterImage(sigmaX: Double, sigmaY: Double) {
        try {
            val inputBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
            val requireWidth = inputBitmap.width
            val requireHeight = inputBitmap.height
            val matFromBitmap = Mat(requireWidth, requireHeight, CvType.CV_8UC1)
            Utils.bitmapToMat(inputBitmap, matFromBitmap)
            val matAfterChangeFilter = Mat(requireWidth, requireHeight, CvType.CV_8UC1)

            Imgproc.boxFilter(matFromBitmap, matAfterChangeFilter, -1, Size(sigmaX, sigmaY))

            matFromBitmap.release()
            val outputBitmap =
                Bitmap.createBitmap(requireWidth, requireHeight, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(matAfterChangeFilter, outputBitmap)
            matAfterChangeFilter.release()

            _binding?.ivImgSample?.setImageBitmap(outputBitmap)
        } catch (ex: Exception) {
            Log.e(TAG, "${ex.message}")
        }
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

    companion object {

        private const val TAG = "MainActivity"
    }
}
