package com.kotlin.education.android.easytodo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import android.R.attr.description
import androidx.core.content.ContextCompat
import com.github.paolorotolo.appintro.model.SliderPage
import androidx.fragment.app.Fragment
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.shared_preferences.SharedPreferencesManager


/**
 * The Splash screen activity. This activity is run at the beginning.
 * The activity was created based on this tutorial:
 * https://www.bignerdranch.com/blog/splash-screens-the-right-way/
 * The example is advanced with an app intro using this library:
 * https://github.com/apl-devs/AppIntro
 */
class SplashScreenActivity : AppIntro() {

    private lateinit var sharedPreferencesManager : SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager()
        // is the app running for the first time?
        // if so, show app intro. If not, just continue to the app.
        if (sharedPreferencesManager.isRunForFirstTime(this)){
            // add the app intro slides.
            addSlide(createFirstSlide())
            addSlide(createSecondSlide())
            addSlide(createThirdSlide())
            // properties of the app intro.
            setBarColor(ContextCompat.getColor(this, android.R.color.transparent))
            setSeparatorColor(ContextCompat.getColor(this, android.R.color.white))
            setDoneText(getString(R.string.done))
            setSkipText(getString(R.string.skip))
            showSkipButton(true)
            setProgressButtonEnabled(true)
        } else {
            continueToApp()
        }
    }

    private fun createFirstSlide(): AppIntroFragment{
        val sliderPage = SliderPage()
        sliderPage.title = getString(R.string.app_name)
        sliderPage.description = getString(R.string.app_intro_1_text)
        sliderPage.imageDrawable = R.drawable.intro_3
        sliderPage.bgColor = ContextCompat.getColor(this, R.color.colorPrimary)
        return AppIntroFragment.newInstance(sliderPage)
    }

    private fun createSecondSlide(): AppIntroFragment{
        val sliderPage = SliderPage()
        sliderPage.title = getString(R.string.app_intro_2_title)
        sliderPage.description = getString(R.string.app_intro_2_text)
        sliderPage.imageDrawable = R.drawable.intro_1
        sliderPage.bgColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        return AppIntroFragment.newInstance(sliderPage)
    }

    private fun createThirdSlide(): AppIntroFragment{
        val sliderPage = SliderPage()
        sliderPage.title = getString(R.string.app_intro_3_title)
        sliderPage.description = getString(R.string.app_intro_3_text)
        sliderPage.imageDrawable = R.drawable.intro_2
        sliderPage.bgColor = ContextCompat.getColor(this, R.color.colorAccent)
        return AppIntroFragment.newInstance(sliderPage)
    }

    private fun continueToApp(){
        startActivity(MainActivity.createIntent(this))
        finish()
    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)
        // sets the first run as completed
        sharedPreferencesManager.saveFirstRun(this)
        // continue to the app
        continueToApp()
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        // sets the first run as completed
        sharedPreferencesManager.saveFirstRun(this)
        // continue to the app
        continueToApp()
    }

}
