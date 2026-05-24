package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiParser {

    private val moshi: Moshi = GeminiClient.moshiInstance
    private val apiService = GeminiClient.apiService

    suspend fun parseText(rawText: String): ParsedTransaction? = withContext(Dispatchers.IO) {
        // First try to parse with Gemini if API Key is available
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    Kamu adalah AI asisten pengatur keuangan pribadi. Tugasmu adalah menganalisis teks input (bisa berupa teks bebas, struk belanja, SMS notifikasi mutasi rekening bank BCA/Mandiri/BNI/BRI, atau pemberitahuan e-wallet seperti GoPay/OVO/ShopeePay/Dana) dan mengekstrak informasi keuangan menjadi format JSON terstruktur.
                    
                    Gunakan kategori yang tepat dari daftar berikut:
                    - Untuk EXPENSE: "Makanan", "Shopping", "Transportasi", "Kesehatan", "Rekreasi", "Lainnya"
                    - Untuk INCOME: "Gaji", "Investasi", "Transfer Masuk", "Lainnya"
                    
                    Gunakan jenis dompet (wallet) yang tepat dari daftar berikut:
                    - "Tunai" (japa berurusan dengan cash fisik atau jika tidak disebutkan spesifik bank/e-wallet)
                    - "Bank" (jika disebutkan nama bank, debit, transfer, qris, atm, dll)
                    - "E-Wallet" (jika disebutkan GoPay, OVO, ShopeePay, Dana, LinkAja, dll)
                    
                    Penting:
                    1. Ambil nilai amount (jumlah uang) sebagai angka desimal positif (Double). Jangan sertakan simbol rupiah atau titik desimal yang keliru. Jika transfer Rp 50.000, maka amount adalah 50000.0.
                    2. Tentukan type apakah "INCOME" (jika ada uang masuk, gaji, transfer dari orang lain, cashback masuk) atau "EXPENSE" (jika ada uang keluar, belanja, debet, qris berhasil, transfer keluar).
                    3. Berikan deskripsi (description) singkat dan jelas mengenai transaksi tersebut (maksimal 30 karakter).
                    
                    Output HARUS berupa JSON murni dengan format sebagai berikut, tanpa tambahan teks apapun di luar JSON:
                    {
                      "amount": 50000.0,
                      "type": "EXPENSE",
                      "category": "Makanan",
                      "wallet": "Bank",
                      "description": "Beli Nasi Goreng BCA"
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = "Tolong analisis teks berikut ini:\n$rawText")
                            )
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.1
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(
                            GeminiPart(text = systemPrompt)
                        )
                    )
                )

                val response = apiService.generateContent(apiKey, request)
                val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!jsonString.isNullOrEmpty()) {
                    val adapter = moshi.adapter(ParsedTransaction::class.java)
                    return@withContext adapter.fromJson(jsonString)
                }
            } catch (e: Exception) {
                Log.e("GeminiParser", "Error parsing with Gemini: ${e.message}", e)
            }
        }

        // Fallback to local regex-based parsing if Gemini fails or is not configured
        return@withContext parseLocalFallback(rawText)
    }

    /**
     * Fallback regex parser for typical Indonesian banking & payment SMS patterns
     */
    fun parseLocalFallback(rawText: String): ParsedTransaction? {
        val text = rawText.trim()
        if (text.isEmpty()) return null

        // Simple normalizations
        val amountRegex = Regex("""(?:Rp|IDR)\s*([0-9.,]+)""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(text)

        var amount = 10000.0 // Default fallback amount
        if (amountMatch != null) {
            val rawAmountStr = amountMatch.groupValues[1]
            // Clean up dots and commas: in Indonesia, 50.000 is 50000, and sometimes 50.000,00 is 50000
            val cleanAmountStr = if (rawAmountStr.contains(",") && rawAmountStr.indexOf(",") > rawAmountStr.indexOf(".")) {
                // E.g., 50.000,00 -> remove dots, replace comma with dot
                rawAmountStr.replace(".", "").replace(",", ".")
            } else if (rawAmountStr.count { it == '.' } > 1 || (rawAmountStr.contains(".") && rawAmountStr.length - rawAmountStr.lastIndexOf(".") != 3)) {
                // E.g. "50.000" or many dots -> remove all dots
                rawAmountStr.replace(".", "")
            } else {
                // E.g. "50000" or single separator
                rawAmountStr.replace(",", "").replace(".", "")
            }
            amount = cleanAmountStr.toDoubleOrNull() ?: 10000.0
        }

        // Determine Type (Expense or Income)
        var type = "EXPENSE"
        var category = "Lainnya"
        var wallet = "Bank"
        var description = "Transaksi Otomatis"

        val lowercase = text.lowercase()

        // Pocket/Wallet keywords
        if (lowercase.contains("gopay") || lowercase.contains("ovo") || lowercase.contains("dana") || lowercase.contains("shopeepay") || lowercase.contains("linkaja")) {
            wallet = "E-Wallet"
        } else if (lowercase.contains("tunai") || lowercase.contains("cash")) {
            wallet = "Tunai"
        } else {
            wallet = "Bank" // Default for most SMS alerts
        }

        // Type & Description detection
        if (lowercase.contains("transfer masuk") || lowercase.contains("diterima") || lowercase.contains("masuk ke rek") || lowercase.contains("kredit") || lowercase.contains("dapat transfer") || lowercase.contains("gaji")) {
            type = "INCOME"
            category = "Transfer Masuk"
            description = "Transfer Masuk"

            if (lowercase.contains("gaji") || lowercase.contains("payroll") || lowercase.contains("salary")) {
                category = "Gaji"
                description = "Penerimaan Gaji"
            } else if (lowercase.contains("invest") || lowercase.contains("bunga") || lowercase.contains("deviden")) {
                category = "Investasi"
                description = "Hasil Investasi"
            }
        } else {
            type = "EXPENSE"
            category = "Lainnya"
            description = "Pengeluaran Berhasil"

            if (lowercase.contains("makan") || lowercase.contains("kuliner") || lowercase.contains("resto") || lowercase.contains("kopi") || lowercase.contains("bakso") || lowercase.contains("warung") || lowercase.contains("cafe")) {
                category = "Makanan"
                description = "Kuliner / Makanan"
            } else if (lowercase.contains("grab") || lowercase.contains("gojek") || lowercase.contains("gocar") || lowercase.contains("ride") || lowercase.contains("bensin") || lowercase.contains("tol") || lowercase.contains("parkir") || lowercase.contains("tiket")) {
                category = "Transportasi"
                description = "Transportasi"
            } else if (lowercase.contains("belanja") || lowercase.contains("tokopedia") || lowercase.contains("shopee") || lowercase.contains("pasar") || lowercase.contains("mall") || lowercase.contains("beli") || lowercase.contains("qris")) {
                category = "Shopping"
                description = "Belanja"
                if (lowercase.contains("qris")) {
                    description = "Bayar QRIS"
                }
            } else if (lowercase.contains("dokter") || lowercase.contains("obat") || lowercase.contains("apotek") || lowercase.contains("sakit") || lowercase.contains("klinik") || lowercase.contains("rs")) {
                category = "Kesehatan"
                description = "Kesehatan / Obat"
            } else if (lowercase.contains("nonton") || lowercase.contains("bioskop") || lowercase.contains("game") || lowercase.contains("travel") || lowercase.contains("liburan") || lowercase.contains("hotel")) {
                category = "Rekreasi"
                description = "Rekreasi / Hiburan"
            }
        }

        // Build neat description from keywords
        if (lowercase.contains("qris") && lowercase.contains("berhasil")) {
            val merchantRegex = Regex("""ke\s+([A-Za-z0-9\s]+)""", RegexOption.IGNORE_CASE)
            val merchantMatch = merchantRegex.find(text)
            description = if (merchantMatch != null) {
                "QRIS: " + merchantMatch.groupValues[1].trim().take(18)
            } else {
                "Grup QRIS"
            }
        } else if (lowercase.contains("transfer") && lowercase.contains("ke")) {
            val destinationRegex = Regex("""ke\s+([A-Za-z0-9\s]+)""", RegexOption.IGNORE_CASE)
            val destinationMatch = destinationRegex.find(text)
            description = if (destinationMatch != null) {
                "TF ke " + destinationMatch.groupValues[1].trim().take(18)
            } else {
                "Transfer Keluar"
            }
        }

        return ParsedTransaction(
            amount = amount,
            type = type,
            category = category,
            wallet = wallet,
            description = description
        )
    }
}
