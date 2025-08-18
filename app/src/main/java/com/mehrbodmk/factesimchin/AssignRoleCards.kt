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
import com.mehrbodmk.factesimchin.models.RoleTypeAndCount
import com.mehrbodmk.factesimchin.models.RoleTypes
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers

class AssignRoleCards : AppCompatActivity() {

    private var players: ArrayList<Player> = arrayListOf()

    private lateinit var playerNames: ArrayList<String>
    private lateinit var roleTypesAndCount: ArrayList<RoleTypeAndCount>

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
        const val ROLE_NAME_GODFATHER = "GodFather"
        const val ROLE_NAME_MAFIA = "Mafia"
        const val ROLE_NAME_NEGOTIATOR = "Mafia"
        const val ROLE_NAME_BOMBER = "Bomber"
        const val ROLE_NAME_CITIZEN = "Citizen"
        const val ROLE_NAME_DETECTIVE = "Detective"
        const val ROLE_NAME_DOCTOR = "Doctor"
        const val ROLE_NAME_SNIPER = "Sniper"
        const val ROLE_NAME_GUNNER = "Gunner"
        const val ROLE_NAME_HARDLIVING = "HardLiving"
        const val ROLE_NAME_DETONATOR = "Detonator"

        fun getRolesLocalNames(context: Context, roles: Iterable<RoleTypes>) : ArrayList<String>
        {
            val result: ArrayList<String> = arrayListOf()
            for(roleType in roles)
            {
                result.add(getRoleLocalName(context, roleType))
            }
            return result
        }

        fun getRoleLocalName(context: Context, role: RoleTypes) : String
        {
            return when (role) {
                RoleTypes.GODFATHER -> context.getString(R.string.role_godfather)
                RoleTypes.MAFIA -> context.getString(R.string.role_mafia)
                RoleTypes.BOMBER -> context.getString(R.string.role_bomber)
                RoleTypes.CITIZEN -> context.getString(R.string.role_citizen)
                RoleTypes.DETECTIVE -> context.getString(R.string.role_detective)
                RoleTypes.DOCTOR -> context.getString(R.string.role_doctor)
                RoleTypes.SNIPER -> context.getString(R.string.role_sniper)
                RoleTypes.GUNNER -> context.getString(R.string.role_gunner)
                RoleTypes.DETONATOR -> context.getString(R.string.role_detonator)
                RoleTypes.HARDLIVING -> context.getString(R.string.role_hardliving)
                RoleTypes.NEGOTIATOR -> context.getString(R.string.role_negotiator)
            }
        }

        fun getRoleName(context: Context, roleType: RoleTypes) : String {
            return when(roleType)
            {
                RoleTypes.GODFATHER -> ROLE_NAME_GODFATHER
                RoleTypes.MAFIA -> ROLE_NAME_MAFIA
                RoleTypes.BOMBER -> ROLE_NAME_BOMBER
                RoleTypes.CITIZEN -> ROLE_NAME_CITIZEN
                RoleTypes.DETECTIVE -> ROLE_NAME_DETECTIVE
                RoleTypes.DOCTOR -> ROLE_NAME_DOCTOR
                RoleTypes.SNIPER -> ROLE_NAME_SNIPER
                RoleTypes.GUNNER -> ROLE_NAME_GUNNER
                RoleTypes.DETONATOR -> ROLE_NAME_DETONATOR
                RoleTypes.HARDLIVING -> ROLE_NAME_HARDLIVING
                RoleTypes.NEGOTIATOR -> ROLE_NAME_NEGOTIATOR
            }
        }

