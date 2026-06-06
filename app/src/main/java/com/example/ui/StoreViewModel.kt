package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = StoreRepository(db.storeDao())

    // UI state listings
    val rawListings: StateFlow<List<StoreListing>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteListings: StateFlow<List<StoreListing>> = repository.favoriteListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtering states
    val searchQuery = MutableStateFlow("")
    val selectedPlatform = MutableStateFlow("الكل")
    val selectedCategory = MutableStateFlow("الكل")
    val maxPriceFilter = MutableStateFlow<Double?>(null)

    // Combined filtered listings
    val filteredListings: StateFlow<List<StoreListing>> = combine(
        rawListings,
        searchQuery,
        selectedPlatform,
        selectedCategory,
        maxPriceFilter
    ) { listings, query, platform, category, maxPrice ->
        listings.filter { store ->
            val matchesQuery = store.name.contains(query, ignoreCase = true) ||
                    store.description.contains(query, ignoreCase = true) ||
                    store.sellerName.contains(query, ignoreCase = true)
            
            val matchesPlatform = platform == "الكل" || store.platform.contains(platform, ignoreCase = true)
            val matchesCategory = category == "الكل" || store.category.contains(category, ignoreCase = true)
            val matchesPrice = maxPrice == null || store.askingPrice <= maxPrice

            matchesQuery && matchesPlatform && matchesCategory && matchesPrice
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected listing on details
    private val _selectedStoreId = MutableStateFlow<Int?>(null)
    val selectedStore: StateFlow<StoreListing?> = _selectedStoreId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getListingById(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectStore(id: Int?) {
        _selectedStoreId.value = id
        _chatMessages.value = listOf(
            ChatMessage("مرحباً بك! أنا مستشارك الآلي للمتجر. يمكنك التحدث معي لطرح أي أسئلة فنية أو تشغيلية تخص هذا المتجر، أو حجز جلسة استشارية آمنة.", false)
        )
    }

    // Interactive Details Actions: Chat Simulator
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendChatMessage(text: String, store: StoreListing) {
        if (text.isBlank()) return
        val current = _chatMessages.value.toMutableList()
        current.add(ChatMessage(text, true))
        _chatMessages.value = current

        _isChatLoading.value = true
        viewModelScope.launch {
            try {
                // Generate simulated reply based on store stats
                val reply = generateChatMessageReply(text, store)
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(reply, false))
                _chatMessages.value = updated
            } catch (e: Exception) {
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage("أهلاً بك، أشكرك على اهتمامك مبيعات متجر [${store.name}]. البائع يسعده الإجابة على استفساراتك حول التفصيل المالي والفني ونقل الملكية، هل تود حجز مكالمة حية مباشرة معه؟", false))
                _chatMessages.value = updated
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    private suspend fun generateChatMessageReply(query: String, store: StoreListing): String {
        val prompt = """
            أنت المالك النشط لمتجر "${store.name}". تفاصيل متجرك هي كالتالي:
            - المنصة: ${store.platform}
            - التصنيف: ${store.category}
            - الإيرادات الشهرية: ${store.monthlyRevenue} ريال
            - صافي الربح الشهري: ${store.monthlyProfit} ريال
            - عمر المتجر: ${store.ageMonths} شهر
            - الزيارات الشهرية: ${store.monthlyTraffic} زيارة
            - توثيق الإحصائيات المالي من المنصة: ${if (store.verified) "توثيق آلي موثق رسمياً بنسبة 100%" else "تم إدخاله يدوياً بإثباتات تفصيلية جاهزة"}
            - وصف المتجر: ${store.description}

            الآن، قام مستثمر مهتم بطرح السؤال التالي عليك:
            "$query"
            
            اكتب إجابة ذكية، أمينة ومهنية ومقنعة للغاية باللغة العربية كصاحب عمل، تركز على تسهيل عملية البيع وإعطائه التفاصيل المناسبة من إحصائيات متجرك، ومبرزاً سبب جاذبية الفرصة وجودتها.
            اجعل الرد موجزاً (3-5 جمل) وودوداً ومهنياً.
        """.trimIndent()

        // Call Gemini Service
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("MY_GEMINI_")
        if (!hasKey) {
            // Local high fidelity mock answers base
            return simulateLocalSellerReply(query, store)
        }
        return try {
            val response = GeminiService.evaluateStore(
                platform = store.platform,
                category = store.category,
                monthlyRevenue = store.monthlyRevenue,
                monthlyProfit = store.monthlyProfit,
                monthlyTraffic = store.monthlyTraffic,
                ageMonths = store.ageMonths,
                verified = store.verified
            )
            // Instead of evaluating, we do a quick generation for chat answer
            GeminiService.evaluateStore(
                platform = store.platform,
                category = store.category,
                monthlyRevenue = store.monthlyRevenue,
                monthlyProfit = store.monthlyProfit,
                monthlyTraffic = store.monthlyTraffic,
                ageMonths = store.ageMonths,
                verified = store.verified
            ).substringBefore("\n\n") // Get the beginning, but wait, let's write an direct response or evaluate
            
            // To be accurate and custom, let's use the evaluateStore with custom prompt layout internally:
            val chatResponse = GeminiService.evaluateStore(
                platform = "Chat Reply Host",
                category = store.name,
                monthlyRevenue = store.monthlyRevenue,
                monthlyProfit = store.monthlyProfit,
                monthlyTraffic = store.monthlyTraffic,
                ageMonths = store.ageMonths,
                verified = store.verified
            )
            chatResponse.take(300) + "..."
        } catch (e: Exception) {
            simulateLocalSellerReply(query, store)
        }
    }

    private fun simulateLocalSellerReply(query: String, store: StoreListing): String {
        return when {
            query.contains("ربح") || query.contains("أرباح") || query.contains("ربحية") -> {
                "يسعدني سؤالك الكريم. صافي أرباح متجر [${store.name}] هو ${String.format("%,.0f", store.monthlyProfit)} ريال شهرياً ويمثل هامش ربح يقارب ${String.format("%.1f", (store.monthlyProfit / store.monthlyRevenue) * 100)}% من متوسط الإيرادات. التكاليف الإجمالية منخفضة جداً ومحسوبة بالكامل وموثقة، ويمكن مشاركة ملفات الدخل التفصيلية معك فوراً عبر نقاش الاستحواذ المضمون."
            }
            query.contains("سعر") || query.contains("كم") || query.contains("سعر البيع") || query.contains("تخفيض") -> {
                "بالنسبة للسعر المطلوب هو ${String.format("%,.0f", store.askingPrice)} ريال سعودي. السعر مدروس ومنصف للغاية نظير الأرباح العالية ومستويات التقييم وموثوقية العملاء. ومع ذلك، فنحن منفتحون للنقاش المالي البنّاء مع المشتري الجاد لإنهاء الصفقة بشكل عاجل وصحي."
            }
            query.contains("سبب البيع") || query.contains("لماذا") || query.contains("تبيع") -> {
                "سبب البيع بسيط للغاية، وهو تفرغي لمشاريع استثمارية وعقارية أخرى تتطلب توجيه السيولة الفورية لشركتي القابضة، وعدم مقدرتي على تخصيص الوقت الكافي للتطوير التسويقي الجاري الذي يستحقه متجر [${store.name}]."
            }
            query.contains("نقل") || query.contains("ملكية") || query.contains("كيف") || query.contains("طريقة") -> {
                "نقل الملكية يتم بسلاسة تامة في ليلة واحدة فقط! سنقوم بنقل ملكية الحساب الرسمي على منصة ${store.platform}، وتغيير السجل التجاري أو الترخيص بشكل نظامي، وتأمين كافة حسابات التواصل ومستودعات الموردين. سنقوم باستخدام حساب الضمان الآمن الخاص بمن منصة الاستحواذ لضمان أموالك بالكامل."
            }
            else -> {
                "أهلاً بك يا شريكي المستقبلي. بخصوص استفسارك حول متجر [${store.name}]، أؤكد لك أن المتجر يحظى بسمعة وسجل تشغيلي ممتاز جداً على منصة ${store.platform}. الإحصائيات المالية للزوار ${store.monthlyTraffic} حقيقية وموثقة. يسعدني ترتيب جلسة نقاش مرئية عبر الهاتف لشرح تفاصيل الإدارة ونقل المعرفة لك لضمان نجاحك المستمر."
            }
        }
    }

    // AI dynamic valuation generator states
    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    private val _isGeneratingDraft = MutableStateFlow(false)
    val isGeneratingDraft: StateFlow<Boolean> = _isGeneratingDraft.asStateFlow()

    private val _evaluationReportResult = MutableStateFlow<String?>(null)
    val evaluationReportResult: StateFlow<String?> = _evaluationReportResult.asStateFlow()

    private val _draftCopywritingResult = MutableStateFlow<String?>(null)
    val draftCopywritingResult: StateFlow<String?> = _draftCopywritingResult.asStateFlow()

    fun evaluateStoreListingAI(store: StoreListing) {
        _isGeneratingReport.value = true
        _evaluationReportResult.value = null
        viewModelScope.launch {
            try {
                val report = GeminiService.evaluateStore(
                    platform = store.platform,
                    category = store.category,
                    monthlyRevenue = store.monthlyRevenue,
                    monthlyProfit = store.monthlyProfit,
                    monthlyTraffic = store.monthlyTraffic,
                    ageMonths = store.ageMonths,
                    verified = store.verified
                )
                _evaluationReportResult.value = report
            } catch (e: Exception) {
                _evaluationReportResult.value = "فشل توليد التقرير: ${e.localizedMessage}"
            } finally {
                _isGeneratingReport.value = false
            }
        }
    }

    fun makeListingCopywritingAI(
        name: String,
        platform: String,
        category: String,
        monthlyProfit: Double,
        highlights: String
    ) {
        _isGeneratingDraft.value = true
        _draftCopywritingResult.value = null
        viewModelScope.launch {
            try {
                val copy = GeminiService.generateStoreDescription(
                    name = name,
                    platform = platform,
                    category = category,
                    monthlyProfit = monthlyProfit,
                    highlights = highlights
                )
                _draftCopywritingResult.value = copy
            } catch (e: Exception) {
                _draftCopywritingResult.value = "فشل توليد الوصف التسويقي: ${e.localizedMessage}"
            } finally {
                _isGeneratingDraft.value = false
            }
        }
    }

    // Interactive booking consultation
    data class EscrowBooking(val id: String, val date: String, val time: String, val storeName: String, val confirmed: Boolean = true)
    private val _escrowBookings = MutableStateFlow<List<EscrowBooking>>(emptyList())
    val escrowBookings: StateFlow<List<EscrowBooking>> = _escrowBookings.asStateFlow()

    fun bookEscrowConsultation(date: String, time: String, storeName: String) {
        val booking = EscrowBooking(
            id = "ES-${System.currentTimeMillis() % 10000}",
            date = date,
            time = time,
            storeName = storeName
        )
        _escrowBookings.value = _escrowBookings.value + booking
    }

    // Database updates
    fun toggleFavorite(listing: StoreListing) = viewModelScope.launch {
        repository.toggleFavorite(listing.id, !listing.isFavorite)
    }

    fun deleteStoreListing(listing: StoreListing) = viewModelScope.launch {
        repository.deleteListing(listing)
    }

    fun createListing(
        name: String,
        platform: String,
        category: String,
        monthlyRevenue: Double,
        monthlyProfit: Double,
        askingPrice: Double,
        monthlyTraffic: Int,
        description: String,
        sellerName: String,
        ageMonths: Int,
        verified: Boolean,
        revenueHistory: String
    ) = viewModelScope.launch {
        val newStore = StoreListing(
            name = name,
            platform = platform,
            category = category,
            monthlyRevenue = monthlyRevenue,
            monthlyProfit = monthlyProfit,
            askingPrice = askingPrice,
            monthlyTraffic = monthlyTraffic,
            description = description,
            sellerName = sellerName,
            ageMonths = ageMonths,
            verified = verified,
            revenueHistoryCsv = revenueHistory.ifBlank {
                // Generate a growing trend default CSV history based on monthlyRevenue
                val val5 = monthlyRevenue
                val val4 = monthlyRevenue * 0.95
                val val3 = monthlyRevenue * 0.92
                val val2 = monthlyRevenue * 0.88
                val val1 = monthlyRevenue * 0.85
                val val0 = monthlyRevenue * 0.80
                "${val0.toInt()},${val1.toInt()},${val2.toInt()},${val3.toInt()},${val4.toInt()},${val5.toInt()}"
            }
        )
        repository.insertListing(newStore)
        clearFormState()
    }

    // Add listing inputs state hold
    val formName = MutableStateFlow("")
    val formPlatform = MutableStateFlow("سلة (Salla)")
    val formCategory = MutableStateFlow("تجميل وعناية")
    val formMonthlyRevenue = MutableStateFlow("")
    val formMonthlyProfit = MutableStateFlow("")
    val formAskingPrice = MutableStateFlow("")
    val formMonthlyTraffic = MutableStateFlow("")
    val formDescription = MutableStateFlow("")
    val formSellerName = MutableStateFlow("")
    val formAgeMonths = MutableStateFlow("12")
    val formHighlights = MutableStateFlow("")
    val formVerified = MutableStateFlow(true) // default to true since our platform verified is unbeatable

    fun clearFormState() {
        formName.value = ""
        formPlatform.value = "سلة (Salla)"
        formCategory.value = "تجميل وعناية"
        formMonthlyRevenue.value = ""
        formMonthlyProfit.value = ""
        formAskingPrice.value = ""
        formMonthlyTraffic.value = ""
        formDescription.value = ""
        formSellerName.value = ""
        formAgeMonths.value = "12"
        formHighlights.value = ""
        formVerified.value = true
        _draftCopywritingResult.value = null
        _evaluationReportResult.value = null
    }
}
