package com.examprep.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.examprep.data.models.Category
import com.examprep.data.models.TestSeries
import com.examprep.data.supabase.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(val supabaseService: SupabaseService) : ViewModel() {

    private val _testSeries = MutableStateFlow<List<TestSeries>>(emptyList())
    val testSeries: StateFlow<List<TestSeries>> = _testSeries

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                println("SupabaseData: Fetching categories...")
                val fetchedCategories = supabaseService.getCategories()
                println("SupabaseData: Fetched ${fetchedCategories.size} categories: $fetchedCategories")
                _categories.value = fetchedCategories

                println("SupabaseData: Fetching all test series for home screen...")
                val allTestSeries = mutableListOf<TestSeries>()
                for (category in fetchedCategories) {
                    val series = supabaseService.getTestSeriesForCategory(category.id)
                    allTestSeries.addAll(series)
                }
                println("SupabaseData: Fetched ${allTestSeries.size} total test series: $allTestSeries")
                _testSeries.value = allTestSeries
            } catch (e: Exception) {
                println("SupabaseData: Error fetching data: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
