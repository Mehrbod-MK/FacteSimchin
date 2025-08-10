package com.mehrbodmk.factesimchin

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.RoleNameAndCount
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers

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

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val allPickers = findAllNumberPickers(rootView)
        allPickers.forEach { picker ->
            picker.minValue = 0
            picker.maxValue = players.size
            picker.setWrapSelectorWheel(true)
        }

        val buttonAccept = findViewById<AppCompatButton>(R.id.buttonAcceptRolesCount)
        buttonAccept.setOnClickListener {
            if(!checkRolesCount()) {
                return@setOnClickListener
            }

            Helpers.playSoundEffect(this@ChooseRolesActivity, R.raw.button)
            val listOfRolesAndCounts = getListOfRoleNamesAndCount()
            val assignRolesIntent = Intent(this@ChooseRolesActivity, AssignRoleCards::class.java)
            assignRolesIntent.putParcelableArrayListExtra(Constants.INTENT_ROLE_NAMES_AND_COUNT_LIST, listOfRolesAndCounts)
            assignRolesIntent.putStringArrayListExtra(Constants.INTENT_PLAYERS_NAMES_LIST, players)
            startActivity(assignRolesIntent)
        }
    }

    private fun findAllNumberPickers(root: ViewGroup): List<NumberPicker> {
        val numberPickers = mutableListOf<NumberPicker>()

        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)

            when (child) {
                is NumberPicker -> numberPickers.add(child)
                is ViewGroup -> numberPickers.addAll(findAllNumberPickers(child))
            }
        }

        return numberPickers
    }

    private fun checkRolesCount() : Boolean
    {
        // TODO: Implement later.
        return true
    }

    private fun getListOfRoleNamesAndCount() : ArrayList<RoleNameAndCount>
    {
        val result: ArrayList<RoleNameAndCount> = arrayListOf()
        result.add(RoleNameAndCount(Constants.ROLE_NAME_GODFATHER, findViewById<NumberPicker>(R.id.numberPickerNumGodFathers).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_MAFIA, findViewById<NumberPicker>(R.id.numberPickerNumMafias).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_BOMBER, findViewById<NumberPicker>(R.id.numberPickerNumBombers).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_CITIZEN, findViewById<NumberPicker>(R.id.numberPickerNumCitizens).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_DETECTIVE, findViewById<NumberPicker>(R.id.numberPickerNumDetectives).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_DOCTOR, findViewById<NumberPicker>(R.id.numberPickerNumDoctors).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_SNIPER, findViewById<NumberPicker>(R.id.numberPickerNumSnipers).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_GUNNER, findViewById<NumberPicker>(R.id.numberPickerNumGunners).value))
        result.add(RoleNameAndCount(Constants.ROLE_NAME_DETONATOR, findViewById<NumberPicker>(R.id.numberPickerNumDetonators).value))
        return result
    }
}