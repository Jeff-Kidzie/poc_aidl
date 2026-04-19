package com.kidzie.poc_aidl.promotion

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.kidzie.poc_aidl.IPromotionEngine

class PromotionService : Service() {

    private val binder = object : IPromotionEngine.Stub() {
        override fun processBarcode(rawBarcode: String): String {
            // Mocking the O(1) in-memory lookup
            val mockDatabase = mapOf(
                "890111" to "PROMO-HALF-OFF-890111",
                "890222" to "PROMO-BOGO-890222"
            )

            // Simulating a tiny bit of processing time
            Thread.sleep(10)

            return mockDatabase[rawBarcode] ?: rawBarcode
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}