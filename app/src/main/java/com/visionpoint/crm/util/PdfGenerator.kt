package com.visionpoint.crm.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.visionpoint.crm.R
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.Payment
import com.visionpoint.crm.data.model.Prescription
import com.visionpoint.crm.data.model.Sale
import com.visionpoint.crm.data.model.SaleItem
import com.visionpoint.crm.data.repository.SettingRepository
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ShopDetails(
    val name: String = "Vision Point",
    val phone: String = "",
    val address: String = SettingRepository.DEFAULT_SHOP_ADDRESS
)

object PdfGenerator {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Dimensions for standard A4 at 72 dpi
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    // Palette matching reference designs
    private const val COLOR_PRIMARY = 0xFF006874.toInt()        // Teal brand accent
    private const val COLOR_BANNER_BG = 0xFF003840.toInt()      // Deep elegant dark teal
    private const val COLOR_ON_PRIMARY = 0xFFFFFFFF.toInt()
    private const val COLOR_CARD_BG = 0xFFF0FDF4.toInt()        // Soft light card background
    private const val COLOR_TABLE_HEADER = 0xFFE0F2F1.toInt()   // Light teal table header
    private const val COLOR_TEXT_PRIMARY = 0xFF191C1D.toInt()   // Deep charcoal
    private const val COLOR_TEXT_SECONDARY = 0xFF536262.toInt() // Muted text
    private const val COLOR_DIVIDER = 0xFFDDE3EA.toInt()        // Thin line divider
    private const val COLOR_DUE_RED = 0xFFBA1A1A.toInt()

