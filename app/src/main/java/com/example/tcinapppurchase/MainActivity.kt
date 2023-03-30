package com.example.tcinapppurchase

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var inAppPurchaseHelper: InAppPurchaseHelper
    private  var isConnectionEstablished: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inAppPurchaseHelper = InAppPurchaseHelper(this)
        setContentView(R.layout.activity_main)
        initInAppPurchaseConnection()
        findViewById<Button>(R.id.buynow).setOnClickListener {
            if(isConnectionEstablished) {
                inAppPurchaseHelper.retrieveProducts()
            } else {
                initInAppPurchaseConnection()
                inAppPurchaseHelper.retrieveProducts()
            }
        }

        inAppPurchaseHelper.connectionStartListener.observe(this) {
            isConnectionEstablished = it
        }
    }

    private fun initInAppPurchaseConnection(){
        inAppPurchaseHelper.startConnection()
    }

    override fun onResume() {
        super.onResume()
        if(isConnectionEstablished) {
            inAppPurchaseHelper.queryPurchaseHistoryAsync()
        } else {
            initInAppPurchaseConnection()
            inAppPurchaseHelper.queryPurchaseHistoryAsync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppPurchaseHelper.endConnection()
    }
}