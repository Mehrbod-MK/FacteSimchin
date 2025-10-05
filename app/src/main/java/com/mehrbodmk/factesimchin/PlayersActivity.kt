package com.mehrbodmk.factesimchin

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mehrbodmk.factesimchin.databinding.ActivityPlayersBinding
import com.mehrbodmk.factesimchin.player.PlayerViewModel
import com.mehrbodmk.factesimchin.player.adapter.PlayerListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class PlayersActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayersBinding
    private val _adpater : PlayerListAdapter by lazy {
        PlayerListAdapter()
    }

    val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(viewModel)
        binding.listViewPlayers.adapter = _adpater
        lifecycleScope.launch {
            launch {
                repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                    viewModel.players.collect { playersList ->
                        Timber.d("=============== player ${playersList.size}")
                        _adpater.submitList(playersList)
                    }
                }
            }
            launch {
                repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                    viewModel.error.collect { playersList ->
                        TODO("showing error to user")
                    }
                }
            }
        }
        binding.playersAddFab.setOnClickListener {
            dialog(
                title = R.string.action_add,
                message = R.string.player_name,
                positive = R.string.add,
                positiveClick = { text ->
                    viewModel.addNewPlayer(text)
                },
                negative = R.string.cancel,
                negativeClick = { dialog, which ->
                    dialog.dismiss()
                }
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        val buttonAddPlayer = findViewById<AppCompatButton>(R.id.buttonAddPlayer)
//        val editTextPlayerName = findViewById<EditText>(R.id.editTextPlayerName)
//        val buttonGoToRoles = findViewById<AppCompatButton>(R.id.buttonGoToRoles)
//        val listViewPlayers = findViewById<ListView>(R.id.listViewPlayers)
//        if(checkIfPlayersListFileExists())
//        {
//            playersPresenceList = readPlayersListFromLocalStorage()
//            listViewPlayers.adapter = _adpater
//            //MyListAdapter(this, R.layout.player_list_item, playersPresenceList)
//        }
//        buttonAddPlayer.setOnClickListener {
//            val newPlayerName = editTextPlayerName.text.toString().trim()
//            if(newPlayerName.isEmpty()) {
//                Toast.makeText(this@PlayersActivity, R.string.name_cannot_be_empty, Toast.LENGTH_SHORT).show()
//                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
//                return@setOnClickListener
//            }
//            else if(playersPresenceList.any { it.playerName == newPlayerName }) {
//                Toast.makeText(this@PlayersActivity, R.string.player_already_exists, Toast.LENGTH_SHORT).show()
//                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
//                return@setOnClickListener
//            }
//            playersPresenceList.add(PlayerPresence(newPlayerName, true))
//            editTextPlayerName.setText("")
//            listViewPlayers.adapter = MyListAdapter(this, R.layout.player_list_item, playersPresenceList)
//            Helpers.playSoundEffect(this@PlayersActivity, R.raw.button)
//        }
//        buttonGoToRoles.setOnClickListener {
//            if(playersPresenceList.count { it.isPresent } == 0) {
//                Toast.makeText(this@PlayersActivity, R.string.no_players_available, Toast.LENGTH_SHORT).show()
//                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
//                return@setOnClickListener
//            }
//            if(playersPresenceList.count { it.isPresent } < 5)
//            {
//                Toast.makeText(this@PlayersActivity, R.string.at_least_5_players, Toast.LENGTH_SHORT).show()
//                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
//                return@setOnClickListener
//            }
//            writePlayersListToLocalStorage()
//            val rolesActivityIntent = Intent(this@PlayersActivity, ChooseRolesActivity::class.java)
//            val playerNames : ArrayList<String> = ArrayList(playersPresenceList.filter { it.isPresent }.map { it.playerName })
//            rolesActivityIntent.putExtra(Constants.INTENT_PLAYERS_NAMES_LIST, playerNames)
//            startActivity(rolesActivityIntent)
//            Helpers.playSoundEffect(this@PlayersActivity, R.raw.button)
//        }
    }
}

fun Context.dialog(
    @StringRes title : Int,
    @StringRes message : Int,
    @StringRes positive : Int,
    positiveClick : (String) -> Unit,
    @StringRes negative : Int,
    negativeClick : DialogInterface.OnClickListener,
) {
    val editText = EditText(this)
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setView(editText)
        .setPositiveButton(positive) { dialog, which ->
            positiveClick.invoke(editText.text.toString().trim())
            dialog.dismiss()
        }
        .setNegativeButton(negative, negativeClick)
        .create()
        .show()
}