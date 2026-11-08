package com.example.contactsapp

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ContactAdapter(
    private val context: Context,
    private val contacts: List<Contact>
) : BaseAdapter() {

    override fun getCount(): Int = contacts.size
    override fun getItem(position: Int): Any = contacts[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // On réutilise la ligne si elle existe déjà, sinon on la crée
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_contact, parent, false)

        val contact = contacts[position]

        val imgPhoto = view.findViewById<ImageView>(R.id.imgPhoto)
        val tvNomPrenom = view.findViewById<TextView>(R.id.tvNomPrenom)
        val tvTelephone = view.findViewById<TextView>(R.id.tvTelephone)

        tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
        tvTelephone.text = contact.telephone

        if (contact.photoUri != null) {
            imgPhoto.setImageURI(Uri.parse(contact.photoUri))
        } else {
            imgPhoto.setImageResource(R.drawable.ic_person)
        }
        return view
    }
}