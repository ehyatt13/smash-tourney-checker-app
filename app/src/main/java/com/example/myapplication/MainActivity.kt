package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    // Day 0 starts at 06:00 UTC on 2024-12-23
    private val initialInstant = Instant.parse("2024-12-23T06:00:00Z")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val card1 = findViewById<LinearLayout>(R.id.card1)
        val time1 = findViewById<TextView>(R.id.time1)
        val pic1 = findViewById<ImageView>(R.id.pic1)
        val dayText1 = findViewById<TextView>(R.id.day1)

        val divider = findViewById<View>(R.id.divider)

        val card2 = findViewById<LinearLayout>(R.id.card2)
        val time2 = findViewById<TextView>(R.id.time2)
        val pic2 = findViewById<ImageView>(R.id.pic2)
        val dayText2 = findViewById<TextView>(R.id.day2)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val btnToday = findViewById<Button>(R.id.btnToday)

        fun getTourneyInfo(instant: Instant): Pair<Int, Boolean> {
            val secondsSinceStart = ChronoUnit.SECONDS.between(initialInstant, instant)
            val daysSinceStart = Math.floorDiv(secondsSinceStart, 24 * 3600)
            val dayIndex = Math.floorMod(daysSinceStart, 4).toInt()
            // 0, 1 -> 1v1 (true), 2, 3 -> FFA (false)
            val is1v1 = dayIndex < 2
            return Pair(dayIndex, is1v1)
        }

        fun updateCard(card: LinearLayout, timeView: TextView, picView: ImageView, textView: TextView, 
                       dayIndex: Int, is1v1: Boolean, timeLabel: String, isLive: Boolean) {
            card.visibility = View.VISIBLE
            timeView.text = if (isLive) "${getString(R.string.live)} - $timeLabel" else timeLabel
            timeView.setTextColor(if (isLive) Color.RED else Color.GRAY)
            
            textView.text = resources.getString(when (dayIndex) {
                0 -> R.string.day1
                1 -> R.string.day2
                2 -> R.string.day3
                else -> R.string.day4
            })

            val drawableId = if (is1v1) R.drawable._v1 else R.drawable.ffa
            picView.setImageDrawable(ResourcesCompat.getDrawable(resources, drawableId, theme))
            
            // Always show the image for visual consistency
            picView.visibility = View.VISIBLE
            
            card.setBackgroundColor(if (isLive) Color.parseColor("#10FF0000") else Color.TRANSPARENT)
        }

        fun displayDay(localDate: LocalDate) {
            val zoneId = ZoneId.systemDefault()
            val now = Instant.now()
            
            // Find transition point for the local date
            val utcTransition = ZonedDateTime.of(localDate, LocalTime.of(6, 0), ZoneId.of("UTC"))
            var transitionInstant = utcTransition.toInstant()
            var transitionLocal = transitionInstant.atZone(zoneId)
            
            if (transitionLocal.toLocalDate() < localDate) {
                transitionInstant = transitionInstant.plus(1, ChronoUnit.DAYS)
                transitionLocal = transitionInstant.atZone(zoneId)
            } else if (transitionLocal.toLocalDate() > localDate) {
                transitionInstant = transitionInstant.minus(1, ChronoUnit.DAYS)
                transitionLocal = transitionInstant.atZone(zoneId)
            }

            val transitionTimeStr = transitionLocal.format(timeFormatter)
            
            // Period 1: From start of local day until transition
            val period1Instant = transitionInstant.minusSeconds(1) 
            val (dayIdx1, is1v1_1) = getTourneyInfo(period1Instant)
            
            // Period 2: From transition until end of local day
            val period2Instant = transitionInstant.plusSeconds(1)
            val (dayIdx2, is1v1_2) = getTourneyInfo(period2Instant)

            val isToday = localDate == LocalDate.now()
            val isLiveBefore = isToday && now.isBefore(transitionInstant)
            val isLiveAfter = isToday && !now.isBefore(transitionInstant)

            // Always show the split view for layout stability
            divider.visibility = View.VISIBLE
            
            updateCard(card1, time1, pic1, dayText1, dayIdx1, is1v1_1, getString(R.string.until_time, transitionTimeStr), isLiveBefore)
            updateCard(card2, time2, pic2, dayText2, dayIdx2, is1v1_2, getString(R.string.from_time, transitionTimeStr), isLiveAfter)
        }

        // Initialize
        displayDay(LocalDate.now())

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            displayDay(selectedDate)
        }

        btnToday.setOnClickListener {
            displayDay(LocalDate.now())
            val calendar = Calendar.getInstance()
            calendarView.date = calendar.timeInMillis
        }
    }
}