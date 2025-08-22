package com.mehrbodmk.factesimchin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.Missions
import com.mehrbodmk.factesimchin.models.NightAction
import com.mehrbodmk.factesimchin.models.Player
import com.mehrbodmk.factesimchin.models.RoleTypes
import com.mehrbodmk.factesimchin.models.commands.NightCommand
import com.mehrbodmk.factesimchin.utils.Constants

class NightActionActivity : AppCompatActivity() {

    private lateinit var nightAction: NightAction

    private lateinit var imageViewNightActionCard: ImageView
    private lateinit var textViewNightActionText: TextView
    private lateinit var buttonSourcePlayers: AppCompatButton
    private lateinit var buttonMissions: AppCompatButton
    private lateinit var buttonTargetPlayers: AppCompatButton
    private lateinit var buttonAddNightCommand: AppCompatButton
    private lateinit var buttonRemoveAllNightCommands: AppCompatButton
    private lateinit var textViewListMissions: TextView
    private lateinit var buttonChooseGodfatherNatoGuessedRole: AppCompatButton
    private lateinit var buttonAcceptNightActions: AppCompatButton
    private lateinit var linearLayoutBomberBomb: LinearLayout
    private lateinit var buttonChooseBomberBombCode: AppCompatButton

    private lateinit var linearLayoutSourcePlayer: LinearLayout
    private lateinit var linearLayoutMission: LinearLayout
    private lateinit var linearLayoutTargetPlayer: LinearLayout

    private lateinit var linearLayoutGodfatherNato: LinearLayout

    private var selectedSourcePlayerIndex: Int = -1
    private var selectedMissionIndex: Int = -1
    private var selectedTargetPlayerIndex: Int = -1
    private var selectedNatoGuessedRoleIndex: Int = -1
    private var selectedBombCodeIndex: Int = -1

    private var nightCommands: ArrayList<NightCommand> = arrayListOf()

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

        imageViewNightActionCard = findViewById(R.id.imageViewNightActionCard)
        textViewNightActionText = findViewById(R.id.textViewNightActionText)
        textViewNightActionText.text = getString(R.string.players_do_what, nightAction.roleLocalName,
            nightAction.candidateSourcePlayers.joinToString(separator = "\n") { it.name },
            nightAction.verbString)
        buttonSourcePlayers = findViewById(R.id.buttonChooseSourcePlayer)
        buttonMissions = findViewById(R.id.buttonChooseMission)
        buttonTargetPlayers = findViewById(R.id.buttonChooseTargetPlayer)
        buttonAddNightCommand = findViewById(R.id.buttonAddNightCommand)
        buttonRemoveAllNightCommands = findViewById(R.id.buttonRemoveAllNightCommands)
        textViewListMissions = findViewById(R.id.textViewListMissions)
        linearLayoutGodfatherNato = findViewById(R.id.linearLayoutGodfatherNato)
        buttonChooseGodfatherNatoGuessedRole = findViewById(R.id.buttonChooseGodfatherNatoGuessedRole)
        buttonAcceptNightActions = findViewById(R.id.buttonAcceptNightActions)
        linearLayoutBomberBomb = findViewById(R.id.linearLayoutBomberBomb)
        buttonChooseBomberBombCode = findViewById(R.id.buttonChooseBomberBombCode)
        linearLayoutSourcePlayer = findViewById(R.id.linearLayoutSourcePlayer)
        linearLayoutMission = findViewById(R.id.linearLayoutMission)
        linearLayoutTargetPlayer = findViewById(R.id.linearLayoutTargetPlayer)

