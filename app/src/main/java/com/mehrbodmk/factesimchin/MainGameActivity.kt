package com.mehrbodmk.factesimchin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mehrbodmk.factesimchin.models.GameSession
import com.mehrbodmk.factesimchin.models.Missions
import com.mehrbodmk.factesimchin.models.NightAction
import com.mehrbodmk.factesimchin.models.NightStepsInOrder
import com.mehrbodmk.factesimchin.models.Player
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

    private lateinit var textViewGameTurn: TextView
    private lateinit var listViewPlayers: ListView
    private lateinit var buttonGoNight: FloatingActionButton
    private lateinit var buttonTimer: FloatingActionButton

    private var nightStepIndex : Int = 0

    private val timerActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            gameSession = data?.extras?.getParcelable(Constants.INTENT_GAME_SESSION)!!
        }
    }

    private var getSleepOrWakeResultFirstRound = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { _ ->
        nightStepIndex++;
        decideNextNightStepsForFirstRound()
    }
    private var getSleepOrWakeResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { _ ->
        nightStepIndex++;
        decideNextNightSteps()
    }
    private var getNightActionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->

        val nightCommands: ArrayList<NightCommand> = result.data?.getParcelableArrayListExtra(Constants.INTENT_NIGHT_COMMANDS)!!
        makeDecisionForNightCommands(nightCommands)

        nightStepIndex++;
        decideNextNightSteps()
    }

    private var mediaPlayerGodfatherSong : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        attachEvents()

        val players = intent.getParcelableArrayListExtra<Player>(Constants.INTENT_PLAYERS_LIST)!!
        createGameSession(players)
        updateUI()
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
                foundTargetPlayer.nightStatus.isShotByGodfather = true
            }
            Missions.GODFATHER_NATOS_PLAYER ->
            {
                foundTargetPlayer.nightStatus.isNatoed = true
                foundTargetPlayer.nightStatus.guessedNatoRole = nightCommand.natoGuessedRole
            }
            Missions.BOMBER_BOMBS_PLAYER ->
            {
                gameSession.bombsActive.add(Bomb(foundSourcePlayer, foundTargetPlayer, nightCommand.bombCode!!))
            }
            Missions.DETECTIVE_ACKNOWLEDGES_PLAYER -> { /* Do nothing. */ }
            Missions.DOCTOR_HEALS_PLAYER ->
            {
                foundTargetPlayer.nightStatus.isSavedByDoctor = true
            }
            Missions.SNIPER_SHOOTS_PLAYER ->
            {
                foundTargetPlayer.nightStatus.snipedBy = Snipe(foundSourcePlayer)
            }
            Missions.GUNNER_GIVES_DUMMY_BULLET ->
            {
                foundSourcePlayer.nightStatus.hasDummyBullet = true
            }
            Missions.GUNNER_GIVES_WAR_BULLET ->
            {
                foundSourcePlayer.nightStatus.hasWarBullet = true
            }
            Missions.DETONATOR_DETONATES -> throw InvalidObjectException("Detonator is not allowed to detonate bombs at night...!")
        }
    }

    private fun goNight()
    {
        nightStepIndex = 0
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
                val godfathers = getPlayerNamesByRole(Constants.ROLE_NAME_GODFATHER)
                val mafias = getPlayerNamesByRole(Constants.ROLE_NAME_MAFIA)
                val bombers = getPlayerNamesByRole(Constants.ROLE_NAME_BOMBER)
                val allMafias = godfathers + '\n' + mafias + '\n' + bombers
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
                val godfathers = getPlayerNamesByRole(Constants.ROLE_NAME_GODFATHER)
                val mafias = getPlayerNamesByRole(Constants.ROLE_NAME_MAFIA)
                val bombers = getPlayerNamesByRole(Constants.ROLE_NAME_BOMBER)
                val allMafias = godfathers + '\n' + mafias + '\n' + bombers
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
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_DOCTOR,
                    R.drawable.card_doctor, R.string.wake_up, R.string.role_doctor)
            }
            NightStepsInOrder.WAKE_DETECTIVE ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_DETECTIVE,
                    R.drawable.card_detective, R.string.wake_up, R.string.role_detective)
            }
            NightStepsInOrder.WAKE_SNIPER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_SNIPER,
                    R.drawable.card_sniper, R.string.wake_up, R.string.role_sniper)
            }
            NightStepsInOrder.WAKE_GUNNER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_GUNNER,
                    R.drawable.card_gunner, R.string.wake_up, R.string.role_gunner)
            }
            NightStepsInOrder.WAKE_DETONATOR ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_DETONATOR,
                    R.drawable.card_detonator, R.string.wake_up, R.string.role_detonator)
            }
            NightStepsInOrder.SLEEP_DOCTOR ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_DOCTOR,
                    R.drawable.card_doctor, R.string.sleep, R.string.role_doctor)
            }
            NightStepsInOrder.SLEEP_DETECTIVE ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_DETECTIVE,
                    R.drawable.card_detective, R.string.sleep, R.string.role_detective)
            }
            NightStepsInOrder.SLEEP_SNIPER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_SNIPER,
                    R.drawable.card_sniper, R.string.sleep, R.string.role_sniper)
            }
            NightStepsInOrder.SLEEP_GUNNER ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_GUNNER,
                    R.drawable.card_gunner, R.string.sleep, R.string.role_gunner)
            }
            NightStepsInOrder.SLEEP_DETONATOR ->
            {
                prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent, Constants.ROLE_NAME_DETONATOR,
                    R.drawable.card_detonator, R.string.sleep, R.string.role_detonator)
            }

            NightStepsInOrder.WAKE_UP_EVERYONE ->
            {
                stopGodFatherWaltzMusic()
                sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, SleepOrWakeSomeoneCommand(R.drawable.card_day, getString(R.string.wake_up_everyone)))
            }

            NightStepsInOrder.GODFATHER_DOES_WHAT ->
            {
                if(gameSession.players.filter { it.role.type == RoleTypes.GODFATHER }.isEmpty())
                    bypassNightDecision()
                val roleLocalName = getString(R.string.role_godfather)
                val verbString = getString(R.string.does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.GODFATHER }
                val missions = getPossibleMissionsForRole(RoleTypes.GODFATHER)
                val targetPlayers = gameSession.players.filter { !it.isDead && it.role.isMafia != true }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_godfather, roleLocalName, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.BOMBER_BOMBS_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.BOMBER })
                    bypassNightDecision()
                val roleLocalName = getString(R.string.role_bomber)
                val verbString = getString(R.string.bomber_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.BOMBER }
                val missions = getPossibleMissionsForRole(RoleTypes.BOMBER)
                val targetPlayers = gameSession.players.filter { !it.isDead && it.role.isMafia != true }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_bomber, roleLocalName, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.DOCTOR_SAVES_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.DOCTOR })
                    bypassNightDecision()
                val roleLocalName = getString(R.string.role_doctor)
                val verbString = getString(R.string.doctor_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.DOCTOR }
                val missions = getPossibleMissionsForRole(RoleTypes.DOCTOR)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_doctor, roleLocalName, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.DETECTIVE_ACKNOWLEDGES_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.DETECTIVE })
                    bypassNightDecision()
                val roleLocalName = getString(R.string.role_detective)
                val verbString = getString(R.string.detective_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.DETECTIVE }
                val missions = getPossibleMissionsForRole(RoleTypes.DETECTIVE)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_detective, roleLocalName, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.SNIPER_SHOOTS_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.SNIPER })
                    bypassNightDecision()
                val roleLocalName = getString(R.string.role_sniper)
                val verbString = getString(R.string.sniper_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.SNIPER }
                val missions = getPossibleMissionsForRole(RoleTypes.SNIPER)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_sniper, roleLocalName, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.GUNNER_GIVES_BULLETS_TO_WHO ->
            {
                if(gameSession.players.none { it.role.type == RoleTypes.GUNNER })
                    bypassNightDecision()
                val roleLocalName = getString(R.string.role_gunner)
                val verbString = getString(R.string.gunner_does_what)
                val sourcePlayers = gameSession.players.filter { !it.isDead && it.role.type == RoleTypes.GUNNER }
                val missions = getPossibleMissionsForRole(RoleTypes.GUNNER)
                val targetPlayers = gameSession.players.filter { !it.isDead }
                nightActionIntent.putExtra(Constants.INTENT_NIGHT_ACTION, NightAction(R.drawable.card_gunner, roleLocalName, verbString, sourcePlayers, missions, targetPlayers))
            }
            NightStepsInOrder.DETONATOR_DETONATES_WHO -> bypassNightDecision()
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
                val godfathers = getPlayerNamesByRole(Constants.ROLE_NAME_GODFATHER)
                val mafias = getPlayerNamesByRole(Constants.ROLE_NAME_MAFIA)
                val bombers = getPlayerNamesByRole(Constants.ROLE_NAME_BOMBER)
                val allMafias = godfathers + '\n' + mafias + '\n' + bombers
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
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_GODFATHER,
                    R.drawable.card_godfather, R.string.show_like, R.string.role_godfather)
            }
            NightStepsInOrder.BOMBER_SHOW_LIKE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_BOMBER,
                    R.drawable.card_bomber, R.string.show_like, R.string.role_bomber)
            }
            NightStepsInOrder.SLEEP_MAFIA ->
            {
                val godfathers = getPlayerNamesByRole(Constants.ROLE_NAME_GODFATHER)
                val mafias = getPlayerNamesByRole(Constants.ROLE_NAME_MAFIA)
                val bombers = getPlayerNamesByRole(Constants.ROLE_NAME_BOMBER)
                val allMafias = godfathers + '\n' + mafias + '\n' + bombers
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
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_DOCTOR,
                    R.drawable.card_doctor, R.string.wake_up, R.string.role_doctor)
            }
            NightStepsInOrder.WAKE_DETECTIVE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_DETECTIVE,
                    R.drawable.card_detective, R.string.wake_up, R.string.role_detective)
            }
            NightStepsInOrder.WAKE_SNIPER ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_SNIPER,
                    R.drawable.card_sniper, R.string.wake_up, R.string.role_sniper)
            }
            NightStepsInOrder.WAKE_GUNNER ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_GUNNER,
                    R.drawable.card_gunner, R.string.wake_up, R.string.role_gunner)
            }
            NightStepsInOrder.WAKE_DETONATOR ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_DETONATOR,
                    R.drawable.card_detonator, R.string.wake_up, R.string.role_detonator)
            }
            NightStepsInOrder.SLEEP_DOCTOR ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_DOCTOR,
                    R.drawable.card_doctor, R.string.sleep, R.string.role_doctor)
            }
            NightStepsInOrder.SLEEP_DETECTIVE ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_DETECTIVE,
                    R.drawable.card_detective, R.string.sleep, R.string.role_detective)
            }
            NightStepsInOrder.SLEEP_SNIPER ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_SNIPER,
                    R.drawable.card_sniper, R.string.sleep, R.string.role_sniper)
            }
            NightStepsInOrder.SLEEP_GUNNER ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_GUNNER,
                    R.drawable.card_gunner, R.string.sleep, R.string.role_gunner)
            }
            NightStepsInOrder.SLEEP_DETONATOR ->
            {
                prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent, Constants.ROLE_NAME_DETONATOR,
                    R.drawable.card_detonator, R.string.sleep, R.string.role_detonator)
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
                arrayListOf(Missions.GODFATHER_SHOOTS_PLAYER, Missions.GODFATHER_NATOS_PLAYER)
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

    private fun prepareSimpleSleepOrWakeCommand(sleepOrWakeIntent: Intent, roleName: String, cardResId: Int, messageStringId: Int, roleStringResId: Int)
    {
        val players = getPlayerNamesByRole(roleName)
        if(players.isBlank())
        {
            bypassNightDecision()
            return
        }
        val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(cardResId, getString(messageStringId, getString(roleStringResId), players))
        sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
    }

    private fun prepareSimpleSleepOrWakeCommandFirstRound(sleepOrWakeIntent: Intent, roleName: String, cardResId: Int, messageStringId: Int, roleStringResId: Int)
    {
        val players = getPlayerNamesByRole(roleName)
        if(players.isBlank())
        {
            bypassNightDecisionFirstRound()
            return
        }
        val sleepOrWakeCommand = SleepOrWakeSomeoneCommand(cardResId, getString(messageStringId, getString(roleStringResId), players))
        sleepOrWakeIntent.putExtra(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND, sleepOrWakeCommand)
    }

    private fun getPlayerNamesByRole(roleName: String) : String
    {
        val playersWithRole = gameSession.players.filter { player -> player.role.name == roleName }
        val stringBuilder: StringBuilder = StringBuilder()
        if(!playersWithRole.any())
            return ""
        for(player in playersWithRole)
        {
            stringBuilder.append(player.name)
            stringBuilder.append(if(player.isDead) getString(R.string.is_dead) else "")
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private fun createGameSession(players: ArrayList<Player>)
    {
        gameSession = GameSession(players, 1)
        Helpers.stopMainMenuMusic()
    }

    private fun updateUI()
    {
        textViewGameTurn.text = getGameTurnText()
        listViewPlayers.adapter = PlayersListAdapter(this@MainGameActivity, R.layout.game_player_list_item, gameSession.players)
    }

    private fun getGameTurnText() : String
    {
        if(gameSession.round == 1)
            return getString(R.string.game_turn, gameSession.round, getString(R.string.introductory_turn))
        else
            return getString(R.string.game_turn, gameSession.round, "")
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
                    checkBoxSelect = view.findViewById(R.id.checkBoxSelectPlayer),
                    isDead = view.findViewById(R.id.checkBoxPlayerIsDead)
                )
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as PlayerViewHolder
            }

            viewHolder.playerNumber.text = (position + 1).toString()
            viewHolder.playerName.text = mObjects[position].name

            return view
        }

        data class PlayerViewHolder(
            val playerNumber: TextView,
            val playerName: TextView,
            val playerRole: TextView,
            val playerRoleImage: ImageView,
            val checkBoxSelect: AppCompatCheckBox,
            val isDead: AppCompatCheckBox
        )
    }
}