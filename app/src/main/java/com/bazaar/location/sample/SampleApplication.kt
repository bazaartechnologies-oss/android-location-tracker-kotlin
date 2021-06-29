package com.bazaar.location.sample

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex


class SampleApplication: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}