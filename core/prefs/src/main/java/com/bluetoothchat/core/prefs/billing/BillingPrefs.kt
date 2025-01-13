package com.bluetoothchat.core.prefs.billing

interface BillingPrefs {

    fun setProPurchased(proPurchased: Boolean)

    fun getProPurchased(): Boolean

}
