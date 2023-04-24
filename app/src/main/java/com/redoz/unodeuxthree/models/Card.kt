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


}