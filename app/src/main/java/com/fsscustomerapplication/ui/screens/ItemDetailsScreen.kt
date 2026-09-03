package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue

import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsscustomerapplication.data.remote.model.TdlModule
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.TdlListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    item: ProductService?,
    onBack: () -> Unit,
    onBuyNow: () -> Unit,
    onTdlClick: (TdlModule) -> Unit = {}
) {
    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FssDarkBlue)
        }
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val viewModel: DashboardViewModel = viewModel()
    val tdlListState by viewModel.tdlListState

    val isTDL = item.displayName().contains("TDL", ignoreCase = true)

    LaunchedEffect(isTDL) {
        if (isTDL) {
            viewModel.fetchTdls()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(isTDL) "TDL Catalog" else item.displayName(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF8FAFF))
            ) {
                if (!isTDL) {
                    // 1. Large Image Section for standard products
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = item.iconLink,
                            contentDescription = item.displayName(),
                            modifier = Modifier.size(180.dp),
                            contentScale = ContentScale.Fit,
                            error = painterResource(R.drawable.hand)
                        )
                    }
                }

                // 2. Info Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    if (isTDL) {
                        Text(
                            text = "Premium TDL Modules",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FssDarkBlue
                        )
                        Text(
                            text = "Expert-crafted addons for your Tally",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                        )

                        when (val state = tdlListState) {
                            is com.fsscustomerapplication.ui.viewmodels.TdlListState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = FssBlue)
                                }
                            }
                            is com.fsscustomerapplication.ui.viewmodels.TdlListState.Success -> {
                                val modules = state.data.map { it.toTdlModule() }
                                if (modules.isEmpty()) {
                                    Text(
                                        text = "No TDL modules available",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                } else {
                                    modules.forEach { module ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .clickable { onTdlClick(module) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(70.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFFF5F9FF)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        AsyncImage(
                                                            model = module.imageUrl,
                                                            contentDescription = module.name,
                                                            modifier = Modifier.size(45.dp),
                                                            contentScale = ContentScale.Fit,
                                                            error = painterResource(R.drawable.hand)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = module.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = FssDarkBlue
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = module.description,
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        maxLines = 2,
                                                        lineHeight = 16.sp
                                                    )
                                                }

                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            is com.fsscustomerapplication.ui.viewmodels.TdlListState.Error -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(state.message, color = Color.Red, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.fetchTdls() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                            is com.fsscustomerapplication.ui.viewmodels.TdlListState.Idle -> {}
                        }
                    } else {
                        Surface(
                            color = FssBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = (item.category ?: "Product").uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = FssBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = item.displayName(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FssDarkBlue
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Experience the power of ${item.displayName()}. Designed to help your business grow faster and smarter.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Features List
                        val features = listOf("Premium Support", "Cloud Ready", "Safe & Secure")
                        features.forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF138808), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(feature, fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }


            // Sticky Bottom Button
            if (!isTDL) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    color = Color.Transparent
                ) {
                    Button(
                        onClick = onBuyNow,
                        modifier = Modifier
                            .height(56.dp)
                            .width(160.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}
