package com.fsscustomerapplication.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fsscustomerapplication.R
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue

@Composable
fun WhatsAppActionDialog(
    spocName: String,
    spocPhone: String,
    licenseNo: String? = null,
    onDismiss: () -> Unit,
    onOpenInAppChat: () -> Unit
) {
    val context = LocalContext.current
    val cleanPhone = remember(spocPhone) {
        spocPhone.replace("+", "").replace(" ", "").replace("-", "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contact $spocName",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Select your preferred message option:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: In-App AI Chat
                ContactOptionItem(
                    icon = {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = FssBlue, modifier = Modifier.size(24.dp))
                    },
                    title = "In-App AI Chat",
                    subtitle = "Instant software & account assistance",
                    onClick = {
                        onDismiss()
                        onOpenInAppChat()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: WhatsApp Message
                ContactOptionItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.whatsapp),
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "WhatsApp Message",
                    subtitle = "Open WhatsApp chat with engineer",
                    onClick = {
                        onDismiss()
                        try {
                            val msg = "Hello $spocName, I need support for FSS Licence #${licenseNo ?: ""}"
                            val url = "https://wa.me/$cleanPhone?text=${Uri.encode(msg)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Normal SMS
                ContactOptionItem(
                    icon = {
                        Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(24.dp))
                    },
                    title = "Normal SMS",
                    subtitle = "Send direct text message",
                    onClick = {
                        onDismiss()
                        try {
                            val msg = "Hello $spocName, I need support for Licence #${licenseNo ?: ""}"
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanPhone")).apply {
                                putExtra("sms_body", msg)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun ContactOptionItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFF),
        border = BorderStroke(1.dp, Color(0xFFE0E6F0))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FssDarkBlue)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
