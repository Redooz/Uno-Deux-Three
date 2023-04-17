package com.redoz.unodeuxthree

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import androidx.core.content.ContextCompat
import com.redoz.unodeuxthree.databinding.ActivityGameBinding
import com.redoz.unodeuxthree.models.Card
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var userButtonCards: MutableList<Button>
    private lateinit var userCards: MutableMap<Int, Card>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userButtonCards = mutableListOf()
        userCards = mutableMapOf()

        mixCards()
    }

    private fun mixCards() {
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

            val newCard = Card(color, type)

            buttonCard.id = i

            userButtonCards.add(buttonCard)
            userCards.put(i, newCard)

            applyStyleToCardButton(buttonCard, newCard)
            binding.cardsChooser.addView(buttonCard)
        }
    }

    private fun addNewCard(color: String, type: String) {
        val newCard = Card(color, type)
        val newButtonCard = Button(this)

        applyStyleToCardButton(newButtonCard, newCard)
        userButtonCards.add(newButtonCard)
        userCards.put(userButtonCards.size + 1, newCard)
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