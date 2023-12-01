package pl.ozodbek.permissionspractice.util

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import coil.request.CachePolicy
import coil.size.ViewSizeResolver
import pl.ozodbek.permissionspractice.R
import pl.ozodbek.permissionspractice.databinding.DialogPermissionAlertBinding


fun Activity.doWhileSelfCheckPermission(
    permission: String,
    onPermissionGranted: () -> Unit,
    inElseCase: () -> Unit,
) {
    when (ContextCompat.checkSelfPermission(this.applicationContext, permission)) {
        PackageManager.PERMISSION_GRANTED -> {
            onPermissionGranted()
        }

        else -> {
            inElseCase()
        }
    }
}


fun Activity.doWhileRequestPermission(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionFullyDenied: () -> Unit,
) {
    when {
        ActivityCompat.checkSelfPermission(
            this.applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED -> {
            // Permission is already granted, execute the action
            onPermissionGranted()
        }

        ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
            // Permission denied but rationale can be shown
            onPermissionDenied()
        }

        else -> {
            onPermissionFullyDenied()
        }
    }
}


fun Activity.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", this.applicationContext.packageName, null)
    intent.data = uri
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

fun openGallery(galleryLauncher: ActivityResultLauncher<Intent>) {
    val intentDocument = Intent(Intent.ACTION_GET_CONTENT)
    intentDocument.type = "image/*"
    intentDocument.putExtra(
        Constants.REQUEST_CODE,
        Constants.REQUEST_PHOTO_FROM_GALLERY
    )
    galleryLauncher.launch(intentDocument)
}

fun openCamera(cameraLauncher: ActivityResultLauncher<Intent>) {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    takePictureIntent.putExtra(
        Constants.REQUEST_CODE,
        Constants.REQUEST_PERMISSIONS_REQUEST_CODE_CAMERA
    )
    cameraLauncher.launch(takePictureIntent)
}

fun Activity.showPermissionAlert(
    title: String,
    message: String,
    ok: String,
    cancel: String,
    function: () -> Unit,
) {
    val binding = DialogPermissionAlertBinding.inflate(layoutInflater)
    val mDialog = Dialog(binding.root.context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    binding.idTitleTv.text = title
    binding.idMessageTv.text = message
    binding.noBtn.text = cancel
    binding.yesBtn.text = ok

    binding.yesBtn.setOnClickListener {
        function.invoke()
        mDialog.dismiss()
    }

    binding.noBtn.setOnClickListener {
        mDialog.dismiss()
    }

    mDialog.setCancelable(true)
    mDialog.show()

    val metrics = binding.root.resources.displayMetrics
    val width = metrics.widthPixels
    mDialog.window!!.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
}

fun View.onClick(clickListener: (View) -> Unit) {
    setOnClickListener(clickListener)
}

fun <T> ImageView.loadImage(image: T?) {
    this.load(image.takeIf { it?.toString()?.isNotBlank() == true } ?: R.drawable.ic_error_placeholder) {
        crossfade(true)
        placeholder(R.drawable.ic_error_placeholder)
        error(R.drawable.ic_error_placeholder)
        size(ViewSizeResolver(this@loadImage))
        memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.ENABLED)
    }


    /** IMAGE LOADING CACHE

    val imageLoader = ImageLoader.Builder(context)
    .respectCacheHeaders(false)
    .build()
    Coil.setImageLoader(imageLoader)


     */
}