package com.example.pasionariastore.repository

import com.example.pasionariastore.model.ProductWithUnit
import com.example.pasionariastore.model.Unit
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsWithUnit(): Flow<List<ProductWithUnit>>
    fun getProductsWithUnitBySearch(search: String): Flow<List<ProductWithUnit>>
    fun getProductsWithUnitById(id: Long): Flow<ProductWithUnit>
    fun getUnits(): Flow<List<Unit>>
    suspend fun saveFirstUnits()
    suspend fun saveFirstProducts()
}