    private fun getQuicksandFont(context: Context, isBold: Boolean = false): Typeface {
        return try {
            val base = ResourcesCompat.getFont(context, R.font.quicksand) ?: Typeface.SANS_SERIF
            if (isBold) Typeface.create(base, Typeface.BOLD) else base
        } catch (e: Exception) {
            if (isBold) Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) else Typeface.SANS_SERIF
        }
    }

    private fun getRobotoFont(isBold: Boolean = false): Typeface {
        return if (isBold) Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) else Typeface.SANS_SERIF
    }

    private fun getMonoFont(context: Context, isBold: Boolean = false): Typeface {
        return try {
            val base = ResourcesCompat.getFont(context, R.font.robotomono) ?: Typeface.MONOSPACE
            if (isBold) Typeface.create(base, Typeface.BOLD) else base
        } catch (e: Exception) {
            if (isBold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
        }
    }

    private fun getWrappedLines(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val measure = paint.measureText(testLine)
            if (measure > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    private fun drawHeader(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        isInvoice: Boolean
    ): Float {
        val headerResId = if (isInvoice) R.drawable.invoice_header else R.drawable.prescription_header
        val headerBitmap = try {
            BitmapFactory.decodeResource(context.resources, headerResId)
        } catch (e: Exception) {
            null
        }

        val headerSideMargin = 18.5f
        val headerWidth = PAGE_WIDTH - (headerSideMargin * 2f)
        val headerHeight = if (headerBitmap != null && headerBitmap.width > 0) {
            headerWidth * (headerBitmap.height.toFloat() / headerBitmap.width.toFloat())
        } else {
            118.4f
        }

        if (headerBitmap != null) {
            val destRect = RectF(headerSideMargin, 0f, PAGE_WIDTH - headerSideMargin, headerHeight)
            canvas.drawBitmap(headerBitmap, null, destRect, paint)
        } else {
            // Fallback header
            paint.color = COLOR_BANNER_BG
            canvas.drawRoundRect(
                RectF(MARGIN, 20f, PAGE_WIDTH - MARGIN, 20f + 100f),
                12f, 12f, paint
            )
            paint.color = COLOR_ON_PRIMARY
            paint.textSize = 20f
            paint.typeface = getQuicksandFont(context, isBold = true)
            paint.textAlign = Paint.Align.CENTER
            val title = if (isInvoice) "INVOICE" else "VISION Rx"
            canvas.drawText("VISION POINT - $title", PAGE_WIDTH / 2f, 75f, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        return headerHeight + 12f
    }

    private fun drawFooter(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        isInvoice: Boolean
    ) {
        val footerResId = if (isInvoice) R.drawable.invoice_footer else R.drawable.prescription_footer
        val footerBitmap = try {
            BitmapFactory.decodeResource(context.resources, footerResId)
        } catch (e: Exception) {
            null
        }

        val footerSideMargin = 18.5f
        val footerWidth = PAGE_WIDTH - (footerSideMargin * 2f)
        val footerHeight = if (footerBitmap != null && footerBitmap.width > 0) {
            footerWidth * (footerBitmap.height.toFloat() / footerBitmap.width.toFloat())
        } else {
            118.4f
        }

        val footerTop = PAGE_HEIGHT - footerHeight
        if (footerBitmap != null) {
            val destRect = RectF(footerSideMargin, footerTop, PAGE_WIDTH - footerSideMargin, PAGE_HEIGHT.toFloat())
            canvas.drawBitmap(footerBitmap, null, destRect, paint)
        } else {
            // Fallback footer
            paint.color = COLOR_BANNER_BG
            canvas.drawRoundRect(
                RectF(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 10f),
                12f, 12f, paint
            )
            paint.color = COLOR_ON_PRIMARY
            paint.textSize = 10f
            paint.typeface = getQuicksandFont(context, isBold = true)
            paint.textAlign = Paint.Align.CENTER
            val msg = if (isInvoice) "Thank you for choosing Vision Point Saket!" else "Thank you for trusting Vision Point Saket!"
            canvas.drawText(msg, PAGE_WIDTH / 2f, PAGE_HEIGHT - 35f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    fun generateBillPdf(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        payments: List<Payment>,
        customer: Customer,
        shopDetails: ShopDetails,
        linkedPrescription: Prescription? = null
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header with invoice_header graphic
        var y = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            isInvoice = true
        )

        // 2. Customer & Invoice Summary Card
        val hasAddress = !customer.address.isNullOrBlank()
        val infoCardTop = y
        val infoCardHeight = if (hasAddress) 64f else 48f
        paint.color = COLOR_CARD_BG
        canvas.drawRoundRect(
            RectF(MARGIN, infoCardTop, PAGE_WIDTH - MARGIN, infoCardTop + infoCardHeight),
            8f, 8f, paint
        )

        val quicksandBold = getQuicksandFont(context, isBold = true)
        val monoRegular = getMonoFont(context, isBold = false)
        val monoBold = getMonoFont(context, isBold = true)
        val robotoRegular = getRobotoFont(isBold = false)

        // Left side: BILLED TO
        paint.color = COLOR_TEXT_PRIMARY
        paint.textSize = 10f
        paint.typeface = quicksandBold
        canvas.drawText("BILLED TO:", MARGIN + 12f, y + 16f, paint)

        paint.typeface = monoRegular
        paint.textSize = 9.5f
        canvas.drawText("Name: ${customer.name}", MARGIN + 12f, y + 29f, paint)
        canvas.drawText("Phone: ${customer.phone}", MARGIN + 12f, y + 42f, paint)
        if (hasAddress) {
            canvas.drawText("Address: ${customer.address}", MARGIN + 12f, y + 54f, paint)
        }

        // Right side: INVOICE SUMMARY
        val summaryX = MARGIN + 325f
        paint.typeface = quicksandBold
        paint.textSize = 10f
        canvas.drawText("INVOICE SUMMARY:", summaryX, y + 16f, paint)

        paint.typeface = monoRegular
        paint.textSize = 9.5f
        val formattedBillNo = String.format(Locale.getDefault(), "#%03d", sale.id)
        canvas.drawText("Bill Number: $formattedBillNo", summaryX, y + 29f, paint)
        canvas.drawText("Invoice Date: ${sale.saleDate.format(dateFormatter)}", summaryX, y + 42f, paint)

        y += infoCardHeight + 14f

        // 3. Items Table Header
        val tableHeaderHeight = 24f
        paint.color = COLOR_TABLE_HEADER
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + tableHeaderHeight), 6f, 6f, paint)

        paint.color = COLOR_TEXT_PRIMARY
        paint.textSize = 10f
        paint.typeface = quicksandBold
        canvas.drawText("#", MARGIN + 8f, y + 16f, paint)
        canvas.drawText("Item / Description", MARGIN + 28f, y + 16f, paint)
        canvas.drawText("Type", MARGIN + 215f, y + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Price", MARGIN + 355f, y + 16f, paint)
        canvas.drawText("Qty", MARGIN + 425f, y + 16f, paint)
        canvas.drawText("Total", PAGE_WIDTH - MARGIN - 8f, y + 16f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += tableHeaderHeight + 6f

        // Items Rows with Multi-line Text Wrapping for Description
        paint.typeface = monoRegular
        paint.textSize = 9.5f
        val descMaxWidth = (MARGIN + 215f) - (MARGIN + 28f) - 10f // Available width before touching Type column

        items.forEachIndexed { index, item ->
            paint.color = COLOR_TEXT_PRIMARY
            canvas.drawText("${index + 1}", MARGIN + 8f, y + 13f, paint)

            val nameLines = getWrappedLines(item.name, descMaxWidth, paint)
            val lineHeight = 12f
            var textY = y + 13f
            for (line in nameLines) {
                canvas.drawText(line, MARGIN + 28f, textY, paint)
                textY += lineHeight
            }

            canvas.drawText(item.itemType ?: item.category, MARGIN + 215f, y + 13f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(currencyFormatter.format(item.price), MARGIN + 355f, y + 13f, paint)
            canvas.drawText("${item.quantity}", MARGIN + 425f, y + 13f, paint)
            canvas.drawText(currencyFormatter.format(item.amount), PAGE_WIDTH - MARGIN - 8f, y + 13f, paint)
            paint.textAlign = Paint.Align.LEFT

            val rowHeight = maxOf(18f, (nameLines.size * lineHeight) + 6f)
            y += rowHeight

            paint.color = COLOR_DIVIDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 6f
        }

        y += 4f

        // 4. Financial Summary & Totals Box (Right Side)
        val totalsWidth = 220f
        val totalsLeft = PAGE_WIDTH - MARGIN - totalsWidth
        val totalsTop = y

        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 1f
        canvas.drawLine(totalsLeft, y, PAGE_WIDTH - MARGIN, y, paint)
        var totY = totalsTop + 14f

        fun drawTotalLine(label: String, value: String, isBold: Boolean = false, color: Int = COLOR_TEXT_PRIMARY) {
            paint.typeface = if (isBold) quicksandBold else robotoRegular
            paint.color = color
            paint.textSize = if (isBold) 11.5f else 10f
            canvas.drawText(label, totalsLeft + 8f, totY, paint)

            paint.typeface = if (isBold) monoBold else monoRegular
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, PAGE_WIDTH - MARGIN - 8f, totY, paint)
            paint.textAlign = Paint.Align.LEFT
            totY += 16f
        }

        drawTotalLine("Subtotal:", currencyFormatter.format(sale.subtotal))
        val discountAmount = if (sale.discountType == "amount") sale.discountAmount else (sale.subtotal * sale.discountPercent / 100.0)
        if (discountAmount > 0) {
            val discountLabel = if (sale.discountType == "amount") "Discount:" else "Discount (${sale.discountPercent}%):"
            drawTotalLine(discountLabel, "-${currencyFormatter.format(discountAmount)}")
        }
        drawTotalLine("Net Amount:", currencyFormatter.format(sale.netAmount), isBold = true, color = COLOR_PRIMARY)
        drawTotalLine("Received Amount:", currencyFormatter.format(sale.receivedAmount))
        if (sale.dueAmount > 0) {
            drawTotalLine("Balance Due:", currencyFormatter.format(sale.dueAmount), isBold = true, color = COLOR_DUE_RED)
        } else {
            drawTotalLine("Balance Due:", "₹0.00", isBold = true, color = COLOR_PRIMARY)
        }

        // 5. Payment Records (Left side under items)
        if (payments.isNotEmpty()) {
            var payY = totalsTop + 14f
            paint.typeface = quicksandBold
            paint.textSize = 10f
            paint.color = COLOR_TEXT_PRIMARY
            canvas.drawText("Payments History:", MARGIN + 8f, payY, paint)
            payY += 14f

            paint.typeface = monoRegular
            paint.textSize = 9f
            paint.color = COLOR_TEXT_SECONDARY
            for (p in payments) {
                canvas.drawText(
                    "•${p.date.format(dateFormatter)} - ${currencyFormatter.format(p.amount)} via ${p.method}",
                    MARGIN + 12f,
                    payY,
                    paint
                )
                payY += 13f
            }
        }

        // 6. Footer using invoice_footer graphic
        drawFooter(
            context = context,
            canvas = canvas,
            paint = paint,
            isInvoice = true
        )

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val pdfsDir = File(context.cacheDir, "pdfs").apply { if (!exists()) mkdirs() }
        val pdfFile = File(pdfsDir, "Bill_${sale.id}_${customer.name.replace(" ", "_")}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun generatePrescriptionPdf(
        context: Context,
        prescription: Prescription,
        customer: Customer,
        shopDetails: ShopDetails,
        eyeFields: List<String> = listOf("SPH", "CYL", "Axis", "Add", "V/A")
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header with prescription_header graphic
        var y = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            isInvoice = false
        )

        val quicksandBold = getQuicksandFont(context, isBold = true)
        val monoRegular = getMonoFont(context, isBold = false)
        val monoBold = getMonoFont(context, isBold = true)

        // 2. Patient & Prescription Details Card
        val hasNotes = !prescription.notes.isNullOrBlank()
        val hasPreviousLens = prescription.isPreviousLens
        val summaryX = MARGIN + 325f
        val notesMaxWidth = PAGE_WIDTH - MARGIN - summaryX - 12f
        paint.typeface = monoRegular
        paint.textSize = 9.5f
        val prevLensLines = if (hasPreviousLens) getWrappedLines("Rx as per patient's previous lenses", notesMaxWidth, paint) else emptyList()
        val notesLines = if (hasNotes) getWrappedLines("Notes: ${prescription.notes}", notesMaxWidth, paint) else emptyList()
        val extraLinesCount = prevLensLines.size + notesLines.size
        val extraNotesHeight = if (extraLinesCount > 0) (extraLinesCount * 12f) + 4f else 0f

        val infoCardTop = y
        val infoCardHeight = maxOf(64f, 64f + extraNotesHeight)
        paint.color = COLOR_CARD_BG
        canvas.drawRoundRect(
            RectF(MARGIN, infoCardTop, PAGE_WIDTH - MARGIN, infoCardTop + infoCardHeight),
            8f, 8f, paint
        )

        // Left side: Patient Details
        paint.color = COLOR_TEXT_PRIMARY
        paint.textSize = 10f
        paint.typeface = quicksandBold
        canvas.drawText("PATIENT DETAILS:", MARGIN + 12f, y + 16f, paint)

        paint.typeface = monoRegular
        paint.textSize = 9.5f
        canvas.drawText("Name: ${customer.name}", MARGIN + 12f, y + 29f, paint)
        val agePart = customer.age?.let { "${it}yrs" } ?: "—"
        val genderPart = customer.gender ?: "—"
        canvas.drawText("Age & Sex: $agePart • $genderPart", MARGIN + 12f, y + 42f, paint)
        canvas.drawText("Phone: ${customer.phone}", MARGIN + 12f, y + 54f, paint)

        // Right side: Prescription Details
        paint.typeface = quicksandBold
        paint.textSize = 10f
        canvas.drawText("PRESCRIPTION DETAILS:", summaryX, y + 16f, paint)

        paint.typeface = monoRegular
        paint.textSize = 9.5f
        canvas.drawText("Type: ${prescription.type ?: "Specs"}", summaryX, y + 29f, paint)
        val doctorStr = prescription.doctorName?.ifBlank { null } ?: "Arun Bhalla"
        canvas.drawText("Examiner: $doctorStr", summaryX, y + 42f, paint)
        canvas.drawText("Date: ${prescription.date.format(dateFormatter)}", summaryX, y + 54f, paint)

        var detailExtraY = y + 66f
        if (hasPreviousLens) {
            paint.color = COLOR_PRIMARY
            for (line in prevLensLines) {
                canvas.drawText(line, summaryX, detailExtraY, paint)
                detailExtraY += 12f
            }
            paint.color = COLOR_TEXT_PRIMARY
        }
        if (hasNotes) {
            for (line in notesLines) {
                canvas.drawText(line, summaryX, detailExtraY, paint)
                detailExtraY += 12f
            }
        }

        y += infoCardHeight + 16f

        // 3. Eye Power Grid Table Header
        val rightEyeData = EyePowerUtils.decodeEyeData(prescription.rightEye)
        val leftEyeData = EyePowerUtils.decodeEyeData(prescription.leftEye)

        val tableHeaderHeight = 24f
        paint.color = COLOR_TABLE_HEADER
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + tableHeaderHeight), 6f, 6f, paint)

        val colWidth = (CONTENT_WIDTH - 100f) / maxOf(1, eyeFields.size)

        paint.color = COLOR_TEXT_PRIMARY
        paint.textSize = 10f
        paint.typeface = quicksandBold
        canvas.drawText("Eye", MARGIN + 12f, y + 16f, paint)

        eyeFields.forEachIndexed { i, field ->
            val colX = MARGIN + 100f + (i * colWidth) + (colWidth / 2f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(field, colX, y + 16f, paint)
        }
        paint.textAlign = Paint.Align.LEFT
        y += tableHeaderHeight + 8f

        // Eye Rows
        fun drawEyeRow(label: String, eyeMap: Map<String, String>) {
            paint.color = COLOR_PRIMARY
            paint.typeface = quicksandBold
            paint.textSize = 10f
            canvas.drawText(label, MARGIN + 12f, y + 13f, paint)

            paint.color = COLOR_TEXT_PRIMARY
            paint.typeface = monoRegular
            paint.textSize = 9.5f
            eyeFields.forEachIndexed { i, field ->
                val colX = MARGIN + 100f + (i * colWidth) + (colWidth / 2f)
                val value = eyeMap[field] ?: "—"
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(value, colX, y + 13f, paint)
            }
            paint.textAlign = Paint.Align.LEFT

            y += 18f
            paint.color = COLOR_DIVIDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 6f
        }

        drawEyeRow("Right Eye (OD)", rightEyeData)
        drawEyeRow("Left Eye (OS)", leftEyeData)

        y += 10f

        // 4. Additional Measurements Section (PD, PSM, FH)
        val hasExtra = !prescription.pd.isNullOrBlank() || !prescription.psm.isNullOrBlank() || !prescription.fh.isNullOrBlank()
        if (hasExtra) {
            val measHeight = 36f
            paint.color = COLOR_CARD_BG
            canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + measHeight), 6f, 6f, paint)

            paint.color = COLOR_TEXT_PRIMARY
            paint.typeface = quicksandBold
            paint.textSize = 10f
            canvas.drawText("Measurements:", MARGIN + 12f, y + 22f, paint)

            paint.typeface = monoRegular
            paint.textSize = 9.5f
            var measX = MARGIN + 110f
            if (!prescription.pd.isNullOrBlank()) {
                canvas.drawText("PD: ${prescription.pd} mm", measX, y + 22f, paint)
                measX += 100f
            }
            if (!prescription.psm.isNullOrBlank()) {
                canvas.drawText("PSM: ${prescription.psm}", measX, y + 22f, paint)
                measX += 100f
            }
            if (!prescription.fh.isNullOrBlank()) {
                canvas.drawText("FH: ${prescription.fh}", measX, y + 22f, paint)
            }
        }

        // 5. Footer using prescription_footer graphic
        drawFooter(
            context = context,
            canvas = canvas,
            paint = paint,
            isInvoice = false
        )

        pdfDocument.finishPage(page)

        val pdfsDir = File(context.cacheDir, "pdfs").apply { if (!exists()) mkdirs() }
        val pdfFile = File(pdfsDir, "Prescription_${prescription.id}_${customer.name.replace(" ", "_")}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }
}
