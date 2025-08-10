package com.mehrbodmk.factesimchin

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.Player
import com.mehrbodmk.factesimchin.models.Role
import com.mehrbodmk.factesimchin.models.RoleNameAndCount
import com.mehrbodmk.factesimchin.utils.Constants

class AssignRoleCards : AppCompatActivity() {

    private lateinit var players: ArrayList<Player>
    private lateinit var playerNames: ArrayList<String>

    private lateinit var roleNamesAndCount: ArrayList<RoleNameAndCount>
    private lateinit var textViewPlayerName:  TextView
    private lateinit var textViewPlayerRole: TextView
    private lateinit var imageViewCard: ImageView
    private lateinit var buttonRoleOK: AppCompatButton

    private lateinit var currentPlayerName: String
    private lateinit var currentPlayerRole: Role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assign_role_cards)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        roleNamesAndCount = IntentCompat.getParcelableArrayListExtra(intent, Constants.INTENT_ROLE_NAMES_AND_COUNT_LIST, RoleNameAndCount::class.java)!!
        playerNames = intent.getStringArrayListExtra(Constants.INTENT_PLAYERS_NAMES_LIST)!!

        textViewPlayerName = findViewById(R.id.textViewPlayerName)
        textViewPlayerRole = findViewById(R.id.textViewPlayerRole)
        imageViewCard = findViewById(R.id.imageViewCard)
        buttonRoleOK = findViewById(R.id.buttonRoleOK)

        if(!prepareNextRoleSelection())
            throw Exception("No players specified.")
    }

    private fun prepareNextRoleSelection() : Boolean
    {
        if(!playerNames.any())
            return false

        currentPlayerName = playerNames.shuffled().first()
        playerNames.remove(currentPlayerName)
        textViewPlayerName.text = currentPlayerName
        imageViewCard.setImageResource(R.drawable.card_question)
        buttonRoleOK.visibility = View.GONE

        return true
    }
}