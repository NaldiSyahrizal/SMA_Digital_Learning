package com.pab.digitallearning.ui.teacher.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.R
import com.pab.digitallearning.core.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradebookPreviewScreen(
    viewModel: TeacherContentViewModel,
    classId: Long,
    className: String,
    subjectId: Long,
    subjectName: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch the PDF for preview
    LaunchedEffect(Unit) {
        viewModel.exportClassGradebook(
            sessionManager.getToken(),
            classId,
            className,
            subjectId,
            subjectName,
            context,
            true // isForPrint = true saves it to cache and returns File
        ) { success, message, file ->
            if (success && file != null) {
                pdfFile = file
                // Loading will be set to false after bitmaps are generated
            } else {
                errorMessage = message
                isLoading = false
            }
        }
    }

    // Render PDF to Bitmaps
    LaunchedEffect(pdfFile) {
        pdfFile?.let { file ->
            try {
                val newBitmaps = mutableListOf<Bitmap>()
                withContext(Dispatchers.IO) {
                    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(fileDescriptor)
                    val pageCount = pdfRenderer.pageCount
                    
                    for (i in 0 until pageCount) {
                        val page = pdfRenderer.openPage(i)
                        
                        // Safeguard dimensions
                        val screenWidth = context.resources.displayMetrics.widthPixels
                        val safeWidth = if (screenWidth > 0) screenWidth else 1080
                        val pageWidth = maxOf(1, page.width)
                        val pageHeight = maxOf(1, page.height)
                        
                        val width = safeWidth
                        val height = maxOf(1, (width.toFloat() / pageWidth * pageHeight).toInt())
                        
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        newBitmaps.add(bitmap)
                        page.close()
                    }
                    pdfRenderer.close()
                    fileDescriptor.close()
                }
                bitmaps = newBitmaps
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Gagal merender PDF: ${e.message}"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Preview Gradebook",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF102B5E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$className - $subjectName",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF102B5E))
                    }
                },
                actions = {
                    // Download Action
                    IconButton(onClick = {
                        Toast.makeText(context, "Mengunduh PDF...", Toast.LENGTH_SHORT).show()
                        viewModel.exportClassGradebook(
                            sessionManager.getToken(),
                            classId,
                            className,
                            subjectId,
                            subjectName,
                            context,
                            false // Save to MediaStore
                        ) { success, message, _ ->
                            Toast.makeText(context, message, if (success) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.stat_sys_download),
                            contentDescription = "Unduh",
                            tint = Color(0xFF102B5E)
                        )
                    }
                    
                    // Print Action
                    IconButton(onClick = {
                        pdfFile?.let { file ->
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                            val printAdapter = object : android.print.PrintDocumentAdapter() {
                                override fun onLayout(old: android.print.PrintAttributes?, new: android.print.PrintAttributes?, cancel: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.LayoutResultCallback?, extras: android.os.Bundle?) {
                                    if (cancel?.isCanceled == true) { callback?.onLayoutCancelled(); return }
                                    val info = android.print.PrintDocumentInfo.Builder(file.name).setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
                                    callback?.onLayoutFinished(info, true)
                                }
                                override fun onWrite(pages: Array<out android.print.PageRange>?, dest: android.os.ParcelFileDescriptor?, cancel: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.WriteResultCallback?) {
                                    try {
                                        FileInputStream(file).use { input ->
                                            FileOutputStream(dest?.fileDescriptor).use { output -> input.copyTo(output) }
                                        }
                                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                                    } catch (e: Exception) { callback?.onWriteFailed(e.message) }
                                }
                            }
                            printManager.print("Gradebook_${className}_${subjectName}", printAdapter, null)
                        } ?: run {
                            Toast.makeText(context, "PDF belum siap", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Cetak",
                            tint = Color(0xFF102B5E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFD)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Memuat PDF...", color = Color.Gray)
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBackPressed,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                        ) {
                            Text("Kembali")
                        }
                    }
                }
                bitmaps.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bitmaps.size) { index ->
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val ratio = bitmaps[index].width.toFloat() / bitmaps[index].height.toFloat()
                                val safeRatio = if (ratio > 0f && !ratio.isNaN()) ratio else 0.7f
                                
                                Image(
                                    bitmap = bitmaps[index].asImageBitmap(),
                                    contentDescription = "Halaman ${index + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(safeRatio)
                                        .background(Color.White),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        "PDF Kosong",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
