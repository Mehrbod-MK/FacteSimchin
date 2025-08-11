package com.mehrbodmk.factesimchin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.GameSession
import com.mehrbodmk.factesimchin.models.Player
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers

class MainGameActivity : AppCompatActivity() {

    private lateinit var gameSession: GameSession

    private lateinit var listViewPlayers: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listViewPlayers = findViewById(R.id.listViewGamePlayers)

        val players = intent.getParcelableArrayListExtra<Player>(Constants.INTENT_PLAYERS_LIST)!!
        createGameSession(players)
        updateUI()
    }

    private fun createGameSession(players: ArrayList<Player>)
    {
        gameSession = GameSession(players, 1)
        Helpers.stopMainMenuMusic()
    }

    private fun updateUI()
    {
        listViewPlayers.adapter = PlayersListAdapter(this@MainGameActivity, R.layout.game_player_list_item, gameSession.players)
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
                    talkButton = view.findViewById(R.id.buttonPlayerTalk),
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
            val talkButton: AppCompatButton,
            val isDead: AppCompatCheckBox
        )
    }
}