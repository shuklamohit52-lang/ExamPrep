package com.examprep.data.supabase

import com.examprep.data.models.Category
import com.examprep.data.models.Question
import com.examprep.data.models.TestSeries
import com.examprep.data.models.Test
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns // Added import for Columns

class SupabaseService {

    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://lvgfblguluclvzorrlfe.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx2Z2ZibGd1bHVjbHZ6b3JybGZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA2Mjk4MjgsImV4cCI6MjA4NjIwNTgyOH0.7AVGkFTcfrlwQboztTQAsBBApoU9U3N_61BVY-Ng5sY"
        ) {
            install(Postgrest)
        }
    }

    suspend fun getCategories(): List<Category> {
        return supabase.postgrest.from("categories").select().decodeList()
    }

    suspend fun getTestSeriesForCategory(categoryId: String): List<TestSeries> {
        return supabase.postgrest.from("test_series")
            .select { filter { eq("category_id", categoryId) } }
            .decodeList()
    }

    suspend fun getTestsForTestSeries(testSeriesId: String): List<Test> {
        return supabase.postgrest.from("tests")
            .select { filter { eq("test_series_id", testSeriesId) } }
            .decodeList()
    }

    // Updated to only fetch the test details, not the questions.
    suspend fun getTestById(testId: String): Test? {
        return supabase.postgrest.from("tests").select {
            filter { eq("id", testId) }
        }.decodeSingleOrNull<Test>()
    }

    // New function to fetch only question IDs. This is very lightweight.
    suspend fun getQuestionIdsForTest(testId: String): List<String> {
        val result = supabase.postgrest.from("questions")
            .select(Columns.list("id")) { // Select only the id column
                filter { eq("test_id", testId) }
            }.decodeList<Map<String, String>>()

        return result.mapNotNull { it["id"] }
    }
    
    // New function to fetch a single question by its ID.
    suspend fun getQuestionById(questionId: String): Question? {
        return supabase.postgrest.from("questions").select {
            filter { eq("id", questionId) }
        }.decodeSingleOrNull<Question>()
    }

    // Note: This function loads all questions at once and should be deprecated
    // in favor of the on-demand loading pattern. It's kept for the debug screen for now.
    suspend fun getQuestionsForTest(testId: String): List<Question> {
        return supabase.postgrest.from("questions")
            .select { filter { eq("test_id", testId) } }
            .decodeList()
    }

    suspend fun getFullCategory(categoryId: String): Category? {
        val category = supabase.postgrest.from("categories").select { filter { eq("id", categoryId) } }.decodeSingleOrNull<Category>()
        return category?.copy(testSeries = getTestSeriesForCategory(categoryId).map {
            it.copy(tests = getTestsForTestSeries(it.id).map {
                it.copy(questions = getQuestionsForTest(it.id))
            })
        })
    }

    companion object {
        @Volatile
        private var INSTANCE: SupabaseService? = null

        fun getInstance(): SupabaseService {
            return INSTANCE ?: synchronized(this) {
                val instance = SupabaseService()
                INSTANCE = instance
                instance
            }
        }
    }
}