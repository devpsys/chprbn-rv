package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Dark primary from the stitch picker HTML (`#045315`). */
private val AccrdPickerPrimary = Color(0xFF045315)
private val AccrdPickerMutedDay = Color(0xFFC0C9BA)
private val AccrdPickerWeekday = Color(0xFF4B5563)

private val HeadlineFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH)
private val MonthYearFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

private val WeekdayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

/**
 * Custom accreditation calendar matching the stitch HTML picker:
 * bottom sheet card, headline selection, month nav, 7-column grid,
 * today dot, primary-container selected day, Cancel / Confirm Date.
 */
@Composable
fun AccrdCalendarDatePickerDialog(
    title: String,
    initialDate: LocalDate?,
    yearOnly: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember {
        mutableStateOf(initialDate ?: today)
    }
    var visibleMonth by remember {
        mutableStateOf(YearMonth.from(initialDate ?: today))
    }
    var showYearGrid by remember { mutableStateOf(yearOnly) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D322C).copy(alpha = 0.60f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 512.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Header
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title.uppercase(Locale.ENGLISH),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (yearOnly) {
                                selectedDate.year.toString()
                            } else {
                                selectedDate.format(HeadlineFormatter)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (showYearGrid) {
                        YearGrid(
                            selectedYear = selectedDate.year,
                            onYearSelected = { year ->
                                val clampedDay = selectedDate.dayOfMonth.coerceAtMost(
                                    YearMonth.of(year, selectedDate.month).lengthOfMonth(),
                                )
                                selectedDate = LocalDate.of(year, selectedDate.month, clampedDay)
                                visibleMonth = YearMonth.of(year, selectedDate.month)
                                if (!yearOnly) {
                                    showYearGrid = false
                                }
                            },
                        )
                    } else {
                        MonthNavigation(
                            visibleMonth = visibleMonth,
                            onPrevious = { visibleMonth = visibleMonth.minusMonths(1) },
                            onNext = { visibleMonth = visibleMonth.plusMonths(1) },
                            onMonthLabelClick = { showYearGrid = true },
                        )
                        WeekdayHeader()
                        MonthDayGrid(
                            visibleMonth = visibleMonth,
                            selectedDate = selectedDate,
                            today = today,
                            onDaySelected = { selectedDate = it },
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.accreditation_section_date_cancel),
                                color = AccrdPickerPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = { onConfirm(selectedDate) },
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen,
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = stringResource(R.string.accreditation_section_date_ok),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigation(
    visibleMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMonthLabelClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onMonthLabelClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = visibleMonth.format(MonthYearFormatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(R.string.accreditation_section_date_picker_month_menu_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.accreditation_section_date_picker_prev_month_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.accreditation_section_date_picker_next_month_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        WeekdayLabels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AccrdPickerWeekday,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class CalendarCell(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
)

@Composable
private fun MonthDayGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
) {
    val cells = remember(visibleMonth) { buildMonthCells(visibleMonth) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        DayCell(
                            day = cell.date.dayOfMonth,
                            inCurrentMonth = cell.inCurrentMonth,
                            selected = cell.date == selectedDate,
                            isToday = cell.date == today,
                            onClick = {
                                if (cell.inCurrentMonth) onDaySelected(cell.date)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    inCurrentMonth: Boolean,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        selected -> PrimaryGreen
        isToday && inCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    val fg = when {
        !inCurrentMonth -> AccrdPickerMutedDay
        selected -> Color.White
        isToday -> AccrdPickerPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (inCurrentMonth) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = fg,
            )
            if (isToday && inCurrentMonth && !selected) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(AccrdPickerPrimary),
                )
            }
        }
    }
}

@Composable
private fun YearGrid(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
) {
    val years = remember(selectedYear) {
        val currentYear = LocalDate.now().year
        // Always span through the current year (and a short future buffer),
        // even when the field is seeded with a historical date like 1967.
        val start = (selectedYear - 80).coerceAtLeast(1900)
        val end = maxOf(selectedYear + 20, currentYear + 10)
        (start..end).toList().asReversed()
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(years, key = { it }) { year ->
            val selected = year == selectedYear
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) PrimaryGreen else Color.Transparent)
                    .clickable { onYearSelected(year) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Sunday-first month grid including leading/trailing days from adjacent months. */
private fun buildMonthCells(month: YearMonth): List<CalendarCell> {
    val firstOfMonth = month.atDay(1)
    // java.time DayOfWeek: MON=1 … SUN=7 → convert to Sunday-first index 0…6
    val lead = firstOfMonth.dayOfWeek.value % 7
    val start = firstOfMonth.minusDays(lead.toLong())
    return (0 until 42).map { offset ->
        val date = start.plusDays(offset.toLong())
        CalendarCell(
            date = date,
            inCurrentMonth = YearMonth.from(date) == month,
        )
    }
}
