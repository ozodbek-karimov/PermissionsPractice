package pl.ozodbek.permissionspractice.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pl.ozodbek.permissionspractice.util.FileUtils
import pl.ozodbek.permissionspractice.R
import pl.ozodbek.permissionspractice.databinding.ActivityMainBinding
import pl.ozodbek.permissionspractice.databinding.DialogProfileImageOptionBinding
import pl.ozodbek.permissionspractice.util.doWhileRequestPermission
import pl.ozodbek.permissionspractice.util.doWhileSelfCheckPermission
import pl.ozodbek.permissionspractice.util.loadImage
import pl.ozodbek.permissionspractice.util.onClick
import pl.ozodbek.permissionspractice.util.openAppSettings
import pl.ozodbek.permissionspractice.util.openCamera
import pl.ozodbek.permissionspractice.util.openGallery
import pl.ozodbek.permissionspractice.util.showPermissionAlert
import pl.ozodbek.permissionspractice.util.viewBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val dynamicReceiverPermissionStorage by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    private val dynamicReceiverPermissionCamera by lazy { Manifest.permission.CAMERA }

    private lateinit var file: File
    private lateinit var imageBitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupClickListeners()
    }


    private fun setupClickListeners() {
        binding.getImageButton.onClick {
            showImageUploadOptions()
        }
    }


    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera(cameraLauncher)
            } else {
                requestCameraPermission()
            }

        }


    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openGallery(galleryLauncher)
            } else {
                requestStoragePermission()
            }
        }


    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val extras = data.extras
                imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras?.getParcelable("data", Bitmap::class.java)!!
                } else {
                    extras?.getParcelable("data")!!
                }
                file = FileUtils.createFile(
                    applicationContext,
                    getString(R.string.app_name),
                    "my_profile_image.png"
                )
                imageBitmap.let { bitmap ->
                    val imageLocalPath = FileUtils.saveImageToInternalStorage(file, bitmap)

                    binding.imageView.loadImage(Uri.fromFile(File(imageLocalPath)))
                }
            }

        }


    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val uri = data.data
                val imageLocalPath = FileUtils.getPathReal(applicationContext, uri!!)
                    ?.let { File(it) }

                imageLocalPath?.let {
                    file = it.absoluteFile
                }
                binding.imageView.loadImage(uri)
            }

        }

    private fun requestCameraPermission() {
        doWhileRequestPermission(
            dynamicReceiverPermissionCamera,
            onPermissionGranted = {
                openCamera(cameraLauncher)
            },
            onPermissionDenied = {
                showPermissionAlert(
                    getString(R.string.camera_permission),
                    getString(R.string.camera_permission_denied),
                    getString(R.string.ok_caps),
                    getString(R.string.cancel_caps)
                ) { requestCameraPermissionLauncher.launch(dynamicReceiverPermissionCamera) }
            },
            onPermissionFullyDenied = {
                showPermissionAlert(
                    getString(R.string.camera_permission),
                    getString(R.string.camera_permission_denied),
                    getString(R.string.settings_caps),
                    getString(R.string.cancel_caps)
                ) {
                    openAppSettings()
                }
            }
        )

    }

    private fun requestStoragePermission() {
        doWhileRequestPermission(
            dynamicReceiverPermissionStorage,
            onPermissionGranted = {
                openGallery(galleryLauncher)
            },
            onPermissionDenied = {
                showPermissionAlert(
                    getString(R.string.read_storage_permission_required),
                    getString(R.string.storage_permission_denied),
                    getString(R.string.ok_caps),
                    getString(R.string.cancel_caps)
                ) { requestStoragePermissionLauncher.launch(dynamicReceiverPermissionStorage) }
            },
            onPermissionFullyDenied = {
                showPermissionAlert(
                    getString(R.string.read_storage_permission_required),
                    getString(R.string.storage_permission_denied),
                    getString(R.string.settings_caps),
                    getString(R.string.cancel_caps)
                ) {
                    openAppSettings()
                }
            }
        )
    }

    private fun callStoragePermission() {
        doWhileSelfCheckPermission(
            dynamicReceiverPermissionStorage,
            onPermissionGranted = {
                openGallery(galleryLauncher)
            },
            inElseCase = {
                requestStoragePermissionLauncher.launch(dynamicReceiverPermissionStorage)
            }
        )
    }

    private fun callCameraPermission() {
        doWhileSelfCheckPermission(
            dynamicReceiverPermissionCamera,
            onPermissionGranted = {
                openCamera(cameraLauncher)
            },
            inElseCase = {
                requestCameraPermissionLauncher.launch(dynamicReceiverPermissionCamera)
            }
        )
    }

    private fun showImageUploadOptions() {
        val binding = DialogProfileImageOptionBinding.inflate(layoutInflater)
        val mDialog = Dialog(binding.root.context)

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(binding.root)
        mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        binding.idGalleryLl.onClick {
            callStoragePermission()
            mDialog.dismiss()
        }

        binding.idCameraLl.onClick {
            callCameraPermission()
            mDialog.dismiss()
        }

        mDialog.setCancelable(true)
        mDialog.show()


        val metrics = binding.root.resources.displayMetrics
        val width = metrics.widthPixels
        mDialog.window!!.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

}