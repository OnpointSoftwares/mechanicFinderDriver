package com.example.mechanicfinder

class User{

    lateinit var name:String
    lateinit var email:String
    lateinit var password:String
    lateinit var longitude:String
    lateinit var latitude:String

    constructor(name:String,email:String,password:String,longitude:String,latitude:String){
        this.name=name
        this.email=email
        this.latitude=latitude
        this.longitude=longitude
        this.password=password
    }
}