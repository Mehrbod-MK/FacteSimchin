package com.mehrbodmk.factesimchin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mehrbodmk.factesimchin.models.GameSession
import com.mehrbodmk.factesimchin.models.Missions
import com.mehrbodmk.factesimchin.models.NightAction
import com.mehrbodmk.factesimchin.models.NightStatus
import com.mehrbodmk.factesimchin.models.NightStepsInOrder
import com.mehrbodmk.factesimchin.models.Player
import com.mehrbodmk.factesimchin.models.PlayerStates
import com.mehrbodmk.factesimchin.models.RoleTypes
import com.mehrbodmk.factesimchin.models.commands.NightCommand
import com.mehrbodmk.factesimchin.models.commands.SleepOrWakeSomeoneCommand
import com.mehrbodmk.factesimchin.models.mafia.Bomb
import com.mehrbodmk.factesimchin.models.mafia.Snipe
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers
import java.io.InvalidObjectException

class MainGameActivity : AppCompatActivity() {

    private lateinit var gameSession: GameSession

    private lateinit var playersListAdapter: PlayersListAdapter

    private lateinit var textViewGameTurn: TextView
    private lateinit var listViewPlayers: ListView
    private lateinit var buttonGoNight: FloatingActionButton
    private lateinit var buttonTimer: FloatingActionButton
    private lateinit var buttonShowHideRoles: FloatingActionButton
    private lateinit var textViewNumMafiasAlive: TextView
    private lateinit var textViewNumCitizensAlive: TextView
    private lateinit var textViewNumNeutralsAlive: TextView
    private lateinit var textViewNumMafiasDead: TextView
    private lateinit var textViewNumCitizensDead: TextView
    private lateinit var textViewNumNeutralsDead: TextView
    private lateinit var buttonViewBullets: FloatingActionButton
    private lateinit var buttonViewBombs: FloatingActionButton

    private var nightStepIndex : Int = 0

