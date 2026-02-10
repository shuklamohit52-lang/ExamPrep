package com.examprep.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

data class BannerItem(
    val title: String,
    val subtitle: String,
    val color: Color
)

data class UserAnswer(
    val questionId: String,
    var selectedOption: String?,
    var status: QuestionStatus
)

data class SectionSummary(
    val sectionName: String,
    val score: String,
    val cutOff: String,
    val progress: Float
)

data class TestResult(
    val rank: String,
    val score: String,
    val averageScore: Int,
    val bestScore: Int,
    val percentile: String,
    val accuracy: String,
    val attempted: String,
    val correct: Int,
    val incorrect: Int,
    val unattempted: Int,
    val sectionalSummary: List<SectionSummary>
)

object MockRepository {
    val topBanners = listOf(
        BannerItem("UP POLICE SI 2025", "500+ Tests | 65+ PYP", Color(0xFF1B2A8F)),
        BannerItem("SSC CGL 2025", "1000+ Tests | 100+ PYP", Color(0xFFC62828)),
        BannerItem("RRB NTPC 2025", "800+ Tests | 80+ PYP", Color(0xFF2E7D32)),
        BannerItem("BANKING EXAMS", "1200+ Tests | 120+ PYP", Color(0xFF6A1B9A))
    )

    val quickAccess = listOf(
        FeatureItem("Study Notes", Icons.Outlined.AutoStories, "NEW"),
        FeatureItem("Live Test", Icons.Outlined.Article),
        FeatureItem("Current Quiz", Icons.Outlined.Quiz, "FREE"),
        FeatureItem("Daily Current", Icons.Outlined.CalendarToday),
        FeatureItem("Rankers Tests", Icons.Outlined.EmojiEvents, "NEW"),
        FeatureItem("Prev. Papers", Icons.Outlined.Feed),
        FeatureItem("eBooks", Icons.Outlined.MenuBook),
        FeatureItem("Free PDF", Icons.Outlined.PictureAsPdf)
    )

    val bottomMenuItems = listOf(
        FeatureItem("Home", Icons.Outlined.Home),
        FeatureItem("Tests", Icons.Outlined.Article),
        FeatureItem("Study PDFs", Icons.Outlined.PictureAsPdf),
        FeatureItem("Courses", Icons.Outlined.School)
    )

    val drawerItems = listOf(
        FeatureItem("My Profile", Icons.Outlined.Person),
        FeatureItem("Settings", Icons.Outlined.Settings),
        FeatureItem("About Us", Icons.Outlined.Info),
        FeatureItem("Help & Support", Icons.Outlined.HelpOutline)
    )

    val sampleTestResult = TestResult(
        rank = "1984/2187",
        score = "0/100",
        averageScore = 38,
        bestScore = 90,
        percentile = "9.33%",
        accuracy = "0%",
        attempted = "0/50",
        correct = 0,
        incorrect = 0,
        unattempted = 50,
        sectionalSummary = listOf(
            SectionSummary("General Intelligence and Reasoning", "0/100", "71-72", 0.26f),
            SectionSummary("General Awareness", "0/100", "65-68", 0.45f)
        )
    )
}
enum class QuestionStatus {
    NOT_VISITED,
    UNANSWERED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED_FOR_REVIEW
}
