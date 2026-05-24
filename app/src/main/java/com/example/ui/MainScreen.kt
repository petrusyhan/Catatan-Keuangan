package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.theme.DarkExpenseRed
import com.example.ui.theme.DarkIncomeGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SleekBluePrimary
import com.example.ui.theme.SleekBlueDark
import com.example.ui.theme.SleekBlueContainer
import com.example.ui.theme.SleekAccentGrey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current
    var isNotificationToastVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Left avatar box circular matching "BN" style
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SleekAccentGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "BN",
                            color = SleekBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Selamat pagi,",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Text(
                            "Budi Nugraha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Notification Badge circular outlined component
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(1.dp, SleekAccentGrey), CircleShape)
                        .clickable {
                            isNotificationToastVisible = true
                        }
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Catat Otomatis") },
                    label = { Text("Catat") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Riwayat") },
                    label = { Text("Riwayat") }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(transactions = transactions, onAddClick = { viewModel.selectTab(1) }, onDeleteTransaction = { viewModel.deleteTransaction(it) })
                1 -> AutoRecordTab(viewModel = viewModel)
                2 -> HistoryTab(transactions = transactions, onDeleteTransaction = { viewModel.deleteTransaction(it) })
            }

            // Beautiful modern Sleek Notification modal dialog
            if (isNotificationToastVisible) {
                AlertDialog(
                    onDismissRequest = { isNotificationToastVisible = false },
                    confirmButton = {
                        TextButton(onClick = { isNotificationToastVisible = false }) {
                            Text("Mengerti", color = SleekBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Alert",
                                tint = SleekBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Notifikasi Pintar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column {
                            Text(
                                "Semua sistem SakuPintar berjalan normal. Sinkronisasi rekening bank & e-wallet otomatis diaktifkan dengan aman.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SleekBlueContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Safe",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Koneksi Enkripsi 256-bit Aktif",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF001D36)
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

// --- Helper Functions ---

fun formatRupiah(amount: Double): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${numberFormat.format(amount)}"
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

// --- 1. Dashboard / Beranda Tab ---

@Composable
fun DashboardTab(
    transactions: List<TransactionEntity>,
    onAddClick: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    // Math computations
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netWorth = totalIncome - totalExpense

    // State for interactive Bento Quick Actions
    var currentBentoMessage by remember { mutableStateOf<String?>(null) }
    var showAnalyticalAlert by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
            
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Core Summary Financial Card (Sleek Interface Gradient look)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SleekBluePrimary, SleekBlueDark)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "Total Saldo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.75f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatRupiah(netWorth),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Glassmorphic Sync pill in upper right
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4ADE80)) // Glow Green
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Auto-Sync On",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Income Summary
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PEMASUKAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.65f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "+${formatRupiah(totalIncome)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Vertical thin divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Expense Summary
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PENGELUARAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.65f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "-${formatRupiah(totalExpense)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Bento (4 elements in a beautiful modern layout)
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: Hubungkan
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                currentBentoMessage = "Fitur Sinkronisasi SakuPintar Bank & E-Wallet Anda Aktif!"
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Hubungkan",
                                tint = Color(0xFF001D36),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Hubungkan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Item 2: Analisa
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showAnalyticalAlert = !showAnalyticalAlert
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Analisa",
                                tint = Color(0xFF001D36),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Analisa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Item 3: Budget
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                currentBentoMessage = "Limit budget bulanan Anda diatur aman sebesar Rp 5.000.000."
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Budget",
                                tint = Color(0xFF001D36),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Budget",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Item 4: Lainnya
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                currentBentoMessage = "SakuPintar Sleek Interface v2.0 - Asisten Cerdas Finansial Anda."
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Lainnya",
                                tint = Color(0xFF001D36),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Lainnya",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Smooth Animation alert for quick clicks Info Banner
                AnimatedVisibility(
                    visible = currentBentoMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (currentBentoMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SleekBlueContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = SleekBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = currentBentoMessage ?: "",
                                        fontSize = 12.sp,
                                        color = Color(0xFF001D36),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { currentBentoMessage = null }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Tutup",
                                        tint = Color(0xFF001D36),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Toggleable Analytical / Category statistics
        if (showAnalyticalAlert || (transactions.isNotEmpty() && totalExpense > 0)) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SleekAccentGrey),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            "Alokasi Pengeluaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Exponent and compute category stats
                        val expenseCategoryMap = transactions
                            .filter { it.type == "EXPENSE" }
                            .groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .toList()
                            .sortedByDescending { it.second }

                        if (expenseCategoryMap.isEmpty()) {
                            Text(
                                "Belum ada pengeluaran tercatat untuk dianalisa.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val categoryColors = listOf(
                                SleekBluePrimary,
                                Color(0xFFEE8A1A), // Shopping orange
                                Color(0xFF0F9D58), // Green
                                Color(0xFFD32F2F), // Red
                                Color(0xFF607D8B)  // Cool grey
                            )

                            expenseCategoryMap.take(5).forEachIndexed { index, (cat, catAmount) ->
                                val percentage = (((catAmount / totalExpense.coerceAtLeast(1.0)) * 100).toFloat())
                                val color = categoryColors[index % categoryColors.size]

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${String.format("%.1f", percentage)}% (${formatRupiah(catAmount)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { percentage / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = color,
                                        trackColor = SleekAccentGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transaksi Otomatis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (transactions.size > 5) {
                    Text(
                        "Lihat Semua",
                        color = SleekBluePrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onAddClick() }
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SleekBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Kosong",
                                tint = SleekBluePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Belum ada transaksi recorded",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Silakan beralih ke menu Catat untuk mulai menulis!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddClick) {
                            Text("Catat Transaksi Sekarang")
                        }
                    }
                }
            }
        } else {
            items(transactions.take(5), key = { it.id }) { item ->
                TransactionRow(transaction = item, onDelete = onDeleteTransaction)
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    onDelete: (TransactionEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SleekAccentGrey),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant category design matching Sleek Interface
            val (containerColor, iconColor, icon) = when (transaction.category) {
                "Makanan", "Makan & Minum" -> Triple(
                    Color(0xFFE8F5E9), // Gentle green
                    Color(0xFF2E7D32),
                    Icons.Default.ShoppingCart
                )
                "Shopping", "Belanja" -> Triple(
                    Color(0xFFFFF3E0), // Gentle Orange
                    Color(0xFFE65100),
                    Icons.Default.ShoppingCart
                )
                "Transportasi" -> Triple(
                    Color(0xFFE3F2FD), // Gentle Blue
                    Color(0xFF0D47A1),
                    Icons.Default.LocationOn
                )
                "Gaji" -> Triple(
                    Color(0xFFEDE7F6), // Gentle Purple
                    Color(0xFF4A148C),
                    Icons.Default.Person
                )
                "Investasi" -> Triple(
                    Color(0xFFFFFDE7), // Gentle Yellow
                    Color(0xFFF57F17),
                    Icons.Default.Star
                )
                else -> Triple(
                    Color(0xFFECEFF1), // Gentle Grey
                    Color(0xFF263238),
                    Icons.Default.Info
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = transaction.category,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(" • ", color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = transaction.wallet,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDate(transaction.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                if (!transaction.rawText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Auto: \"${transaction.rawText}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = (if (transaction.type == "INCOME") "+ " else "- ") + formatRupiah(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == "INCOME") IncomeGreen else ExpenseRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Verified badge next to price if rawText is parsed automatically
                    if (!transaction.rawText.isNullOrEmpty()) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "Verified",
                            fontSize = 9.sp,
                            color = Color(0xFF44474E),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Quick Delete Assist
                    IconButton(
                        onClick = { onDelete(transaction) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Transaksi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- 2. Auto Record & Manual Input Tab ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoRecordTab(viewModel: FinanceViewModel) {
    val isParsing by viewModel.isParsing.collectAsState()
    val parsedPreview by viewModel.parsedPreview.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var selectedRecordMode by remember { mutableStateOf(0) } // 0: Auto, 1: Manual

    // Manual Input Fields State
    var manualAmount by remember { mutableStateOf("") }
    var manualDesc by remember { mutableStateOf("") }
    var manualType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var manualCategory by remember { mutableStateOf("Lainnya") }
    var manualWallet by remember { mutableStateOf("Tunai") }

    val incomeCategories = listOf("Gaji", "Investasi", "Transfer Masuk", "Lainnya")
    val expenseCategories = listOf("Makanan", "Shopping", "Transportasi", "Kesehatan", "Rekreasi", "Lainnya")
    val wallets = listOf("Tunai", "Bank", "E-Wallet")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector Switch Segment
        item {
            TabRow(
                selectedTabIndex = selectedRecordMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedRecordMode == 0,
                    onClick = { selectedRecordMode = 0 },
                    text = { Text("Pencatatan Otomatis (AI)") }
                )
                Tab(
                    selected = selectedRecordMode == 1,
                    onClick = { selectedRecordMode = 1 },
                    text = { Text("Pencatatan Manual") }
                )
            }
        }

        if (selectedRecordMode == 0) {
            // AI Pencatatan Otomatis Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Ketik Bebas atau Tempel Notifikasi Bank",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "AI kami akan otomatis membaca nilai uang, kategori, jenis pembayaran, dan menyimpannya secara otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Contoh: Makan soto ayam dapet diskon bayar pake gopay 15000") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sample Templates
                        Text(
                            "Sentuh template contoh di bawah ini untuk mencoba:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val IndonesianSamples = listOf(
                            "[SMS BCA] QRIS Rp35.000 ke MCDONALD berhasil pada 24/05/2026",
                            "Dapat uang bulanan gaji pokok masuk rek Mandiri Rp7.500.000",
                            "Beli bensin motor dex pertamax 50rb bayar cash tunai tadi pagi"
                        )

                        IndonesianSamples.forEach { sample ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { textInput = sample }
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Contoh",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = sample,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.parseTransactionText(textInput)
                            },
                            enabled = textInput.isNotEmpty() && !isParsing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isParsing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sedang Menganalisis...")
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Parse")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analisis Teks dengan AI")
                            }
                        }
                    }
                }
            }

            // Error Display Room
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage ?: "Unknown error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Preview Confirmation Panel Form for Auto Parsed Transactions
            if (parsedPreview != null) {
                item {
                    val preview = parsedPreview!!
                    var editableAmount by remember(preview) { mutableStateOf(preview.amount.toString()) }
                    var editableDesc by remember(preview) { mutableStateOf(preview.description) }
                    var editableType by remember(preview) { mutableStateOf(preview.type) }
                    var editableCategory by remember(preview) { mutableStateOf(preview.category) }
                    var editableWallet by remember(preview) { mutableStateOf(preview.wallet) }

                    var isCategoryExpanded by remember { mutableStateOf(false) }
                    var isWalletExpanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Hasil Analisis AI (Silakan Review)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Type Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editableType = "EXPENSE"
                                        editableCategory = "Lainnya"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (editableType == "EXPENSE") ExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (editableType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("PENGELUARAN")
                                }
                                Button(
                                    onClick = {
                                        editableType = "INCOME"
                                        editableCategory = "Transfer Masuk"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (editableType == "INCOME") IncomeGreen else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (editableType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("PENDAPATAN")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Form Field: Amount
                            OutlinedTextField(
                                value = editableAmount,
                                onValueChange = { editableAmount = it },
                                label = { Text("Nominal Uang (Rupiah)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Form Field: Description
                            OutlinedTextField(
                                value = editableDesc,
                                onValueChange = { editableDesc = it },
                                label = { Text("Deskripsi Singkat") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Category Box Dropdown Selector
                            ExposedDropdownMenuBox(
                                expanded = isCategoryExpanded,
                                onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = editableCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Kategori") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = isCategoryExpanded,
                                    onDismissRequest = { isCategoryExpanded = false }
                                ) {
                                    val currentCategories = if (editableType == "INCOME") incomeCategories else expenseCategories
                                    currentCategories.forEach { selectedCat ->
                                        DropdownMenuItem(
                                            text = { Text(selectedCat) },
                                            onClick = {
                                                editableCategory = selectedCat
                                                isCategoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Wallet Box Dropdown Selector
                            ExposedDropdownMenuBox(
                                expanded = isWalletExpanded,
                                onExpandedChange = { isWalletExpanded = !isWalletExpanded }
                            ) {
                                OutlinedTextField(
                                    value = editableWallet,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Dompet / Wallet") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWalletExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = isWalletExpanded,
                                    onDismissRequest = { isWalletExpanded = false }
                                ) {
                                    wallets.forEach { selectedWallet ->
                                        DropdownMenuItem(
                                            text = { Text(selectedWallet) },
                                            onClick = {
                                                editableWallet = selectedWallet
                                                isWalletExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Save Decisions buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.clearParserState() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Batal")
                                }
                                Button(
                                    onClick = {
                                        val amt = editableAmount.toDoubleOrNull() ?: preview.amount
                                        viewModel.confirmAndSaveTransaction(
                                            amount = amt,
                                            type = editableType,
                                            category = editableCategory,
                                            wallet = editableWallet,
                                            description = editableDesc
                                        )
                                        textInput = "" // Clear the AI input space
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Simpan!")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Manual Add Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    var isCatManualExpanded by remember { mutableStateOf(false) }
                    var isWalletManualExpanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Input Finansial Manual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Type Switcher
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    manualType = "EXPENSE"
                                    manualCategory = "Lainnya"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (manualType == "EXPENSE") ExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (manualType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("PENGELUARAN")
                            }
                            Button(
                                onClick = {
                                    manualType = "INCOME"
                                    manualCategory = "Transfer Masuk"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (manualType == "INCOME") IncomeGreen else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (manualType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("PENDAPATAN")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Amount Field
                        OutlinedTextField(
                            value = manualAmount,
                            onValueChange = { manualAmount = it },
                            label = { Text("Nominal Uang (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description Field
                        OutlinedTextField(
                            value = manualDesc,
                            onValueChange = { manualDesc = it },
                            label = { Text("Deskripsi Pengeluaran/Pemasukan") },
                            placeholder = { Text("e.g. Belanja Indomaret, Gaji Pokok") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isCatManualExpanded,
                            onExpandedChange = { isCatManualExpanded = !isCatManualExpanded }
                        ) {
                            OutlinedTextField(
                                value = manualCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kategori") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCatManualExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = isCatManualExpanded,
                                onDismissRequest = { isCatManualExpanded = false }
                            ) {
                                val relevantCats = if (manualType == "INCOME") incomeCategories else expenseCategories
                                relevantCats.forEach { cellCat ->
                                    DropdownMenuItem(
                                        text = { Text(cellCat) },
                                        onClick = {
                                            manualCategory = cellCat
                                            isCatManualExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Wallet Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isWalletManualExpanded,
                            onExpandedChange = { isWalletManualExpanded = !isWalletManualExpanded }
                        ) {
                            OutlinedTextField(
                                value = manualWallet,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Transaksi Melalui") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWalletManualExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = isWalletManualExpanded,
                                onDismissRequest = { isWalletManualExpanded = false }
                            ) {
                                wallets.forEach { cellWallet ->
                                    DropdownMenuItem(
                                        text = { Text(cellWallet) },
                                        onClick = {
                                            manualWallet = cellWallet
                                            isWalletManualExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val amountVal = manualAmount.toDoubleOrNull() ?: 0.0
                                if (amountVal > 0 && manualDesc.trim().isNotEmpty()) {
                                    viewModel.insertManualTransaction(
                                        amount = amountVal,
                                        type = manualType,
                                        category = manualCategory,
                                        wallet = manualWallet,
                                        description = manualDesc
                                    )
                                    // Reset fields
                                    manualAmount = ""
                                    manualDesc = ""
                                    // Re-route to dashboard
                                    viewModel.selectTab(0)
                                }
                            },
                            enabled = manualAmount.isNotEmpty() && manualDesc.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Simpan")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Manual")
                        }
                    }
                }
            }
        }
    }
}

// --- 3. History Tab ---

@Composable
fun HistoryTab(
    transactions: List<TransactionEntity>,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var walletFilter by remember { mutableStateOf("Semua") } // "Semua", "Tunai", "Bank", "E-Wallet"
    var typeFilter by remember { mutableStateOf("Semua") } // "Semua", "INCOME", "EXPENSE"

    val filteredTransactions = transactions.filter { item ->
        val matchesSearch = item.description.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
        val matchesWallet = walletFilter == "Semua" || item.wallet == walletFilter
        val matchesType = typeFilter == "Semua" || item.type == typeFilter

        matchesSearch && matchesWallet && matchesType
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar Text Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari rincian transaksimu...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Small Horizontal filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Type filtering trigger
            Box(modifier = Modifier.weight(1f)) {
                var isTypeOpen by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { isTypeOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    val displayTypeText = when (typeFilter) {
                        "INCOME" -> "Pemasukan"
                        "EXPENSE" -> "Pengeluaran"
                        else -> "Semua Tipe"
                    }
                    Text(displayTypeText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                }
                DropdownMenu(expanded = isTypeOpen, onDismissRequest = { isTypeOpen = false }) {
                    DropdownMenuItem(text = { Text("Semua Tipe") }, onClick = { typeFilter = "Semua"; isTypeOpen = false })
                    DropdownMenuItem(text = { Text("Pemasukan (Income)") }, onClick = { typeFilter = "INCOME"; isTypeOpen = false })
                    DropdownMenuItem(text = { Text("Pengeluaran (Expense)") }, onClick = { typeFilter = "EXPENSE"; isTypeOpen = false })
                }
            }

            // Wallet filtering trigger
            Box(modifier = Modifier.weight(1f)) {
                var isWalletOpen by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { isWalletOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(if (walletFilter == "Semua") "Semua Dompet" else walletFilter, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                }
                DropdownMenu(expanded = isWalletOpen, onDismissRequest = { isWalletOpen = false }) {
                    DropdownMenuItem(text = { Text("Semua Dompet") }, onClick = { walletFilter = "Semua"; isWalletOpen = false })
                    DropdownMenuItem(text = { Text("Tunai") }, onClick = { walletFilter = "Tunai"; isWalletOpen = false })
                    DropdownMenuItem(text = { Text("Bank") }, onClick = { walletFilter = "Bank"; isWalletOpen = false })
                    DropdownMenuItem(text = { Text("E-Wallet") }, onClick = { walletFilter = "E-Wallet"; isWalletOpen = false })
                }
            }

            // Quick reset filter if modified
            if (walletFilter != "Semua" || typeFilter != "Semua" || searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        searchQuery = ""
                        walletFilter = "Semua"
                        typeFilter = "Semua"
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Hapus Filter",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Content
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Kosong",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Tidak ditemukan hasil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Coba sesuaikan pencarian atau filter Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { item ->
                    TransactionRow(transaction = item, onDelete = onDeleteTransaction)
                }
            }
        }
    }
}
