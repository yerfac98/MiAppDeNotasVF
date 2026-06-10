package com.example.miappdenotas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var buttonGoogleLogin: Button
    private lateinit var buttonSkipLogin: Button
    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión iniciada correctamente", Toast.LENGTH_SHORT).show()
                        openMainActivity()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al iniciar sesión: ${it.message}", Toast.LENGTH_LONG).show()
                    }

            } catch (e: Exception) {
                Toast.makeText(this, "Error con Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            openMainActivity()
            return
        }

        buttonGoogleLogin = findViewById(R.id.button_google_login)
        buttonSkipLogin = findViewById(R.id.button_skip_login)

        buttonGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        buttonSkipLogin.setOnClickListener {
            openMainActivity()
        }
    }

    private fun signInWithGoogle() {
        val webClientId = "678714401329-tah261lf1ut7boel2r4a4sgdih786imc.apps.googleusercontent.com"

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}