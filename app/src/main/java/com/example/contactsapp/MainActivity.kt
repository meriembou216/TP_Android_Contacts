package com.example.contactsapp

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val contacts = mutableListOf(
        Contact("Dupont", "Claire", "06 12 34 56 78", "claire.dupont@example.com"),
        Contact("Martin", "Thomas", "07 23 45 67 89", "thomas.martin@example.com"),
        Contact("Bernard", "Sophie", "06 98 76 54 32", "sophie.bernard@example.com"),
        Contact("Petit", "Lucas", "07 11 22 33 44", "lucas.petit@example.com"),
        Contact("Moreau", "Julien", "06 55 44 33 22", "julien.moreau@example.com")
    )

    private lateinit var adapter: ContactAdapter

    // Le "lanceur" qui ouvre DetailActivity ET reçoit sa réponse
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val position = data.getIntExtra(DetailActivity.EXTRA_POSITION, -1)

            if (position in contacts.indices) {
                // On met à jour le contact dans la liste
                val contact = contacts[position]
                contact.nom = data.getStringExtra(DetailActivity.EXTRA_NOM) ?: contact.nom
                contact.prenom = data.getStringExtra(DetailActivity.EXTRA_PRENOM) ?: contact.prenom
                contact.telephone = data.getStringExtra(DetailActivity.EXTRA_TELEPHONE) ?: contact.telephone
                contact.email = data.getStringExtra(DetailActivity.EXTRA_EMAIL) ?: contact.email
                contact.photoUri = data.getStringExtra(DetailActivity.EXTRA_PHOTO)

                // On rafraîchit immédiatement la ListView
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Contact enregistré", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Modifications annulées", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.listContacts)
        adapter = ContactAdapter(this, contacts)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val contact = contacts[position]

            // Intent EXPLICITE : on dit exactement quelle activité ouvrir
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_POSITION, position)
            intent.putExtra(DetailActivity.EXTRA_NOM, contact.nom)
            intent.putExtra(DetailActivity.EXTRA_PRENOM, contact.prenom)
            intent.putExtra(DetailActivity.EXTRA_TELEPHONE, contact.telephone)
            intent.putExtra(DetailActivity.EXTRA_EMAIL, contact.email)
            intent.putExtra(DetailActivity.EXTRA_PHOTO, contact.photoUri)

            detailLauncher.launch(intent)
        }
    }
}