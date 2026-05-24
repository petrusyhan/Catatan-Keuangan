package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.network.GeminiParser
import com.example.data.network.ParsedTransaction
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val repository: TransactionRepository,
    private val geminiParser: GeminiParser
) : ViewModel() {

    // Main Transaction Streams
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI Interactive States
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _parsedPreview = MutableStateFlow<ParsedTransaction?>(null)
    val parsedPreview: StateFlow<ParsedTransaction?> = _parsedPreview.asStateFlow()

    private val _rawTextUsed = MutableStateFlow<String?>(null)
    val rawTextUsed: StateFlow<String?> = _rawTextUsed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Parse raw text (SMS or speech) and create a transaction preview
     */
    fun parseTransactionText(rawText: String) {
        if (rawText.trim().isEmpty()) {
            _errorMessage.value = "Teks input tidak boleh kosong."
            return
        }

        viewModelScope.launch {
            _isParsing.value = true
            _errorMessage.value = null
            _parsedPreview.value = null
            _rawTextUsed.value = rawText

            try {
                val result = geminiParser.parseText(rawText)
                if (result != null) {
                    _parsedPreview.value = result
                } else {
                    _errorMessage.value = "Gagal menganalisis teks. Struktur tidak dikenali."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.localizedMessage}"
            } finally {
                _isParsing.value = false
            }
        }
    }

    /**
     * Confirm and save the previewed transaction to the database
     */
    fun confirmAndSaveTransaction(
        amount: Double,
        type: String,
        category: String,
        wallet: String,
        description: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                amount = amount,
                type = type,
                category = category,
                wallet = wallet,
                description = description,
                rawText = _rawTextUsed.value
            )
            repository.insertTransaction(entity)
            clearParserState()
            // Redirect back to Home/Dashboard tab (0)
            _selectedTab.value = 0
        }
    }

    /**
     * Directly insert manual transaction
     */
    fun insertManualTransaction(
        amount: Double,
        type: String,
        category: String,
        wallet: String,
        description: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                amount = amount,
                type = type,
                category = category,
                wallet = wallet,
                description = description
            )
            repository.insertTransaction(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun clearParserState() {
        _parsedPreview.value = null
        _rawTextUsed.value = null
        _errorMessage.value = null
    }

    /**
     * ViewModel Factory for proper instantiations without heavy frameworks
     */
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application.applicationContext)
                    val repo = TransactionRepository(db.transactionDao)
                    val parser = GeminiParser()
                    return FinanceViewModel(repo, parser) as T
                }
            }
        }
    }
}
