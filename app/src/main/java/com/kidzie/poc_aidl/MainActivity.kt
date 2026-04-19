package com.kidzie.poc_aidl

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kidzie.poc_aidl.promotion.PromotionService
import com.kidzie.poc_aidl.ui.theme.PocAidlTheme
import com.kidzie.poc_aidl.watchdog.WatchdogService
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var promotionEngine: IPromotionEngine? = null
    private var isBound = false

    private val bindConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            promotionEngine = IPromotionEngine.Stub.asInterface(service)
            isBound = true
            Log.d("promotionEngine", "Service connected: $promotionEngine")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            promotionEngine = null
            isBound = false
            Log.d("promotionEngine", "Service disconnected")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val intent = Intent(this, PromotionService::class.java)
        bindService(intent, bindConnection, BIND_AUTO_CREATE)

        startForegroundService(Intent(this, WatchdogService::class.java))
        
        val onClickScan: () -> Unit = {
            if (isBound) {
                val startTime = System.currentTimeMillis()
                val result = promotionEngine?.processBarcode("890111") ?: "No result"
                val endTime = System.currentTimeMillis()
                Log.d("promotionEngine", "Result: $result, Latency: ${endTime - startTime}ms")
            } else {
                Log.d("promotionEngine", "Service not bound")
            }
        }

        setContent {
            PocAidlTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContentApp(innerPadding, onClickScan, 0L)
                }
            }
        }
    }
}

@Composable
fun ContentApp(innerPaddingValues: PaddingValues, onClickScan: () -> Unit = {}, latencyTime: Long) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onClickScan) {
            Text("SCAN ME")

        }
        Spacer(modifier = Modifier.size(height = 8.dp, width = 2.dp))
        Text(text = "Latency: ${latencyTime}ms")
    }
}

