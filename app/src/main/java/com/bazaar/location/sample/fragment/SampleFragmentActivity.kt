package com.bazaar.location.sample.fragment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bazaar.location.sample.R

class SampleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_fragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        dispatchToFragment(requestCode, resultCode, data)
    }

    /**
     * This is required because GooglePlayServicesApi and SettingsApi requires Activity,
     * and they call startActivityForResult from the activity, not fragment,
     * fragment doesn't receive onActivityResult callback. We need to call/redirect manually.
     */
    private fun dispatchToFragment(requestCode: Int, resultCode: Int, data: Intent?) {
        val sampleFragment = supportFragmentManager
                .findFragmentById(R.id.sample_fragment) as SampleFragment?
        sampleFragment?.onActivityResult(requestCode, resultCode, data)
    }
}