package com.example.contactsapp

data class Contact(var nom: String,
                   var prenom: String,
                   var telephone: String,
                   var email: String,
                   var photoUri: String? = null )
