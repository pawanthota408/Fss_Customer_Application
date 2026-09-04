package com.fsscustomerapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsscustomerapplication.data.remote.model.License
import com.fsscustomerapplication.ui.theme.FssBlue

@Composable
fun SectionTitle(text: String, color: Color = FssBlue) {
    Text(text = text, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun LabelText(text: String) {
    Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Black.copy(alpha = 0.8f))
}

@Composable
fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF138808), modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 9.sp, color = Color.DarkGray)
    }
}

@Composable
fun ContactMethodItem(label: String, icon: ImageVector, isChecked: Boolean, modifier: Modifier, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if(label == "Whatsapp") Color(0xFF25D366) else Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp)
        }
    }
}

@Composable
fun SimpleDropdown(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
fun LicenseDropdown(
    licences: List<License>,
    selected: String,
    showProduct: Boolean = true,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (selected.isEmpty()) "Choose License" else selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            licences.forEach { licence ->
                val displayText = if (showProduct) {
                    "${licence.number} (${licence.productName})"
                } else {
                    licence.number
                }
                DropdownMenuItem(
                    text = { Text(displayText) },
                    onClick = { onSelect(licence.number); expanded = false }
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, subLabel: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
                Text(text = subLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Text(text = label, fontSize = 8.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PaginationButton(text: String, isSelected: Boolean) {
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) FssBlue else Color.White,
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontSize = 11.sp, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
