    package com.fsscustomerapplication

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.platform.LocalContext
    import com.fsscustomerapplication.data.local.SessionManager
    import com.fsscustomerapplication.ui.screens.DashboardScreen
    import com.fsscustomerapplication.ui.screens.FssLoginScreen
    import com.fsscustomerapplication.ui.screens.ItemDetailsScreen
    import com.fsscustomerapplication.ui.screens.LicencesDetailsScreen
    import com.fsscustomerapplication.ui.screens.ProductRequestScreen
    import com.fsscustomerapplication.ui.screens.ProfileScreen
    import com.fsscustomerapplication.ui.screens.RenewalSummaryScreen
    import com.fsscustomerapplication.ui.screens.ServiceRequestScreen
import com.fsscustomerapplication.ui.screens.ServicesScreen
import com.fsscustomerapplication.ui.screens.SplashScreen
import com.fsscustomerapplication.ui.screens.TdlDetailsScreen
import com.fsscustomerapplication.ui.screens.TdlListScreen
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.data.remote.model.Tdl
    import com.google.gson.Gson
    import java.net.URLDecoder
    import java.net.URLEncoder
    import java.nio.charset.StandardCharsets
    import com.fsscustomerapplication.ui.theme.FSSCUSTOMERAPPLICATIONTheme
    import com.fsscustomerapplication.utils.LanguageManager

    class MainActivity : ComponentActivity() {
        override fun attachBaseContext(newBase: android.content.Context) {
            val sessionManager = SessionManager(newBase)
            val langCode = sessionManager.getLanguage()
            val context = LanguageManager.applyLanguage(newBase, langCode)
            super.attachBaseContext(context)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                FSSCUSTOMERAPPLICATIONTheme {
                    AppNavigation()
                }
            }
        }
    }

    @Composable
    fun AppNavigation() {
        val context = LocalContext.current
        val sessionManager = remember { SessionManager(context) }
        val navController = rememberNavController()

        val startDestination = if (sessionManager.isLoggedIn()) "dashboard/${sessionManager.getUserId()}" else "splash"

        NavHost(navController = navController, startDestination = startDestination) {
            composable("splash") {
                SplashScreen {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            composable("login") {
                FssLoginScreen { userId ->
                    sessionManager.saveUserId(userId)
                    navController.navigate("dashboard/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            composable("dashboard/{userId}") { backStackEntry ->
                val userIdStr = backStackEntry.arguments?.getString("userId")
                val userId = userIdStr?.toIntOrNull() ?: sessionManager.getUserId()

                if (userId == -1) {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                } else {
                    DashboardScreen(
                        userId = userId,
                        onRenewClick = { licenseNo ->
                            navController.navigate("renewal_summary/$licenseNo")
                        },
                        onNavigateToServices = {
                            navController.navigate("services/$userId")
                        },
                        onNavigateToReports = {
                            navController.navigate("reports/$userId")
                        },
                        onNavigateToTickets = {
                            navController.navigate("tickets/$userId")
                        },
                        onNavigateToItemDetails = { item ->
                        try {
                            val prunedItem = item.copy(services = emptyList(), addons = emptyList())
                            val itemJson = URLEncoder.encode(Gson().toJson(prunedItem), StandardCharsets.UTF_8.toString())
                            navController.navigate("item_details/$itemJson")
                        } catch (_: Exception) {
                            val itemJson = URLEncoder.encode("{\"id\":${item.id},\"name\":\"${item.displayName()}\"}", StandardCharsets.UTF_8.toString())
                            navController.navigate("item_details/$itemJson")
                        }
                    },
                    onLicenceClick = { product ->
                        navController.navigate("services/$userId?productId=${product.id}")
                    },
                    onProfileClick = {
                        navController.navigate("profile/$userId")
                    },
                )
            }
            }

            composable("item_details/{itemJson}") { backStackEntry ->
                val itemJson = backStackEntry.arguments?.getString("itemJson") ?: ""
                val item = try {
                    val decodedJson = URLDecoder.decode(itemJson, StandardCharsets.UTF_8.toString())
                    Gson().fromJson(decodedJson, ProductService::class.java)
                } catch (e: Exception) {
                    null
                }

                ItemDetailsScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onBuyNow = {
                        item?.let { nonNullItem ->
                            val prunedItem = nonNullItem.copy(services = emptyList(), addons = emptyList())
                            val encodedItem = URLEncoder.encode(Gson().toJson(prunedItem), StandardCharsets.UTF_8.toString())
                            if (nonNullItem.category == "Product") {
                                navController.navigate("product_request/$encodedItem")
                            } else {
                                navController.navigate("service_request/$encodedItem")
                            }
                        }
                    },
                    onTdlClick = { tdl ->
                        val tdlJson = URLEncoder.encode(Gson().toJson(tdl), StandardCharsets.UTF_8.toString())
                        navController.navigate("tdl_details/$tdlJson")
                    }
                )
            }
            composable("tdl_details/{tdlJson}") { backStackEntry ->
                val tdlJson = backStackEntry.arguments?.getString("tdlJson") ?: ""
                val tdlId = try {
                    val decodedJson = URLDecoder.decode(tdlJson, StandardCharsets.UTF_8.toString())
                    val tdl = Gson().fromJson(decodedJson, Tdl::class.java)
                    tdl.id
                } catch (e: Exception) {
                    0
                }
                TdlDetailsScreen(
                    tdlId = tdlId,
                    onBack = { navController.popBackStack() },
                    onBuyNow = { tdlName ->
                        navController.navigate("service_request_tdl/$tdlName")
                    }
                )
            }
            composable("tdl_list") {
                TdlListScreen(
                    onBack = { navController.popBackStack() },
                    onTdlClick = { tdl ->
                        navController.navigate("tdl_detail/${tdl.id}")
                    }
                )
            }
            composable("tdl_detail/{tdlId}") { backStackEntry ->
                val tdlId = backStackEntry.arguments?.getString("tdlId")?.toIntOrNull() ?: 0
                TdlDetailsScreen(
                    tdlId = tdlId,
                    onBack = { navController.popBackStack() },
                    onBuyNow = { tdlName ->
                        navController.navigate("service_request_tdl/$tdlName")
                    }
                )
            }
            composable("service_request_tdl/{tdlName}") { backStackEntry ->
                val tdlName = backStackEntry.arguments?.getString("tdlName") ?: ""
                val userId = sessionManager.getUserId()
                ServiceRequestScreen(
                    userId = userId,
                    item = null,
                    tdlName = tdlName,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("product_request") {
                val userId = sessionManager.getUserId()
                ProductRequestScreen(
                    userId = userId,
                    item = null,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("service_request/{itemJson}") { backStackEntry ->
                val itemJson = backStackEntry.arguments?.getString("itemJson") ?: ""
                val item = if (itemJson.isNotEmpty()) {
                    val decodedJson = URLDecoder.decode(itemJson, StandardCharsets.UTF_8.toString())
                    Gson().fromJson(decodedJson, ProductService::class.java)
                } else null
                val userId = sessionManager.getUserId()
                ServiceRequestScreen(
                    userId = userId,
                    item = item,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("service_request") {
                val userId = sessionManager.getUserId()
                ServiceRequestScreen(
                    userId = userId,
                    item = null,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: sessionManager.getUserId()
                ProfileScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate("dashboard/$userId") {
                            popUpTo("dashboard/$userId") { inclusive = true }
                        }
                    },
                    onNavigateToServices = {
                        navController.navigate("services/$userId")
                    },
                    onNavigateToReports = {
                        navController.navigate("reports/$userId")
                    },
                    onNavigateToTickets = {
                        navController.navigate("tickets/$userId")
                    },
                    onSpocChatClick = {
                        navController.navigate("spoc_chat/")
                    },
                    onLogout = {
                        sessionManager.logout()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable("tickets/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: sessionManager.getUserId()
                var activePaymentInvoice by remember { mutableStateOf<com.fsscustomerapplication.data.remote.model.TicketInvoice?>(null) }

                com.fsscustomerapplication.ui.screens.TicketsScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onPayWithRazorpay = { invoice ->
                        activePaymentInvoice = invoice
                    }
                )

                if (activePaymentInvoice != null) {
                    com.fsscustomerapplication.ui.components.RazorpayPaymentSheet(
                        invoiceNo = activePaymentInvoice!!.invoiceNo ?: "INV-#${activePaymentInvoice!!.id}",
                        amount = activePaymentInvoice!!.displayBalanceAmount(),
                        itemName = "Payment for Invoice #${activePaymentInvoice!!.invoiceNo ?: activePaymentInvoice!!.id}",
                        customerName = sessionManager.getUserName().ifBlank { "Customer" },
                        customerPhone = sessionManager.getUserPhone().ifBlank { "9848012345" },
                        customerEmail = sessionManager.getUserEmail().ifBlank { "support@friendssoftware.in" },
                        onDismiss = { activePaymentInvoice = null },
                        onPaymentSuccess = { paymentId ->
                            activePaymentInvoice = null
                        }
                    )
                }
            }
            composable(
                route = "services/{userId}?productId={productId}",
                arguments = listOf(
                    androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("productId") { 
                        type = androidx.navigation.NavType.IntType
                        defaultValue = -1 
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: sessionManager.getUserId()
                val productId = backStackEntry.arguments?.getInt("productId") ?: -1
                var activePaymentService by remember { mutableStateOf<com.fsscustomerapplication.data.remote.model.ProductService?>(null) }

                ServicesScreen(
                    userId = userId,
                    selectedProductId = if (productId != -1) productId else null,
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate("dashboard/$userId") {
                            popUpTo("dashboard/$userId") { inclusive = true }
                        }
                    },
                    onNavigateToReports = {
                        navController.navigate("reports/$userId")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile/$userId")
                    },
                    onGetServiceSupport = { service ->
                        try {
                            val prunedItem = service.copy(services = emptyList(), addons = emptyList())
                            val itemJson = URLEncoder.encode(Gson().toJson(prunedItem), StandardCharsets.UTF_8.toString())
                            navController.navigate("item_details/$itemJson")
                        } catch (e: Exception) {
                            // Fallback: minimal data to avoid size limit
                            val itemJson = URLEncoder.encode("{\"id\":${service.id},\"name\":\"${service.displayName()}\"}", StandardCharsets.UTF_8.toString())
                            navController.navigate("item_details/$itemJson")
                        }
                    },
                    onPayWithRazorpay = { service ->
                        activePaymentService = service
                    }
                )

                if (activePaymentService != null) {
                    com.fsscustomerapplication.ui.components.RazorpayPaymentSheet(
                        invoiceNo = activePaymentService!!.displayKey(),
                        amount = 12500.0,
                        itemName = "Renewal for ${activePaymentService!!.displayName()} (#${activePaymentService!!.displayKey()})",
                        customerName = sessionManager.getUserName().ifBlank { "Customer" },
                        customerPhone = sessionManager.getUserPhone().ifBlank { "9848012345" },
                        customerEmail = sessionManager.getUserEmail().ifBlank { "support@friendssoftware.in" },
                        onDismiss = { activePaymentService = null },
                        onPaymentSuccess = { paymentId ->
                            activePaymentService = null
                        }
                    )
                }
            }
            composable("licence_details/{licenseNo}") { backStackEntry ->
                val licenseNo = backStackEntry.arguments?.getString("licenseNo") ?: ""
                val userId = sessionManager.getUserId()
                LicencesDetailsScreen(
                    licenseNo = licenseNo,
                    customerId = userId,
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("dashboard/$userId") },
                    onNavigateToLicences = { navController.navigate("services/$userId") },
                    onNavigateToReports = { navController.navigate("reports/$userId") },
                    onNavigateToProfile = { navController.navigate("profile/$userId") }
                )
            }
            composable("reports/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: sessionManager.getUserId()
                com.fsscustomerapplication.ui.screens.ReportsScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onRequestEarlyAccess = {
                        navController.navigate("spoc_chat/")
                    }
                )
            }
            composable("renewal_summary/{licenseNo}") { backStackEntry ->
                val licenseNo = backStackEntry.arguments?.getString("licenseNo") ?: ""
                RenewalSummaryScreen(
                    licenseNumber = licenseNo,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "spoc_chat/{licenseNo}?spocName={spocName}&spocPhone={spocPhone}",
                arguments = listOf(
                    androidx.navigation.navArgument("licenseNo") { 
                        type = androidx.navigation.NavType.StringType
                        defaultValue = "" 
                    },
                    androidx.navigation.navArgument("spocName") { 
                        type = androidx.navigation.NavType.StringType
                        defaultValue = "" 
                    },
                    androidx.navigation.navArgument("spocPhone") { 
                        type = androidx.navigation.NavType.StringType
                        defaultValue = "" 
                    }
                )
            ) { backStackEntry ->
                val licenseNo = backStackEntry.arguments?.getString("licenseNo") ?: ""
                val spocName = backStackEntry.arguments?.getString("spocName") ?: ""
                val spocPhone = backStackEntry.arguments?.getString("spocPhone") ?: ""
                val userId = sessionManager.getUserId()
                com.fsscustomerapplication.ui.screens.SpocChatScreen(
                    userId = userId,
                    licenseNo = licenseNo,
                    spocNameParam = spocName.ifBlank { null },
                    spocPhoneParam = spocPhone.ifBlank { null },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
