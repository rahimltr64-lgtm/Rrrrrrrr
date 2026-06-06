package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [StoreListing::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "store_marketplace_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.storeDao())
                }
            }
        }

        suspend fun populateDatabase(storeDao: StoreDao) {
            // Seed with incredibly polished starting listings representing elite stores
            val initialStores = listOf(
                StoreListing(
                    name = "متجر كوزمو للعناية العضوية ✨",
                    platform = "سلة (Salla)",
                    category = "تجميل وعناية",
                    monthlyRevenue = 45000.0,
                    monthlyProfit = 16200.0,
                    askingPrice = 175000.0,
                    monthlyTraffic = 38000,
                    description = "متجر سعودي متميز متخصص في العناية والجمال العضوي بمنتجات مرخصة وسلسلة إمداد مجهزة بالكامل ونموذج دروبشيبينغ مؤتمت بنسبة 90%. يتمتع المتجر بقاعدة عملاء نشطة تزيد عن 8,000 عميل مسجل، ومعدل شراء متكرر 35%، وحسابات تواصل اجتماعي نشطة بأكثر من 20 ألف متابع حقيقي.",
                    logoUrl = "ORGANIC",
                    verified = true,
                    sellerName = "عبدالرحمن الشمري",
                    ageMonths = 18,
                    revenueHistoryCsv = "28000,32000,35000,41000,43000,45000"
                ),
                StoreListing(
                    name = "تيك زون - ملحقات الهواتف والذكاء الاصطناعي 🔌",
                    platform = "شوبيفاي (Shopify)",
                    category = "إلكترونيات",
                    monthlyRevenue = 85000.0,
                    monthlyProfit = 24500.0,
                    askingPrice = 290000.0,
                    monthlyTraffic = 72000,
                    description = "براند إلكتروني لبيع ملحقات الهواتف الذكية والأدوات الذكية بتصاميم عصرية واشتراكات حصرية مع كبار المصنعين. يتم الشحن المباشر السريع من مستودعات في دبي إلى جميع دول الخليج. عقود موثقة ونظام تتبع شحن متكامل مع تكاليف إعلانية منخفضة وعائد استثماري متميز ROAS 4.2.",
                    logoUrl = "ELECTRONICS",
                    verified = true,
                    sellerName = "سارة أحمد",
                    ageMonths = 24,
                    revenueHistoryCsv = "70000,74000,81000,79000,83000,85000"
                ),
                StoreListing(
                    name = "خيوط الأناقة - أزياء رجالية فاخرة 👔",
                    platform = "زد (Zid)",
                    category = "أزياء وملابس",
                    monthlyRevenue = 120000.0,
                    monthlyProfit = 42000.0,
                    askingPrice = 540000.0,
                    monthlyTraffic = 110000,
                    description = "متجر سعودي رائد لبيع الملابس الجاهزة الفخمة والأثواب الأنيقة. يتميز بالتعامل مع مصانع محلية ومعامل تفصيل متعاقد معها حصرياً، تضمن هوامش ربح عالية تتخطى 50%. يشمل البيع العلامة التجارية، المخزون الحالي بقيمة 80,000 ريال، وحساب موثق في زد بالإضافة لخدمة شحن سلسة.",
                    logoUrl = "CLOTHING",
                    verified = true,
                    sellerName = "محمد بن فيصل",
                    ageMonths = 36,
                    revenueHistoryCsv = "98000,105000,112000,108000,115000,120000"
                ),
                StoreListing(
                    name = "مطبخك العائلي - أدوات منزلية مبتكرة 🍳",
                    platform = "سلة (Salla)",
                    category = "المنزل والمطبخ",
                    monthlyRevenue = 32000.0,
                    monthlyProfit = 9500.0,
                    askingPrice = 98000.0,
                    monthlyTraffic = 19500,
                    description = "متجر لمنتجات وأدوات المطبخ والحلول المنزلية المبتكرة التي تحل مشاكل يومية. يتم تسويق المنتجات عبر فيديوهات تيك توك الفيروسية والريلز. تكلفة التشغيل منخفضة جداً والاعتماد كامل على الشحن بالطلب. فرصة نمو كبرى للمشتري الجديد عبر تفعيل حملات جوجل وسناب شات.",
                    logoUrl = "HOME",
                    verified = false,
                    sellerName = "خالد العتيبي",
                    ageMonths = 10,
                    revenueHistoryCsv = "18000,21000,24000,28000,30000,32000"
                ),
                StoreListing(
                    name = "متجر ألعاب فيوتشر - ترفيه أطفال 🦄",
                    platform = "أمازون (Amazon)",
                    category = "ألعاب وأطفال",
                    monthlyRevenue = 64000.0,
                    monthlyProfit = 18000.0,
                    askingPrice = 195000.0,
                    monthlyTraffic = 45000,
                    description = "متجر معتمد على منصة أمازون السعودية لبيع الألعاب التعليمية والذكية للأطفال بماركة مسجلة. تقييم المتجر 4.9 نجوم بناءً على أكثر من 1500 تقييم إيجابي. البيع يشمل حقوق الماركة وحساب التاجر النشط والربط مع شركة الشحن FBA (مستودعات أمازون).",
                    logoUrl = "TOYS",
                    verified = true,
                    sellerName = "منى الهاشمي",
                    ageMonths = 15,
                    revenueHistoryCsv = "52000,55000,58000,61000,59000,64000"
                )
            )
            storeDao.insertAll(initialStores)
        }
    }
}
