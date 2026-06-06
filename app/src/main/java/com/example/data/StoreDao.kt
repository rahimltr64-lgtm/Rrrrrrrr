package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM store_listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<StoreListing>>

    @Query("SELECT * FROM store_listings WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteListings(): Flow<List<StoreListing>>

    @Query("SELECT * FROM store_listings WHERE id = :id")
    fun getListingById(id: Int): Flow<StoreListing?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: StoreListing): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<StoreListing>)

    @Update
    suspend fun updateListing(listing: StoreListing)

    @Delete
    suspend fun deleteListing(listing: StoreListing)

    @Query("UPDATE store_listings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM store_listings")
    suspend fun getCount(): Int
}
