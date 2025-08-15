package com.mehrbodmk.factesimchin

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.animation.doOnEnd
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.Player
import com.mehrbodmk.factesimchin.models.Role
import com.mehrbodmk.factesimchin.models.RoleNameAndCount
import com.mehrbodmk.factesimchin.models.RoleTypes
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers

class AssignRoleCards : AppCompatActivity() {

    private var players: ArrayList<Player> = arrayListOf()

    private lateinit var playerNames: ArrayList<String>
    private lateinit var roleNamesAndCount: ArrayList<RoleNameAndCount>

    private lateinit var textViewPlayerName:  TextView
    private lateinit var textViewPlayerRole: TextView
    private lateinit var imageViewCard: ImageView
    private lateinit var buttonRoleOK: AppCompatButton

    private var isCardEnable = false

    private lateinit var currentPlayerName: String
    private lateinit var currentPlayerRole: Role

    private lateinit var cardAnimator : ObjectAnimator

    companion object
    {
        fun getRoleLocalName(context: Context, roleName: String) : String
        {
            return when (roleName) {
                Constants.ROLE_NAME_GODFATHER -> context.getString(R.string.role_godfather)
                Constants.ROLE_NAME_MAFIA -> context.getString(R.string.role_mafia)
                Constants.ROLE_NAME_BOMBER -> context.getString(R.string.role_bomber)
                Constants.ROLE_NAME_CITIZEN -> context.getString(R.string.role_citizen)
                Constants.ROLE_NAME_DETECTIVE -> context.getString(R.string.role_detective)
                Constants.ROLE_NAME_DOCTOR -> context.getString(R.string.role_doctor)
                Constants.ROLE_NAME_SNIPER -> context.getString(R.string.role_sniper)
                Constants.ROLE_NAME_GUNNER -> context.getString(R.string.role_gunner)
                Constants.ROLE_NAME_DETONATOR -> context.getString(R.string.role_detonator)
                else -> ""
            }
        }

        fun getRole(context: Context, roleName: String) : Role
        {
            return when(roleName)
            {
                Constants.ROLE_NAME_GODFATHER -> Role(Constants.ROLE_NAME_GODFATHER, getRoleLocalName(context, roleName), true, RoleTypes.GODFATHER)
                Constants.ROLE_NAME_MAFIA -> Role(Constants.ROLE_NAME_MAFIA, getRoleLocalName(context, roleName), true, RoleTypes.MAFIA)
                Constants.ROLE_NAME_BOMBER -> Role(Constants.ROLE_NAME_BOMBER, getRoleLocalName(context, roleName), true, RoleTypes.BOMBER)
                Constants.ROLE_NAME_CITIZEN -> Role(Constants.ROLE_NAME_CITIZEN, getRoleLocalName(context, roleName), false, RoleTypes.CITIZEN)
                Constants.ROLE_NAME_DETECTIVE -> Role(Constants.ROLE_NAME_DETECTIVE, getRoleLocalName(context, roleName), false, RoleTypes.DETECTIVE)
                Constants.ROLE_NAME_DOCTOR -> Role(Constants.ROLE_NAME_DOCTOR, getRoleLocalName(context, roleName), false, RoleTypes.DOCTOR)
                Constants.ROLE_NAME_SNIPER -> Role(Constants.ROLE_NAME_SNIPER, getRoleLocalName(context, roleName), false, RoleTypes.SNIPER)
                Constants.ROLE_NAME_GUNNER -> Role(Constants.ROLE_NAME_GUNNER, getRoleLocalName(context, roleName), false, RoleTypes.GUNNER)
                Constants.ROLE_NAME_DETONATOR -> Role(Constants.ROLE_NAME_DETONATOR, getRoleLocalName(context, roleName), false, RoleTypes.DETONATOR)
                else -> throw Exception("Invalid player role name: $roleName")
            }
        }

        fun getRoleImageCardResource(roleName: String) : Int
        {
            return when(roleName)
            {
                Constants.ROLE_NAME_GODFATHER -> R.drawable.card_godfather
                Constants.ROLE_NAME_MAFIA -> R.drawable.card_mafia
                Constants.ROLE_NAME_BOMBER -> R.drawable.card_bomber
                Constants.ROLE_NAME_CITIZEN -> R.drawable.card_citizen
                Constants.ROLE_NAME_DETECTIVE -> R.drawable.card_detective
                Constants.ROLE_NAME_DOCTOR -> R.drawable.card_doctor
                Constants.ROLE_NAME_SNIPER -> R.drawable.card_sniper
                Constants.ROLE_NAME_GUNNER -> R.drawable.card_gunner
                Constants.ROLE_NAME_DETONATOR -> R.drawable.card_detonator
                else -> throw Exception("Invalid player role name: $roleName")
            }
        }
    }

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

        imageViewCard.setOnClickListener {
            if(!isCardEnable)
            {
                Helpers.playSoundEffect(this@AssignRoleCards, R.raw.event_bad)
                return@setOnClickListener
            }
            isCardEnable = false

            cardAnimator = ObjectAnimator.ofFloat(imageViewCard, "rotationY", 0f, 90f)
            cardAnimator.duration = 1000
            cardAnimator.start()
            cardAnimator.doOnEnd {
                assignNewRoleToCurrentPlayer()
            }
        }

        buttonRoleOK.setOnClickListener {
            Helpers.playSoundEffect(this, R.raw.button)
            players.add(Player(currentPlayerName, currentPlayerRole))
            if(!prepareNextRoleSelection())
            {
                val mainGameIntent = Intent(this@AssignRoleCards, MainGameActivity::class.java)
                mainGameIntent.putParcelableArrayListExtra(Constants.INTENT_PLAYERS_LIST, players)
                finish()
                startActivity(mainGameIntent)
            }
        }
    }

    private fun prepareNextRoleSelection() : Boolean
    {
        if(!playerNames.any())
            return false

        currentPlayerName = playerNames.shuffled().first()
        playerNames.remove(currentPlayerName)
        textViewPlayerName.text = currentPlayerName
        textViewPlayerRole.text = ""
        imageViewCard.setImageResource(R.drawable.card_question)
        buttonRoleOK.visibility = View.INVISIBLE
        isCardEnable = true

        return true
    }

    private fun assignNewRoleToCurrentPlayer()
    {
        roleNamesAndCount.shuffle()
        val pickedRole = roleNamesAndCount.find { it.count > 0 }!!
        currentPlayerRole = getRole(this, pickedRole.roleName)
        pickedRole.count--;
        if(pickedRole.count <= 0)
        {
            roleNamesAndCount.remove(roleNamesAndCount.find { it.roleName == pickedRole.roleName }!!)
        }
        textViewPlayerRole.text = getRoleLocalName(this, pickedRole.roleName)
        imageViewCard.setImageResource(getRoleImageCardResource(pickedRole.roleName))

        cardAnimator = ObjectAnimator.ofFloat(imageViewCard, "rotationY", 90f, 0f)
        cardAnimator.duration = 1000
        cardAnimator.start()
        cardAnimator.doOnEnd {
            buttonRoleOK.visibility = View.VISIBLE
        }
    }
}