package com.bluetoothchat.core.prefs.billing

import android.content.Context
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.fredporciuncula.flow.preferences.FlowSharedPreferences

class BillingPrefsImp(
    context: Context,
    dispatcherManager: DispatcherManager,
) : BillingPrefs {

    private val preferences = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
    private val flowPreferences = FlowSharedPreferences(preferences, dispatcherManager.io)

    override fun setProPurchased(proPurchased: Boolean) =
        flowPreferences.getBoolean(KEY_PRO_PURCHASED, DEFAULT_PRO_PURCHASED).set(proPurchased)

    override fun getProPurchased(): Boolean =
        flowPreferences.getBoolean(KEY_PRO_PURCHASED, DEFAULT_PRO_PURCHASED).get()

    companion object {
        private const val KEY_PRO_PURCHASED = "pro_purchased"
        private const val DEFAULT_PRO_PURCHASED = false
    }
}
