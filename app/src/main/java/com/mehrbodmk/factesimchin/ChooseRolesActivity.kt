package com.mehrbodmk.factesimchin

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.RoleTypeAndCount
import com.mehrbodmk.factesimchin.models.RoleTypes
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers

class ChooseRolesActivity : AppCompatActivity() {

    private lateinit var players : ArrayList<String>

    private lateinit var textViewChosenRoleStats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_roles)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewChosenRoleStats = findViewById(R.id.textViewChosenRoleStats)

        players = intent.getStringArrayListExtra(Constants.INTENT_PLAYERS_NAMES_LIST) ?: arrayListOf()

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val allPickers = findAllNumberPickers(rootView)
        allPickers.forEach { picker ->
            picker.minValue = 0
            picker.maxValue = players.size
            picker.value = 0
            picker.setWrapSelectorWheel(true)
        }
        distributePlayers()
        updateStats()
        allPickers.forEach { picker -> picker.setOnValueChangedListener { _, _, _ ->
            updateStats()
        } }

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

    private fun distributePlayers()
    {
        var numPlayers = players.count()
        var roleIndex = 0
        while(numPlayers-- > 0)
        {
            if(roleIndex >= RoleTypes.entries.count())
            {
                numPlayers++
                break
            }
            when(RoleTypes.entries[roleIndex++])
            {
                RoleTypes.GODFATHER -> findViewById<NumberPicker>(R.id.numberPickerNumGodFathers).value++
                RoleTypes.MAFIA -> findViewById<NumberPicker>(R.id.numberPickerNumMafias).value++
                RoleTypes.BOMBER ->
                {
                    findViewById<NumberPicker>(R.id.numberPickerNumBombers).value++
                    findViewById<NumberPicker>(R.id.numberPickerNumDetonators).value++
                }
                RoleTypes.CITIZEN -> findViewById<NumberPicker>(R.id.numberPickerNumCitizens).value++
                RoleTypes.DETECTIVE -> findViewById<NumberPicker>(R.id.numberPickerNumDetectives).value++
                RoleTypes.DOCTOR -> findViewById<NumberPicker>(R.id.numberPickerNumDoctors).value++
                RoleTypes.SNIPER -> findViewById<NumberPicker>(R.id.numberPickerNumSnipers).value++
                RoleTypes.GUNNER -> findViewById<NumberPicker>(R.id.numberPickerNumGunners).value++
                RoleTypes.HARDLIVING -> findViewById<NumberPicker>(R.id.numberPickerNumHardLivings).value++
                RoleTypes.DETONATOR -> { /* Do nothing. */ }
                RoleTypes.NEGOTIATOR -> findViewById<NumberPicker>(R.id.numberPickerNumNegotiators).value++
                RoleTypes.JOKER -> findViewById<NumberPicker>(R.id.numberPickerNumJokers).value++
            }
        }
        if(numPlayers > 0)
            findViewById<NumberPicker>(R.id.numberPickerNumCitizens).value += numPlayers
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

    private fun updateStats()
    {
        val roles = getListOfRoleNamesAndCount()
        val numMafias = roles.filter { it.isMafia == true }.sumOf { it.count }
        val numCitizens = roles.filter { it.isMafia == false }.sumOf { it.count }
        val numNeutrals = roles.filter { it.isMafia == null }.sumOf { it.count }
        textViewChosenRoleStats.text = getString(R.string.chosen_role_stats, numMafias, numCitizens,
            numNeutrals, numMafias + numCitizens + numNeutrals, players.count())
    }

    private fun checkRolesCount() : Boolean
    {
        val roleTypes = getListOfRoleNamesAndCount()
        val numMafias = roleTypes.filter { it.isMafia == true }.sumOf { it.count }
        val numCitizens = roleTypes.filter { it.isMafia == false }.sumOf { it.count }
        val numNeutrals = roleTypes.filter { it.isMafia == null }.sumOf { it.count }
        if((numMafias + numCitizens + numNeutrals) != players.count())
        {
            Toast.makeText(this@ChooseRolesActivity, R.string.incorrect_number_of_roles, Toast.LENGTH_SHORT).show()
            Helpers.playSoundEffect(this@ChooseRolesActivity, R.raw.event_bad)
            return false
        }
        else if(numMafias == 0)
        {
            Toast.makeText(this@ChooseRolesActivity, R.string.why_no_mafias, Toast.LENGTH_SHORT).show()
            Helpers.playSoundEffect(this@ChooseRolesActivity, R.raw.event_bad)
            return false
        }
        else if(numMafias >= numCitizens)
        {
            Toast.makeText(this@ChooseRolesActivity, R.string.mafia_always_wins, Toast.LENGTH_SHORT).show()
            Helpers.playSoundEffect(this@ChooseRolesActivity, R.raw.event_bad)
            return false
        }
        return true
    }

    private fun getListOfRoleNamesAndCount() : ArrayList<RoleTypeAndCount>
    {
        val result: ArrayList<RoleTypeAndCount> = arrayListOf()
        result.add(RoleTypeAndCount(RoleTypes.GODFATHER, findViewById<NumberPicker>(R.id.numberPickerNumGodFathers).value, true))
        result.add(RoleTypeAndCount(RoleTypes.MAFIA, findViewById<NumberPicker>(R.id.numberPickerNumMafias).value, true))
        result.add(RoleTypeAndCount(RoleTypes.BOMBER, findViewById<NumberPicker>(R.id.numberPickerNumBombers).value, true))
        result.add(RoleTypeAndCount(RoleTypes.CITIZEN, findViewById<NumberPicker>(R.id.numberPickerNumCitizens).value, false))
        result.add(RoleTypeAndCount(RoleTypes.DETECTIVE, findViewById<NumberPicker>(R.id.numberPickerNumDetectives).value, false))
        result.add(RoleTypeAndCount(RoleTypes.DOCTOR, findViewById<NumberPicker>(R.id.numberPickerNumDoctors).value, false))
        result.add(RoleTypeAndCount(RoleTypes.SNIPER, findViewById<NumberPicker>(R.id.numberPickerNumSnipers).value, false))
        result.add(RoleTypeAndCount(RoleTypes.GUNNER, findViewById<NumberPicker>(R.id.numberPickerNumGunners).value, false))
        result.add(RoleTypeAndCount(RoleTypes.HARDLIVING, findViewById<NumberPicker>(R.id.numberPickerNumHardLivings).value, false))
        result.add(RoleTypeAndCount(RoleTypes.DETONATOR, findViewById<NumberPicker>(R.id.numberPickerNumDetonators).value, false))
        result.add(RoleTypeAndCount(RoleTypes.NEGOTIATOR, findViewById<NumberPicker>(R.id.numberPickerNumNegotiators).value, true))
        result.add(RoleTypeAndCount(RoleTypes.JOKER, findViewById<NumberPicker>(R.id.numberPickerNumJokers).value, null))
        return result
    }
}