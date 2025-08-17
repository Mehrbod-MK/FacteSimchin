package com.mehrbodmk.factesimchin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mehrbodmk.factesimchin.models.PlayerPresence
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers
import java.io.File

class PlayersActivity : AppCompatActivity() {

    private var playersPresenceList : ArrayList<PlayerPresence> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_players)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonAddPlayer = findViewById<AppCompatButton>(R.id.buttonAddPlayer)
        val buttonGoToRoles = findViewById<AppCompatButton>(R.id.buttonGoToRoles)
        val listViewPlayers = findViewById<ListView>(R.id.listViewPlayers)
        val editTextPlayerName = findViewById<EditText>(R.id.editTextPlayerName)
        if(checkIfPlayersListFileExists())
        {
            playersPresenceList = readPlayersListFromLocalStorage()
            listViewPlayers.adapter = MyListAdapter(this, R.layout.player_list_item, playersPresenceList)
        }
        buttonAddPlayer.setOnClickListener {
            val newPlayerName = editTextPlayerName.text.toString().trim()
            if(newPlayerName.isEmpty()) {
                Toast.makeText(this@PlayersActivity, R.string.name_cannot_be_empty, Toast.LENGTH_SHORT).show()
                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
                return@setOnClickListener
            }
            else if(playersPresenceList.any { it.playerName == newPlayerName }) {
                Toast.makeText(this@PlayersActivity, R.string.player_already_exists, Toast.LENGTH_SHORT).show()
                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
                return@setOnClickListener
            }
            playersPresenceList.add(PlayerPresence(newPlayerName, true))
            editTextPlayerName.setText("")
            listViewPlayers.adapter = MyListAdapter(this, R.layout.player_list_item, playersPresenceList)
            Helpers.playSoundEffect(this@PlayersActivity, R.raw.button)
        }
        buttonGoToRoles.setOnClickListener {
            if(playersPresenceList.isEmpty()) {
                Toast.makeText(this@PlayersActivity, R.string.no_players_available, Toast.LENGTH_SHORT).show()
                Helpers.playSoundEffect(this@PlayersActivity, R.raw.event_bad)
                return@setOnClickListener
            }
            writePlayersListToLocalStorage()
            val rolesActivityIntent = Intent(this@PlayersActivity, ChooseRolesActivity::class.java)
            val playerNames : ArrayList<String> = ArrayList(playersPresenceList.filter { it.isPresent }.map { it.playerName })
            rolesActivityIntent.putExtra(Constants.INTENT_PLAYERS_NAMES_LIST, playerNames)
            startActivity(rolesActivityIntent)
            Helpers.playSoundEffect(this@PlayersActivity, R.raw.button)
        }
    }

    private fun checkIfPlayersListFileExists() : Boolean
    {
        val fileName = Constants.FILENAME_PLAYERS_LIST
        val file = File(this.filesDir, fileName)
        return file.exists()
    }

    private fun readPlayersListFromLocalStorage() : ArrayList<PlayerPresence>
    {
        try {
            val fileName = Constants.FILENAME_PLAYERS_LIST
            val fileContents = this.openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<ArrayList<PlayerPresence>>() {}.type
            val myList: ArrayList<PlayerPresence> = Gson().fromJson(fileContents, type)
            return myList
        }
        catch(ex: Exception) {
            writePlayersListToLocalStorage()
            return arrayListOf()
        }
    }

    private fun writePlayersListToLocalStorage()
    {
        val fileName = Constants.FILENAME_PLAYERS_LIST
        val fileContents = Gson().toJson(playersPresenceList)
        this.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
    }

    private class MyListAdapter(
        context: Context,
        private val layout: Int,
        private val mObjects: MutableList<PlayerPresence>
    ) : ArrayAdapter<PlayerPresence>(context, layout, mObjects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder
            val view: View

            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(layout, parent, false)
                viewHolder = ViewHolder(
                    selectPlayerName = view.findViewById(R.id.buttonSelectPlayerNameItem),
                    title = view.findViewById(R.id.list_item_text),
                    button = view.findViewById(R.id.list_item_removeButton)
                )
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            viewHolder.button.setOnClickListener {
                Helpers.playSoundEffect(this.context, R.raw.button)
                mObjects.removeAt(position)
                notifyDataSetChanged()
            }
            viewHolder.selectPlayerName.setOnClickListener {
                mObjects[position].isPresent = !mObjects[position].isPresent
                Helpers.playSoundEffect(this.context, if(mObjects[position].isPresent) R.raw.checkbox_on else R.raw.checkbox_off)
                notifyDataSetChanged()
            }

            val item = getItem(position)
            viewHolder.title.text =item!!.playerName
            viewHolder.selectPlayerName.text = if(item.isPresent) context.getString(R.string.present) else context.getString(R.string.absent)
            viewHolder.selectPlayerName.backgroundTintList = ContextCompat.getColorStateList(context, if(item.isPresent) R.color.present else R.color.absent)

            return view
        }

        data class ViewHolder(
            val selectPlayerName: AppCompatButton,
            val title: TextView,
            val button: Button
        )
    }
}