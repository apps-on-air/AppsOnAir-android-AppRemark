package com.example.appsonair_android_appremark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appsonair.appremark.services.AppRemarkService
import com.example.appsonair_android_appremark.ui.theme.AppsOnAirAndroidAppRemarkTheme

class MainActivity2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppsOnAirAndroidAppRemarkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Button(
                            elevation = ButtonDefaults.elevatedButtonElevation(8.dp), // Define elevation
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Green,
                                contentColor = Color.Black
                            ),
                            onClick = {
                                //with extra payload
                                AppRemarkService.addRemark(
                                    this@MainActivity2,
                                    //pass your customized payload, which you want to save in order to monitor your app
                                    extraPayload = mapOf(
                                        "title" to "Initial Demo",
                                        "isFromIndia" to true
                                    )
                                )

                                //without extra payload
                                //AppRemarkService.addRemark(this@MainActivity2)
                            },
                        ) {
                            Text(
                                text = getString(R.string.create_new_remark),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

