package com.task.mobiadmin.customViews

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

class MyEditText : android.support.v7.widget.AppCompatEditText {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.editTextStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun setTypeface(tf: Typeface) {
        super.setTypeface(getFont())
    }

    override fun setTypeface(tf: Typeface, style: Int) {
        super.setTypeface(getFont(), style)
    }

    private fun init() {
        this.typeface = getFont()
        includeFontPadding = false
    }

    private fun getFont() = Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
}
