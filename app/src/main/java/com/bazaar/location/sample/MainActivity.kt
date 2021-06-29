package com.bazaar.location.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import bazaar.tech.library.location.LocationManager
import com.bazaar.location.sample.activity.SampleActivity
import com.bazaar.location.sample.fragment.SampleFragmentActivity
import com.bazaar.location.sample.service.SampleServiceActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LocationManager.enableLog(true)
    }

    fun inActivityClick(view: View?) {
        startActivity(Intent(this, SampleActivity::class.java))
    }

    fun inFragmentClick(view: View?) {
        startActivity(Intent(this, SampleFragmentActivity::class.java))
    }

    fun inServiceClick(view: View?) {
        startActivity(Intent(this, SampleServiceActivity::class.java))
    }
}