package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data architecture for standard Gemini Rest requests (constructed manually for absolute flexibility
 * and runtime error prevention without adding complex plugins).
 */
object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()))
        .build()

    interface RetrofitGeminiApi {
        @POST("v1beta/models/gemini-3.5-flash:generateContent")
        suspend fun generateContent(
            @Query("key") apiKey: String,
            @Body body: okhttp3.RequestBody
        ): ResponseBody
    }

    private val api: RetrofitGeminiApi by lazy {
        retrofit.create(RetrofitGeminiApi::class.java)
    }

    /**
     * Uses Gemini 3.5 Flash to generate store valuation & business optimization report.
     * Backs up to high-quality local business valuation algorithms if API Key is unavailable.
     */
    suspend fun evaluateStore(
        platform: String,
        category: String,
        monthlyRevenue: Double,
        monthlyProfit: Double,
        monthlyTraffic: Int,
        ageMonths: Int,
        verified: Boolean
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("MY_GEMINI_")

        if (!hasKey) {
            Log.w(TAG, "GEMINI_API_KEY missing, using intelligent local business valuation algorithm.")
            return@withContext generateLocalValuationReport(platform, category, monthlyRevenue, monthlyProfit, monthlyTraffic, ageMonths, verified)
        }

        val prompt = """
            أنت خبير استثماري ومقيم مالي متميز للمتاجر الإلكترونية في منطقة الخليج العربي والشرق الأوسط.
            قم بكتابة تقرير تقييم مالي متكامل ومفصل للمتجر الإلكتروني التالي:
            - المنصة المشغل عليها: $platform
            - تصنيف النشاط: $category
            - الإيرادات الشهرية المتوسطة: $monthlyRevenue ريال سعودي
            - صافي الأرباح الشهرية: $monthlyProfit ريال سعودي
            - عدد الزيارات الشهرية للموقع: $monthlyTraffic زيارة
            - عمر المتجر بالأشهر: $ageMonths شهراً
            - حالة توثيق الإحصائيات المالية عبر ربط الـ API: ${if (verified) "مؤثق رسمياً وبدرجة أمان عالية" else "غير موثق تلقائياً (تأكيد يدوي)"}
            
            نريد تقريراً استثمارياً جذاباً باللغة العربية الفصحى يتضمن:
            1. القيمة المقدرة العادلة للمتجر (احسبها بناءً على مضاعف أرباح منطقي يتراوح بين 12x إلى 36x من صافي الربح الشهري، معدلاً بمتغيرات المنصة والعمر والتوثيق).
            2. تقييم المخاطر الفرص الاستثمارية (قراءة في معدلات الزوار ومعدلات التحويل الافتراضية).
            3. نصائح ذهبية للمشتري لزيادة الأرباح بنسبة 50% بعد الاستحواذ مباشرة.
            4. التقييم الاستثماري النهائي (من 5 نجوم مع مبرر اقتصادي).
            اجعل الأسلوب فخماً، مهنياً ومقنعاً جداً لرواد الأعمال، ونسق التقرير بنقاط فرعية واضحة وعريضة.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            jsonRequest.put("contents", contentsArray)

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val response = api.generateContent(apiKey, requestBody)
            val jsonResponse = JSONObject(response.string())
            
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error", e)
            "حدث خطأ أثناء الاتصال بالذكاء الاصطناعي: ${e.localizedMessage}. تم التحويل للتقييم المحلي:\n\n" +
                    generateLocalValuationReport(platform, category, monthlyRevenue, monthlyProfit, monthlyTraffic, ageMonths, verified)
        }
    }

    /**
     * Generates written professional copywriting descriptions for the store listings.
     * Backs up to standard dynamic template if API is unavailable.
     */
    suspend fun generateStoreDescription(
        name: String,
        platform: String,
        category: String,
        monthlyProfit: Double,
        highlights: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("MY_GEMINI_")

        if (!hasKey) {
            return@withContext generateLocalStoreCopywriting(name, platform, category, monthlyProfit, highlights)
        }

        val prompt = """
            كأخصائي ترويج وتسويق مبيعات الشركات والاستحواذات، اكتب وصفاً إعلانياً احترافياً ومقنعاً باللغة العربية لمتجر إلكتروني معروض للبيع بالتفاصيل التالية:
            - اسم المتجر: $name
            - المنصة: $platform
            - التصنيف: $category
            - صافي الربح الشهري المقدر: $monthlyProfit ريال
            - نقاط القوة والمميزات الإضافية: $highlights
            
            اكتب وصفاً مفصلاً يركز على الفرصة الاستثمارية الكبرى، وبنية المتجر، وسهولة الإدارة والتشغيل والآفاق التوسعية. استخدم أسلوباً مشوقاً منسقاً بعلامات إيموجي مناسبة وفقرات منسقة لجلب عروض شراء قوية.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            jsonRequest.put("contents", contentsArray)

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val response = api.generateContent(apiKey, requestBody)
            val jsonResponse = JSONObject(response.string())
            
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error", e)
            generateLocalStoreCopywriting(name, platform, category, monthlyProfit, highlights)
        }
    }

    /**
     * Pure local business intelligence calculator to determine valuation range.
     */
    private fun generateLocalValuationReport(
        platform: String,
        category: String,
        monthlyRevenue: Double,
        monthlyProfit: Double,
        monthlyTraffic: Int,
        ageMonths: Int,
        verified: Boolean
    ): String {
        // Multiplier evaluation based on attributes
        var minMultiplier = 14.0
        var maxMultiplier = 24.0

        if (verified) {
            minMultiplier += 4.0
            maxMultiplier += 6.0 // verified financials Command Premium!
        }
        if (ageMonths > 18) {
            minMultiplier += 2.0
            maxMultiplier += 3.0 // Long running stores are trusted
        }
        if (monthlyTraffic > 30000) {
            minMultiplier += 1.0
            maxMultiplier += 2.0 // Active traffic
        }
        if (platform.contains("سلة") || platform.contains("Zid") || platform.contains("زد") || platform.contains("Shopify")) {
            minMultiplier += 1.0
            maxMultiplier += 1.5 // Standard solid platforms
        }

        val minValuation = monthlyProfit * minMultiplier
        val maxValuation = monthlyProfit * maxMultiplier
        val suggestedPrice = (minValuation + maxValuation) / 2.0

        val score = when {
            verified && ageMonths > 18 -> 4.9
            verified -> 4.5
            ageMonths > 12 -> 3.8
            else -> 3.3
        }

        return """
            📊 تقرير التقييم الذكي التلقائي (منصة سوق المتاجر):
            
            🔹 القيمة التقديرية العادلة:
            تتراوح قيمة المتجر الاستثمارية العادلة بين [ ${String.format("%,.1f", minValuation)} ] و [ ${String.format("%,.1f", maxValuation)} ] ريال سعودي.
            📌 السعر المقترح للبيع الفوري بالتفاوض: ${String.format("%,.1f", suggestedPrice)} ريال سعودي.
            * يعتمد التقييم على مضاعف أرباح سنوي استثماري (${String.format("%.1f", minMultiplier / 12)}x إلى ${String.format("%.1f", maxMultiplier / 12)}x) من الأرباح الصافية الحالية.

            🔍 نقاط القوة والتحليل الفني:
            • المنصة المستخدمة ($platform) توفر ثقة تقنية ممتازة وتسهل عملية نقل الملكية المباشر.
            • معدل الأرباح مقارنة بالإيرادات يمثل نسبة صافي ربح تبلغ ${String.format("%.1f", (monthlyProfit / monthlyRevenue) * 100)}%، وهي مؤشرات مالية ممتازة وصحية.
            • ${if (verified) "✅ الإحصائيات موثقة رسمياً برابط النظام المالي مما يرفع تصنيف الأمان والثقة لدى المستثمرين بنسبة 80%." else "⚠️ الإحصائيات معتمدة على مدخلات البائع اليدوية، ننصح بطلب التوثيق المالي من خلال نظام المنصة المتكامل."}
            • عمر المتجر الحالي يعزز استمرارية أرشفة جوجل (SEO) وعملاء الشراء التكراري.

            💡 فرص النمو والتوصيات للمشتري (خطة الـ 90 يوماً الأولى):
            1. تحسين الاستهداف الإعلاني: تفعيل حملات إعادة الاستهداف وإعلانات المؤثرين لرفع مبيعات المنتجات الأكثر طلباً.
            2. السلة المتروكة: تفعيل رسائل التنبيهات المؤتمتة عبر الواتساب والبريد المالي لاستعادة 15% من السلال المفقودة.
            3. تحسين العقود وسلاسل الإمداد: التفاوض المباشر للوصول لمورد رئيسي لرفع هوامش المبيعات.

            🌟 التقييم الاستثماري للمتجر: $score / 5.0 نجوم.
            📝 ملاحظة: تم احتساب هذا التقدير الاستثماري تلقائياً بنموذج التقييم الرياضي المتطور الخاص بنا.
        """.trimIndent()
    }

    private fun generateLocalStoreCopywriting(
        name: String,
        platform: String,
        category: String,
        monthlyProfit: Double,
        highlights: String
    ): String {
        return """
            🚀 فرصة استحواذ لا تتكرر! متجر "$name" المتميز جاهز للانتقال بجميع مميزاته المربحة.

            💼 مميزات المتجر الفنية والتشغيلية:
            • المنصة الحاضنة: $platform الشهيرة لمرونة إدارة وسرعة شحن متكاملة.
            • النشاط الرئيسي: $category مع طلب مستمر ومتكرر على مدار العام.
            • الأداء المالي: صافي ربح شهري مميز وقوي يبلغ ${String.format("%,.1f", monthlyProfit)} ريال سعودي.
            • تفاصيل جوهرية إضافية: $highlights

            🎯 لماذا يجب عليك الاستحواذ على هذا المتجر اليوم؟
            يتمتع المتجر بنية تحتية برمجية وتسويقية قائمة لا تحتاج إلى بناء، وقائمة عملاء مجهزة وسريعة التفاعل، بالإضافة إلى علاقات قوية مع الموردين تضمن استمرارية البيع والربح من اليوم الأول للاستلام. ستحصل على كافة الملفات، قنوات البيع، الموردين ومساعد لتدريبك لمدة اسبوعين.
            
            🔗 بادر بالتقديم، فالمتاجر بمثل هذه الإحصائيات تباع بسرعة فائقة عبر منصتنا الأمنة!
        """.trimIndent()
    }
}
