package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.StoreListing
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ExploreTab(val route: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    MARKETPLACE("marketplace", "السوق", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    VALUATION("valuation", "تقييم ذكي", Icons.Filled.Calculate, Icons.Outlined.Calculate),
    SELL("sell", "أضف متجر", Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
    SAFETY("safety", "دليل الأمان", Icons.Filled.Security, Icons.Outlined.Security),
    FAVORITES("favorites", "المحفظة", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreMarketplaceApp(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(ExploreTab.MARKETPLACE) }
    var selectedStoreIdForDetails by remember { mutableStateOf<Int?>(null) }
    
    val selectedStore by viewModel.selectedStore.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Monitor changes in detail selection
    LaunchedEffect(selectedStoreIdForDetails) {
        viewModel.selectStore(selectedStoreIdForDetails)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "حصري الاستحواذ",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Text(
                            text = "سوق المتاجر",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 21.sp
                            ),
                            textAlign = TextAlign.End
                        )
                    }
                },
                navigationIcon = {
                    if (selectedStoreIdForDetails != null) {
                        IconButton(onClick = { selectedStoreIdForDetails = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "أمان الاستحواذ",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, size.height - strokeWidth),
                        end = Offset(size.width, size.height - strokeWidth),
                        strokeWidth = strokeWidth
                    )
                }
            )
        },
        bottomBar = {
            if (selectedStoreIdForDetails == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                    }
                ) {
                    ExploreTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = if (selectedStoreIdForDetails != null) "details" else currentTab.route,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { state ->
                when (state) {
                    "details" -> {
                        selectedStore?.let { store ->
                            StoreDetailScreen(
                                store = store,
                                viewModel = viewModel,
                                onBackPressed = { selectedStoreIdForDetails = null }
                            )
                        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    ExploreTab.MARKETPLACE.route -> {
                        MarketplaceScreen(
                            viewModel = viewModel,
                            onStoreClicked = { id -> selectedStoreIdForDetails = id }
                        )
                    }
                    ExploreTab.VALUATION.route -> {
                        ValuationCalculatorScreen(viewModel = viewModel)
                    }
                    ExploreTab.SELL.route -> {
                        AddListingScreen(viewModel = viewModel) {
                            currentTab = ExploreTab.MARKETPLACE
                        }
                    }
                    ExploreTab.SAFETY.route -> {
                        SafetyGuideScreen()
                    }
                    ExploreTab.FAVORITES.route -> {
                        FavoritesAndBookingsScreen(
                            viewModel = viewModel,
                            onStoreClicked = { id -> selectedStoreIdForDetails = id }
                        )
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 1: MARKETPLACE ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceScreen(
    viewModel: StoreViewModel,
    onStoreClicked: (Int) -> Unit
) {
    val listings by viewModel.filteredListings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    
    val platforms = listOf("الكل", "سلة", "شوبيفاي", "زد", "أمازون")
    val categories = listOf("الكل", "تجميل وعناية", "إلكترونيات", "أزياء وملابس", "المنزل والمطبخ", "ألعاب وأطفال")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and intro card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "استحوذ على مستقبلك التجاري اليوم 🚀",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "منصة متكاملة لبيع وشراء المتاجر الإلكترونية النشطة والمربحة بنظام وساطة آمن بالكامل ومحقَق بالذكاء الاصطناعي.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Search and Filters layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { 
                        Text("ابحث باسم المتجر، التصنيف، الوصف أو البائع...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) 
                    },
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("store_search_input")
                )

                // Platforms pills
                Text(
                    text = "حسب منصة التشغيل:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    textAlign = TextAlign.End
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(platforms) { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { viewModel.selectedPlatform.value = platform },
                            label = { Text(platform, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Categories pills
                Text(
                    text = "حسب تصنيف النشاط الافتراضي:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    textAlign = TextAlign.End
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.selectedCategory.value = category },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        // Listings cards
        if (listings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "لا يوجد متاجر",
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم نجد أي متاجر مطابقة للخيارات المحددة.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(listings, key = { it.id }) { store ->
                StoreItemCard(
                    store = store,
                    onFavClicked = { viewModel.toggleFavorite(store) },
                    onCardClicked = { onStoreClicked(store.id) }
                )
            }
        }
    }
}

@Composable
fun StoreItemCard(
    store: StoreListing,
    onFavClicked: () -> Unit,
    onCardClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClicked)
            .testTag("store_card_${store.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Upper row: Logo indicator, Titles, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Favorite Button
                IconButton(onClick = onFavClicked, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (store.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "حفظ",
                        tint = if (store.isFavorite) Color.Red else Color.Gray
                    )
                }

                // Title and badges
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = store.category,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Platform Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = store.platform,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Verified Badge
                        if (store.verified) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandEmerald.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "إحصائيات موثقة",
                                        fontSize = 10.sp,
                                        color = BrandEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "موثق",
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Dummy Logo placeholder
                LogoPlaceholder(category = store.logoUrl)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Lower area: Crucial Financial Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Asking price
                Column(horizontalAlignment = Alignment.Start) {
                    Text("السعر المطلوب", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${String.format("%,.0f", store.askingPrice)} ر.س",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Profit
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("صافي الربح الشهري", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${String.format("%,.0f", store.monthlyProfit)} ر.س/شهرياً",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald
                    )
                }

                // Revenue
                Column(horizontalAlignment = Alignment.End) {
                    Text("المبيعات الشهرية", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${String.format("%,.0f", store.monthlyRevenue)} ر.س",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عمر المتجر: ${store.ageMonths} شهراً  •  الزيارات: ${String.format("%,d", store.monthlyTraffic)}/شهرياً",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun LogoPlaceholder(category: String, modifier: Modifier = Modifier) {
    val (bg, icon, color) = when (category) {
        "ORGANIC" -> Triple(Color(0xFFE8F5E9), Icons.Default.Spa, BrandEmerald)
        "ELECTRONICS" -> Triple(Color(0xFFE3F2FD), Icons.Default.Bolt, Color(0xFF1E88E5))
        "CLOTHING" -> Triple(Color(0xFFFFF3E0), Icons.Default.Checkroom, Color(0xFFFB8C00))
        "HOME" -> Triple(Color(0xFFEDE7F6), Icons.Default.Countertops, Color(0xFF5E35B1))
        "TOYS" -> Triple(Color(0xFFFCE4EC), Icons.Default.SmartToy, Color(0xFFD81B60))
        else -> Triple(Color(0xFFECEFF1), Icons.Default.ShoppingBag, Color(0xFF546E7A))
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==================== SCREEN 2: VALUATION ENGINE ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ValuationCalculatorScreen(viewModel: StoreViewModel) {
    var platformInput by remember { mutableStateOf("سلة (Salla)") }
    var categoryInput by remember { mutableStateOf("تجميل وعناية") }
    var revenueInput by remember { mutableStateOf("") }
    var profitInput by remember { mutableStateOf("") }
    var trafficInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("12") }
    var verifiedInput by remember { mutableStateOf(true) }

    val isGenerating by viewModel.isGeneratingReport.collectAsStateWithLifecycle()
    val reportResult by viewModel.evaluationReportResult.collectAsStateWithLifecycle()

    val platforms = listOf("سلة (Salla)", "شوبيفاي (Shopify)", "زد (Zid)", "أمازون (Amazon)", "ووردبريس (WooCommerce)", "أخرى")
    val categories = listOf("تجميل وعناية", "إلكترونيات", "أزياء وملابس", "المنزل والمطبخ", "ألعاب وأطفال", "عقود وخدمات", "أخرى")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "مقيم الأداء المالي الذكي 🧮",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "احسب القيمة السوقية العادلة لأي متجر إلكتروني بناءً على محركات الذكاء الاصطناعي وصيغ التثمين المالي المعتمدة لدى المستثمرين.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "أدخل بيانات تشغيل المتجر لتثمين الأداء دقيقاً:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Platform select
                    Text("منصة تأسيس المتجر", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        platforms.forEach { pl ->
                            FilterChip(
                                selected = platformInput == pl,
                                onClick = { platformInput = pl },
                                label = { Text(pl, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Category select
                    Text("تصنيف نشاط المبيعات الرئيسي", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = categoryInput == cat,
                                onClick = { categoryInput = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Monthly Revenue
                    OutlinedTextField(
                        value = revenueInput,
                        onValueChange = { revenueInput = it },
                        label = { Text("متوسط الإيرادات الشهرية (ر.س)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Monthly Profit
                    OutlinedTextField(
                        value = profitInput,
                        onValueChange = { profitInput = it },
                        label = { Text("متوسط صافي الربح الشهري الحقيقي (ر.س)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Traffic
                    OutlinedTextField(
                        value = trafficInput,
                        onValueChange = { trafficInput = it },
                        label = { Text("متوسط الزيارات الشهرية (زيارة)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Age in Months
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("عمر المتجر بالأشهر", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Verify toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = verifiedInput,
                            onCheckedChange = { verifiedInput = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandEmerald,
                                checkedTrackColor = BrandEmerald.copy(alpha = 0.3f)
                            )
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text("محقق عبر ربط الـ API", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("رفع مستوى موثوقية وجودة الأرقام يزيد من الثمن والجاذبية بنسبة 30%", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            val rev = revenueInput.toDoubleOrNull() ?: 0.0
                            val prof = profitInput.toDoubleOrNull() ?: 0.0
                            val traf = trafficInput.toIntOrNull() ?: 0
                            val age = ageInput.toIntOrNull() ?: 12
                            viewModel.evaluateStoreListingAI(
                                StoreListing(
                                    name = "متجر تقييم تجريبي",
                                    platform = platformInput,
                                    category = categoryInput,
                                    monthlyRevenue = rev,
                                    monthlyProfit = prof,
                                    askingPrice = 0.0,
                                    monthlyTraffic = traf,
                                    description = "",
                                    sellerName = "محاكاة",
                                    ageMonths = age,
                                    verified = verifiedInput
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_valuation_btn"),
                        enabled = !isGenerating && revenueInput.isNotBlank() && profitInput.isNotBlank()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ابدأ الاستعلام والتقييم بالذكاء الاصطناعي 🧠", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Output Valuation report
        if (isGenerating || reportResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.DownloadForOffline, contentDescription = "تحميل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { })
                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { })
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("تقرير تثمين الأداء المتكامل", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isGenerating) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("يجري حالياً معالجة القوائم المالية واحتساب معايير السوق...", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            Text(
                                text = reportResult ?: "",
                                fontSize = 13.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 3: ADD/SELL PORTAL ====================

@Composable
fun AddListingScreen(viewModel: StoreViewModel, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    val isDrafting by viewModel.isGeneratingDraft.collectAsStateWithLifecycle()
    val draftResult by viewModel.draftCopywritingResult.collectAsStateWithLifecycle()

    val name by viewModel.formName.collectAsStateWithLifecycle()
    val platform by viewModel.formPlatform.collectAsStateWithLifecycle()
    val category by viewModel.formCategory.collectAsStateWithLifecycle()
    val monthlyRevenue by viewModel.formMonthlyRevenue.collectAsStateWithLifecycle()
    val monthlyProfit by viewModel.formMonthlyProfit.collectAsStateWithLifecycle()
    val askingPrice by viewModel.formAskingPrice.collectAsStateWithLifecycle()
    val monthlyTraffic by viewModel.formMonthlyTraffic.collectAsStateWithLifecycle()
    val description by viewModel.formDescription.collectAsStateWithLifecycle()
    val sellerName by viewModel.formSellerName.collectAsStateWithLifecycle()
    val ageMonths by viewModel.formAgeMonths.collectAsStateWithLifecycle()
    val highlights by viewModel.formHighlights.collectAsStateWithLifecycle()
    val verified by viewModel.formVerified.collectAsStateWithLifecycle()

    val platforms = listOf("سلة", "شوبيفاي", "زد", "أمازون", "ووردبريس", "أخرى")
    val categories = listOf("تجميل وعناية", "إلكترونيات", "أزياء وملابس", "المنزل والمطبخ", "ألعاب وأطفال", "عقود وخدمات")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "منصة طرح وبيع متجرك الإلكتروني 💼",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اعرض متجرك للبيع لأكثر من 5,000 مستثمر جاد في منطقة الخليج العربي. استخدم مساعد كتابة الوصف لصياغة تسويقية تقود الاستحواذ فوراً.",
                        color = Color.Black.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "بيانات المتجر الأساسية لطلبات الاستحواذ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    // Store Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.formName.value = it },
                        label = { Text("اسم المتجر الإلكتروني المقترح لغرض البيع", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_store_name")
                    )

                    // Platform select
                    Text("المنصة المضيفة", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        platforms.forEach { pl ->
                            FilterChip(
                                selected = platform == pl,
                                onClick = { viewModel.formPlatform.value = pl },
                                label = { Text(pl, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Category select
                    Text("تصنيف بيع السلع والنشاط", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { viewModel.formCategory.value = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Seller Name
                    OutlinedTextField(
                        value = sellerName,
                        onValueChange = { viewModel.formSellerName.value = it },
                        label = { Text("اسم البائع / صاحب المتجر الافتراضي", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = monthlyRevenue,
                            onValueChange = { viewModel.formMonthlyRevenue.value = it },
                            label = { Text("المبيعات الشهرية (ر.س)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = monthlyProfit,
                            onValueChange = { viewModel.formMonthlyProfit.value = it },
                            label = { Text("صافي الأرباح (ر.س)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = askingPrice,
                            onValueChange = { viewModel.formAskingPrice.value = it },
                            label = { Text("السعر المطلوب (ر.س)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = monthlyTraffic,
                            onValueChange = { viewModel.formMonthlyTraffic.value = it },
                            label = { Text("الزيارات شهرياً", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = { viewModel.formAgeMonths.value = it },
                        label = { Text("عمر المتجر بالأشهر", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = highlights,
                        onValueChange = { viewModel.formHighlights.value = it },
                        label = { Text("أهم المقومات الإضافية (مثال: الشحن مؤتمت، قاعدة عملاء، حساب تيكتوك)", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Copywriting Generator Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.makeListingCopywritingAI(
                                    name = name,
                                    platform = platform,
                                    category = category,
                                    monthlyProfit = monthlyProfit.toDoubleOrNull() ?: 0.0,
                                    highlights = highlights
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            enabled = !isDrafting && name.isNotBlank() && monthlyProfit.isNotBlank()
                        ) {
                            if (isDrafting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("توليد الوصف بالذكاء الاصطناعي ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("وصف المتجر والفرصة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Simulated copy to clipboard indicator or direct update
                    draftResult?.let { draft ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { 
                                        viewModel.formDescription.value = draft
                                    }) {
                                        Text("اعتماد هذا الوصف ومزامنته ✍️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("مسودة وصف ذكي:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = draft,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.formDescription.value = it },
                        placeholder = { Text("اكتب الوصف التفصيلي هنا أو دع الذكاء الاصطناعي يقوم بالصياغة...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 6
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = verified,
                            onCheckedChange = { viewModel.formVerified.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandEmerald)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text("توثيق الإيرادات رسمياً", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("الربط يتيح سحب الإيرادات والأرباح آلياً لضمان مصداقية العرض للجميع", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.End)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.createListing(
                                name = name,
                                platform = platform,
                                category = category,
                                monthlyRevenue = monthlyRevenue.toDoubleOrNull() ?: 0.0,
                                monthlyProfit = monthlyProfit.toDoubleOrNull() ?: 0.0,
                                askingPrice = askingPrice.toDoubleOrNull() ?: 0.0,
                                monthlyTraffic = monthlyTraffic.toIntOrNull() ?: 0,
                                description = description,
                                sellerName = sellerName,
                                ageMonths = ageMonths.toIntOrNull() ?: 12,
                                verified = verified,
                                revenueHistory = ""
                            )
                            onSaved()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_store_btn"),
                        enabled = name.isNotBlank() && askingPrice.isNotBlank() && description.isNotBlank()
                    ) {
                        Text("مراجعة ونشر المتجر للبيع الفوري 🚀", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 4: DETAILED VIEW ====================

@Composable
fun StoreDetailScreen(
    store: StoreListing,
    viewModel: StoreViewModel,
    onBackPressed: () -> Unit
) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var showConsultDialog by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper back button navigation & title Info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleFavorite(store) }) {
                            Icon(
                                imageVector = if (store.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "تفضيل",
                                tint = if (store.isFavorite) Color.Red else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(store.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End)
                                Text("البائع: ${store.sellerName}", fontSize = 12.sp, color = Color.Gray)
                            }
                            LogoPlaceholder(category = store.logoUrl)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Finance tags grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("السعر المطلوب", fontSize = 11.sp, color = Color.Gray)
                            Text("${String.format("%,.0f", store.askingPrice)} ر.س", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("صافي الربح الشهري", fontSize = 11.sp, color = Color.Gray)
                            Text("${String.format("%,.0f", store.monthlyProfit)} ر.س", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("المنصة المضيفة", fontSize = 11.sp, color = Color.Gray)
                            Text(store.platform, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Stats Visual Graph Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مؤشر نمو الأداء والتدفق المالي (آخر 6 أشهر)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "منحنى تطور المبيعات الشهرية الفعلية بالريال السعودي.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Line chart Canvas
                    val points = store.revenueHistoryCsv.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                    if (points.isNotEmpty()) {
                        RevenueLineChart(dataPoints = points)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("البيانات التاريخية قيد المراجعة الرسمية", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الشهر الحالي", fontSize = 10.sp, color = Color.Gray)
                        Text("قبل 6 أشهر", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Description
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text("تقرير النشاط وثيقة الاستحواذ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = store.description,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Action: Escrow Consultation BOOKER Floating Trigger
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "وساطة آمنة",
                        tint = BrandEmerald,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نظام وساطة وضمان الاستحواذ المالي الآمن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تعمل منصتنا كحساب ضمان (Escrow) مرخص لترحيل الحوالة وحفظ البيانات ونقل الملكية بضمان حماية حقوق الطرفين 100%.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Button(
                        onClick = { showConsultDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("حجز جلسة استشارية ونقل ملكية آمن ⚖️", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Chat simulation with dynamic Seller
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التواصل الفوري مع بائع المتجر 💬",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "اطرح أسئلتك على البائع للتفاوض أو الاستيضاح المالي وتلقى إجابة حية فوراً.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat messages container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 220.dp)
                            .background(Color.Gray.copy(alpha = 0.05f))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.forEach { msg ->
                            val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                            val bg = if (msg.isUser) MaterialTheme.colorScheme.primary else Color.White
                            val fg = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            val shape = if (msg.isUser) {
                                RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
                            } else {
                                RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
                            }
                            
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Surface(
                                    color = bg,
                                    contentColor = fg,
                                    shape = shape,
                                    shadowElevation = 1.dp
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp),
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }

                        if (isChatLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp))
                                    Text("يجري كتابة الرد من البائع...", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input message row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.sendChatMessage(chatInput, store)
                                chatInput = ""
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = chatInput.isNotBlank() && !isChatLoading,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("مثال: ما هو سبب البيع؟ هل السعر قابل للتفاوض؟", fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            shape = CircleShape,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Modal consultation booking Dialog
    if (showConsultDialog) {
        Dialog(onDismissRequest = { showConsultDialog = false }) {
            var dateState by remember { mutableStateOf("2026-06-15") }
            var timeState by remember { mutableStateOf("11:00 صباحاً") }
            var bookingSuccess by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!bookingSuccess) {
                        Text("طلب عقد جلسة نقاش آمنة ⚙️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "سنقوم بحجز جلسة لك مع بائع متجر [${store.name}] بإشراف أخصائي الاستحواذ الفني لدى منصتنا من أجل إيضاح العقود والموافقة على نقل الأموال والملكية.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.End
                        )

                        Divider()

                        Text("اختر تاريخ الجلسة الملائم", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { dateState = "2026-06-10" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (dateState == "2026-06-10") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)) {
                                Text("10 يونيو", fontSize = 11.sp)
                            }
                            OutlinedButton(onClick = { dateState = "2026-06-12" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (dateState == "2026-06-12") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)) {
                                Text("12 يونيو", fontSize = 11.sp)
                            }
                            OutlinedButton(onClick = { dateState = "2026-06-15" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (dateState == "2026-06-15") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)) {
                                Text("15 يونيو", fontSize = 11.sp)
                            }
                        }

                        Text("اختر الوقت المفضل للتواصل بتقدير مكة المكرمة", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { timeState = "10:00 صباحاً" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (timeState == "10:00 صباحاً") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)) {
                                Text("10:00 ص", fontSize = 11.sp)
                            }
                            OutlinedButton(onClick = { timeState = "04:30 مساءً" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (timeState == "04:30 مساءً") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)) {
                                Text("4:30 م", fontSize = 11.sp)
                            }
                            OutlinedButton(onClick = { timeState = "08:00 مساءً" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (timeState == "08:00 مساءً") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)) {
                                Text("8:00 م", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.bookEscrowConsultation(dateState, timeState, store.name)
                                bookingSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("تأكيد حجز جلسة الاستحواذ 🌟", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Success receipt screen
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "بنجاح",
                                tint = BrandEmerald,
                                modifier = Modifier.size(56.dp)
                            )
                            Text("تم تأكيد وتوثيق موعد الجلسة! 🎉", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandEmerald)
                            Text(
                                text = "تم إرسال إشعارات الحجز والتذاكر القانونية إلى البائع [${store.sellerName}] ومسؤولي الوساطة المالية. تم التنسيق التلقائي وسيتم الاتصال الهاتفي والمراسلة المعتمدة في التفاصيل التالية:",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Text("المتجر: ${store.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("التاريخ المعتمد: $dateState", fontSize = 11.sp)
                                    Text("التوقيت المقدر: $timeState", fontSize = 11.sp)
                                    Text("قناة التواصل: مكالمة هاتفية حية + رابط Google Meet للاستحواد", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Button(
                                onClick = { showConsultDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("العودة لصفحة المتجر")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueLineChart(dataPoints: List<Double>) {
    val maxVal = dataPoints.maxOrNull() ?: 1.0
    val minVal = dataPoints.minOrNull() ?: 0.0
    val diff = (maxVal - minVal).coerceAtLeast(1.0)

    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

        // Draw background grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = Color.LightGray.copy(alpha = 0.25f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val pointsList = mutableListOf<Offset>()
        for (i in dataPoints.indices) {
            val rawValue = dataPoints[i]
            val x = i * stepX
            // Normalize y coordinate correctly
            val y = height - ((rawValue - minVal) / diff * (height - 30.dp.toPx()) + 15.dp.toPx()).toFloat()
            pointsList.add(Offset(x, y))
        }

        // Draw gradient area below line
        if (pointsList.isNotEmpty()) {
            val gradientPath = Path().apply {
                moveTo(pointsList[0].x, height)
                for (point in pointsList) {
                    lineTo(point.x, point.y)
                }
                lineTo(pointsList.last().x, height)
                close()
            }
            drawPath(
                path = gradientPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        }

        // Draw connecting lines
        for (i in 0 until pointsList.size - 1) {
            drawLine(
                color = primaryColor,
                start = pointsList[i],
                end = pointsList[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw data circles & text labels
        for (i in pointsList.indices) {
            val p = pointsList[i]
            val valueText = "${String.format("%,.0f", dataPoints[i] / 1000)}k"

            drawCircle(
                color = goldColor,
                radius = 5.dp.toPx(),
                center = p
            )

            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = p
            )
        }
    }
}

// ==================== SCREEN 5: SAFETY CENTER ====================

@Composable
fun SafetyGuideScreen() {
    val steps = listOf(
        Triple("الخطوة 1: تقييم وتثمين المتجر 🧮", "يقوم المستثمر أولاً بمراجعة المتجر والتدقيق ببيانات الأداء المالي والزيارات الموثقة آلياً عبر نظام ربط الـ API لمنصة (سوق المتاجر).", Icons.Default.Search),
        Triple("الخطوة 2: حجز جلسة الضمان ⚖️", "يطلب المستثمر حجز جلسة استشارية بإشراف محامٍ ومدقق قانوني تابع لمنصة الاستحواذ لمباركة الصفقة، وتأمين صيغة العقد ونقل الكيان القانوني.", Icons.Default.CalendarMonth),
        Triple("الخطوة 3: إيداع الأموال في الضمان 🔒", "يقوم المشتري بإيداع القيمة المالية المتفق عليها بالكامل مباشرة في الحساب البنكي القانوني للضمان (Escrow) الخاص بالمنصة لضمان النية والجدية المطلقة.", Icons.Default.Lock),
        Triple("الخطوة 4: تسليم الكيان والأصول 📦", "بعد تأكيد ثبوت الإيداع، يلزم البائع بنقل الحسابات وحقوق الماركات، ونوع الاستضافة، وإيقاف المبيعات لترحيل الملكية بالكامل بإشراف الأخصائي الفني للمنصة.", Icons.Default.SwapHoriz),
        Triple("الخطوة 5: تحرير الأموال والتدريب 🎉", "بعد فحص الأصول من المشتري، يتم ترحيل دفعة الاستحواذ لحساب البائع والبدء في فترة المساعدة الفنية والتدريب المتفق عليها لضمان نجاح رائد الأعمال الجديد.", Icons.Default.Recommend)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "دليل الأمان وضمان الاستحقاق ⚖️",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "لماذا يعتبر (سوق المتاجر) نظامنا للوساطة والضمان الاستثنائي للشركات مستحيلاً للمنافسة؟ خذ رحلة تفاعلية مع أسلوب تدوير وتأمين صفقات الاستحواذ.",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        item {
            Text(
                text = "دورة عمل وساطة الاستحواذ الموثقة:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                textAlign = TextAlign.End
            )
        }

        items(steps) { (title, desc, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 6: FAVORITES & AGREEMENTS ====================

@Composable
fun FavoritesAndBookingsScreen(
    viewModel: StoreViewModel,
    onStoreClicked: (Int) -> Unit
) {
    val favoriteListings by viewModel.favoriteListings.collectAsStateWithLifecycle()
    val bookings by viewModel.escrowBookings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Booked meetings Section
        if (bookings.isNotEmpty()) {
            item {
                Text(
                    text = "جلسات الاستحواذ المحجوزة 📅",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    textAlign = TextAlign.End
                )
            }

            items(bookings) { b ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandEmerald.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandEmerald
                        ) {
                            Text(
                                text = "مجدول ومؤكد",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = b.storeName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "رقم التذكرة: ${b.id}", fontSize = 10.sp, color = Color.Gray)
                            Text(text = "التاريخ والوقت: ${b.date} • ${b.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Favorites Stores Section
        item {
            Text(
                text = "محفظة متاجر المفضلة 📁",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                textAlign = TextAlign.End
            )
        }

        if (favoriteListings.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "المحفظة فارغة",
                            tint = Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد متاجر مضافة للمفضلة حتى الآن.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اضغط على أيقونة القلب في أي متجر ليظهر في محفظتك المجهزة لتسهيل المقارنة والمتابعة الفورية.",
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            items(favoriteListings, key = { it.id }) { store ->
                StoreItemCard(
                    store = store,
                    onFavClicked = { viewModel.toggleFavorite(store) },
                    onCardClicked = { onStoreClicked(store.id) }
                )
            }
        }
    }
}
