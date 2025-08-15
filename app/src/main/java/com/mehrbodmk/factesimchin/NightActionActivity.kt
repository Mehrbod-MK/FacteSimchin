package com.mehrbodmk.factesimchin

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.Missions
import com.mehrbodmk.factesimchin.models.NightAction
import com.mehrbodmk.factesimchin.models.commands.NightCommand
import com.mehrbodmk.factesimchin.utils.Constants

class NightActionActivity : AppCompatActivity() {

    private lateinit var nightAction: NightAction

    private lateinit var textViewNightActionText: TextView
    private lateinit var buttonSourcePlayers: AppCompatButton
    private lateinit var buttonMissions: AppCompatButton
    private lateinit var buttonTargetPlayers: AppCompatButton

    private var selectedSourcePlayerIndex: Int = -1
    private var selectedMissionIndex: Int = -1
    private var selectedTargetPlayerIndex: Int = -1

    private lateinit var nightCommands: ArrayList<NightCommand>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_night_action)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nightAction = intent.extras?.getParcelable(Constants.INTENT_NIGHT_ACTION)!!

        textViewNightActionText = findViewById(R.id.textViewNightActionText)
        textViewNightActionText.text = getString(R.string.players_do_what, nightAction.roleLocalName,
            nightAction.candidateSourcePlayers.joinToString(separator = "\n") { it.name },
            nightAction.verbString)
        buttonSourcePlayers = findViewById(R.id.buttonChooseSourcePlayer)
        buttonMissions = findViewById(R.id.buttonChooseMission)
        buttonTargetPlayers = findViewById(R.id.buttonChooseTargetPlayer)

        attachEvents()
    }

    private fun attachEvents()
    {
        var selectedSourcePlayerItem = 0
        var selectedMission = 0
        var selectedTargetPlayerItem = 0

        buttonSourcePlayers.setOnClickListener {
            val alertDialogSelectSourcePlayer = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
                .setTitle(getString(R.string.choose_source_player))
                .setSingleChoiceItems(nightAction.candidateSourcePlayers.map { it.name }.toTypedArray(), selectedSourcePlayerItem) { _, which ->
                    selectedSourcePlayerItem = which
                }
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    selectedSourcePlayerIndex =
                        selectedSourcePlayerItem; dialog.dismiss(); updateUI()
                }
                .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
            alertDialogSelectSourcePlayer.show()
        }
        buttonMissions.setOnClickListener {
            val alertDialogSelectMission = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
                .setTitle(getString(R.string.choose_mission))
                .setSingleChoiceItems(getMissionsLocalNames(nightAction.missions), selectedMission) { _, which ->
                    selectedMission = which
                }
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    selectedMissionIndex =
                        selectedMission; dialog.dismiss(); updateUI()
                }
                .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
            alertDialogSelectMission.show()
        }
        buttonTargetPlayers.setOnClickListener {
            val alertDialogSelectTargetPlayer = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
                .setTitle(getString(R.string.choose_target_player))
                .setSingleChoiceItems(nightAction.candidateTargetPlayers.map { it.name }.toTypedArray(), selectedTargetPlayerItem) { _, which ->
                    selectedTargetPlayerItem = which
                }
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    selectedTargetPlayerIndex =
                        selectedTargetPlayerItem; dialog.dismiss(); updateUI()
                }
                .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
            alertDialogSelectTargetPlayer.show()
        }
    }

    private fun updateUI()
    {
        if(selectedSourcePlayerIndex >= 0)
        {
            buttonSourcePlayers.text = nightAction.candidateSourcePlayers[selectedSourcePlayerIndex].name
        }
        if(selectedMissionIndex >= 0)
        {
            buttonMissions.text = getMissionsLocalNames(nightAction.missions)[selectedMissionIndex]
        }
        if(selectedTargetPlayerIndex >= 0)
        {
            buttonTargetPlayers.text = nightAction.candidateTargetPlayers[selectedTargetPlayerIndex].name
        }
    }

    private fun getMissionsLocalNames(missions: ArrayList<Missions>) : Array<String>
    {
        val result : ArrayList<String> = arrayListOf()
        for(mission in missions)
        {
            result.add(
                when(mission) {
                Missions.GODFATHER_SHOOTS_PLAYER -> getString(R.string.action_godfather_shoot)
                Missions.GODFATHER_NATOS_PLAYER -> getString(R.string.action_godfather_nato)
                Missions.BOMBER_BOMBS_PLAYER -> getString(R.string.action_bomber_bomb)
                Missions.DETECTIVE_ACKNOWLEDGES_PLAYER -> getString(R.string.action_detective_acknowledge)
                Missions.DOCTOR_HEALS_PLAYER -> getString(R.string.action_doctor_heal)
                Missions.SNIPER_SHOOTS_PLAYER -> getString(R.string.action_sniper_shoot)
                Missions.GUNNER_GIVES_DUMMY_BULLET -> getString(R.string.action_gunner_dummy_bullet)
                Missions.GUNNER_GIVES_WAR_BULLET -> getString(R.string.action_gunner_war_bullet)
                Missions.DETONATOR_DETONATES -> getString(R.string.action_detonator_detonate)
            })
        }
        return result.toTypedArray()
    }
}