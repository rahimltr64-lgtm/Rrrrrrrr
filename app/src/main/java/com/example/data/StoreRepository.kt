package com.example.data

import kotlinx.coroutines.flow.Flow

class StoreRepository(private val storeDao: StoreDao) {
    val allListings: Flow<List<StoreListing>> = storeDao.getAllListings()
    val favoriteListings: Flow<List<StoreListing>> = storeDao.getFavoriteListings()

    fun getListingById(id: Int): Flow<StoreListing?> {
        return storeDao.getListingById(id)
    }

    suspend fun insertListing(listing: StoreListing): Long {
        return storeDao.insertListing(listing)
    }

    suspend fun updateListing(listing: StoreListing) {
        storeDao.updateListing(listing)
    }

    suspend fun deleteListing(listing: StoreListing) {
        storeDao.deleteListing(listing)
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        storeDao.setFavorite(id, isFavorite)
    }
}
