package com.mehrbodmk.factesimchin

import android.content.Context
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class players : AppCompatActivity() {

    private var playersList : ArrayList<String> = ArrayList()

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
        val listViewPlayers = findViewById<ListView>(R.id.listViewPlayers)
        val editTextPlayerName = findViewById<EditText>(R.id.editTextPlayerName)
        buttonAddPlayer.setOnClickListener {
            playersList.add(editTextPlayerName.text.toString())
            listViewPlayers.adapter = MyListAdapter(this, R.layout.player_list_item, playersList)
            android.util.Log.i("tag", playersList.count().toString())
        }
    }

    private class MyListAdapter(
        context: Context,
        private val layout: Int,
        private val mObjects: List<String>
    ) : ArrayAdapter<String>(context, layout, mObjects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder
            val view: View

            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(layout, parent, false)
                viewHolder = ViewHolder(
                    thumbnail = view.findViewById(R.id.list_item_thumbnail),
                    title = view.findViewById(R.id.list_item_text),
                    button = view.findViewById(R.id.list_item_removeButton)
                )
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            viewHolder.button.setOnClickListener {
                Toast.makeText(context, "Button was clicked for list item $position", Toast.LENGTH_SHORT).show()
            }

            viewHolder.title.text = getItem(position)

            return view
        }

        data class ViewHolder(
            val thumbnail: ImageView,
            val title: TextView,
            val button: Button
        )
    }
}