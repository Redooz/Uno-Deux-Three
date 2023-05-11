package com.redoz.unodeuxthree.models

class Card {
    var senderUid:String ?= null
    var color:String ?= null
    var type:String ?= null

    constructor(){}
    constructor(senderId: String?, color: String?, type: String?) {
        this.senderUid = senderId
        this.color = color
        this.type = type
    }

    override fun toString(): String {
        return "$type $color"
    }

    fun isCompatibleWith(card: Card?): Boolean {
        val specialType = listOf("Change Color", "+4")

        if (specialType.contains(card!!.type) || specialType.contains(this.type)) {
            return true
        }

        if (card.color == this.color || card.type == this.type) {
            return true
        }

        return false
    }

}