        fun getRole(context: Context, roleType: RoleTypes) : Role
        {
            return when(roleType)
            {
                RoleTypes.GODFATHER -> Role(ROLE_NAME_GODFATHER, getRoleLocalName(context, roleType), true, RoleTypes.GODFATHER)
                RoleTypes.MAFIA     -> Role(ROLE_NAME_MAFIA, getRoleLocalName(context, roleType), true, RoleTypes.MAFIA)
                RoleTypes.BOMBER    -> Role(ROLE_NAME_BOMBER, getRoleLocalName(context, roleType), true, RoleTypes.BOMBER)
                RoleTypes.CITIZEN   -> Role(ROLE_NAME_CITIZEN, getRoleLocalName(context, roleType), false, RoleTypes.CITIZEN)
                RoleTypes.DETECTIVE -> Role(ROLE_NAME_DETECTIVE, getRoleLocalName(context, roleType), false, RoleTypes.DETECTIVE)
                RoleTypes.DOCTOR    -> Role(ROLE_NAME_DOCTOR, getRoleLocalName(context, roleType), false, RoleTypes.DOCTOR)
                RoleTypes.SNIPER    -> Role(ROLE_NAME_SNIPER, getRoleLocalName(context, roleType), false, RoleTypes.SNIPER)
                RoleTypes.GUNNER    -> Role(ROLE_NAME_GUNNER, getRoleLocalName(context, roleType), false, RoleTypes.GUNNER)
                RoleTypes.DETONATOR -> Role(ROLE_NAME_DETONATOR, getRoleLocalName(context, roleType), false, RoleTypes.DETONATOR)
                RoleTypes.HARDLIVING -> Role(ROLE_NAME_HARDLIVING, getRoleLocalName(context, roleType), false, RoleTypes.HARDLIVING)
                RoleTypes.NEGOTIATOR -> Role(ROLE_NAME_NEGOTIATOR, getRoleLocalName(context, roleType), true, RoleTypes.NEGOTIATOR)
            }
        }

        fun getRoleImageCardResource(roleType: RoleTypes) : Int
        {
            return when(roleType)
            {
                RoleTypes.GODFATHER   -> R.drawable.card_godfather
                RoleTypes.MAFIA       -> R.drawable.card_mafia
                RoleTypes.BOMBER      -> R.drawable.card_bomber
                RoleTypes.CITIZEN     -> R.drawable.card_citizen
                RoleTypes.DETECTIVE   -> R.drawable.card_detective
                RoleTypes.DOCTOR      -> R.drawable.card_doctor
                RoleTypes.SNIPER      -> R.drawable.card_sniper
                RoleTypes.GUNNER      -> R.drawable.card_gunner
                RoleTypes.DETONATOR   -> R.drawable.card_detonator
                RoleTypes.HARDLIVING -> R.drawable.card_hardliving
                RoleTypes.NEGOTIATOR -> R.drawable.card_negotiator
            }
        }

        fun getRoleImageThumbnailResource(role: RoleTypes) : Int {
            return when(role)
            {
                RoleTypes.GODFATHER -> R.drawable.role_godfather
                RoleTypes.MAFIA -> R.drawable.role_mafia
                RoleTypes.BOMBER -> R.drawable.role_bomber
                RoleTypes.CITIZEN -> R.drawable.role_citizen
                RoleTypes.DETECTIVE -> R.drawable.role_detective
                RoleTypes.DOCTOR -> R.drawable.role_doctor
                RoleTypes.SNIPER -> R.drawable.role_sniper
                RoleTypes.GUNNER -> R.drawable.role_gunner
                RoleTypes.DETONATOR -> R.drawable.role_detonator
                RoleTypes.HARDLIVING -> R.drawable.role_hardliving
                RoleTypes.NEGOTIATOR -> R.drawable.role_negotiator
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

        roleTypesAndCount = IntentCompat.getParcelableArrayListExtra(intent, Constants.INTENT_ROLE_NAMES_AND_COUNT_LIST, RoleTypeAndCount::class.java)!!
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
            cardAnimator.duration = 250
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

        currentPlayerName = playerNames.random()
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
        val pickedRole = roleTypesAndCount.filter { it.count > 0 }.shuffled().first()
        currentPlayerRole = getRole(this@AssignRoleCards, pickedRole.roleType)
        pickedRole.count--;
        if(pickedRole.count <= 0)
        {
            roleTypesAndCount.remove(roleTypesAndCount.find { it.roleType == pickedRole.roleType }!!)
        }
        textViewPlayerRole.text = getRoleLocalName(this, pickedRole.roleType)
        imageViewCard.setImageResource(getRoleImageCardResource(pickedRole.roleType))

        cardAnimator = ObjectAnimator.ofFloat(imageViewCard, "rotationY", 90f, 0f)
        cardAnimator.duration = 250
        cardAnimator.start()
        cardAnimator.doOnEnd {
            buttonRoleOK.visibility = View.VISIBLE
        }
    }
}