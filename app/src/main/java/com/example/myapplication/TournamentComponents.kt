package com.example.myapplication

import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.ui.theme.CalendarBackground
import com.example.myapplication.ui.theme.LiveHighlight
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

data class TourneyPeriod(
    val dayIndex: Int,
    val is1v1: Boolean,
    val timeLabel: String,
    val isLive: Boolean
)

@Composable
fun TournamentCard(
    period: TourneyPeriod,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (period.isLive) LiveHighlight else androidx.compose.ui.graphics.Color.Transparent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (period.isLive) {
                stringResource(R.string.live_label, stringResource(R.string.live), period.timeLabel)
            } else {
                period.timeLabel
            },
            color = if (period.isLive) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Gray,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic
        )

        Icon(
            painter = painterResource(id = if (period.is1v1) R.drawable._v1 else R.drawable.ffa),
            contentDescription = stringResource(R.string.content),
            modifier = Modifier.size(200.dp),
            tint = androidx.compose.ui.graphics.Color.Unspecified
        )

        Text(
            text = stringResource(
                when (period.dayIndex) {
                    0 -> R.string.day1
                    1 -> R.string.day2
                    2 -> R.string.day3
                    else -> R.string.day4
                }
            ),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TournamentDashboard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onResetToToday: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val initialInstant = Instant.parse("2024-12-23T06:00:00Z")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val zoneId = ZoneId.systemDefault()
    val now = Instant.now()

    fun getTourneyInfo(instant: Instant): Pair<Int, Boolean> {
        val secondsSinceStart = ChronoUnit.SECONDS.between(initialInstant, instant)
        val daysSinceStart = Math.floorDiv(secondsSinceStart, 24 * 3600)
        val dayIndex = Math.floorMod(daysSinceStart, 4).toInt()
        val is1v1 = dayIndex < 2
        return Pair(dayIndex, is1v1)
    }

    // Calculate periods
    val utcTransition = ZonedDateTime.of(selectedDate, LocalTime.of(6, 0), ZoneId.of("UTC"))
    var transitionInstant = utcTransition.toInstant()
    var transitionLocal = transitionInstant.atZone(zoneId)
    
    if (transitionLocal.toLocalDate() < selectedDate) {
        transitionInstant = transitionInstant.plus(1, ChronoUnit.DAYS)
        transitionLocal = transitionInstant.atZone(zoneId)
    } else if (transitionLocal.toLocalDate() > selectedDate) {
        transitionInstant = transitionInstant.minus(1, ChronoUnit.DAYS)
        transitionLocal = transitionInstant.atZone(zoneId)
    }

    val transitionTimeStr = transitionLocal.format(timeFormatter)
    
    val isTodayDate = selectedDate == LocalDate.now()
    val isLiveBefore = isTodayDate && now.isBefore(transitionInstant)
    val isLiveAfter = isTodayDate && !now.isBefore(transitionInstant)

    val (dayIdx1, is1v1_1) = getTourneyInfo(transitionInstant.minusSeconds(1))
    val (dayIdx2, is1v1_2) = getTourneyInfo(transitionInstant.plusSeconds(1))

    val period1 = TourneyPeriod(dayIdx1, is1v1_1, stringResource(R.string.until_time, transitionTimeStr), isLiveBefore)
    val period2 = TourneyPeriod(dayIdx2, is1v1_2, stringResource(R.string.from_time, transitionTimeStr), isLiveAfter)

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                TournamentCard(period = period1)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = androidx.compose.ui.graphics.Color.DarkGray)
                TournamentCard(period = period2)
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(CalendarBackground)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CalendarWidget(selectedDate, onDateSelected)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onResetToToday) {
                    Text(text = stringResource(R.string.today))
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TournamentCard(period = period1)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = androidx.compose.ui.graphics.Color.DarkGray)
            TournamentCard(period = period2)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CalendarWidget(selectedDate, onDateSelected)
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onResetToToday) {
                Text(text = stringResource(R.string.today))
            }
        }
    }
}

@Composable
fun CalendarWidget(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    AndroidView(
        factory = { context ->
            CalendarView(context).apply {
                setOnDateChangeListener { _, year, month, dayOfMonth ->
                    onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                }
            }
        },
        update = { view ->
            val calendar = Calendar.getInstance()
            calendar.set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
            view.date = calendar.timeInMillis
        },
        modifier = Modifier.wrapContentSize()
    )
}