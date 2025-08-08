package com.mehrbodmk.factesimchin

import android.content.Intent
import android.os.Bundle
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.utils.Constants

class ChooseRolesActivity : AppCompatActivity() {

    private lateinit var players : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_roles)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        players = intent.getStringArrayListExtra(Constants.INTENT_PLAYERS_NAMES_LIST) ?: arrayListOf()

        val numberPickerNumMafias = findViewById<NumberPicker>(R.id.numberPickerNumGodFathers)
        numberPickerNumMafias.minValue = 0
        numberPickerNumMafias.maxValue = players.size
        numberPickerNumMafias.setWrapSelectorWheel(true);
    }
}