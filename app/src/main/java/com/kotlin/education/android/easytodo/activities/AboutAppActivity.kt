package com.kotlin.education.android.easytodo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.education.android.easytodo.BuildConfig
import com.kotlin.education.android.easytodo.R

import kotlinx.android.synthetic.main.activity_about_app.*
import kotlinx.android.synthetic.main.content_about_app.*

/**
 * The activity for showing the info about the application.
 */
class AboutAppActivity : AppCompatActivity() {

    // all the static things for the activity
    companion object {

        /**
         * The method for creating intent to run the activity. The activity should be only run
         * using this method. This way no one doesn't need to know what to put to intent
         */
        fun createIntent(context: Context) : Intent {
            return Intent(context, AboutAppActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener({ finish()})

        // get the version name.
        val versionName = BuildConfig.VERSION_NAME
        // show the version name in TextView
        // TODO Homework: Read this article: https://medium.com/@maxirosson/versioning-android-apps-d6ec171cfd82
        appVersion.setValue(versionName)


    }

}