        imageViewNightActionCard.setImageResource(nightAction.cardResId)
        attachEvents()
        updateUI()
    }

    private fun fastUISelection()
    {
        // Reset UI status.
        linearLayoutSourcePlayer.visibility = View.VISIBLE
        linearLayoutMission.visibility = View.VISIBLE
        linearLayoutTargetPlayer.visibility = View.VISIBLE
        buttonAddNightCommand.visibility = View.VISIBLE
        buttonRemoveAllNightCommands.visibility = View.VISIBLE

        // If there are no source players available, hide the whole UI.
        if(nightAction.candidateSourcePlayers.isEmpty())
        {
            linearLayoutSourcePlayer.visibility = View.GONE
            linearLayoutMission.visibility = View.GONE
            linearLayoutTargetPlayer.visibility = View.GONE
            buttonAddNightCommand.visibility = View.GONE
            buttonRemoveAllNightCommands.visibility = View.GONE
            return
        }

        if(nightAction.candidateSourcePlayers.size == 1)
        {
            selectedSourcePlayerIndex = 0
            linearLayoutSourcePlayer.visibility = View.GONE
        }
        else
        {
            // Check mafia status for UI.
            if(nightAction.roleType == RoleTypes.GODFATHER)
            {
                // If GodFather is alive, select it by default.
                // Otherwise, choose the next available mafia.
                selectedSourcePlayerIndex = nightAction.candidateSourcePlayers.indexOfFirst { it.role.type == RoleTypes.GODFATHER }
                if(selectedSourcePlayerIndex < 0)
                    selectedSourcePlayerIndex = nightAction.candidateSourcePlayers.indexOfFirst { it.role.isMafia == true }
            }
        }
        if(nightAction.missions.size == 1)
        {
            selectedMissionIndex = 0
            linearLayoutMission.visibility = View.GONE
        }
        if(nightAction.candidateTargetPlayers.size == 1)
            selectedTargetPlayerIndex = 0
    }

    private fun selectSourcePlayerItemDialog()
    {
        var selectedSourcePlayerItem = 0
        val items = getPlayerNamesAndRoleNames(nightAction.candidateSourcePlayers)
        val alertDialogSelectSourcePlayer = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
            .setTitle(getString(R.string.choose_source_player))
            .setSingleChoiceItems(items.toTypedArray(), selectedSourcePlayerItem) { dialog, which ->
                selectedSourcePlayerItem = which
                if(selectedSourcePlayerItem >= 0 && selectedSourcePlayerItem < items.count())
                    selectedSourcePlayerIndex = selectedSourcePlayerItem
                dialog.dismiss()
                updateUI()
                selectMissionDialog()
            }
            .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
        alertDialogSelectSourcePlayer.show()
    }

    private fun selectMissionDialog()
    {
        var selectedMission = 0
        val items = getMissionsLocalNames(nightAction.missions)
        val alertDialogSelectMission = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
            .setTitle(getString(R.string.choose_mission))
            .setSingleChoiceItems(items, selectedMission) { dialog, which ->
                selectedMission = which
                if(selectedMission >= 0 && selectedMission < items.count())
                    selectedMissionIndex = selectedMission
                dialog.dismiss()
                updateUI()
                selectTargetPlayerDialog()
            }
            .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
        alertDialogSelectMission.show()
    }

    private fun selectTargetPlayerDialog()
    {
        var selectedTargetPlayerItem = 0
        val items = getPlayerNamesAndRoleNames(nightAction.candidateTargetPlayers)
        val alertDialogSelectTargetPlayer = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
            .setTitle(getString(R.string.choose_target_player))
            .setSingleChoiceItems(items.toTypedArray(), selectedTargetPlayerItem) { dialog, which ->
                selectedTargetPlayerItem = which
                if(selectedTargetPlayerItem >= 0 && selectedTargetPlayerItem < items.count())
                    selectedTargetPlayerIndex = selectedTargetPlayerItem
                dialog.dismiss()
                updateUI()
            }
            .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
        alertDialogSelectTargetPlayer.show()
    }

    private fun selectNatoGuessedRoleDialog()
    {
        var selectedNatoGuessedRoleItem = 0
        val items = AssignRoleCards.getRolesLocalNames(this@NightActionActivity, RoleTypes.entries)
        val alertDialogChooseGodFatherNatoGuessedRole = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
            .setTitle(getString(R.string.choose_godfahter_nato))
            .setSingleChoiceItems(items.toTypedArray(), selectedNatoGuessedRoleItem) { dialog, which ->
                selectedNatoGuessedRoleItem = which
                if(selectedNatoGuessedRoleItem >= 0 && selectedNatoGuessedRoleItem < items.count())
                    selectedNatoGuessedRoleIndex = selectedNatoGuessedRoleItem
                dialog.dismiss()
                updateUI()
            }
            .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
        alertDialogChooseGodFatherNatoGuessedRole.show()
    }

    private fun selectBombCodeDialog()
    {
        var selectedBombCodeItem = 0
        val alertDialogChooseBombCode = AlertDialog.Builder(this@NightActionActivity, R.style.FacteSimchin_AlertDialogsTheme)
            .setTitle(getString(R.string.choose_bomb_code))
            .setSingleChoiceItems(arrayOf("1", "2", "3", "4"), selectedBombCodeItem) { dialog, which ->
                selectedBombCodeItem = which
                selectedBombCodeIndex = selectedBombCodeItem
                dialog.dismiss()
                updateUI()
            }
            .setNegativeButton(getString(R.string.cancel), { dialog, _ -> dialog.dismiss(); })
        alertDialogChooseBombCode.show()
    }

    @SuppressLint("SetTextI18n")
    private fun attachEvents()
    {
        buttonSourcePlayers.setOnClickListener {
            selectSourcePlayerItemDialog()
        }

        buttonMissions.setOnClickListener {
            selectMissionDialog()
        }

        buttonTargetPlayers.setOnClickListener {
            selectTargetPlayerDialog()
        }

        // Special Roles Action Buttons.
        buttonChooseGodfatherNatoGuessedRole.setOnClickListener {
            selectNatoGuessedRoleDialog()
        }
        buttonChooseBomberBombCode.setOnClickListener {
            selectBombCodeDialog()
        }

        buttonAddNightCommand.setOnClickListener {
            val newNightCommand = getNewNightCommand()
            resetNightUI()
            if(newNightCommand != null)
            {
                nightCommands.add(newNightCommand)
                textViewListMissions.text = "${textViewListMissions.text}\n${getNightCommandString(newNightCommand)}"
            }
        }

        buttonAcceptNightActions.setOnClickListener {
            val answerIntent = Intent()
            answerIntent.putParcelableArrayListExtra(Constants.INTENT_NIGHT_COMMANDS, nightCommands)
            setResult(Activity.RESULT_OK, answerIntent)
            finish()
        }

        buttonRemoveAllNightCommands.setOnClickListener {
            nightCommands.clear()
            textViewListMissions.text = ""
            updateUI()
        }
    }

    private fun getPlayerNamesAndRoleNames(players: Iterable<Player>) : ArrayList<String>
    {
        val result: ArrayList<String> = arrayListOf()
        for(player in players)
        {
            result.add(getString(R.string.player_name_and_role_format, player.name,
                AssignRoleCards.getRoleLocalName(this@NightActionActivity, player.role.type)))
        }
        return result
    }

    private fun resetNightUI()
    {
        selectedSourcePlayerIndex = -1
        selectedMissionIndex = -1
        selectedTargetPlayerIndex = -1
        selectedNatoGuessedRoleIndex = -1
        selectedBombCodeIndex = -1

        updateUI()
    }

    private fun getNightCommandString(nightCommand: NightCommand) : String
    {
        val additionalInfoStringBuilder: StringBuilder = StringBuilder()
        if(nightCommand.natoGuessedRole != null)
            additionalInfoStringBuilder.append("(${AssignRoleCards.getRoleLocalName(this@NightActionActivity, nightCommand.natoGuessedRole)}) ")
        if(nightCommand.bombCode != null)
            additionalInfoStringBuilder.append("(${nightCommand.bombCode}) ")
        if(nightCommand.mission == Missions.DETECTIVE_ACKNOWLEDGES_PLAYER)
        {
            var isPositive: Boolean = (nightCommand.targetPlayer.role.isMafia == true
                    && nightCommand.targetPlayer.role.type != RoleTypes.GODFATHER)
            // If detective is drunk, reverse the acknowledgement result.
            if(nightCommand.sourcePlayer.nightStatus.isDrunk)
                isPositive = !isPositive
            additionalInfoStringBuilder.append(getString(R.string.acknowledge_result,
                if(isPositive) getString(R.string.positive) else getString(R.string.negative) ))
        }
        if(nightCommand.mission == Missions.GODFATHER_TALKS_WITH_ROLED_CITIZEN)
        {
            var touchPlayer = nightCommand.targetPlayer.role.type == RoleTypes.CITIZEN
            // If negotiator is drunk, then don't apply negotiation, no matter what.
            if(nightCommand.sourcePlayer.nightStatus.isDrunk)
                touchPlayer = false
            additionalInfoStringBuilder.append(if(touchPlayer) getString(R.string.touch) else getString(R.string.dont_touch))
        }

        return getString(R.string.night_command_item, nightCommands.size,
            nightCommand.sourcePlayer.name, getMissionLocalName(nightCommand.mission), nightCommand.targetPlayer.name,
            additionalInfoStringBuilder.toString())
    }

    private fun getNewNightCommand() : NightCommand?
    {
        var sourcePlayer: Player? = null
        var mission: Missions? = null
        var targetPlayer: Player? = null
        var natoGuessedRole: RoleTypes? = null
        var bombCode: Int? = null

        var result: Boolean = true

        if(selectedSourcePlayerIndex >= 0)
            sourcePlayer = nightAction.candidateSourcePlayers[selectedSourcePlayerIndex]
        else
            result = false
        if(selectedMissionIndex >= 0)
            mission = nightAction.missions[selectedMissionIndex]
        else
            result = false
        if(selectedTargetPlayerIndex >= 0)
            targetPlayer = nightAction.candidateTargetPlayers[selectedTargetPlayerIndex]
        else
            result = false
        if(mission == Missions.GODFATHER_NATOS_PLAYER)
        {
            if(selectedNatoGuessedRoleIndex >= 0)
                natoGuessedRole = RoleTypes.entries[selectedNatoGuessedRoleIndex]
            else
                result = false
        }
        if(mission == Missions.BOMBER_BOMBS_PLAYER)
        {
            if(selectedBombCodeIndex >= 0)
                bombCode = selectedBombCodeIndex + 1
            else
                result = false
        }

        if(!result)
            return null

        return NightCommand(sourcePlayer!!, mission!!, targetPlayer!!, natoGuessedRole, bombCode)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI()
    {
        fastUISelection()
        val chooseString = getString(R.string.choose)
        if(selectedSourcePlayerIndex >= 0)
        {
            buttonSourcePlayers.text = nightAction.candidateSourcePlayers[selectedSourcePlayerIndex].name
        }
        else
        {
            buttonSourcePlayers.text = chooseString
        }
        if(selectedMissionIndex >= 0)
        {
            buttonMissions.text = getMissionsLocalNames(nightAction.missions)[selectedMissionIndex]
        }
        else
        {
            buttonMissions.text = chooseString
        }
        if(selectedTargetPlayerIndex >= 0)
        {
            buttonTargetPlayers.text = nightAction.candidateTargetPlayers[selectedTargetPlayerIndex].name
        }
        else
        {
            buttonTargetPlayers.text = chooseString
        }

        // Special roles.
        linearLayoutGodfatherNato.visibility = View.GONE
        linearLayoutBomberBomb.visibility = View.GONE
        if(selectedMissionIndex >= 0)
        {
            when(nightAction.missions[selectedMissionIndex])
            {
                Missions.GODFATHER_NATOS_PLAYER ->
                {
                    linearLayoutGodfatherNato.visibility = View.VISIBLE
                    if(selectedNatoGuessedRoleIndex >= 0)
                    {
                        buttonChooseGodfatherNatoGuessedRole.text = AssignRoleCards.getRoleLocalName(this@NightActionActivity, RoleTypes.entries[selectedNatoGuessedRoleIndex])
                    }
                    else
                    {
                        buttonChooseGodfatherNatoGuessedRole.text = chooseString
                    }
                }

                Missions.BOMBER_BOMBS_PLAYER ->
                {
                    linearLayoutBomberBomb.visibility = View.VISIBLE
                    if(selectedBombCodeIndex >= 0)
                    {
                        buttonChooseBomberBombCode.text = "${selectedBombCodeIndex + 1}"
                    }
                    else
                    {
                        buttonChooseBomberBombCode.text = chooseString
                    }
                }

                else -> { /* Do nothing. */ }
            }
        }
    }

    private fun getMissionsLocalNames(missions: Iterable<Missions>) : Array<String>
    {
        val result : ArrayList<String> = arrayListOf()
        for(mission in missions)
        {
            result.add(getMissionLocalName(mission))
        }
        return result.toTypedArray()
    }

    private fun getMissionLocalName(mission: Missions) : String
    {
        return when(mission)
        {
            Missions.GODFATHER_SHOOTS_PLAYER -> getString(R.string.action_godfather_shoot)
            Missions.GODFATHER_NATOS_PLAYER -> getString(R.string.action_godfather_nato)
            Missions.BOMBER_BOMBS_PLAYER -> getString(R.string.action_bomber_bomb)
            Missions.DETECTIVE_ACKNOWLEDGES_PLAYER -> getString(R.string.action_detective_acknowledge)
            Missions.DOCTOR_HEALS_PLAYER -> getString(R.string.action_doctor_heal)
            Missions.SNIPER_SHOOTS_PLAYER -> getString(R.string.action_sniper_shoot)
            Missions.GUNNER_GIVES_DUMMY_BULLET -> getString(R.string.action_gunner_dummy_bullet)
            Missions.GUNNER_GIVES_WAR_BULLET -> getString(R.string.action_gunner_war_bullet)
            Missions.DETONATOR_DETONATES -> getString(R.string.action_detonator_detonate)
            Missions.GODFATHER_TALKS_WITH_ROLED_CITIZEN -> getString(R.string.mafia_talk)
            Missions.BARTENDER_GIVES_DRINK -> getString(R.string.bartender_gives_drink)
        }
    }
}