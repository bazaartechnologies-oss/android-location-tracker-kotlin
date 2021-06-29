package bazaar.tech.library.location.view

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class ContextProcessor(val context: Context) {
    private var weakActivity: WeakReference<Activity?> =  WeakReference(null)
    private var weakFragment: WeakReference<Fragment?> = WeakReference(null)

    init {
        require(context is Application) { "ContextProcessor can only be initialized with Application!" }
    }

    /**
     * In order to use in Activity or Service
     */
    fun setActivity(activity: Activity?): ContextProcessor {
        weakActivity = WeakReference(activity)
        weakFragment = WeakReference(null)
        return this
    }

    /**
     * In order to use in Fragment
     */
    fun setFragment(fragment: Fragment): ContextProcessor {
        weakActivity = WeakReference(null)
        weakFragment = WeakReference(fragment)
        return this
    }

    val fragment: Fragment?
        get() = weakFragment.get()

    val activity: Activity?
        get() {
            if (weakActivity.get() != null) return weakActivity.get()
            return if (weakFragment.get() != null && weakFragment.get()!!.activity != null) weakFragment.get()!!.activity else null
        }

}