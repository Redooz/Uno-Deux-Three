package com.redoz.unodeuxthree

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
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

    var receiverRoom: String? = null
    var senderRoom: String? = null
    var senderUid: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState:  Bundle?) {
        super.onCreate(savedInstanceState)

        val rivalName = intent.getStringExtra("currentUserName")
        val receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val randomUID = UUID.randomUUID()

        senderRoom = receiverUid + senderUid + randomUID
        receiverRoom = senderUid + receiverUid + randomUID

        mDbRef = FirebaseDatabase.getInstance().reference

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userButtonCards = mutableListOf()
        userCards = mutableMapOf()
        gameCards = mutableListOf()

        binding.topTitle.text = rivalName.toString()
        mixCards(receiverUid as String)

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
                    val color = when (card.color?.lowercase()) {
                        "red" -> {
                            R.color.red
                        }
                        "blue" -> {
                            R.color.blue
                        }
                        "yellow" -> {
                            R.color.yellow
                        }
                        "green" -> {
                            R.color.green
                        }
                        else -> {
                            R.color.black
                        }
                    }
                    binding.card.setBackgroundColor(ContextCompat.getColor(this@GameActivity, color))

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun mixCards(uid: String) {
        val colors = listOf("red", "blue", "yellow", "green", "special")
        val regularType = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+2", "stop")
        val specialType = listOf("Change Color", "+4")

        for (i in 0 until 5) {
            val buttonCard = Button(this)
            val color = colors[Random.nextInt(0, colors.size)]

            val type: String = if (color != "special") {
                regularType[Random.nextInt(0, regularType.size)]
            } else {
                specialType[Random.nextInt(0, specialType.size)]
            }

            val newCard = Card(uid, color, type)

            buttonCard.id = i

            userButtonCards.add(buttonCard)
            userCards.put(i, newCard)

            applyStyleToCardButton(buttonCard, newCard)
            binding.cardsChooser.addView(buttonCard)

            // Adding cards to database
            buttonCard.setOnClickListener { sendCard(buttonCard) }
        }
    }

    private fun sendCard(buttonCard: Button) {
        senderRoom?.let { mDbRef.child("games").child(it).child("cards").push()
            .setValue(userCards[buttonCard.id]).addOnSuccessListener {
                receiverRoom?.let { it1 -> mDbRef.child("games").child(it1).child("cards").push()
                    .setValue(userCards[buttonCard.id])}
            }}
    }

    private fun addNewCard(uid:String, color: String, type: String) {
        val newCard = Card(uid,color, type)
        val newButtonCard = Button(this)

        applyStyleToCardButton(newButtonCard, newCard)
        userButtonCards.add(newButtonCard)
        userCards.put(userButtonCards.size + 1, newCard)

        newButtonCard.setOnClickListener { sendCard(newButtonCard) }
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