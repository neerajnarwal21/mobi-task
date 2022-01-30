package com.task.mobiadmin.utils

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.BaseActivity


/**
 * Created by neeraj on 22/5/18.
 */
class AppUtils {

    fun loadStaticMap(baseActivity: BaseActivity, imageView: ImageView, lat: String, lng: String, onClickMap: Boolean) {
        if (!lat.isEmpty()) {
            val tgt = MyTarget(imageView, null, false)
            baseActivity.picasso.load("https://maps.googleapis.com/maps/api/staticmap?" +
                    "&zoom=14&size=350x250" +
                    "&markers=color:red" + "%7C"
                    + lat + "," + lng + "&format=jpg").placeholder(R.mipmap.ic_default_map)
                    .error(R.mipmap.ic_default_map).into(tgt)
            imageView.tag = tgt

            if (onClickMap) {
                imageView.setOnClickListener {
                    if (baseActivity.store.getBoolean(Const.SHOW_GOOGLE_MAPS_INTENT_DIALOG, true)) {
                        showDialog(baseActivity, lat, lng)
                    } else {
                        launchGoogleMaps(baseActivity, lat, lng)
                    }
                }
            }
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.mipmap.ic_default_map))
        }
    }

    private fun showDialog(baseActivity: BaseActivity, lat: String, lng: String) {
        val bldr = AlertDialog.Builder(baseActivity)
        bldr.setTitle("View Location")
        bldr.setMessage("Open Google Maps to view this location?")
        val checkBox = CheckBox(baseActivity)
        checkBox.text = "Don't ask again"
        val ll = LinearLayout(baseActivity)
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(30, 50, 0, 0)
        checkBox.layoutParams = params
        ll.addView(checkBox)
        bldr.setView(ll)
        bldr.setPositiveButton("Yes") { _, _ ->
            if (checkBox.isChecked) {
                baseActivity.store.setBoolean(Const.SHOW_GOOGLE_MAPS_INTENT_DIALOG, false)
            }
            launchGoogleMaps(baseActivity, lat, lng)
        }
        bldr.setNegativeButton("No", null)
        bldr.create().show()
    }

    fun launchGoogleMaps(baseActivity: BaseActivity, lat: String, lng: String) {
        val gmmIntentUri = Uri.parse("geo:${lat},${lng}" +
                "?q=${lat},${lng}" +
                "&?z=12")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        if (mapIntent.resolveActivity(baseActivity.packageManager) != null) {
            baseActivity.startActivity(mapIntent)
        } else {
            baseActivity.showToast("Google Maps is not installed", true)
        }
    }

    internal class MyTarget(imageView: ImageView, activity: AppCompatActivity?, onClick: Boolean) : Target {

        val imageView: ImageView
        val onClick: Boolean
        val activity: AppCompatActivity?

        init {
            this.imageView = imageView
            this.onClick = onClick
            this.activity = activity
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            imageView.setImageDrawable(placeHolderDrawable)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            imageView.setImageDrawable(errorDrawable)
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            imageView.setImageBitmap(bitmap)
            if (onClick && activity != null && !activity.isFinishing) {
                imageView.setOnClickListener {
                    loadImage(activity, bitmap)
                }
            }
        }

        fun loadImage(activity: AppCompatActivity, bitmap: Bitmap?) {
            val view = View.inflate(activity, R.layout.view_full_screen_image, null)
            val bldr = AlertDialog.Builder(activity)
            bldr.setView(view)
            val closeIV = view.findViewById<ImageView>(R.id.closeIV)
            val touchIV = view.findViewById<ImageView>(R.id.imageTIV)
            touchIV.setImageBitmap(bitmap)
            val dialog = bldr.create()
            closeIV.tag = dialog
            closeIV.setOnClickListener { v ->
                val alert = v.tag as AlertDialog
                alert.dismiss()
            }
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (!dialog.isShowing) dialog.show()
            dialog.window.setLayout(getDisplaySize(activity), getDisplaySize(activity))
        }

        fun getDisplaySize(activity: AppCompatActivity): Int {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val height = metrics.heightPixels
            val width = metrics.widthPixels
            return if (height < width) height else width
        }
    }
}