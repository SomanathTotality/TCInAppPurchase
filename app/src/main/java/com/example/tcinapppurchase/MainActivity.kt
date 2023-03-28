package com.example.tcinapppurchase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {
    private lateinit var inAppPurchaseHelper: InAppPurchaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inAppPurchaseHelper = InAppPurchaseHelper(this)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.buynow).setOnClickListener {
                inAppPurchaseHelper.startConnection()
        }

        inAppPurchaseHelper.connectionStartListener.observe(this, Observer {
            inAppPurchaseHelper.retrieveProducts()
        })
    }
}