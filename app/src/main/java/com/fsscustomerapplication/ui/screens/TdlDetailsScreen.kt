package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.Tdl
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.theme.FssGradientEnd
import com.fsscustomerapplication.ui.theme.FssGradientStart
import com.fsscustomerapplication.ui.theme.FssLightBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.TdlDetailState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdlDetailsScreen(
    tdlId: Int,
    onBack: () -> Unit,
    onBuyNow: (String) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel()
    val tdlState by viewModel.tdlDetailState

    LaunchedEffect(tdlId) {
        viewModel.fetchTdlDetail(tdlId)
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
        topBar = {
            TopAppBar(
                title = {
                    when (val state = tdlState) {
                        is TdlDetailState.Success -> Text(
                            state.data.displayName(),
                            fontWeight = FontWeight.Bold,
                            color = FssDarkBlue,
                            fontSize = 18.sp
                        )
                        else -> Text("TDL Module", fontWeight = FontWeight.Bold, color = FssDarkBlue)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FssDarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = tdlState) {
                is TdlDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FssBlue
                    )
                }
                is TdlDetailState.Success -> {
                    TdlDetailsContent(
                        tdl = state.data,
                        onBuyNow = onBuyNow,
                        padding = padding
                    )
                }
                is TdlDetailState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchTdlDetail(tdlId) },
                            colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is TdlDetailState.Idle -> {}
            }
        }
    }
}

@Composable
fun TdlDetailsContent(
    tdl: Tdl,
    onBuyNow: (String) -> Unit,
    padding: androidx.compose.foundation.layout.PaddingValues
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Hero Section with Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(FssGradientStart, FssGradientEnd)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Featured badge
                        if (tdl.isFeatured()) {
                            Surface(
                                color = Color(0xFFFF6F00),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "FEATURED",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        AsyncImage(
                            model = tdl.displayImage(),
                            contentDescription = tdl.displayName(),
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Fit,
                            error = painterResource(R.drawable.hand)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = tdl.displayName(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = FssDarkBlue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "OFFICIAL FSS TDL",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // 2. Price & Compatibility Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Price",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = tdl.displayPrice(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FssBlue
                        )
                    }

                    tdl.displayCompatibility()?.let { compat ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Compatibility",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = compat,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = FssDarkBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Overview Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = FssBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Overview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FssDarkBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = tdl.displayFullDesc(),
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Demo Section
            if (tdl.displayVideo() != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = FssBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Demonstration",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FssDarkBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = tdl.displayImage(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().alpha(0.3f),
                                    contentScale = ContentScale.Crop
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.9f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = FssBlue,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Watch Tutorial",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Features Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Stars,
                            contentDescription = null,
                            tint = FssBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Key Features",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FssDarkBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val detailFeatures = listOf(
                        "Lifetime Access" to "One-time purchase benefit with no recurring fees.",
                        "One-Click Setup" to "Easy installation and configuration.",
                        "24/7 Expert Support" to "Always ready to help you."
                    )

                    detailFeatures.forEachIndexed { index, (title, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FssLightBlue,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = FssBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FssDarkBlue
                                )
                                Text(
                                    desc,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Sticky Bottom CTA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onBuyNow(tdl.displayName()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FssBlue)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Request & Buy Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
