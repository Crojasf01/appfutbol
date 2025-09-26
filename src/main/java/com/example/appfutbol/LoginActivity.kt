package com.example.appfutbol

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiy_login)

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance("https://appbosque-6dfd8-default-rtdb.firebaseio.com/").reference
        auth = FirebaseAuth.getInstance()

        // Enlazar vistas
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPasswordButton = findViewById(R.id.forgotPassword)

        // Filtro para caracteres permitidos
        val filter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("[a-zA-Z0-9@#$&()/*._+-]+")
            if (source.isEmpty() || source.matches(regex)) source else ""
        }
        emailEditText.filters = arrayOf(filter)

        // Focus listener para cambiar borde
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            emailInputLayout.boxStrokeColor = if (hasFocus)
                ContextCompat.getColor(this, R.color.verde)
            else
                ContextCompat.getColor(this, R.color.white)
        }

        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            passwordInputLayout.boxStrokeColor = if (hasFocus)
                ContextCompat.getColor(this, R.color.verde)
            else
                ContextCompat.getColor(this, R.color.white)
        }

        // Bloquear enter
        emailEditText.setOnEditorActionListener { _, keycode, event ->
            keycode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
        }

        // Botón Login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        // Ir a registro
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // Ir a recuperar contraseña
        forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        val db = FirebaseFirestore.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val loginData = hashMapOf(
                            "uid" to it.uid,
                            "email" to it.email,
                            "timestamp" to Date()
                        )

                        db.collection("logins")
                            .add(loginData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error guardando login: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Sobrescribir dispatchTouchEvent para quitar foco al tocar afuera
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
