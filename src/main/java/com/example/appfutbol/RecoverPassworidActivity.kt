package com.example.appfutbol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.Dispatchers


class RecoverPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperarcontra)

        val emailInput: EditText = findViewById(R.id.email_input)
        val requestCodeButton: Button = findViewById(R.id.request_code_button)

        val toolbar = findViewById<Toolbar>(R.id.toolbarRecover)
        setSupportActionBar(toolbar)

        // Opcional: activar botón atrás
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.btn_back)
        supportActionBar?.title = "Recuperar contraseña"

        //Handle back button click
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        requestCodeButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (isValidEmail(email)) {
                requestPasswordRecoveryCode(email)
            } else {
                Toast.makeText(this, "Por favor, introduce un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun requestPasswordRecoveryCode(email: String) {
        // Aquí, implementa la lógica para solicitar el código de recuperación
        // Por ejemplo, una llamada a un API.
        Toast.makeText(this, "Código de recuperación enviado a $email", Toast.LENGTH_SHORT).show()
    }
}