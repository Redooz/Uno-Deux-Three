package com.redoz.unodeuxthree.models

class Card {
    var color:String ?= null
    var type:String ?= null

    constructor(){}

    constructor(color: String?, type: String?) {
        this.color = color
        this.type = type
    }


}