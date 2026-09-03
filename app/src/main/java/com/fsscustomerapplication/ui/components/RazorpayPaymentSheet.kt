package com.fsscustomerapplication.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import java.util.Locale

object RazorpayConfig {
    const val KEY_ID = "rzp_test_TLSEMrtU25Oced"
    const val MERCHANT_NAME = "Friends Software Solutions"
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RazorpayPaymentSheet(
    invoiceNo: String,
    amount: Double,
    itemName: String,
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (paymentId: String) -> Unit
) {
    val context = LocalContext.current
    var isWebCheckoutOpen by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var generatedPaymentId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val formattedAmount = remember(amount) {
        String.format(Locale.US, "%.2f", amount)
    }

    val amountInPaise = remember(amount) {
        (amount * 100).toLong().toString()
    }

    val htmlData = remember(formattedAmount, itemName, customerName, customerPhone, customerEmail) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #F8FAFF; color: #0D47A1; }
                .loading { font-size: 14px; font-weight: bold; color: #0D47A1; margin-top: 12px; }
            </style>
        </head>
        <body>
            <div class="loading">Loading Razorpay Secure Payment...</div>
            <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
            <script>
            var options = {
                "key": "${RazorpayConfig.KEY_ID}",
                "amount": "$amountInPaise",
                "currency": "INR",
                "name": "${RazorpayConfig.MERCHANT_NAME}",
                "description": "$itemName",
                "image": "https://crm.friendssoftwaresolutions.in/assets/images/logo.png",
                "handler": function (response){
                    RazorpayApp.onPaymentSuccess(response.razorpay_payment_id);
                },
                "prefill": {
                    "name": "$customerName",
                    "email": "$customerEmail",
                    "contact": "$customerPhone"
                },
                "theme": {
                    "color": "#0D47A1"
                },
                "modal": {
                    "ondismiss": function(){
                        RazorpayApp.onPaymentDismiss();
                    }
                }
            };
            var rzp1 = new Razorpay(options);
            rzp1.on('payment.failed', function (response){
                RazorpayApp.onPaymentError(response.error.description || 'Payment Failed');
            });
            window.onload = function() {
                rzp1.open();
            };
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF528FF0).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color(0xFF528FF0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Razorpay Secure Pay",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FssDarkBlue
                            )
                            Text(
                                text = RazorpayConfig.MERCHANT_NAME,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSuccess) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Payment Successful! 🎉",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Payment ID: $generatedPaymentId",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FssBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Amount Paid: ₹ $formattedAmount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FssDarkBlue
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onPaymentSuccess(generatedPaymentId) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)
                        ) {
                            Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isWebCheckoutOpen) {
                    // In-App WebView Checkout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                                    webViewClient = WebViewClient()

                                    addJavascriptInterface(object {
                                        @JavascriptInterface
                                        fun onPaymentSuccess(paymentId: String) {
                                            generatedPaymentId = paymentId
                                            isSuccess = true
                                            isWebCheckoutOpen = false
                                        }

                                        @JavascriptInterface
                                        fun onPaymentError(err: String) {
                                            errorMessage = err
                                            isWebCheckoutOpen = false
                                        }

                                        @JavascriptInterface
                                        fun onPaymentDismiss() {
                                            isWebCheckoutOpen = false
                                        }
                                    }, "RazorpayApp")

                                    loadDataWithBaseURL(
                                        "https://checkout.razorpay.com",
                                        htmlData,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            }
                        )
                    }
                } else {
                    // Order Summary & Pay Button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0F5FF),
                        border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = itemName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FssDarkBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹ $formattedAmount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ref: $invoiceNo • Customer: $customerName",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 12.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            errorMessage = ""
                            isWebCheckoutOpen = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF528FF0))
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pay ₹ $formattedAmount via Razorpay",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
