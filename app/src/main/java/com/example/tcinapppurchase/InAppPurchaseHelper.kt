package com.example.tcinapppurchase

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.QueryProductDetailsParams.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppPurchaseHelper(val context: Activity) {
    companion object {
        const val TAG = "InAppPurchaseHelper"
    }

    val connectionStartListener = MutableLiveData<Boolean>()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            Log.d(TAG, "response code = $billingResult.responseCode  " )
            Log.d(TAG, "purchases list  = ${purchases?.toList().toString()} " )
            Log.d(TAG, "purchases  = $purchases " )
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    Log.d(TAG, "purchase = ${purchase.orderId}")
                    GlobalScope.launch {
                        handlePurchase(purchase)
                    }
                }
            } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
                Log.d(TAG, "error   = ${billingResult.debugMessage}  " )
                // Handle an error caused by a user cancelling the purchase flow.

            } else {
                Log.d(TAG, " other error   = ${billingResult.responseCode}  " )
                // Handle any other error codes.
            }
        }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun startConnection(){
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    connectionStartListener.postValue(true)
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onBillingServiceDisconnected" )
            }
        })


    }


    fun launchBillingFlow(productDetails:  ProductDetails) {
        Log.d(TAG, " launchBillingFlow")
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                .build()
        )
        Log.d(TAG, "productDetailsParamsList = $productDetailsParamsList")
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        Log.d(TAG, "billingFlowParams = $billingFlowParams")
        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(context, billingFlowParams)
        Log.d(TAG, "billingResult = $billingResult")
    }

    suspend fun handlePurchase(purchase: Purchase) {
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        Log.d(TAG, "handlePurchase purchase = $purchase")
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        val consumeResult = withContext(Dispatchers.IO) {
            billingClient.consumeAsync(consumeParams,
                ConsumeResponseListener { p0, p1 ->
                    Log.d(TAG, "handlePurchase BillingResult = $p0")
                    Log.d(TAG, "handlePurchase String = $p1")
                    Log.d(TAG, "handlePurchase responseCode = ${p0.responseCode}")
                    Log.d(TAG, "handlePurchase debugMessage = ${p0.debugMessage}")
                })
        }

        Log.d(TAG, "handlePurchase consumeResult = $consumeResult")
    }


    fun retrieveProducts() {
        Log.d(TAG, "retrieveProducts start")

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(listOf( Product.newBuilder()
                    .setProductId("test_prod_10")
                    .setProductType(ProductType.INAPP)
                    .build()))
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            skuDetailsList ->
            Log.d(TAG, "skuDetailsList $skuDetailsList" )
            Log.d(TAG, "billingResult $billingResult" )
            Log.d(TAG, "billingResult responseCode ${billingResult.responseCode}" )
            Log.d(TAG, "billingResult debugMessage  ${billingResult.debugMessage}" )
            if (skuDetailsList.isNotEmpty()) {
                for (productDet in skuDetailsList) {
                    Log.d(TAG, "product name = ${productDet.name}")
                    launchBillingFlow(productDet)
                }
                // Process list of matching products
            } else {
                Log.d(TAG, "No product matches found")
                // No product matches found
            }
            // Process the result
        }
    }
}