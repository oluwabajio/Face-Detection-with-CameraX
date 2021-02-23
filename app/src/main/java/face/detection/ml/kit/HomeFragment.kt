package face.detection.ml.kit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import face.detection.ml.kit.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment() : Fragment() {
    lateinit var binding: FragmentHomeBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

//binding.drawon.setOnClickListener {  }

        initCamera()

        binding.cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
        return view
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(activity), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                Log.e(TAG, msg)
            }
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = context?.let { ProcessCameraProvider.getInstance(it) }

        cameraProviderFuture?.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                    }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder()
                    .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma: Double, imageProxy: ImageProxy ->
                            //      Log.d(TAG, "Average luminosity: $luma")
                            activity?.runOnUiThread {
                                val bitmap = viewFinder.bitmap
                                binding.imgView.setImageBitmap(bitmap)




                                val mediaImage = imageProxy.image
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)


                                val realTimeOpts = FaceDetectorOptions.Builder()
                                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                                        .build()
                                val detector = FaceDetection.getClient()
                                val result = detector.process(image)
                                        .addOnSuccessListener { faces ->
                                            // Task completed successfully
                                            // ...
                                            for (face in faces) {


                                                val previewSize = preview.attachedSurfaceResolution?: Size(0, 0)
                                                Log.e(TAG, "startCamera: Preview Size = width = "+ previewSize.width + " height = "+ previewSize.height )

                                                Log.e(TAG, "startCamera: ImageItself Size = width = "+ image.width + " height = "+ image.height )

                                                Log.e(TAG, "startCamera: Face Size = width = "+ (face.boundingBox.left -face.boundingBox.right) + " height = "+ (face.boundingBox.bottom -face.boundingBox.top) )
                                                Log.e(TAG, "startCamera: Face Size top = "+face.boundingBox.top + " left = "+ face.boundingBox.left + " right = "+face.boundingBox.right + " bottom "+ face.boundingBox.bottom )



                                                Log.e(TAG, "startCamera: Drawon Size = width = "+binding.drawon.width + " height = "+ binding.drawon.height )


//
                                                val size: Size = Size(image.width, image.height)
                                                val min: Int = Math.min(size.getWidth(), size.getHeight())
                                                val max: Int = Math.max(size.getWidth(), size.getHeight())
                                               var mWidthScaleFactor : Float= (binding.drawon.width.toFloat() / min).toFloat()
                                               var mHeightScaleFactor : Float= (binding.drawon.height.toFloat() / max).toFloat()
//
//
//
                                                val bounds = face.boundingBox
                                                val rectf = RectF(bounds) // Convert the rectangle dimension values from int type to float type



                                                rectf.left = rectf.left * mWidthScaleFactor //scale the dimensions with our scaling factor

                                                rectf.top = rectf.top *mHeightScaleFactor
                                                rectf.right = rectf.right * mWidthScaleFactor
                                                rectf.bottom = rectf.bottom * mHeightScaleFactor

                                                Log.e(TAG, "startCamera: top = "+rectf.top + " left = "+ rectf.left + " width = "+rectf.right + " height "+ rectf.bottom )
                                                binding.drawon.drawRecto(rectf)
                                            }
                                            Log.e(TAG, "startCamera: Success - Size is " + faces.size)

                                        }
                                        .addOnFailureListener { e ->
                                            // Task failed with an exception
                                            // ...
                                            Log.e(TAG, "startCamera: Failed" + e.message)
                                        }
                                        .addOnCompleteListener {
                                            mediaImage?.close()
                                            imageProxy.close()
                                        }

                            }
                        })
                    }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else activity?.filesDir!!
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun initCamera() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(activity,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    private class LuminosityAnalyzer(private val listener:(luma: Double, imageProxy:ImageProxy) -> Unit) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {

            val buffer = imageProxy.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma, imageProxy)
            ///   Log.e(TAG, "analyze: aaa = "+ luma )

            //  val image = InputImage.fromBitmap(bitmap, 0)

//
//            val mediaImage = imageProxy.image
//            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//
//
//            val realTimeOpts = FaceDetectorOptions.Builder()
//                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
//                    .build()
//            val detector = FaceDetection.getClient()
//            val result = detector.process(image)
//                    .addOnSuccessListener { faces ->
//                        // Task completed successfully
//                        // ...
//
//
//
//                        Log.e(TAG, "startCamera: Success - Size is " + faces.size)
//
//
////                        val faceBounds = faces.map { face -> face.toFaceBounds(this) }
////                        mainExecutor.execute { faceBoundsOverlay.updateFaces(faceBounds) }
//
////                    // Task completed successfully
////                    val drawingView = DrawRect(activity, faces)
////                    drawingView.draw(Canvas(bitmap))
////                    activity.runOnUiThread { sampleImage.setImageBitmap(bitmap) }
//
////                    for (face in faces) {
////                        val bounds = face.boundingBox
////                        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
////                        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
////
////
////                        // If contour detection was enabled:
//////                                      val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
//////                                      val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
////
////
////                    }
//
//                    }
//                    .addOnFailureListener { e ->
//                        // Task failed with an exception
//                        // ...
//                        Log.e(TAG, "startCamera: Failed" + e.message)
//                    }
//                    .addOnCompleteListener {
//                        mediaImage?.close()
//                        imageProxy.close()
//                    }


        }



    }

    interface onDrawStarted {
        fun drawBox()
    }

}