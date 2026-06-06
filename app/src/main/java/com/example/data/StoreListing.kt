package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_listings")
data class StoreListing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val platform: String,          // سلة (Salla), شوبيفاي (Shopify), زد (Zid), أمازون (Amazon), إلخ
    val category: String,          // إلكترونيات, أزياء, تجميل, خدمات, ألعاب
    val monthlyRevenue: Double,    // الإيرادات الشهرية
    val monthlyProfit: Double,     // الأرباح الصافية الشهرية
    val askingPrice: Double,       // السعر المطلوب
    val monthlyTraffic: Int,       // الزيارات الشهرية
    val description: String,       // الوصف الكامل
    val logoUrl: String = "",      // رابط اللوجو / الأيقونة
    val verified: Boolean = false,  // موثق من المنصة الرسمية (Shopify/Salla API Sync)
    val sellerName: String,        // اسم البائع
    val ageMonths: Int = 12,       // عمر المتجر بالأشهر
    val isFavorite: Boolean = false, // مضاف للمفضلة
    val revenueHistoryCsv: String = "", // إيرادات آخر 6 أشهر تفصلها فاصلة (مثال: "12000,15000,14000,17000,19000,18500")
    val createdAt: Long = System.currentTimeMillis()
)
