package com.examprep.ui.ebooks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image

data class EbookSample(
    val id: String,
    val title: String,
    val author: String,
    val coverColor: Color,
    val pages: List<String>
)

private val sampleBooks = listOf(
    EbookSample(
        id = "math-foundation",
        title = "Math Foundation Mastery",
        author = "ExamPrep Faculty",
        coverColor = Color(0xFF355CDE),
        pages = listOf(
            "Chapter 1: Number System\n\nIntegers, rational numbers and quick tricks for exam speed.",
            "Chapter 2: Algebra\n\nLinear equations and shortcuts for simplification.",
            "Chapter 3: Mensuration\n\nImportant formulas with solved examples and practice tips."
        )
    ),
    EbookSample(
        id = "reasoning-boost",
        title = "Reasoning Booster",
        author = "Mock Test Team",
        coverColor = Color(0xFF1AAE9F),
        pages = listOf(
            "Chapter 1: Series\n\nNumber, letter and mixed series patterns.",
            "Chapter 2: Coding-Decoding\n\nCommon models and elimination method.",
            "Chapter 3: Syllogism\n\nVenn diagram approach for faster accuracy."
        )
    ),
    EbookSample(
        id = "general-awareness",
        title = "General Awareness Capsule",
        author = "GK Editorial",
        coverColor = Color(0xFF8E54E9),
        pages = listOf(
            "Part 1: Static GK\n\nNational parks, important days and Indian geography.",
            "Part 2: Current Affairs\n\nMonthly highlights and government schemes.",
            "Part 3: Science Snapshot\n\nPhysics, chemistry and biology revision notes."
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbooksScreen(onReadClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredBooks = remember(query) {
        sampleBooks.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Ebooks") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6FB)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search ebook by title or author") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            if (filteredBooks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "No books found for your search.",
                            modifier = Modifier.padding(18.dp),
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            items(filteredBooks) { book ->
                EbookCard(book = book, onReadClick = { onReadClick(book.id) })
            }
        }
    }
}

@Composable
private fun EbookCard(book: EbookSample, onReadClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 110.dp)
                    .background(book.coverColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EBOOK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(book.author, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("${book.pages.size} pages", color = Color(0xFF355CDE), style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = onReadClick, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Visibility, contentDescription = "Read")
                Spacer(Modifier.size(6.dp))
                Text("Read")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookReaderScreen(bookId: String?, onBack: () -> Unit) {
    val book = remember(bookId) { sampleBooks.firstOrNull { it.id == bookId } }
    var pageCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val pdfFile = remember(book) {
        if (book == null) null else createSamplePdf(context, book)
    }

    LaunchedEffect(pdfFile, currentPage) {
        if (pdfFile != null) {
            pageCount = getPageCount(pdfFile.absolutePath)
            currentPage = currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            currentBitmap = renderPdfPage(pdfFile.absolutePath, currentPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Ebook Reader") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6FB))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (book == null || pdfFile == null) {
                Text("Book not found.")
                return@Column
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val bitmap = currentBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page ${currentPage + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        )
                    } else {
                        Text("Loading page...")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentPage > 0) currentPage -= 1 }, enabled = currentPage > 0) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous page")
                }
                Text("Page ${currentPage + 1} / ${if (pageCount == 0) 1 else pageCount}")
                IconButton(
                    onClick = { if (currentPage < pageCount - 1) currentPage += 1 },
                    enabled = currentPage < pageCount - 1
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next page")
                }
            }
        }
    }
}

private fun createSamplePdf(context: Context, book: EbookSample): java.io.File {
    val file = java.io.File(context.cacheDir, "${book.id}.pdf")
    if (file.exists()) return file

    val document = PdfDocument()
    val titlePaint = Paint().apply {
        textSize = 24f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }
    val bodyPaint = Paint().apply {
        textSize = 16f
        color = android.graphics.Color.DKGRAY
    }

    book.pages.forEachIndexed { index, content ->
        val pageInfo = PdfDocument.PageInfo.Builder(1080, 1440, index + 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawText(book.title, 60f, 90f, titlePaint)
        canvas.drawText("Page ${index + 1}", 60f, 130f, bodyPaint)

        var y = 200f
        content.split("\n").forEach { line ->
            canvas.drawText(line, 60f, y, bodyPaint)
            y += 40f
        }

        document.finishPage(page)
    }

    file.outputStream().use { output -> document.writeTo(output) }
    document.close()
    return file
}

private fun getPageCount(pdfPath: String): Int {
    val descriptor = ParcelFileDescriptor.open(java.io.File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(descriptor)
    val count = renderer.pageCount
    renderer.close()
    descriptor.close()
    return count
}

private fun renderPdfPage(pdfPath: String, pageIndex: Int): Bitmap? {
    return try {
        val descriptor = ParcelFileDescriptor.open(java.io.File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val safePage = pageIndex.coerceIn(0, renderer.pageCount - 1)
        val page = renderer.openPage(safePage)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        descriptor.close()
        bitmap
    } catch (_: Exception) {
        null
    }
}
