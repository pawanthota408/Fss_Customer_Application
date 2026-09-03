package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsscustomerapplication.R
import com.fsscustomerapplication.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.seconds
import com.fsscustomerapplication.data.remote.RetrofitClient
import com.fsscustomerapplication.data.remote.model.LoginRequest
import com.fsscustomerapplication.data.remote.model.LoginSlider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun FssLoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (Int) -> Unit = { },
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isPasswordStep by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }

    var sliderItems by remember { mutableStateOf<List<LoginSlider>>(emptyList()) }
    var logoUrl by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(pageCount = { if (sliderItems.isEmpty()) 1 else sliderItems.size })

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getLoginSliders()
            if (response.isSuccessful && response.body() != null) {
                sliderItems = response.body()!!.sliders
                logoUrl = response.body()!!.logoUrl ?: ""
            }
        } catch (e: Exception) {
            // Error handling
        }
    }

    LaunchedEffect(sliderItems) {
        if (sliderItems.isNotEmpty()) {
            while (true) {
                yield()
                delay(4.seconds)
                val nextPage = (pagerState.currentPage + 1) % sliderItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Top Section: Background Image Slider with Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
        ) {
            if (sliderItems.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true
                ) { page ->
                    AsyncImage(
                        model = sliderItems[page].imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        brush = Brush.verticalGradient(
                            colors = listOf(FssGradientStart, FssGradientEnd)
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FssBlue)
                }
            }

            // Gradient overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f),
                                Color.White
                            ),
                            startY = 0f,
                            endY = 800f
                        )
                    )
            )

            // Logo
            Box(modifier = Modifier.statusBarsPadding().padding(24.dp)) {
                Card(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (logoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = "FSS Logo",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.hand),
                                contentDescription = "FSS Logo",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Pager indicators
            if (sliderItems.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(sliderItems.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) FssBlue else Color.Gray.copy(alpha = 0.4f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 24.dp else 8.dp)
                        )
                    }
                }
            }
        }

        // 2. Bottom Section: Login Form Card
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 50.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App branding
                Text(
                    text = "FSS Customer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FssDarkBlue
                )
                Text(
                    text = "Application",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FssBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (!isPasswordStep) "Sign in to your account" else "Enter your password",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isPasswordStep) {
                    // Email/Phone Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter Email Id / Mobile", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FssBlue)
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FssBlue,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
                            focusedLeadingIconColor = FssBlue,
                            unfocusedLeadingIconColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next Button
                    Button(
                        onClick = { if (email.isNotEmpty()) isPasswordStep = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter Password", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = FssBlue)
                        },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FssBlue,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
                            focusedLeadingIconColor = FssBlue,
                            unfocusedLeadingIconColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    Button(
                        onClick = {
                            if (password.isNotEmpty()) {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val response = RetrofitClient.apiService.login(
                                            LoginRequest(email, password)
                                        )
                                        if (response.isSuccessful) {
                                            val body = response.body()
                                            loginMessage = body?.message ?: "Unknown error"
                                            if (body?.status == "success" && body.userId != null) {
                                                val sessionManager = com.fsscustomerapplication.data.local.SessionManager(context)
                                                sessionManager.saveUserId(body.userId)
                                                sessionManager.saveUserData(
                                                    name = body.name,
                                                    email = body.email ?: email,
                                                    phone = body.phone,
                                                    company = body.company
                                                )
                                                onLoginClick(body.userId)
                                            }
                                        } else {
                                            loginMessage = "Error: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        loginMessage = "Connection failed"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Login",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Change Email/Mobile
                    TextButton(onClick = { isPasswordStep = false }) {
                        Text("Change Email/Mobile", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                if (loginMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = loginMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Text(
                    text = "© 2024 Friends Software Solutions",
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
