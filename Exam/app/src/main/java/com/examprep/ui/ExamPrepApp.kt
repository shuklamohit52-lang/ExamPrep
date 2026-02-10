package com.examprep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Description // For Tests/Categories
import androidx.compose.material.icons.filled.Lightbulb // For QuickAction "New Test Series"
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School // For Study Notes
import androidx.compose.material.icons.filled.Quiz // For Live Quizzes
import androidx.compose.material.icons.filled.Article // For Prev. Papers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage // Import Coil
import com.examprep.data.BannerItem
import com.examprep.data.FeatureItem
import com.examprep.data.MockRepository
import com.examprep.data.QuestionStatus
import com.examprep.data.TestResult
import com.examprep.data.UserAnswer
import com.examprep.data.models.Category
import com.examprep.data.models.Question
import com.examprep.data.models.TestSeries
import com.examprep.data.supabase.SupabaseService
import com.examprep.ui.auth.LoginScreen
import com.examprep.ui.ebooks.EbookReaderScreen
import com.examprep.ui.ebooks.EbooksScreen
import com.examprep.ui.home.HomeViewModel
import com.examprep.ui.pdf.PdfReaderScreen
import com.examprep.ui.pdf.PdfSectionScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector // Import ImageVector

// Define sealed class for Bottom Navigation Items
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Tests : BottomNavItem("categoryList", Icons.Default.Description, "Categories")
    object StudyPdfs : BottomNavItem("studyPdfs", Icons.Default.PictureAsPdf, "Study PDFs")
    object Ebooks : BottomNavItem("ebooks", Icons.Default.MenuBook, "Ebooks")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamPrepApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val supabaseService = SupabaseService.getInstance()

    // ViewModel Factory
    val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(supabaseService) as T
                modelClass.isAssignableFrom(QuizViewModel::class.java) -> QuizViewModel(supabaseService) as T
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    // ViewModel instances
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val quizViewModel: QuizViewModel = viewModel(factory = viewModelFactory)

    val categories by homeViewModel.categories.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tests,
        BottomNavItem.StudyPdfs,
        BottomNavItem.Ebooks
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(onItemClick = { route ->
                navController.navigate(route)
                scope.launch { drawerState.close() }
            })
        },
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Scaffold(
            topBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                if (currentRoute != "login") {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    navController.navigate(BottomNavItem.Tests.route) {
                                        // Pop up to the start destination of the graph to avoid building up a large backstack
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        // Avoid multiple copies of the same destination when reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            ) {
                                Text("RRB Technician") // Updated title
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Category List") // Updated content description
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notifications")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                // Only show bottom bar if not on login, quiz, or result screens
                if (currentRoute != "login" && currentRoute?.startsWith("quiz/") == false && currentRoute != "result") {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            // Determine if this item is selected.
                            // Handle "category/{categoryId}" as part of "Category" selection for UI highlighting.
                            val isSelected = when (item.route) {
                                BottomNavItem.Tests.route -> currentDestination?.hierarchy?.any { it.route == BottomNavItem.Tests.route || it.route?.startsWith("category/") == true } == true
                                else -> currentDestination?.hierarchy?.any { it.route == item.route } == true
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination of the graph to avoid building up a large backstack
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        // Avoid multiple copies of the same destination when reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, fontSize = 10.sp) } // Uses new label "Category" if item.route is "categoryList"
                            )
                        }
                    }
                }
            }
        ) { padding ->
            AppNavHost(
                navController = navController,
                homeViewModel = homeViewModel,
                quizViewModel = quizViewModel,
                modifier = Modifier.padding(padding),
                categories = categories // Pass categories to AppNavHost
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    quizViewModel: QuizViewModel,
    modifier: Modifier = Modifier,
    categories: List<Category> // Added categories parameter
) {
    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("login") {
            LoginScreen(
                onLogin = { _, _ -> navController.navigate(BottomNavItem.Home.route) { popUpTo("login") { inclusive = true } } },
                onGoogleSignIn = { /* TODO */ },
                onFacebookSignIn = { /* TODO */ },
                onForgotPassword = { /* TODO */ },
                onSignUp = { /* TODO */ }
            )
        }
        composable(BottomNavItem.Home.route) { // Use defined route
            val allTestSeries by homeViewModel.testSeries.collectAsState()
            HomeScreen(
                enrolledTestSeries = allTestSeries.shuffled().take(3), // Example: first 3 for enrolled
                newTestSeries = allTestSeries.shuffled().take(3), // Example: another 3 for new
                onTestSeriesClick = { testSeriesId -> navController.navigate("test_series/$testSeriesId") },
                onAttemptedTestsClick = { navController.navigate("attemptedTests") },
                onQuickActionClick = { title ->
                    when (title) {
                        "Ebooks" -> navController.navigate(BottomNavItem.Ebooks.route)
                        "Study Notes" -> navController.navigate("studyNotes")
                        "Previous Papers" -> navController.navigate("previousPapers")
                        "Study PDFs", "Free PDFs" -> navController.navigate("freePdfs")
                        else -> {}
                    }
                },
                onViewAllEnrolled = { /* TODO: Navigate to a screen showing all enrolled tests */ },
                onViewAllNew = { /* TODO: Navigate to a screen showing all new tests */ }
            )
        }
        composable(BottomNavItem.Tests.route) { // Route is "categoryList"
            CategoryListScreen(
                categories = categories,
                onCategoryClick = { categoryId -> navController.navigate("category/$categoryId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "category/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val allTestSeries by homeViewModel.testSeries.collectAsState()
            val categoryTestSeries = allTestSeries.filter { it.categoryId == categoryId }

            // Display a filtered list of test series based on category.
            // For simplicity, reusing HomeScreen but it could be a dedicated CategoryDetailScreen.
            HomeScreen(
                enrolledTestSeries = categoryTestSeries.shuffled().take(3), // Filtered enrolled
                newTestSeries = categoryTestSeries.shuffled().take(3), // Filtered new
                onTestSeriesClick = { testSeriesId -> navController.navigate("test_series/$testSeriesId") },
                onAttemptedTestsClick = { navController.navigate("attemptedTests") },
                onQuickActionClick = { title ->
                    when (title) {
                        "Ebooks" -> navController.navigate(BottomNavItem.Ebooks.route)
                        "Study Notes" -> navController.navigate("studyNotes")
                        "Previous Papers" -> navController.navigate("previousPapers")
                        "Study PDFs", "Free PDFs" -> navController.navigate("freePdfs")
                        else -> {}
                    }
                },
                onViewAllEnrolled = { /* TODO */ },
                onViewAllNew = { /* TODO */ }
            )
        }
        composable(
            route = "test_series/{testSeriesId}",
            arguments = listOf(navArgument("testSeriesId") { type = NavType.StringType })
        ) { backStackEntry ->
            val testSeriesId = backStackEntry.arguments?.getString("testSeriesId")
            TestSeriesDetailScreen(
                testSeriesId = testSeriesId,
                onTestClick = { testId -> navController.navigate("quiz/$testId") }, // Navigate to quiz
                onBack = { navController.popBackStack() },
                homeViewModel = homeViewModel
            )
        }
        composable(
            route = "quiz/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.StringType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getString("testId")
            QuizScreen(
                testId = testId,
                onFinish = {
                    quizViewModel.finishTest()
                    navController.navigate("result") { popUpTo(navController.graph.startDestinationId) { inclusive = false } }
                },
                viewModel = quizViewModel
            )
        }
        composable("result") {
            val testResult by quizViewModel.testResult.collectAsState()
            TestResultScreen(
                result = testResult,
                onDone = {
                    quizViewModel.clearTestResult()
                    navController.navigate(BottomNavItem.Home.route) { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
                },
                onReattempt = {
                    navController.navigate("resultReview")
                },
                viewModel = quizViewModel
            )
        }
        composable("resultReview") {
            ResultReviewScreen(
                viewModel = quizViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") { ComingSoonScreen { navController.popBackStack() } }
        composable("attemptedTests") { ComingSoonScreen { navController.popBackStack() } } // New route for attempted tests
        composable(BottomNavItem.StudyPdfs.route) {
            PdfSectionScreen(sectionKey = "free-pdfs", onBack = { navController.popBackStack() }, onViewPdf = { sectionKey, pdfId ->
                navController.navigate("pdfSectionReader/$sectionKey/$pdfId")
            })
        }
        composable("studyNotes") {
            PdfSectionScreen(sectionKey = "study-notes", onBack = { navController.popBackStack() }, onViewPdf = { sectionKey, pdfId ->
                navController.navigate("pdfSectionReader/$sectionKey/$pdfId")
            })
        }
        composable("previousPapers") {
            PdfSectionScreen(sectionKey = "prev-papers", onBack = { navController.popBackStack() }, onViewPdf = { sectionKey, pdfId ->
                navController.navigate("pdfSectionReader/$sectionKey/$pdfId")
            })
        }
        composable("freePdfs") {
            PdfSectionScreen(sectionKey = "free-pdfs", onBack = { navController.popBackStack() }, onViewPdf = { sectionKey, pdfId ->
                navController.navigate("pdfSectionReader/$sectionKey/$pdfId")
            })
        }
        composable(
            route = "pdfSectionReader/{sectionKey}/{pdfId}",
            arguments = listOf(
                navArgument("sectionKey") { type = NavType.StringType },
                navArgument("pdfId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            PdfReaderScreen(
                sectionKey = backStackEntry.arguments?.getString("sectionKey"),
                pdfId = backStackEntry.arguments?.getString("pdfId"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(BottomNavItem.Ebooks.route) {
            EbooksScreen(onReadClick = { bookId -> navController.navigate("ebookReader/$bookId") })
        }
        composable(
            route = "ebookReader/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            EbookReaderScreen(
                bookId = backStackEntry.arguments?.getString("bookId"),
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDrawer(onItemClick: (String) -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ExamPrep", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Settings") },
                selected = false,
                onClick = { onItemClick("settings") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coming Soon") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Coming Soon!", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AutoScrollingBanner(items: List<BannerItem>) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState.currentPage) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 16.dp), modifier = Modifier.fillMaxWidth().height(160.dp)) { page ->
            val item = items[page]
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = item.color)) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(item.subtitle, color = Color.White, fontSize = 14.sp)
                }
            }
        }
        HorizontalPagerIndicator(pagerState)
    }
}

@Composable
private fun HomeScreen(
    enrolledTestSeries: List<TestSeries>,
    newTestSeries: List<TestSeries>,
    onTestSeriesClick: (String) -> Unit,
    onAttemptedTestsClick: () -> Unit,
    onQuickActionClick: (String) -> Unit,
    onViewAllEnrolled: () -> Unit,
    onViewAllNew: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        item {
            // Placeholder for new banner items that look like the image
            // In a real app, MockRepository would have these.
            val mockBanners = listOf(
                BannerItem("Current Quiz", "Daily updated practice quiz", Color(0xFF355CDE)),
                BannerItem("Free PDFs", "All study material unlocked", Color(0xFF1AAE9F)),
                BannerItem("Ebooks", "Structured preparation guides", Color(0xFF8E54E9))
            )
            AutoScrollingBanner(items = mockBanners) // Using mock banners for now
            Spacer(Modifier.height(20.dp))
        }

        // Enrolled Test Series Section
        if (enrolledTestSeries.isNotEmpty()) {
            item {
                SectionTitle("Enrolled Test Series", "View All", onViewAllEnrolled)
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(enrolledTestSeries) { series ->
                        // Pass mock icon and progress for demonstration
                        TestSeriesCard(
                            series = series,
                            onActionClick = onTestSeriesClick,
                            icon = Icons.Default.School, // Example icon
                            progressText = "0/${series.tests.size * 10}" // Example progress
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // Quick Action Grid
        item {
            val quickAccessItems = listOf(
                FeatureItem("Current Quiz", Icons.Default.Quiz, null),
                FeatureItem("Study Notes", Icons.Default.School, null),
                FeatureItem("Free PDFs", Icons.Default.Description, null),
                FeatureItem("Ebooks", Icons.Outlined.BookmarkBorder, null),
                FeatureItem("Study PDFs", Icons.Default.PictureAsPdf, null),
                FeatureItem("Courses", Icons.Default.School, null),
                FeatureItem("Previous Papers", Icons.Default.Article, null)
            )
            QuickActionGrid(items = quickAccessItems, onActionClick = { item -> onQuickActionClick(item.title) })
            Spacer(Modifier.height(20.dp))
        }

        // All Your Attempted Tests
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onAttemptedTestsClick() },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "All Your Attempted Tests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Add an icon here if desired, e.g., Icons.Default.ArrowForward
                }
            }
            Spacer(Modifier.height(20.dp))
        }


        // New Test Series Section (Recommended)
        if (newTestSeries.isNotEmpty()) {
            item {
                SectionTitle("New Test Series", "View All", onViewAllNew)
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(newTestSeries) { series ->
                        // Pass mock icon and progress for demonstration
                        TestSeriesCard(
                            series = series,
                            onActionClick = onTestSeriesClick,
                            icon = Icons.Default.Lightbulb, // Example icon
                            progressText = "${series.tests.size} Tests" // Example text
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestSeriesCard(series: TestSeries, onActionClick: (String) -> Unit, icon: ImageVector?, progressText: String?) {
    Card(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(250.dp)
            .clickable { onActionClick(series.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp).padding(end = 8.dp))
                }
                Text(series.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            }
            Spacer(Modifier.height(8.dp))
            Text("Exams: ${series.tests.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            if (progressText != null) {
                Spacer(Modifier.height(4.dp))
                Text(progressText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestSeriesDetailScreen(
    testSeriesId: String?,
    onTestClick: (String) -> Unit,
    onBack: () -> Unit,
    homeViewModel: HomeViewModel
) {
    var tests by remember { mutableStateOf<List<com.examprep.data.models.Test>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(testSeriesId) {
        if (testSeriesId != null) {
            isLoading = true
            tests = homeViewModel.supabaseService.getTestsForTestSeries(testSeriesId)
            isLoading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tests") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text("Tests", style = MaterialTheme.typography.headlineSmall)
                LazyColumn(modifier = Modifier.fillMaxSize()) { // Changed max height to fill remaining space
                    items(tests) { test ->
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onTestClick(test.id) }) {
                            Text(test.name, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QuizScreen(
    testId: String?,
    onFinish: () -> Unit,
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(testId) {
        if (testId != null) {
            viewModel.loadTest(testId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isTimeUp) {
        AlertDialog(onDismissRequest = {},
            title = { Text("Time's Up!") },
            text = { Text("Your test has been automatically submitted.") },
            confirmButton = { TextButton(onClick = { viewModel.finishTest() }) { Text("OK") } }
        )
    }

    val test = uiState.test
    val currentQuestion = uiState.currentQuestion // Get current question from UiState

    if (test == null || currentQuestion == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val questionCount = uiState.questionIds.size // Total questions from IDs

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            QuestionPalette(
                userAnswers = uiState.userAnswers,
                questionIds = uiState.questionIds,
                currentQuestionIndex = uiState.currentQuestionIndex,
                onQuestionClick = { index ->
                    viewModel.navigateToQuestion(index)
                    scope.launch { drawerState.close() }
                },
                onSubmit = onFinish
            )
        },
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Scaffold(
            topBar = {
                Row(Modifier.fillMaxWidth().background(Color(0xFF101722)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    val minutes = uiState.timeLeft / 60000
                    val seconds = (uiState.timeLeft % 60000) / 1000
                    val durationInMinutes = test.durationInMinutes ?: 30
                    CircularTimer(time = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}", progress = uiState.timeLeft.toFloat() / (durationInMinutes * 60000).toFloat())
                    Spacer(Modifier.width(16.dp))
                    Text(test.name, color = Color.White, modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Outlined.List, null, tint = Color.White)
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF0F4F8))) {
                item {
                    Card(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Question ${uiState.currentQuestionIndex + 1} / $questionCount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                IconButton(onClick = { /* TODO: Report question */ }) {
                                    Icon(Icons.Outlined.BookmarkBorder, null)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(currentQuestion.questionText, fontSize = 16.sp, lineHeight = 24.sp)

                            // Display image if available
                            currentQuestion.imageUrl?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Question Diagram",
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                        currentQuestion.options.forEachIndexed { index, option ->
                            val userAnswer = uiState.userAnswers.find { it.questionId == currentQuestion.id }
                            val isSelected = userAnswer?.selectedOption == option
                            Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.selectAnswer(option) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
                                    if (isSelected) {
                                        Box(modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(option, fontSize = 16.sp)
                            }
                            if (index < currentQuestion.options.size - 1) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { viewModel.previousQuestion() }, enabled = uiState.currentQuestionIndex > 0) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        }
                        IconButton(onClick = { viewModel.nextQuestion() }, enabled = uiState.currentQuestionIndex < questionCount - 1) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        OutlinedButton(onClick = { viewModel.markForReview(); if (uiState.currentQuestionIndex < questionCount - 1) viewModel.nextQuestion() else onFinish() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9C27B0))) {
                            Text("Mark & Next")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { if (uiState.currentQuestionIndex < questionCount - 1) viewModel.nextQuestion() else onFinish() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E67D1))) {
                            Text(if (uiState.currentQuestionIndex < questionCount - 1) "Save & Next" else "Submit")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPalette(
    userAnswers: List<UserAnswer>,
    questionIds: List<String>, // Now takes question IDs directly
    currentQuestionIndex: Int,
    onQuestionClick: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    ModalDrawerSheet {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            // Legend for status (Visited, Answered, Marked for Review, etc.)
            item { 
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text("Legend", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                        Text("Answered")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF9C27B0)))
                        Text("Marked for Review")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.LightGray, CircleShape))
                        Text("Not Answered/Not Visited")
                    }
                }
            }

            // Display questions by sections (for now, just one section "All Questions")
            item {
                Text("All Questions", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 56.dp), modifier = Modifier.heightIn(max = 300.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(questionIds.size) { index ->
                        val questionId = questionIds[index]
                        val answer = userAnswers.find { it.questionId == questionId }
                        val color = when (answer?.status) {
                            QuestionStatus.ANSWERED, QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW -> Color(0xFF4CAF50)
                            QuestionStatus.MARKED_FOR_REVIEW -> Color(0xFF9C27B0)
                            else -> if (index == currentQuestionIndex) Color(0xFF2E67D1) else Color.White // Highlight current question
                        }
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color).border(1.dp, Color.LightGray, CircleShape).clickable { onQuestionClick(index) }, contentAlignment = Alignment.Center) {
                            Text((index + 1).toString(), color = if (color == Color.White) Color.Black else Color.White)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Text("Submit")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF1F2F7))) {
            if (categories.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No categories available.")
                    }
                }
            } else {
                items(categories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onCategoryClick(category.id) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            category.name,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestResultScreen(
    result: TestResult?,
    onDone: () -> Unit,
    onReattempt: () -> Unit,
    viewModel: QuizViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Solutions", "Insights")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Result") },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.Default.ArrowBack, contentDescription = "Done") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF4F6FB))) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }) {
                        Text(title, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            when (selectedTabIndex) {
                0 -> AnalysisContent(result = result, onReattempt = onReattempt)
                1 -> SolutionsContent(viewModel = viewModel)
                else -> DetailedAnalysisContent(result = result)
            }
        }
    }
}

@Composable
private fun AnalysisContent(result: TestResult?, onReattempt: () -> Unit) {
    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No result available.")
        }
        return
    }

    val attemptedParts = result.attempted.split("/")
    val attempted = attemptedParts.getOrNull(0)?.toIntOrNull() ?: 0
    val total = attemptedParts.getOrNull(1)?.toIntOrNull() ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Performance", style = MaterialTheme.typography.titleMedium, color = Color(0xFF6B7280))
                    Spacer(Modifier.height(10.dp))
                    Text(result.score, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2E67D1))
                    Spacer(Modifier.height(6.dp))
                    Text("Accuracy ${result.accuracy}", color = Color(0xFF4B5563))
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onReattempt, shape = RoundedCornerShape(12.dp)) { Text("Review Answers") }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Correct", result.correct.toString(), Color(0xFFE8F8EE), Color(0xFF1F8B4C), Modifier.weight(1f))
                MetricCard("Wrong", result.incorrect.toString(), Color(0xFFFDECEC), Color(0xFFC62828), Modifier.weight(1f))
                MetricCard("Unattempted", result.unattempted.toString(), Color(0xFFF3F4F6), Color(0xFF4B5563), Modifier.weight(1f))
            }
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Attempt Summary", fontWeight = FontWeight.SemiBold)
                    Text("Attempted: $attempted / $total")
                    Text("Percentile: ${result.percentile}")
                    Text("Rank: ${result.rank}")
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, background: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, color = textColor, fontSize = 20.sp)
            Text(title, fontSize = 12.sp, color = Color(0xFF4B5563))
        }
    }
}

@Composable
private fun SolutionsContent(viewModel: QuizViewModel) {
    val questions by viewModel.resultQuestions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Solutions will appear after submission.")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(questions) { question ->
            val userAnswer = uiState.userAnswers.find { it.questionId == question.id }?.selectedOption
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(question.questionText, fontWeight = FontWeight.SemiBold)
                    Text("Your Answer: ${userAnswer ?: "Not Attempted"}", color = if (userAnswer == question.correctAnswer) Color(0xFF1F8B4C) else Color(0xFFC62828))
                    Text("Correct Answer: ${question.correctAnswer}", color = Color(0xFF1F8B4C))
                    question.explanation?.let { Text("Explanation: $it", color = Color(0xFF4B5563)) }
                }
            }
        }
    }
}

@Composable
private fun DetailedAnalysisContent(result: TestResult?) {
    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No analysis available.") }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Detailed Insights", fontWeight = FontWeight.Bold)
                Text("• Correct answers add +2 marks.")
                Text("• Incorrect answers deduct -1 mark.")
                Text("• Focus on accuracy to increase overall score.")
            }
        }
        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Current Stats", fontWeight = FontWeight.Bold)
                Text("Correct: ${result.correct}")
                Text("Wrong: ${result.incorrect}")
                Text("Unattempted: ${result.unattempted}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultReviewScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val questions by viewModel.resultQuestions.collectAsState()
    val answers by viewModel.uiState.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    val selectedByUser = remember { mutableStateOf(mutableMapOf<String, String?>()) }

    LaunchedEffect(questions, answers.userAnswers) {
        if (selectedByUser.value.isEmpty()) {
            selectedByUser.value.putAll(answers.userAnswers.associate { it.questionId to it.selectedOption })
        }
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No submitted questions found for review.")
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, questions.lastIndex)
    val question = questions[safeIndex]
    val selectedOption = selectedByUser.value[question.id]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Attempt") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6FB))
                .padding(16.dp)
        ) {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Question ${safeIndex + 1} / ${questions.size}", color = Color(0xFF6B7280))
                    Spacer(Modifier.height(8.dp))
                    Text(question.questionText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEach { option ->
                        val isSelected = selectedOption == option
                        val isCorrect = option == question.correctAnswer
                        val optionColor = when {
                            selectedOption == null -> Color(0xFFF9FAFB)
                            isCorrect -> Color(0xFFE8F8EE)
                            isSelected && !isCorrect -> Color(0xFFFDECEC)
                            else -> Color(0xFFF9FAFB)
                        }
                        val borderColor = when {
                            selectedOption == null -> Color(0xFFE5E7EB)
                            isCorrect -> Color(0xFF1F8B4C)
                            isSelected && !isCorrect -> Color(0xFFC62828)
                            else -> Color(0xFFE5E7EB)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(optionColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedByUser.value[question.id] = option }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            val nowSelected = selectedByUser.value[question.id]
            if (nowSelected != null) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (nowSelected == question.correctAnswer) "Correct selection" else "Incorrect selection",
                            color = if (nowSelected == question.correctAnswer) Color(0xFF1F8B4C) else Color(0xFFC62828),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Correct answer: ${question.correctAnswer}", color = Color(0xFF1F8B4C))
                        Text("Explanation: ${question.explanation ?: "No explanation available."}", color = Color(0xFF4B5563))
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { if (currentIndex > 0) currentIndex -= 1 }, enabled = currentIndex > 0) {
                    Text("Previous")
                }
                Button(onClick = { if (currentIndex < questions.lastIndex) currentIndex += 1 }, enabled = currentIndex < questions.lastIndex) {
                    Text(if (currentIndex == questions.lastIndex) "Completed" else "Next")
                }
            }
        }
    }
}

@Composable
fun CircularTimer(time: String, progress: Float) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(color = Color.White, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 4.dp.toPx()))
            drawArc(color = Color(0xFF2E67D1), startAngle = -90f, sweepAngle = 360 * progress, useCenter = false, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(time, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
