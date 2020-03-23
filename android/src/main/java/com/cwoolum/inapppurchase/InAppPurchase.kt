package com.cwoolum.inapppurchase

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.getcapacitor.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


@NativePlugin(
        requestCodes = [InAppPurchase.CREATE_PURCHASE]
)
class InAppPurchase : Plugin(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    protected val TAG = "google.payments"

    private var manifestObject: JSONObject? = null

    private fun getManifestContents(): JSONObject? {
        if (manifestObject != null) return manifestObject

        val `is`: InputStream
        try {
            `is` = context.assets.open("www/manifest.json")
            val s = Scanner(`is`).useDelimiter("\\A")
            val manifestString = if (s.hasNext()) s.next() else ""
            Log.d(TAG, "manifest:$manifestString")
            manifestObject = JSONObject(manifestString)
        } catch (e: IOException) {
            Log.d(TAG, "Unable to read manifest file:$e")
            manifestObject = null
        } catch (e: JSONException) {
            Log.d(TAG, "Unable to parse manifest file:$e")
            manifestObject = null
        }
        return manifestObject
    }

    protected fun getBase64EncodedPublicKey(): String? {
        val manifestObject = getManifestContents()
        return manifestObject?.optString("play_store_key")
    }

    protected fun shouldSkipPurchaseVerification(): Boolean {
        val manifestObject = getManifestContents()
        return manifestObject?.optBoolean("skip_purchase_verification") ?: false
    }

    @PluginMethod
    fun initialize(call: PluginCall) {
        billingClient = BillingClient
                .newBuilder(context.applicationContext)
                .setListener(this)
                .enablePendingPurchases()
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    call.resolve()
                }else{
                    call.error("An error occurred connecting to the billing service. Response Code: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {

            }
        })
    }


    private suspend fun runPayment(call: PluginCall, subscribe: Boolean) {
        val sku = call.data["sku"] as String

        val skuDetails = getSkuDetailsInternal(mutableListOf(sku), if (subscribe) BillingClient.SkuType.SUBS else BillingClient.SkuType.INAPP)

        var skuDetailsList = skuDetails.skuDetailsList
        if (skuDetailsList != null && skuDetailsList.count() > 0) {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetailsList[0])
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity as Activity, flowParams)

            saveCall(call)

        } else {
            call.reject("SKU is invalid!")
        }
    }

    @PluginMethod
    suspend fun subscribe(call: PluginCall) {
        return runPayment(call, true)
    }

    @PluginMethod
    suspend fun buy(call: PluginCall) {
        return runPayment(call, false)
    }

    @PluginMethod
    suspend fun consumePurchase(call: PluginCall) {
        val purchaseToken = call.data["purchaseToken"] as String

        val paramsBuilder = ConsumeParams.newBuilder()
        paramsBuilder.setPurchaseToken(purchaseToken)

        val ackPurchaseResult = withContext(Dispatchers.IO) {
            billingClient.consumePurchase(paramsBuilder.build())
        }

        if (ackPurchaseResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            call.success()
        } else {
            call.error("Error consuming purchase")
        }
    }

    @PluginMethod
    fun getSkuDetails(call: PluginCall) {
        val moreSkus: MutableList<String> = ArrayList()

        val skus = call.data.getJSONArray("skus")
        val skuType = call.data["skuType"] as String

        for (i in 0 until skus.length()) {
            moreSkus.add(skus.getString(i))
            Log.d(TAG, "get sku:" + skus.getString(i))
        }

        runBlocking {
            val skuDetailsResult = getSkuDetailsInternal(moreSkus, skuType)

            val skuResponseArray = JSONArray()

            val skuList = skuDetailsResult.skuDetailsList
            if (skuList != null) {
                for (skuDetails in skuList) {
                    val detailsJson = JSONObject()
                    detailsJson.put("productId", skuDetails.sku)
                    detailsJson.put("title", skuDetails.title)
                    detailsJson.put("description", skuDetails.description)
                    detailsJson.put("price", skuDetails.price)
                    detailsJson.put("type", skuDetails.type)
                    detailsJson.put("currency", skuDetails.priceCurrencyCode)
                    detailsJson.put("subscriptionPeriod", skuDetails.subscriptionPeriod)
                    skuResponseArray.put(detailsJson)
                }
            }

            val response = JSObject();
            response.put("data", skuResponseArray)
            call.resolve(response);
        }
    }

    private suspend fun getSkuDetailsInternal(moreSkus: MutableList<String>, skuType: String): SkuDetailsResult {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(moreSkus).setType(skuType)

        return withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }
    }

    @PluginMethod
    fun restorePurchases(call: PluginCall) {
        val responseArray = JSArray()

        val subscriptions = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        for (purchase in subscriptions.purchasesList) {
            val detailsJson = JSObject()
            detailsJson.put("orderId", purchase.orderId)
            detailsJson.put("packageName", purchase.packageName)
            detailsJson.put("productId", purchase.sku)
            detailsJson.put("purchaseTime", purchase.purchaseTime)
            detailsJson.put("purchaseState", purchase.purchaseState)
            detailsJson.put("purchaseToken", purchase.purchaseToken)
            detailsJson.put("signature", purchase.signature)
            detailsJson.put("type", BillingClient.SkuType.SUBS)
            detailsJson.put("receipt", purchase.originalJson)
            responseArray.put(detailsJson)
        }

        val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        for (purchase in purchases.purchasesList) {
            val detailsJson = JSObject()
            detailsJson.put("orderId", purchase.orderId)
            detailsJson.put("packageName", purchase.packageName)
            detailsJson.put("productId", purchase.sku)
            detailsJson.put("purchaseTime", purchase.purchaseTime)
            detailsJson.put("purchaseState", purchase.purchaseState)
            detailsJson.put("purchaseToken", purchase.purchaseToken)
            detailsJson.put("signature", purchase.signature)
            detailsJson.put("type", BillingClient.SkuType.INAPP)
            detailsJson.put("receipt", purchase.originalJson)
            responseArray.put(detailsJson)
        }

        val response = JSObject()
        response.put("data", responseArray)

        call.resolve(response)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        // Get the previously saved call
        // Get the previously saved call
        val savedCall = savedCall ?: return

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase, savedCall)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            savedCall.reject("User Canceled Flow");
        } else {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.ERROR || billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                savedCall.error("Could not complete purchase")
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                savedCall.error("Purchase Cancelled")
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                savedCall.error("Item already owned")
            } else {
                savedCall.error("Error completing purchase: $billingResult")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase, call: PluginCall) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val pluginResponse = JSObject()
            pluginResponse.put("orderId", purchase.orderId)
            pluginResponse.put("packageName", purchase.packageName)
            pluginResponse.put("productId", purchase.sku)
            pluginResponse.put("purchaseTime", purchase.purchaseTime)
            pluginResponse.put("purchaseState", purchase.purchaseState)
            pluginResponse.put("purchaseToken", purchase.purchaseToken)
            pluginResponse.put("signature", purchase.signature)
            pluginResponse.put("receipt", purchase.originalJson)

            call.resolve(pluginResponse)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.
        }
    }

    companion object {
        const val CREATE_PURCHASE = 87978
    }
}