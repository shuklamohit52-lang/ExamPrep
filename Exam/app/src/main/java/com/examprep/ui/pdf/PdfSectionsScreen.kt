package com.examprep.ui.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

data class PdfItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val pages: List<String>
)

private val sectionData: Map<String, Pair<String, List<PdfItem>>> = mapOf(
    "study-notes" to (
        "Study Notes" to listOf(
            PdfItem("sn-1", "Math Short Notes", "Algebra + Mensuration", listOf("Key formulas for Algebra.", "Mensuration area/volume quick revision.")),
            PdfItem("sn-2", "Reasoning Notes", "Coding & Syllogism", listOf("Coding-decoding pattern notes.", "Syllogism venn shortcuts.")),
            PdfItem("sn-3", "Science Notes", "Physics Chemistry Biology", listOf("Basic physics laws.", "Chemistry periodic quick summary."))
        )
    ),
    "prev-papers" to (
        "Previous Papers" to listOf(
            PdfItem("pp-1", "RRB 2022 Paper", "Shift 1 with solutions", listOf("Question set with answer key page 1.", "Detailed explanation for paper 1.")),
            PdfItem("pp-2", "SSC 2023 Paper", "Tier-1 memory based", listOf("Memory based questions section A.", "Section B and solutions.")),
            PdfItem("pp-3", "Railway Mock Paper", "Expected pattern", listOf("Expected pattern paper set.", "Answer key and analysis."))
        )
    ),
    "free-pdfs" to (
        "Free PDFs" to listOf(
            PdfItem("fp-1", "Current Affairs April", "Monthly capsule", listOf("National updates and schemes.", "International and sports highlights.")),
            PdfItem("fp-2", "GK Booster", "Static GK one liner", listOf("Important days and books-authors.", "States, capitals and parks.")),
            PdfItem("fp-3", "Exam Strategy Guide", "Preparation roadmap", listOf("Daily timetable sample.", "Revision and mock strategy."))
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfSectionScreen(sectionKey: String, onBack: () -> Unit, onViewPdf: (String, String) -> Unit) {
    val context = LocalContext.current
    val section = sectionData[sectionKey]
    val title = section?.first ?: "PDF Library"
    val list = section?.second ?: emptyList()
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, list) {
        list.filter { it.title.contains(query, true) || it.subtitle.contains(query, true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6FB)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search PDFs") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            items(filtered) { pdf ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8EEFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF355CDE))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(pdf.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(4.dp))
                            Text(pdf.subtitle, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(6.dp))
                            Text("${pdf.pages.size} pages", color = Color(0xFF355CDE), fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onViewPdf(sectionKey, pdf.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("View")
                        }
                        Button(
                            onClick = {
                                val bytes = createPdfBytes(pdf)
                                val ok = savePdfToDownloads(context, "${pdf.title}.pdf", bytes)
                                Toast.makeText(context, if (ok) "Downloaded to Downloads" else "Download failed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Download")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(sectionKey: String?, pdfId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val section = sectionData[sectionKey]
    val pdfItem = section?.second?.firstOrNull { it.id == pdfId }

    var currentPage by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val file = remember(sectionKey, pdfId) {
        if (sectionKey == null || pdfItem == null) null
        else createOrGetCachedPdf(context, sectionKey, pdfItem)
    }

    LaunchedEffect(file, currentPage) {
        if (file != null) {
            pageCount = getPageCount(file.absolutePath)
            currentPage = currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            bitmap = renderPdfPage(file.absolutePath, currentPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pdfItem?.title ?: "PDF Reader") },
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
            if (file == null || pdfItem == null) {
                Text("PDF not found")
                return@Column
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val current = bitmap
                    if (current != null) {
                        Image(bitmap = current.asImageBitmap(), contentDescription = "PDF page", modifier = Modifier.fillMaxSize().padding(10.dp))
                    } else {
                        Text("Loading page...")
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentPage > 0) currentPage -= 1 }, enabled = currentPage > 0) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                }
                Text("Page ${currentPage + 1} / ${if (pageCount == 0) 1 else pageCount}", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { if (currentPage < pageCount - 1) currentPage += 1 }, enabled = currentPage < pageCount - 1) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}

private fun createPdfBytes(item: PdfItem): ByteArray {
    val document = PdfDocument()
    val titlePaint = Paint().apply {
        textSize = 22f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }
    val textPaint = Paint().apply {
        textSize = 16f
        color = android.graphics.Color.DKGRAY
    }

    item.pages.forEachIndexed { index, pageText ->
        val info = PdfDocument.PageInfo.Builder(1080, 1440, index + 1).create()
        val page = document.startPage(info)
        val canvas: Canvas = page.canvas
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawText(item.title, 60f, 90f, titlePaint)
        canvas.drawText("Page ${index + 1}", 60f, 130f, textPaint)

        var y = 210f
        pageText.split("\n").forEach { line ->
            canvas.drawText(line, 60f, y, textPaint)
            y += 40f
        }

        document.finishPage(page)
    }

    val output = java.io.ByteArrayOutputStream()
    document.writeTo(output)
    document.close()
    return output.toByteArray()
}

private fun savePdfToDownloads(context: Context, fileName: String, bytes: ByteArray): Boolean {
    return try {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val update = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
            resolver.update(uri, update, null, null)
        }
        true
    } catch (_: Exception) {
        false
    }
}

private fun createOrGetCachedPdf(context: Context, sectionKey: String, item: PdfItem): File {
    val safeSection = sectionKey.replace("/", "-")
    val file = File(context.cacheDir, "${safeSection}_${item.id}.pdf")
    if (file.exists()) return file
    file.writeBytes(createPdfBytes(item))
    return file
}

private fun getPageCount(path: String): Int {
    val descriptor = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(descriptor)
    val count = renderer.pageCount
    renderer.close()
    descriptor.close()
    return count
}

private fun renderPdfPage(path: String, pageIndex: Int): Bitmap? {
    return try {
        val descriptor = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val safe = pageIndex.coerceIn(0, renderer.pageCount - 1)
        val page = renderer.openPage(safe)
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
