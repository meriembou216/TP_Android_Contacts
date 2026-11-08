package com.example.contactsapp

import android.Manifest
import android.util.Patterns
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POSITION = "position"
        const val EXTRA_NOM = "nom"
        const val EXTRA_PRENOM = "prenom"
        const val EXTRA_TELEPHONE = "telephone"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_PHOTO = "photo"
    }

    private lateinit var imgPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText

    private var position = -1
    private var photoUri: String? = null

    // 🆕 Lanceur GALERIE : renvoie l'Uri de l'image choisie (ou null si annulé)
    private val galerieLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(this, "Aucune photo choisie", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        try {
            // On copie l'image dans le dossier privé de l'appli
            val fichier = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { entree ->
                fichier.outputStream().use { sortie -> entree.copyTo(sortie) }
            }
            photoUri = Uri.fromFile(fichier).toString()
            afficherPhoto()
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible de lire cette image", Toast.LENGTH_SHORT).show()
        }
    }

    // 🆕 Lanceur CAMÉRA : renvoie la photo prise (ou null si annulé)
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) {
            Toast.makeText(this, "Photo annulée", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        // On enregistre la photo dans un fichier JPG
        val fichier = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
        fichier.outputStream().use { sortie ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, sortie)
        }
        photoUri = Uri.fromFile(fichier).toString()
        afficherPhoto()
    }

    // 🆕 Lanceur PERMISSION : demande l'autorisation d'utiliser la caméra
    private val permissionCameraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee: Boolean ->
        if (accordee) {
            lancerCamera()
        } else {
            Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        imgPhoto = findViewById(R.id.imgPhoto)
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etTelephone = findViewById(R.id.etTelephone)
        etEmail = findViewById(R.id.etEmail)

        // On récupère les données envoyées par MainActivity
        position = intent.getIntExtra(EXTRA_POSITION, -1)
        etNom.setText(intent.getStringExtra(EXTRA_NOM))
        etPrenom.setText(intent.getStringExtra(EXTRA_PRENOM))
        etTelephone.setText(intent.getStringExtra(EXTRA_TELEPHONE))
        etEmail.setText(intent.getStringExtra(EXTRA_EMAIL))
        photoUri = intent.getStringExtra(EXTRA_PHOTO)
        afficherPhoto()

        findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }

        // 🆕 Boutons photo
        findViewById<Button>(R.id.btnGalerie).setOnClickListener { choisirPhoto() }
        findViewById<Button>(R.id.btnCamera).setOnClickListener { prendrePhoto() }
        // Bouton Enregistrer
        findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        // Bouton Annuler : retour à la liste sans rien renvoyer
        findViewById<Button>(R.id.btnAnnuler).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // 🆕 Ouvre la galerie (seulement les images)
    private fun choisirPhoto() {
        try {
            galerieLauncher.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Aucune application galerie trouvée", Toast.LENGTH_LONG).show()
        }
    }

    // 🆕 Vérifie la caméra et la permission avant de l'ouvrir
    private fun prendrePhoto() {
        // 1) Le téléphone a-t-il une caméra ?
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "Aucune caméra disponible", Toast.LENGTH_LONG).show()
            return
        }
        // 2) A-t-on déjà la permission ?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            lancerCamera()
        } else {
            permissionCameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun lancerCamera() {
        try {
            cameraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Aucune application caméra trouvée", Toast.LENGTH_LONG).show()
        }
    }

    private fun appeler() {
        val numero = etTelephone.text.toString().trim()
        if (numero.isEmpty()) {
            etTelephone.error = "Numéro obligatoire"
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Aucune application de téléphone trouvée", Toast.LENGTH_LONG).show()
        }
    }

    private fun envoyerEmail() {
        val adresse = etEmail.text.toString().trim()
        if (adresse.isEmpty()) {
            etEmail.error = "E-mail obligatoire"
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$adresse"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Aucune application de messagerie trouvée", Toast.LENGTH_LONG).show()
        }
    }
    // Vérifie les champs puis renvoie les données modifiées à MainActivity
    private fun enregistrer() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // 1) Vérification des champs obligatoires
        var valide = true
        if (nom.isEmpty()) {
            etNom.error = "Nom obligatoire"
            valide = false
        }
        if (prenom.isEmpty()) {
            etPrenom.error = "Prénom obligatoire"
            valide = false
        }
        if (telephone.isEmpty()) {
            etTelephone.error = "Téléphone obligatoire"
            valide = false
        }
        if (email.isEmpty()) {
            etEmail.error = "E-mail obligatoire"
            valide = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "E-mail invalide"
            valide = false
        }
        if (!valide) {
            Toast.makeText(this, "Veuillez corriger les champs en rouge", Toast.LENGTH_SHORT).show()
            return   // on reste sur l'écran, rien n'est envoyé
        }

        // 2) On prépare la réponse
        val resultat = Intent()
        resultat.putExtra(EXTRA_POSITION, position)
        resultat.putExtra(EXTRA_NOM, nom)
        resultat.putExtra(EXTRA_PRENOM, prenom)
        resultat.putExtra(EXTRA_TELEPHONE, telephone)
        resultat.putExtra(EXTRA_EMAIL, email)
        resultat.putExtra(EXTRA_PHOTO, photoUri)

        // 3) On renvoie la réponse et on ferme l'écran
        setResult(RESULT_OK, resultat)
        finish()
    }
    private fun afficherPhoto() {
        if (photoUri != null) {
            imgPhoto.setImageURI(Uri.parse(photoUri))
        } else {
            imgPhoto.setImageResource(R.drawable.ic_person)
        }
    }
}