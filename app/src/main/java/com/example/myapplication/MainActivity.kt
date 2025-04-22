package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDateTime
import kotlin.time.toKotlinDuration

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val initialDate = LocalDateTime.of(2024,12, 23, 1,0,0)
        val currentDate = LocalDateTime.now()
        val days = java.time.Duration.between(initialDate, currentDate).toKotlinDuration()
        val tourneyDay = days.inWholeDays % 4

        val text = findViewById<TextView>(R.id.day)
        val picture = findViewById<ImageView>(R.id.pic)

        when (tourneyDay) {
            0.toLong() -> text.text = resources.getString(R.string.day1)
            1.toLong() -> text.text = resources.getString(R.string.day2)
            2.toLong() -> text.text = resources.getString(R.string.day3)
            3.toLong() -> text.text = resources.getString(R.string.day4)
        }

        if (tourneyDay == 0.toLong() || tourneyDay == 1.toLong()) {
            picture.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable._v1, applicationContext.theme))
        }
        else {
            picture.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ffa, applicationContext.theme))
        }
    }
}