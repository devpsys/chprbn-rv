package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

/**
 * Displays digit-only amount strings with thousand separators (e.g. `48000000` → `48,000,000`).
 * Underlying field value stays digits-only.
 */
internal object CurrencyAmountVisualTransformation : VisualTransformation {

    private val numberFormat: NumberFormat =
        NumberFormat.getIntegerInstance(Locale.US).apply {
            isGroupingUsed = true
        }

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        if (digits.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val formatted = numberFormat.format(digits.toLong())
        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = DigitGroupingOffsetMapping(
                originalLength = digits.length,
                transformed = formatted,
            ),
        )
    }
}

private class DigitGroupingOffsetMapping(
    private val originalLength: Int,
    transformed: String,
) : OffsetMapping {

    private val originalToTransformed: IntArray
    private val transformedToOriginal: IntArray

    init {
        originalToTransformed = IntArray(originalLength + 1)
        transformedToOriginal = IntArray(transformed.length + 1)
        var originalIndex = 0
        for (transformedIndex in transformed.indices) {
            transformedToOriginal[transformedIndex] = originalIndex
            if (transformed[transformedIndex].isDigit()) {
                originalToTransformed[originalIndex] = transformedIndex
                originalIndex++
            }
        }
        originalToTransformed[originalLength] = transformed.length
        transformedToOriginal[transformed.length] = originalLength
    }

    override fun originalToTransformed(offset: Int): Int =
        originalToTransformed[offset.coerceIn(0, originalLength)]

    override fun transformedToOriginal(offset: Int): Int =
        transformedToOriginal[offset.coerceIn(0, transformedToOriginal.lastIndex)]
}
