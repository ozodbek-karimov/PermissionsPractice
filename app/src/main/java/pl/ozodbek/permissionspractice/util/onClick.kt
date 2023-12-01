package pl.ozodbek.permissionspractice.util

import android.view.View

fun View.onClick(clickListener: (View) -> Unit) {
    setOnClickListener(clickListener)
}