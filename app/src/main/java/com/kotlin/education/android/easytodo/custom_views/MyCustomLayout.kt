package com.kotlin.education.android.easytodo.custom_views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kotlin.education.android.easytodo.R

/**
 * This is a custom button. This button can be used at multiple places.
 * By using custom view, we are ensuring, that the element will always look the same
 * anywhere in the application.
 */
class MyCustomLayout: FrameLayout
{
    // the custom view will have a TextView in the layout
    private lateinit var header: TextView
    private lateinit var value: TextView
    private lateinit var image: ImageView

    // ********* The constructors *****
    // we need all three constructors to properly initialize the view.
    constructor(context: Context) : super(context){
        init(context, null, null)
    }

    constructor(context: Context, attrs: AttributeSet):
            super(context, attrs){
        init(context, attrs, null)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    // ********* end of constructors *****

    /**
     * The method init is called in every constructor.
     * It is responsible for view initialization.
     */
    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int?) {

        // getting the reference for the atributes.
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.MyCustomLayout, 0, 0
        )

        // getting the values of the text atrribute inside the view.
        val headerAttr = a.getString(R.styleable.MyCustomLayout_header)
        val valueAttr = a.getString(R.styleable.MyCustomLayout_value)
        val imageAttr = a.getDrawable(R.styleable.MyCustomLayout_image)
        // finish working with atrributes
        a.recycle()

        // inflate the custom layout.
        inflate(context, R.layout.my_custom_layout, this)
        // find the TextView inside the layout.
        header = findViewById<TextView>(R.id.header)
        value = findViewById<TextView>(R.id.value)
        image = findViewById<ImageView>(R.id.image)
        // set the attribute value to the TextView
        header.text = headerAttr
        value.text = valueAttr
        setImage(imageAttr)


    }

    /**
     * Sets the header text. This method is used to set text programmatically from the activities.
     */
    fun setHeader(header : String){
        this.header.text = header
    }

    /**
     * Sets the header text. This method is used to set text programmatically from the activities.
     */
    fun setValue(value : String){
        this.value.text = value
    }

    /**
     * Sets the header text. This method is used to set text programmatically from the activities.
     */
    private fun setImage(image : Drawable?){
        image?.let {
            this.image.setImageDrawable(image)
        }?:kotlin.run {
            this.image.visibility = View.GONE
        }
    }

}