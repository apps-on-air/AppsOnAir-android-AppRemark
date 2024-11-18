package com.example.appsonair_android_appremark

import android.content.Intent
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
import com.example.appsonair_android_appremark.ui.theme.AppsOnAirAndroidAppRemarkTheme

class MainActivity : ComponentActivity() {

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
                                containerColor = Color.Yellow,
                                contentColor = Color.Black
                            ),
                            onClick = {
                                gotoNext()
                            },
                        ) {
                            Text(
                                text = getString(R.string.go_to_next),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    private fun gotoNext() {
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }
}
