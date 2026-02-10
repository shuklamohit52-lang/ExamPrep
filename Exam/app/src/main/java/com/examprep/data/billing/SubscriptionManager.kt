package com.examprep.data.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult

class SubscriptionManager(private val billingClient: BillingClient) {

    fun connect(onReady: () -> Unit, onError: (String) -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    onReady()
                } else {
                    onError(result.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                onError("Billing service disconnected")
            }
        })
    }

    fun launchSubscriptionPurchase(activity: Activity, productDetailsParams: List<BillingFlowParams.ProductDetailsParams>) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParams)
            .build()
        billingClient.launchBillingFlow(activity, params)
    }
}