    private val timerActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            gameSession = data?.extras?.getParcelable(Constants.INTENT_GAME_SESSION)!!
        }
    }

    private var getSleepOrWakeResultFirstRound = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if(result.resultCode == Activity.RESULT_OK)
        {
            nightStepIndex++;
        }
        // Don't allow user to cancel.
        decideNextNightStepsForFirstRound()
    }
    private var getSleepOrWakeResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if(result.resultCode == Activity.RESULT_OK)
        {
            nightStepIndex++;
        }
        // Don't allow user to cancel.
        decideNextNightSteps()
    }
    private var getNightActionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val nightCommands: ArrayList<NightCommand> =
                result.data?.getParcelableArrayListExtra(Constants.INTENT_NIGHT_COMMANDS)!!
            makeDecisionForNightCommands(nightCommands)
            nightStepIndex++;
        }
        // Don't allow user to cancel.
        decideNextNightSteps()
    }

    private var mediaPlayerGodfatherSong : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewGameTurn = findViewById(R.id.textViewGameTurn)
        listViewPlayers = findViewById(R.id.listViewGamePlayers)
        buttonGoNight = findViewById(R.id.buttonGoNight)
        buttonTimer = findViewById(R.id.buttonTimer)
        buttonShowHideRoles = findViewById(R.id.buttonShowHideRoles)
        textViewNumMafiasAlive = findViewById(R.id.textViewNumMafiasAlive)
        textViewNumCitizensAlive = findViewById(R.id.textViewNumCitizensAlive)
        textViewNumNeutralsAlive = findViewById(R.id.textViewNumNeutralsAlive)
        textViewNumMafiasDead = findViewById(R.id.textViewNumMafiasDead)
        textViewNumCitizensDead = findViewById(R.id.textViewNumCitizensDead)
        textViewNumNeutralsDead = findViewById(R.id.textViewNumNeutralsDead)
        buttonViewBullets = findViewById(R.id.buttonViewBullets)
        buttonViewBombs = findViewById(R.id.buttonViewBombs)
        attachEvents()

        val players = intent.getParcelableArrayListExtra<Player>(Constants.INTENT_PLAYERS_LIST)!!
        createGameSession(players)

        playersListAdapter = PlayersListAdapter(this@MainGameActivity, R.layout.game_player_list_item, gameSession.players)
        listViewPlayers.adapter = playersListAdapter

        updateUI()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        updateUI()
    }

    private fun decideNewDayEvents()
    {
        val dayEventsStringBuilder: StringBuilder = StringBuilder()

        for(player in gameSession.players)
        {
            // If player was drunk last night, report it.
            if(player.nightStatus.isDrunk)
            {
                dayEventsStringBuilder.appendLine(getString(R.string.player_was_drunk, player.name))
                dayEventsStringBuilder.appendLine()
            }

            // Player is shot by Godfather.
            if(!player.isDead && player.nightStatus.isShotByGodfather)
            {
                // If player is a joker, turn it into mafia.
                if(player.role.type == RoleTypes.JOKER)
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.MAFIA)
                    dayEventsStringBuilder.appendLine(getString(R.string.joker_turned_mafia,
                        player.name, getString(R.string.reason_mafia_shot)))
                    dayEventsStringBuilder.appendLine()
                }
                // Player is saved by doctor, so they won't die.
                else if(player.nightStatus.isSavedByDoctor)
                {
                    dayEventsStringBuilder.appendLine(getString(R.string.doctor_saved_player_from_godfather,
                        player.name))
                    dayEventsStringBuilder.appendLine()
                }
                // Player is hard-living, drop its shield by turning it into simple citizen.
                else if(player.role.type == RoleTypes.HARDLIVING)
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.CITIZEN)
                    dayEventsStringBuilder.appendLine(getString(R.string.hardliving_shield_dropped,
                        player.name))
                    dayEventsStringBuilder.appendLine()
                }
                // Player was not saved, kill them.
                else
                {
                    player.isDead = true
                    dayEventsStringBuilder.appendLine(getString(R.string.say_goodbye_to_because_mafia_shot, player.name))
                    dayEventsStringBuilder.appendLine()
                }
            }

            // Check if player is talked into being mafia.
            if(!player.isDead && player.nightStatus.isTalkedIntoMafia)
            {
                // If player is a joker, turn it into mafia.
                if(player.role.type == RoleTypes.JOKER)
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.MAFIA)
                    dayEventsStringBuilder.appendLine(getString(R.string.joker_turned_mafia,
                        player.name, getString(R.string.reason_mafia_negotiated)))
                    dayEventsStringBuilder.appendLine()
                }
                // If the player has a special role other than citizen, reject it.
                else if(player.role.type != RoleTypes.CITIZEN)
                {
                    dayEventsStringBuilder.appendLine(getString(R.string.talk_failed_because,
                        player.name,
                        AssignRoleCards.getRoleLocalName(this@MainGameActivity, player.role.type)))
                    dayEventsStringBuilder.appendLine()
                }
                // Otherwise, turn the simple citizen into simple mafia.
                else
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.MAFIA)
                    dayEventsStringBuilder.appendLine(getString(R.string.talk_success,
                        player.name))
                    dayEventsStringBuilder.appendLine()
                }
            }

            // Player is attempted to being natoed.
            if(!player.isDead && player.nightStatus.isNatoed)
            {
                // If player is a joker, turn it into mafia.
                if(player.role.type == RoleTypes.JOKER)
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.MAFIA)
                    dayEventsStringBuilder.appendLine(getString(R.string.joker_turned_mafia,
                        player.name, getString(R.string.reason_mafia_natoed)))
                    dayEventsStringBuilder.appendLine()
                }
                // Citizen's role was correctly guessed by mafia.
                if(player.nightStatus.guessedNatoRole == player.role.type)
                {
                    player.isDead = true
                    dayEventsStringBuilder.appendLine(getString(R.string.say_goodbye_to_because_natoed,
                        player.name, AssignRoleCards.getRoleLocalName(this@MainGameActivity, player.role.type)))
                    dayEventsStringBuilder.appendLine()
                }
                // Role guess was wrong.
                else
                {
                    dayEventsStringBuilder.appendLine(getString(R.string.nato_failed,
                        player.name, AssignRoleCards.getRoleLocalName(this@MainGameActivity, player.role.type),
                        AssignRoleCards.getRoleLocalName(this@MainGameActivity, player.nightStatus.guessedNatoRole!!)))
                    dayEventsStringBuilder.appendLine()
                }
            }

            // Check if player was saved by doctor.
            if(!player.isDead && player.nightStatus.isSavedByDoctor)
            {
                // If player is a joker, turn it into citizen.
                if(player.role.type == RoleTypes.JOKER)
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.CITIZEN)
                    dayEventsStringBuilder.appendLine(getString(R.string.joker_turned_citizen,
                        player.name, getString(R.string.reason_doctor_saved)))
                    dayEventsStringBuilder.appendLine()
                }
            }

            // Check if player was sniped.
            if(player.nightStatus.snipedBy != null)
            {
                // If player is a joker, turn it into citizen.
                if(player.role.type == RoleTypes.JOKER)
                {
                    player.role = AssignRoleCards.getRole(this@MainGameActivity, RoleTypes.CITIZEN)
                    dayEventsStringBuilder.appendLine(getString(R.string.joker_turned_citizen,
                        player.name, getString(R.string.reason_sniper_shot)))
                    dayEventsStringBuilder.appendLine()
                }
                // If player was a mafia, check if Dr. Lecter hadn't previously saved the mafia.
                else if(player.role.isMafia == true)
                {
                    // If saved by dr. lecter, report it.
                    if(player.nightStatus.isSavedByDrLecter)
                    {
                        dayEventsStringBuilder.appendLine(getString(R.string.lecter_saved_mafia_from_shot, player.name))
                        dayEventsStringBuilder.appendLine()
                    }
                    // Otherwise, congratulate the citizens!
                    else
                    {
                        player.isDead = true
                        dayEventsStringBuilder.appendLine(getString(R.string.congratulations_sniper, player.name))
                        dayEventsStringBuilder.appendLine()
                    }
                }
                // Else if it was a citizen, then dismiss the sniper instead.
                else if(player.role.isMafia == false)
                {
                    val sniperPlayer = gameSession.players.find { it.name == player.nightStatus.snipedBy!!.sniper.name }
                    sniperPlayer!!.isDead = true
                    dayEventsStringBuilder.appendLine(getString(R.string.say_goodbye_to_because_wrong_snipe, sniperPlayer.name))
                    dayEventsStringBuilder.appendLine()
                }
            }
        }

        val resultString = dayEventsStringBuilder.toString()
        val alertDialogEvents = AlertDialog.Builder(this@MainGameActivity, R.style.FacteSimchin_AlertDialogsTheme)
            .setTitle(getString(R.string.day_events_title))
            .setMessage(resultString)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        alertDialogEvents.show()

        removeNightFlags()
        updateUI()
    }

    private fun removeNightFlags()
    {
        for(player in gameSession.players)
        {
            player.nightStatus = NightStatus(
                hasDummyBullet = player.nightStatus.hasDummyBullet,
                hasWarBullet = player.nightStatus.hasWarBullet,
            )
        }
    }

    private fun resetAllNightStats()
    {
        for(player in gameSession.players)
        {
            player.nightStatus = NightStatus()
            gameSession.bombsActive = arrayListOf()
        }
    }

    private fun attachEvents()
    {
        buttonTimer.setOnClickListener {
            val intent = Intent(this@MainGameActivity, TimerActivity::class.java)
            intent.putExtra(Constants.INTENT_GAME_SESSION, gameSession)
            timerActivityLauncher.launch(intent)
            Helpers.playSoundEffect(this@MainGameActivity, R.raw.button)
        }
        buttonGoNight.setOnClickListener {
            Helpers.askUserYesNo(this@MainGameActivity, R.style.FacteSimchin_AlertDialogsTheme,
                getString(R.string.question), getString(R.string.are_you_sure_go_night),
                getString(R.string.yes), getString(R.string.no),
                R.raw.dialog_show, R.raw.dialog_hide, { goNight() }, { })
        }
        buttonShowHideRoles.setOnClickListener {
            val arePlayersVisible = gameSession.players[0].showRole
            for(player in gameSession.players)
                player.showRole = !arePlayersVisible
            buttonShowHideRoles.foreground = if(arePlayersVisible) AppCompatResources.getDrawable(this@MainGameActivity, R.drawable.icon_show_roles)
                else AppCompatResources.getDrawable(this@MainGameActivity, R.drawable.icon_hide_roles)
            updateUI()
        }
        buttonViewBombs.setOnClickListener {
            val stringBuilder = StringBuilder()
            for(bomb in gameSession.bombsActive)
            {
                stringBuilder.appendLine(getString(R.string.report_bomb, bomb.bomber.name, bomb.target.name, bomb.bombCode.toString()))
            }
            val alertDialogBombs = AlertDialog.Builder(this@MainGameActivity, R.style.FacteSimchin_AlertDialogsTheme)
                .setTitle(getString(R.string.report_game_bombs))
                .setMessage(stringBuilder.toString())
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            alertDialogBombs.show()
        }
        buttonViewBullets.setOnClickListener {
            val stringBuilder = StringBuilder()
            for(player in gameSession.players.filter { it.nightStatus.hasWarBullet || it.nightStatus.hasDummyBullet })
            {
                stringBuilder.appendLine(getString(R.string.report_bullet, player.name,
                    AssignRoleCards.getRoleLocalName(this@MainGameActivity, player.role.type),
                    if(player.nightStatus.hasDummyBullet) getString(R.string.bullet_dummy) else getString(R.string.bullet_war)))
            }
            val alertDialogBullets = AlertDialog.Builder(this@MainGameActivity, R.style.FacteSimchin_AlertDialogsTheme)
                .setTitle(getString(R.string.report_game_bullets))
                .setMessage(stringBuilder.toString())
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            alertDialogBullets.show()
        }
    }

    private fun makeDecisionForNightCommands(nightCommands: Iterable<NightCommand>)
    {
        for(nightCommand in nightCommands)
            makeDecisionForNightCommand(nightCommand)
    }

    private fun makeDecisionForNightCommand(nightCommand: NightCommand)
    {
        val foundSourcePlayer = gameSession.players.find { it == nightCommand.sourcePlayer }!!
        val foundTargetPlayer = gameSession.players.find { it == nightCommand.targetPlayer }!!
        when(nightCommand.mission)
        {
            Missions.GODFATHER_SHOOTS_PLAYER ->
            {
                // If GodFather is not drunk, shoot player.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                    foundTargetPlayer.nightStatus.isShotByGodfather = true
            }
            Missions.GODFATHER_NATOS_PLAYER ->
            {
                // If GodFather is not drunk, nato player.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.isNatoed = true
                    foundTargetPlayer.nightStatus.guessedNatoRole = nightCommand.natoGuessedRole
                }
            }
            Missions.BOMBER_BOMBS_PLAYER ->
            {
                // If bomber is not drunk, plant bomb.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    gameSession.bombsActive.add(Bomb(foundSourcePlayer, foundTargetPlayer, nightCommand.bombCode!!))
                }
            }
            Missions.DETECTIVE_ACKNOWLEDGES_PLAYER -> { /* Do nothing. If the detective is drunk, their acknowledgements must be inverted in NightAction activity, */ }
            Missions.DOCTOR_HEALS_PLAYER ->
            {
                // If doctor is not drunk, set save status.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.isSavedByDoctor = true
                }
            }
            Missions.SNIPER_SHOOTS_PLAYER ->
            {
                // If sniper is not drunk, set save status.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.snipedBy = Snipe(foundSourcePlayer)
                }
            }
            Missions.GUNNER_GIVES_DUMMY_BULLET ->
            {
                // If gunner is not drunk, hand over the dummy bullet.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.hasDummyBullet = true
                }
            }
            Missions.GUNNER_GIVES_WAR_BULLET ->
            {
                // If gunner is not drunk, hand over the dummy bullet.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.hasWarBullet = true
                }
            }
            Missions.DETONATOR_DETONATES -> throw InvalidObjectException("Detonator is not allowed to detonate bombs at night...!")
            Missions.GODFATHER_TALKS_WITH_ROLED_CITIZEN ->
            {
                // If Godfather is not drunk, negotiate.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.isTalkedIntoMafia = true
                }
            }
            Missions.BARTENDER_GIVES_DRINK ->
            {
                foundTargetPlayer.nightStatus.isDrunk = true
            }
            Missions.DR_LECTER_SAVES_MAFIA ->
            {
                // If Dr. Lecter is not drunk, save mafia.
                if(!foundSourcePlayer.nightStatus.isDrunk)
                {
                    foundTargetPlayer.nightStatus.isSavedByDrLecter = true
                }
            }
        }
    }

    private fun goNight()
    {
        nightStepIndex = 0
        resetAllNightStats()
        if(gameSession.round == 1)
            decideNextNightStepsForFirstRound()
        else
            decideNextNightSteps()
    }

    private fun playGodfatherWaltzMusic()
    {
        mediaPlayerGodfatherSong = MediaPlayer.create(this@MainGameActivity, R.raw.godfather_waltz)
        mediaPlayerGodfatherSong!!.isLooping = true
        mediaPlayerGodfatherSong!!.start()
    }

    private fun stopGodFatherWaltzMusic()
    {
        if(mediaPlayerGodfatherSong?.isPlaying == true)
            mediaPlayerGodfatherSong?.stop()
        mediaPlayerGodfatherSong?.release()
    }

    private fun decideNextNightSteps()
    {
        if(nightStepIndex >= NightStepsInOrder.entries.size)
        {
            gameSession.round++
            updateUI()
            return
        }
        val nightStepNow = NightStepsInOrder.entries[nightStepIndex]
        val sleepOrWakeIntent = Intent(this@MainGameActivity, SleepOrWakeSomeoneActivity::class.java)
        val nightActionIntent = Intent(this@MainGameActivity, NightActionActivity::class.java)
        when(nightStepNow)
        {
            NightStepsInOrder.SLEEP_EVERYONE ->
            {
                playGodfatherWaltzMusic()
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, SleepOrWakeSomeoneCommand(R.drawable.card_night, getString(R.string.everyone_sleep)))
            }
            NightStepsInOrder.WAKE_ALL_MAFIAS ->
            {
                val godfathers = getPlayerNamesByRole(RoleTypes.GODFATHER)
                val mafias = getPlayerNamesByRole(RoleTypes.MAFIA)
                val bombers = getPlayerNamesByRole(RoleTypes.BOMBER)
                val negotiators = getPlayerNamesByRole(RoleTypes.NEGOTIATOR)
                val drLecters = getPlayerNamesByRole(RoleTypes.LECTER)
                val allMafias = godfathers + mafias + bombers + negotiators + drLecters
                if(allMafias.isBlank())
                {
                    bypassNightDecision()
                    return
                }
                val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(R.drawable.card_mafia, getString(R.string.wake_up, getString(R.string.role_mafia), allMafias))
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
            }
            NightStepsInOrder.GODFATHER_SHOW_LIKE ->
            {
                bypassNightDecision()
            }
            NightStepsInOrder.BOMBER_SHOW_LIKE ->
            {
                bypassNightDecision()
            }
            NightStepsInOrder.SLEEP_MAFIA ->
            {
                val godfathers = getPlayerNamesByRole(RoleTypes.GODFATHER)
                val mafias = getPlayerNamesByRole(RoleTypes.MAFIA)
                val bombers = getPlayerNamesByRole(RoleTypes.BOMBER)
                val negotiators = getPlayerNamesByRole(RoleTypes.NEGOTIATOR)
                val drLecters = getPlayerNamesByRole(RoleTypes.LECTER)
                val allMafias = godfathers + mafias + bombers + negotiators + drLecters
                if(allMafias.isBlank())
                {
                    bypassNightDecision()
                    return
                }
                val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(R.drawable.card_mafia, getString(R.string.sleep, getString(R.string.role_mafia), allMafias))
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
            }
            NightStepsInOrder.WAKE_DOCTOR ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.DOCTOR,
                    R.drawable.card_doctor, R.string.wake_up, R.string.role_doctor)
            }
            NightStepsInOrder.WAKE_DETECTIVE ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.DETECTIVE,
                    R.drawable.card_detective, R.string.wake_up, R.string.role_detective)
            }
            NightStepsInOrder.WAKE_SNIPER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.SNIPER,
                    R.drawable.card_sniper, R.string.wake_up, R.string.role_sniper)
            }
            NightStepsInOrder.WAKE_GUNNER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.GUNNER,
                    R.drawable.card_gunner, R.string.wake_up, R.string.role_gunner)
            }
            NightStepsInOrder.WAKE_DETONATOR -> bypassNightDecision()
            NightStepsInOrder.SLEEP_DOCTOR ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.DOCTOR,
                    R.drawable.card_doctor, R.string.sleep, R.string.role_doctor)
            }
            NightStepsInOrder.SLEEP_DETECTIVE ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.DETECTIVE,
                    R.drawable.card_detective, R.string.sleep, R.string.role_detective)
            }
            NightStepsInOrder.SLEEP_SNIPER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.SNIPER,
                    R.drawable.card_sniper, R.string.sleep, R.string.role_sniper)
            }
            NightStepsInOrder.SLEEP_GUNNER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.GUNNER,
                    R.drawable.card_gunner, R.string.sleep, R.string.role_gunner)
            }
            NightStepsInOrder.SLEEP_DETONATOR -> bypassNightDecision()
            NightStepsInOrder.WAKE_UP_EVERYONE ->
            {
                stopGodFatherWaltzMusic()
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, SleepOrWakeSomeoneCommand(R.drawable.card_day, getString(R.string.wake_up_everyone)))
            }

            NightStepsInOrder.GODFATHER_DOES_WHAT ->
            {
                if(gameSession.players.none { it.role.isMafia == true })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_mafia)
                val verbString = getString(R.string.does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.isMafia == true }
                val missions = getPossibleMissionsForRole(RoleTypes.GODFATHER)
                val targetPlayers = gameSession.players.filter { !it.isDead && it.role.isMafia != true }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_godfather, roleLocalName, RoleTypes.GODFATHER, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.BOMBER_BOMBS_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.BOMBER })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_bomber)
                val verbString = getString(R.string.bomber_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.BOMBER }
                val missions = getPossibleMissionsForRole(RoleTypes.BOMBER)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_bomber, roleLocalName, RoleTypes.BOMBER, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.DOCTOR_SAVES_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.DOCTOR })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_doctor)
                val verbString = getString(R.string.doctor_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.DOCTOR }
                val missions = getPossibleMissionsForRole(RoleTypes.DOCTOR)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_doctor, roleLocalName, RoleTypes.DOCTOR, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.DETECTIVE_ACKNOWLEDGES_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.DETECTIVE })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_detective)
                val verbString = getString(R.string.detective_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.DETECTIVE }
                val missions = getPossibleMissionsForRole(RoleTypes.DETECTIVE)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_detective, roleLocalName, RoleTypes.DETECTIVE, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.SNIPER_SHOOTS_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.SNIPER })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_sniper)
                val verbString = getString(R.string.sniper_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.SNIPER }
                val missions = getPossibleMissionsForRole(RoleTypes.SNIPER)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_sniper, roleLocalName, RoleTypes.SNIPER, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.GUNNER_GIVES_BULLETS_TO_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.GUNNER })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_gunner)
                val verbString = getString(R.string.gunner_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.GUNNER }
                val missions = getPossibleMissionsForRole(RoleTypes.GUNNER)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_gunner, roleLocalName, RoleTypes.GUNNER, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.DETONATOR_DETONATES_WHO -> bypassNightDecision()
            NightStepsInOrder.DISPLAY_EVENTS ->
            {
                decideNewDayEvents()
                bypassNightDecision()
                return
            }
            NightStepsInOrder.WAKE_HARDLIVING -> bypassNightDecision()
            NightStepsInOrder.SLEEP_HARDLIVING -> bypassNightDecision()
            NightStepsInOrder.NEGOTIATOR_SHOW_LIKE -> bypassNightDecision()
            NightStepsInOrder.WAKE_BARTENDER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.BARTENDER,
                    R.drawable.card_bartender, R.string.wake_up, R.string.role_bartender)
            }
            NightStepsInOrder.BARTENDER_DOES_WHAT ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.BARTENDER })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_bartender)
                val verbString = getString(R.string.bartender_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.BARTENDER }
                val missions = getPossibleMissionsForRole(RoleTypes.BARTENDER)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_bartender, roleLocalName, RoleTypes.BARTENDER, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.SLEEP_BARTENDER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, RoleTypes.BARTENDER,
                    R.drawable.card_bartender, R.string.sleep, R.string.role_bartender)
            }

            NightStepsInOrder.DR_LECTER_SHOW_LIKE -> bypassNightDecision()
            NightStepsInOrder.DR_LECTER_SAVES_WHO ->
            {
                if(gameSession.players.none { it.role.isMafia == true })
                {
                    bypassNightDecision()
                    return
                }
                val roleLocalName = getString(R.string.role_lecter)
                val verbString = getString(R.string.lecter_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.LECTER }
                val missions = getPossibleMissionsForRole(RoleTypes.LECTER)
                val targetPlayers = gameSession.players.filter { !it.isDead && it.role.isMafia == true }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_lecter, roleLocalName, RoleTypes.LECTER, verbString, sourcePlayers, missions, targetPlayers))
            }
        }

        // Simple wake/sleep.
        if(sleepOrWakeIntent.extras != null && sleepOrWakeIntent.extras?.size()!! > 0)
        {
            getSleepOrWakeResult.launch(sleepOrWakeIntent)
        }
        // Night Actions -> Commands.
        else if(nightActionIntent.extras != null && nightActionIntent.extras?.size()!! > 0)
        {
            getNightActionResult.launch(nightActionIntent)
        }
    }

    private fun decideNextNightStepsForFirstRound()
    {
        if(nightStepIndex >= NightStepsInOrder.entries.size)
        {
            gameSession.round = 2
            updateUI()
            return
        }
        val nightStepNow = NightStepsInOrder.entries[nightStepIndex]
        val sleepOrWakeIntent = Intent(this@MainGameActivity, SleepOrWakeSomeoneActivity::class.java)
        when (nightStepNow) {
            NightStepsInOrder.SLEEP_EVERYONE ->
            {
                playGodfatherWaltzMusic()
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, SleepOrWakeSomeoneCommand(R.drawable.card_night, getString(R.string.everyone_sleep)))
            }
            NightStepsInOrder.WAKE_ALL_MAFIAS ->
            {
                val godfathers = getPlayerNamesByRole(RoleTypes.GODFATHER)
                val mafias = getPlayerNamesByRole(RoleTypes.MAFIA)
                val bombers = getPlayerNamesByRole(RoleTypes.BOMBER)
                val negotiators = getPlayerNamesByRole(RoleTypes.NEGOTIATOR)
                val drLecters = getPlayerNamesByRole(RoleTypes.LECTER)
                val allMafias = godfathers + mafias + bombers + negotiators + drLecters
                if(allMafias.isBlank())
                {
                    bypassNightDecisionFirstRound()
                    return
                }
                val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(R.drawable.card_mafia, getString(R.string.wake_up, getString(R.string.role_mafia), allMafias))
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
            }
            NightStepsInOrder.GODFATHER_SHOW_LIKE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, RoleTypes.GODFATHER,
                    R.drawable.card_godfather, R.string.show_like, R.string.role_godfather)
            }
            NightStepsInOrder.BOMBER_SHOW_LIKE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, RoleTypes.BOMBER,
                    R.drawable.card_bomber, R.string.show_like, R.string.role_bomber)
            }
            NightStepsInOrder.SLEEP_MAFIA ->
            {
                val godfathers = getPlayerNamesByRole(RoleTypes.GODFATHER)
                val mafias = getPlayerNamesByRole(RoleTypes.MAFIA)
                val negotiators = getPlayerNamesByRole(RoleTypes.NEGOTIATOR)
                val bombers = getPlayerNamesByRole(RoleTypes.BOMBER)
                val drLecters = getPlayerNamesByRole(RoleTypes.LECTER)
                val allMafias = godfathers + mafias + bombers + negotiators + drLecters
                if(allMafias.isBlank())
                {
                    bypassNightDecisionFirstRound()
                    return
                }
                val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(R.drawable.card_mafia, getString(R.string.sleep, getString(R.string.role_mafia), allMafias))
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
            }
            NightStepsInOrder.WAKE_DOCTOR ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.WAKE_DETECTIVE ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.WAKE_SNIPER ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.WAKE_GUNNER ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.WAKE_DETONATOR ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_DOCTOR ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_DETECTIVE ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_SNIPER ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_GUNNER ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_DETONATOR ->
            {
                bypassNightDecisionFirstRound()
                return
            }

            NightStepsInOrder.WAKE_UP_EVERYONE ->
            {
                stopGodFatherWaltzMusic()
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, SleepOrWakeSomeoneCommand(R.drawable.card_day, getString(R.string.wake_up_everyone)))
            }

            NightStepsInOrder.GODFATHER_DOES_WHAT -> bypassNightDecisionFirstRound()
            NightStepsInOrder.BOMBER_BOMBS_WHO -> bypassNightDecisionFirstRound()
            NightStepsInOrder.DOCTOR_SAVES_WHO -> bypassNightDecisionFirstRound()
            NightStepsInOrder.DETECTIVE_ACKNOWLEDGES_WHO -> bypassNightDecisionFirstRound()
            NightStepsInOrder.SNIPER_SHOOTS_WHO -> bypassNightDecisionFirstRound()
            NightStepsInOrder.GUNNER_GIVES_BULLETS_TO_WHO -> bypassNightDecisionFirstRound()
            NightStepsInOrder.DETONATOR_DETONATES_WHO -> bypassNightDecisionFirstRound()
            NightStepsInOrder.DISPLAY_EVENTS -> bypassNightDecisionFirstRound()
            NightStepsInOrder.WAKE_HARDLIVING ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_HARDLIVING ->
            {
                bypassNightDecisionFirstRound()
                return
            }

            NightStepsInOrder.NEGOTIATOR_SHOW_LIKE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, RoleTypes.NEGOTIATOR,
                    R.drawable.card_negotiator, R.string.show_like, R.string.role_negotiator)
            }

            NightStepsInOrder.WAKE_BARTENDER ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.BARTENDER_DOES_WHAT ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.SLEEP_BARTENDER ->
            {
                bypassNightDecisionFirstRound()
                return
            }
            NightStepsInOrder.DR_LECTER_SHOW_LIKE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, RoleTypes.LECTER,
                    R.drawable.card_lecter, R.string.show_like, R.string.role_lecter)
            }
            NightStepsInOrder.DR_LECTER_SAVES_WHO ->
            {
                bypassNightDecisionFirstRound()
                return
            }
        }

        // Simple wake/sleep.
        if(sleepOrWakeIntent.extras != null && sleepOrWakeIntent.extras?.size()!! > 0)
        {
            getSleepOrWakeResultFirstRound.launch(sleepOrWakeIntent)
        }
    }

    private fun getPossibleMissionsForRole(role: RoleTypes) : ArrayList<Missions>
    {
        return when(role)
        {
            RoleTypes.GODFATHER ->
            {
                arrayListOf(Missions.GODFATHER_SHOOTS_PLAYER, Missions.GODFATHER_NATOS_PLAYER,
                    Missions.GODFATHER_TALKS_WITH_ROLED_CITIZEN)
            }
            RoleTypes.MAFIA ->
            {
                arrayListOf()
            }
            RoleTypes.BOMBER ->
            {
                arrayListOf(Missions.BOMBER_BOMBS_PLAYER)
            }
            RoleTypes.CITIZEN ->
            {
                arrayListOf()
            }
            RoleTypes.DETECTIVE ->
            {
                arrayListOf(Missions.DETECTIVE_ACKNOWLEDGES_PLAYER)
            }
            RoleTypes.DOCTOR ->
            {
                arrayListOf(Missions.DOCTOR_HEALS_PLAYER)
            }
            RoleTypes.SNIPER ->
            {
                arrayListOf(Missions.SNIPER_SHOOTS_PLAYER)
            }
            RoleTypes.GUNNER ->
            {
                arrayListOf(Missions.GUNNER_GIVES_DUMMY_BULLET, Missions.GUNNER_GIVES_WAR_BULLET)
            }
            RoleTypes.DETONATOR ->
            {
                arrayListOf(Missions.DETONATOR_DETONATES)
            }
            RoleTypes.HARDLIVING ->
            {
                arrayListOf()
            }
            RoleTypes.NEGOTIATOR ->
            {
                arrayListOf()
            }
            RoleTypes.JOKER ->
            {
                arrayListOf()
            }
            RoleTypes.BARTENDER ->
            {
                arrayListOf(Missions.BARTENDER_GIVES_DRINK)
            }
            RoleTypes.LECTER ->
            {
                arrayListOf(Missions.DR_LECTER_SAVES_MAFIA)
            }
        }
    }

    private fun bypassNightDecisionFirstRound()
    {
        nightStepIndex++
        decideNextNightStepsForFirstRound()
    }

    private fun bypassNightDecision()
    {
        nightStepIndex++
        decideNextNightSteps()
    }

    private fun prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent: Intent, roleType: RoleTypes, cardResId: Int, messageStringId: Int, roleStringResId: Int)
    {
        val players = getPlayerNamesByRole(roleType)
        if(players.isBlank())
        {
            bypassNightDecision()
            return
        }
        val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(cardResId, getString(messageStringId, getString(roleStringResId), players))
        sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
    }

    private fun prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent: Intent, roleType: RoleTypes, cardResId: Int, messageStringId: Int, roleStringResId: Int)
    {
        val players = getPlayerNamesByRole(roleType)
        if(players.isBlank())
        {
            bypassNightDecisionFirstRound()
            return
        }
        val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(cardResId, getString(messageStringId, getString(roleStringResId), players))
        sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
    }

    private fun getPlayerNamesByRole(roleType: RoleTypes) : String
    {
        val playersWithRole = gameSession.players.filter { player -> player.role.type == roleType }
        val stringBuilder: StringBuilder = StringBuilder()
        if(!playersWithRole.any())
            return ""
        for(player in playersWithRole)
        {
            stringBuilder.append(player.name)
            stringBuilder.append(if(player.isDead) getString(R.string.died) else "")
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private fun createGameSession(players: ArrayList<Player>)
    {
        gameSession = GameSession(players, 1)
        Helpers.stopMainMenuMusic()
    }

    private fun updateUI(signalDataChange: Boolean = true)
    {
        if(signalDataChange)
            listViewPlayers.adapter = PlayersListAdapter(this@MainGameActivity,
                R.layout.game_player_list_item,
                gameSession.players)
        textViewGameTurn.text = getGameTurnText()
        buttonViewBullets.visibility = if(gameSession.players.map { it.nightStatus }.none { it.hasWarBullet || it.hasDummyBullet })
            View.INVISIBLE else View.VISIBLE
        buttonViewBombs.visibility = if(gameSession.bombsActive.none()) View.INVISIBLE else View.VISIBLE

        updateStats()
    }

    private fun updateStats()
    {
        textViewNumMafiasAlive.text = gameSession.players.count { !it.isDead && it.role.isMafia == true }.toString()
        textViewNumMafiasDead.text = gameSession.players.count { it.isDead && it.role.isMafia == true }.toString()
        textViewNumCitizensAlive.text = gameSession.players.count { !it.isDead && it.role.isMafia == false }.toString()
        textViewNumCitizensDead.text = gameSession.players.count { it.isDead && it.role.isMafia == false }.toString()
        textViewNumNeutralsAlive.text = gameSession.players.count { !it.isDead && it.role.isMafia == null }.toString()
        textViewNumNeutralsDead.text = gameSession.players.count { it.isDead && it.role.isMafia == null }.toString()
    }

    private fun getGameTurnText() : String
    {
        return if(gameSession.round == 1)
            getString(R.string.game_turn, gameSession.round, getString(R.string.introductory_turn))
        else
            getString(R.string.game_turn, gameSession.round, "")
    }

    override fun onResume() {
        super.onResume()
        listViewPlayers.adapter = PlayersListAdapter(this@MainGameActivity,
            R.layout.game_player_list_item,
            gameSession.players)  // Rebind if needed
    }

    private class PlayersListAdapter(
        context: Context,
        private val layout: Int,
        private val mObjects: MutableList<Player>
    ) : ArrayAdapter<Player>(context, layout, mObjects) {

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: PlayerViewHolder
            val view: View

            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(layout, parent, false)
                viewHolder = PlayerViewHolder(
                    playerNumber = view.findViewById(R.id.textViewPlayerNumber),
                    playerName = view.findViewById(R.id.textViewPlayerName),
                    playerRole = view.findViewById(R.id.textViewPlayerRole),
                    playerRoleImage = view.findViewById(R.id.imageViewRole),
                    buttonSetPlayerGeneralStatus = view.findViewById(R.id.buttonSetPlayerGeneralStatus),
                    buttonSetPlayerDeadStatus = view.findViewById(R.id.buttonSetPlayerDeadStatus)
                )
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as PlayerViewHolder
            }

            val item = getItem(position)!!
            viewHolder.playerNumber.text = (position + 1).toString()
            viewHolder.playerName.text = item.name
            viewHolder.playerRole.text = item.role.localName
            viewHolder.playerRoleImage.setImageResource(AssignRoleCards.getRoleImageThumbnailResource(item.role.type))
            viewHolder.playerRole.visibility = if(item.showRole) View.VISIBLE else View.INVISIBLE
            viewHolder.playerRoleImage.visibility = if(item.showRole) View.VISIBLE else View.INVISIBLE
            viewHolder.buttonSetPlayerGeneralStatus.setOnClickListener(null)
            viewHolder.buttonSetPlayerDeadStatus.setOnClickListener(null)
            viewHolder.buttonSetPlayerGeneralStatus.setOnClickListener {
                item.playerState = PlayerStates.entries[(PlayerStates.entries.indexOf(item.playerState) + 1) % PlayerStates.entries.count()]
                notifyDataSetChanged()
            }
            viewHolder.buttonSetPlayerDeadStatus.setOnClickListener {
                val mainGameActivity = (context as? MainGameActivity)
                // Check special condition for Hard-Living.
                if(item.role.type == RoleTypes.HARDLIVING)
                {
                    // Turn it into simple citizen.
                    item.role = AssignRoleCards.getRole(context, RoleTypes.CITIZEN)
                    mainGameActivity?.updateUI(false)
                }
                else
                {
                    item.isDead = !item.isDead
                    mainGameActivity?.updateStats()
                }
                notifyDataSetChanged()
            }
            viewHolder.buttonSetPlayerDeadStatus.backgroundTintList = ContextCompat.getColorStateList(context, if(item.isDead) R.color.absent else R.color.present)
            viewHolder.buttonSetPlayerDeadStatus.text = if(item.isDead) context.getString(R.string.dead) else context.getString(R.string.alive)
            setPlayerStatusOnButton(item, viewHolder.buttonSetPlayerGeneralStatus)

            return view
        }

        private fun setPlayerStatusOnButton(player: Player, button: AppCompatButton)
        {
            when(player.playerState)
            {
                PlayerStates.NORMAL ->
                {
                    button.text = context.getString(R.string.player_state_normal)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.state_normal)
                }
                PlayerStates.GOT_CHALLENGE ->
                {
                    button.text = context.getString(R.string.player_state_got_challenge)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.state_got_challenge)
                }
                PlayerStates.VOTED ->
                {
                    button.text = context.getString(R.string.player_state_voted)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.state_voted)
                }
                PlayerStates.BOOKMARKED ->
                {
                    button.text = context.getString(R.string.player_state_bookmarked)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.state_bookmarked)
                }
                PlayerStates.KICKED ->
                {
                    button.text = context.getString(R.string.player_state_kicked)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.state_kicked)
                }
                PlayerStates.CITIZEN ->
                {
                    button.text = context.getString(R.string.citizen_abbreviation)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.present)
                }
                PlayerStates.MAFIA ->
                {
                    button.text = context.getString(R.string.mafia)
                    button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.absent)
                }
            }
        }

        data class PlayerViewHolder(
            val playerNumber: TextView,
            val playerName: TextView,
            val playerRole: TextView,
            val playerRoleImage: ImageView,
            val buttonSetPlayerGeneralStatus: AppCompatButton,
            val buttonSetPlayerDeadStatus: AppCompatButton,
        )
    }
}