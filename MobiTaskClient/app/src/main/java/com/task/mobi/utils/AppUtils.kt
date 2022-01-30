package com.task.mobi.utils

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.ContextThemeWrapper
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.task.mobi.R
import com.task.mobi.activity.BaseActivity


/**
 * Created by neeraj on 22/5/18.
 */
class AppUtils {

    fun loadStaticMap(baseActivity: BaseActivity, imageView: ImageView, lat: String, lng: String, onClickMap: Boolean) {
        val tgt = MyTarget(imageView)
        baseActivity.picasso.load("https://maps.googleapis.com/maps/api/staticmap?" +
                "&zoom=14&size=350x250" +
                "&markers=color:blue" + "%7C"
                + lat + "," + lng + "&format=jpg").placeholder(R.mipmap.ic_default_map).into(tgt)
        imageView.setTag(tgt)

        if (onClickMap) {
            imageView.setOnClickListener({
                if (baseActivity.store.getBoolean(Const.SHOW_GOOGLE_MAPS_INTENT_DIALOG, true)) {
                    showDialog(baseActivity, lat, lng)
                } else {
                    launchGoogleMaps(baseActivity, lat, lng)
                }
            })
        }
    }

    private fun showDialog(baseActivity: BaseActivity, lat: String, lng: String) {
        val bldr = AlertDialog.Builder(baseActivity)
        bldr.setTitle("View Location")
        bldr.setMessage("Open Google Maps to view this location?")
        val checkBox = CheckBox(ContextThemeWrapper(baseActivity, R.style.colorPrimaryTheme))
        checkBox.setText("Don't ask again")
        val ll = LinearLayout(baseActivity)
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(30, 50, 0, 0)
        checkBox.layoutParams = params
        ll.addView(checkBox)
        bldr.setView(ll)
        bldr.setPositiveButton("Yes", { dialog, which ->
            if (checkBox.isChecked) {
                baseActivity.store.setBoolean(Const.SHOW_GOOGLE_MAPS_INTENT_DIALOG, false)
            }
            launchGoogleMaps(baseActivity, lat, lng)
        })
        bldr.setNegativeButton("No", null)
        bldr.create().show()
    }

    fun launchGoogleMaps(baseActivity: BaseActivity, lat: String, lng: String) {
        val gmmIntentUri = Uri.parse("geo:${lat},${lng}" +
                "?q=${lat},${lng}" +
                "&?z=12")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        if (mapIntent.resolveActivity(baseActivity.getPackageManager()) != null) {
            baseActivity.startActivity(mapIntent)
        } else {
            baseActivity.showToast("Google Maps is not installed", true)
        }
    }

    internal class MyTarget(imageView: ImageView) : Target {

        val userIV: ImageView

        init {
            this.userIV = imageView
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            userIV.setImageDrawable(placeHolderDrawable)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            userIV.setImageDrawable(errorDrawable)
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            userIV.setImageBitmap(bitmap)
        }

    }
}