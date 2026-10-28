package com.example.appsonair_android_appremark

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appsonair.appremark.enums.ShakeSensitivity
import com.example.appsonair_android_appremark.ui.theme.AppsOnAirAndroidAppRemarkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppsOnAirAndroidAppRemarkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ShakeSensitivityDropdown(
                            onSelected = { changeShakeSensitivity(it) }
                        )
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

    private fun changeShakeSensitivity(shakeSensitivity: ShakeSensitivity) {
        MyApp.shakeSensitivity = shakeSensitivity
        //re-initialize so the new sensitivity applies without restarting the app
        (application as MyApp).initAppRemark(this)
    }

    private fun gotoNext() {
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShakeSensitivityDropdown(onSelected: (ShakeSensitivity) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(MyApp.shakeSensitivity) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            value = selected.displayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Shake sensitivity") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ShakeSensitivity.values().forEach { sensitivity ->
                DropdownMenuItem(
                    text = { Text(sensitivity.displayName()) },
                    onClick = {
                        selected = sensitivity
                        expanded = false
                        onSelected(sensitivity)
                    },
                )
            }
        }
    }
}

// VERY_LOW -> "Very low"
private fun ShakeSensitivity.displayName(): String =
    name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
