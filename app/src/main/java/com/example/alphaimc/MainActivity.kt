package com.example.alphaimc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaimc.ui.theme.AlphaIMCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlphaIMCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IMCCalculatorApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun IMCCalculatorApp(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf(170) }  // cm
    var weight by remember { mutableStateOf(70) }   // kg
    var selectedGender by remember { mutableStateOf("Homme") }
    var activityLevel by remember { mutableStateOf("Sédentaire") }
    var imcResult by remember { mutableStateOf<Double?>(null) }

    val activityLevels = listOf("Sédentaire", "Faible", "Actif", "Sportif", "Athlète")

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Calculateur IMC", fontSize = 26.sp, modifier = Modifier.padding(bottom = 16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Prénom") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Âge") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Sexe : ")
                    listOf("Homme", "Femme").forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            RadioButton(
                                selected = (selectedGender == gender),
                                onClick = { selectedGender = gender }
                            )
                            Text(text = gender, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Poids : $weight kg", fontSize = 18.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { if (weight > 30) weight-- }) { Text("-") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { if (weight < 200) weight++ }) { Text("+") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = height.toString(),
                    onValueChange = { newValue -> newValue.toIntOrNull()?.let { height = it } },
                    label = { Text("Taille (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                DropdownMenuActivity(activityLevel, activityLevels) { selected ->
                    activityLevel = selected
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val heightInMeters = height / 100.0
                        imcResult = weight / (heightInMeters * heightInMeters)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Calculer IMC", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                imcResult?.let {
                    Text(text = "Votre IMC : %.2f".format(it), fontSize = 20.sp)
                    Text(text = "Catégorie : ${getIMCCategory(it)}", fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    WeightScale(weight)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuActivity(selectedActivity: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedActivity,
            onValueChange = {},
            readOnly = true,
            label = { Text("Niveau d'activité") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()  // Assure que le champ prend toute la largeur
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun WeightScale(weight: Int) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
    ) {
        val barWidth = size.width
        val barHeight = size.height / 2

        // Dessine la barre de fond
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, barHeight),
            end = Offset(barWidth, barHeight),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )

        val filledWidth = (weight - 30) / 170f * barWidth
        drawLine(
            color = Color.Blue,
            start = Offset(0f, barHeight),
            end = Offset(filledWidth, barHeight),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )
    }
}

fun getIMCCategory(imc: Double): String {
    return when {
        imc < 18.5 -> "Maigreur"
        imc < 24.9 -> "Poids normal"
        imc < 29.9 -> "Surpoids"
        imc < 34.9 -> "Obésité modérée"
        imc < 39.9 -> "Obésité sévère"
        else -> "Obésité morbide"
    }
}
