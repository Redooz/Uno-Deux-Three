package com.redoz.unodeuxthree

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.redoz.unodeuxthree.databinding.ActivityGameBinding
import com.redoz.unodeuxthree.models.Card
import java.util.*
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var userButtonCards: MutableList<Button>
    private lateinit var userCards: MutableMap<Int, Card>
    private lateinit var gameCards: MutableList<Card>
    private lateinit var mDbRef: DatabaseReference


    private val colors = listOf("red", "blue", "yellow", "green", "special")
    private val regularType = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+2", "stop")
    private val specialType = "+4"
    private var receiverRoom: String? = null
    private var senderRoom: String? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private var lastSender: String? = null
    private var numCards = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) = try {
        super.onCreate(savedInstanceState)

        val rivalName = intent.getStringExtra("currentUserName")
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        mDbRef = FirebaseDatabase.getInstance().reference

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userButtonCards = mutableListOf()
        userCards = mutableMapOf()
        gameCards = mutableListOf()

        binding.topTitle.text = rivalName.toString()
        mixCards(senderUid as String)
        binding.topNumCards.text = userCards.size.toString()

        lastSender = receiverUid

        mDbRef.child("games").child(senderRoom!!).child("cards")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var card = Card()
                    gameCards.clear()

                    for (postSnapshot in snapshot.children) {
                        card = postSnapshot.getValue(Card::class.java)!!
                        gameCards.add(card)
                    }

                    binding.card.text = card.type

                    val colorForButton = getColor(card.color)
                    binding.card.setBackgroundColor(
                        ContextCompat.getColor(
                            this@GameActivity,
                            colorForButton
                        )
                    )

                    lastSender = card.senderUid

                    if (lastSender != senderUid) { // Checking if the turn is for the sender, if it isn't, the cards will be disabled
                        binding.bottomContainer.visibility = View.VISIBLE
                    } else {
                        binding.bottomContainer.visibility = View.GONE
                    }

                    if (card.type == "+4") {
                        addFourCardsToChooser()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        binding.newCardBtn.setOnClickListener {
            val color: String = colors[Random.nextInt(0, colors.size)]

            val type: String = if (color != "special") {
                regularType[Random.nextInt(0, regularType.size)]
            } else {
                specialType
            }
            addNewCardToChooser(senderUid as String, color, type)
        }
    } catch (ex: Exception) {
        println(ex)
    }

    private fun addFourCardsToChooser() {
        for (i in 0 until 4) {
            val color = colors[Random.nextInt(0, colors.size)]
            val type = if (color != "special") {
                regularType[Random.nextInt(0, regularType.size)]
            } else {
                specialType
            }
            addNewCardToChooser(receiverUid!!, color, type)
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        mDbRef.child("games").child(senderRoom!!).child("cards").removeValue()
    }

    private fun getColor(color: String?): Int {
        return when (color?.lowercase()) {
            "red" -> R.color.red
            "blue" -> R.color.blue
            "yellow" -> R.color.yellow
            "green" -> R.color.green
            else -> R.color.black
        }
    }

    private fun mixCards(uid: String) {
        for (i in 0 until 5) {
            val buttonCard = Button(this)
            val color = colors[Random.nextInt(0, colors.size)]

            val type: String = if (color != "special") {
                regularType[Random.nextInt(0, regularType.size)]
            } else {
                specialType
            }

            val newCard = Card(uid, color, type)

            val id = View.generateViewId()

            buttonCard.id = id

            userButtonCards.add(buttonCard)
            userCards.put(id, newCard)

            applyStyleToCardButton(buttonCard, newCard)
            binding.cardsChooser.addView(buttonCard)

            // Adding cards to database
            buttonCard.setOnClickListener { sendCard(buttonCard) }
            numCards = i
        }
    }

    private fun canSendCard(newCard: Card?): Boolean {
        val lastCard = if (gameCards.isNotEmpty()) {
            gameCards.last()
        } else {
            return true
        }

        return lastCard.isCompatibleWith(newCard)
    }

    private fun sendCard(buttonCard: Button) {
        val newCard = userCards[buttonCard.id]
        if (canSendCard(newCard)) {
            senderRoom?.let { senderRoom ->
                sendCardToDatabase(senderRoom, buttonCard)
            }

            removeCardFromChooser(buttonCard)

        } else {
            Toast.makeText(this@GameActivity, "That card isn't compatible", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun sendCardToDatabase(senderRoom: String, buttonCard: Button) {
        // push the card to the sender's room in the database
        val senderCardRef = mDbRef.child("games").child(senderRoom).child("cards").push()
        val senderCard = userCards[buttonCard.id]
        senderCardRef.setValue(senderCard).addOnSuccessListener {
            // check if receiver room is not null using safe call operator
            receiverRoom?.let { receiverRoom ->
                // push the card to the receiver's room in the database
                val receiverCardRef =
                    mDbRef.child("games").child(receiverRoom).child("cards").push()
                receiverCardRef.setValue(senderCard)
            }
        }
    }

    private fun removeCardFromChooser(buttonCard: Button) {
        userButtonCards.remove(buttonCard)
        userCards.remove(buttonCard.id)

        binding.cardsChooser.removeView(buttonCard)
        binding.topNumCards.text = userCards.size.toString()
    }

    private fun addNewCardToChooser(uid: String, color: String, type: String) {
        val buttonCard = Button(this)
        val newCard = Card(uid, color, type)

        val id = View.generateViewId()
        buttonCard.id = id

        userButtonCards.add(buttonCard)

        userCards[id] = newCard

        applyStyleToCardButton(buttonCard, newCard)

        binding.cardsChooser.addView(buttonCard)
        buttonCard.setOnClickListener { sendCard(buttonCard) }

        binding.topNumCards.text = userCards.size.toString()
    }

    private fun applyStyleToCardButton(card: Button, newCard: Card) {
        card.apply {
            this.text = newCard.type
            this.width = convertDpToPixel(96.toFloat(), this@GameActivity).toInt()
            this.height = convertDpToPixel(74.toFloat(), this@GameActivity).toInt()
            this.setTextColor(ContextCompat.getColorStateList(context, R.color.white))
            this.backgroundTintList = when (newCard.color?.lowercase()) {
                "red" -> {
                    ContextCompat.getColorStateList(context, R.color.red)
                }
                "blue" -> {
                    ContextCompat.getColorStateList(context, R.color.blue)
                }
                "yellow" -> {
                    ContextCompat.getColorStateList(context, R.color.yellow)
                }
                "green" -> {
                    ContextCompat.getColorStateList(context, R.color.green)
                }
                else -> {
                    ContextCompat.getColorStateList(context, R.color.black)
                }
            }
        }
    }

    private fun convertDpToPixel(dp: Float, context: Context?): Float {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}
