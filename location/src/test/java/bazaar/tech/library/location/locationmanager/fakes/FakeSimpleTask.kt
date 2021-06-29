package bazaar.tech.library.location.locationmanager.fakes

import android.app.Activity
import com.google.android.gms.tasks.*
import java.util.*
import java.util.concurrent.Executor

class FakeSimpleTask<TResult> : Task<TResult?>() {
    var mResult: TResult? = null
    var error: Exception? = null
    private val onSuccessListeners: MutableList<OnSuccessListener<in TResult?>> = ArrayList()
    private val onFailureListeners: MutableList<OnFailureListener> = ArrayList()
    private val onCompleteListeners: MutableList<OnCompleteListener<TResult?>> = ArrayList()

    fun success(result: TResult?) {
        this.mResult = result
        for (onSuccessListener in onSuccessListeners) {
            onSuccessListener.onSuccess(result)
        }
        for (completeListener in onCompleteListeners) {
            completeListener.onComplete(this)
        }
    }

    fun error(error: Exception) {
        this.error = error
        for (onFailureListener in onFailureListeners) {
            onFailureListener.onFailure(error)
        }
        for (completeListener in onCompleteListeners) {
            completeListener.onComplete(this)
        }
    }

    override fun isComplete(): Boolean {
        return true
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun isSuccessful(): Boolean {
        return error == null
    }

    override fun getResult(): TResult? {
        return if (error != null) {
            throw RuntimeExecutionException(error)
        } else {
            mResult
        }
    }

    override fun <X : Throwable?> getResult(throwableClass: Class<X>): TResult? {
        return when {
            throwableClass.isInstance(error) -> {
                throw throwableClass.cast(error)!!
            }
            error != null -> {
                throw RuntimeExecutionException(error)
            }
            else -> {
                result
            }
        }
    }

    override fun getException(): Exception? {
        return error
    }

    override fun addOnSuccessListener(listener: OnSuccessListener<in TResult?>): Task<TResult?> {
        onSuccessListeners.add(listener)
        return this
    }

    override fun addOnSuccessListener(executor: Executor, listener: OnSuccessListener<in TResult?>): Task<TResult?> {
        onSuccessListeners.add(listener)
        return this
    }

    override fun addOnSuccessListener(activity: Activity, listener: OnSuccessListener<in TResult?>): Task<TResult?> {
        onSuccessListeners.add(listener)
        return this
    }

    override fun addOnFailureListener(listener: OnFailureListener): Task<TResult?> {
        onFailureListeners.add(listener)
        return this
    }

    override fun addOnFailureListener(executor: Executor, listener: OnFailureListener): Task<TResult?> {
        onFailureListeners.add(listener)
        return this
    }

    override fun addOnFailureListener(activity: Activity, listener: OnFailureListener): Task<TResult?> {
        onFailureListeners.add(listener)
        return this
    }

    override fun addOnCompleteListener(listener: OnCompleteListener<TResult?>): Task<TResult?> {
        onCompleteListeners.add(listener)
        return this
    }
}