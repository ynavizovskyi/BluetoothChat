package com.bluetoothchat.core.ui.util

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

fun Activity.launchInAppReview(onComplete: (success: Boolean) -> Unit) {
    val reviewManager = ReviewManagerFactory.create(this)
    val request = reviewManager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val flow = reviewManager.launchReviewFlow(this, reviewInfo)
            flow.addOnCompleteListener {
                // The flow has finished. The API doesn't indicate whether the user
                // reviewed or not, or even whether the review dialog was shown.
                // Therefore, no matter the result, continue with your app's flow.
                onComplete.invoke(true)
            }
        } else {
            // Log or handle error if you want to
            onComplete.invoke(false)
        }
    }
}
