package com.example.appfutbol

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.room.Database
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.app.DatePickerDialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.CalendarConstraints
import java.text.SimpleDateFormat
import java.util.*


class RegistroActivity : AppCompatActivity() {
    private lateinit var nombreEditText: EditText
    private lateinit var apellidoPaternoEditText: EditText
    private lateinit var apellidoMaternoEditText: EditText
    private lateinit var fechaNacimientoEditText: EditText
    private lateinit var emailEditTextRegister: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var codigoSocioEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var puestoSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // tu XML de registro

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //mostrar el toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_white)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        //titulo de registrarse
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //inicializar las vistas
        nombreEditText = findViewById(R.id.nombre)
        apellidoPaternoEditText = findViewById(R.id.apellidoPaterno)
        apellidoMaternoEditText = findViewById(R.id.apellidoMaterno)
        fechaNacimientoEditText = findViewById(R.id.fechaNacimiento)
        emailEditTextRegister = findViewById(R.id.emailEditTextRegister)
        telefonoEditText = findViewById(R.id.telefonoEditText)
        passwordEditText = findViewById(R.id.contrasenaEditText)
        codigoSocioEditText = findViewById(R.id.codigodeSocio)
        registerButton = findViewById(R.id.registrarseButton)

        puestoSpinner = findViewById(R.id.spinnerPuesto)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appfutbol-132bc-default-rtdb.firebaseio.com/")

        //opciones del puesto
        val puestos = listOf(
            "Lateral derecho",
            "Lateral izquierdo",
            "Central",
            "Armador",
            "Puntero Derecho",
            "Puntero Izquierdo",
            "Delantero"
        )

        fechaNacimientoEditText.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccione su fecha de nacimiento")
                .setTheme(R.style.ThemeOverlay_DatePicker_WhiteButtons)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            // Evitar que ya exista un fragment con este tag
            val fragmentManager = supportFragmentManager
            val prev = fragmentManager.findFragmentByTag("MATERIAL_DATE_PICKER")
            if (prev != null) {
                (prev as DialogFragment).dismiss()
            }

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaNacimientoEditText.setText(formato.format(calendar.time))
            }

            datePicker.show(fragmentManager, "MATERIAL_DATE_PICKER")
        }

        // Adaptador personalizado para texto blanco en spinner
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            puestos
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(android.graphics.Color.WHITE) // texto del spinner visible
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(android.graphics.Color.WHITE) // texto del dropdown
                view.setBackgroundColor(android.graphics.Color.BLACK) // fondo negro en dropdown
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        puestoSpinner.adapter = adapter

        registerButton.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val apellidoPaterno = apellidoPaternoEditText.text.toString().trim()
            val apellidoMaterno = apellidoMaternoEditText.text.toString().trim()
            val fechaNacimiento = fechaNacimientoEditText.text.toString().trim()
            val codigoSocio = codigoSocioEditText.text.toString().trim()
            val email = emailEditTextRegister.text.toString().trim()
            val telefono = telefonoEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val puesto = puestoSpinner.selectedItem.toString()

            // Validaciones básicas
            if (nombre.isEmpty() || apellidoPaterno.isEmpty() || apellidoMaterno.isEmpty() ||
                fechaNacimiento.isEmpty() || codigoSocio.isEmpty() || email.isEmpty() ||
                telefono.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cuando presione Enter en el campo "nombre", pasar al campo correo
            nombreEditText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    nombreEditText.requestFocus() // mueve el foco al EditText de correo
                    true
                } else {
                    false
                }
            }

            // Nombre -> Apellido Paterno
            nombreEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    apellidoPaternoEditText.requestFocus()
                    true
                } else {
                    false
                }
            }

            // Apellido Paterno -> Apellido Materno
            apellidoPaternoEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    apellidoMaternoEditText.requestFocus()
                    true
                } else {
                    false
                }
            }

// Apellido Materno -> Fecha de nacimiento
            apellidoMaternoEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    fechaNacimientoEditText.performClick() // abrir calendario
                    true
                } else {
                    false
                }
            }

// Fecha de nacimiento -> Código de socio
            fechaNacimientoEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    codigoSocioEditText.requestFocus()
                    true
                } else {
                    false
                }
            }

// Código de socio -> Email
            codigoSocioEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    emailEditTextRegister.requestFocus()
                    true
                } else {
                    false
                }
            }

// Email -> Teléfono
            emailEditTextRegister.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    telefonoEditText.requestFocus()
                    true
                } else {
                    false
                }
            }

// Teléfono -> Contraseña
            telefonoEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    passwordEditText.requestFocus()
                    true
                } else {
                    false
                }
            }

            // Contraseña -> Botón Registrarse
            passwordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    registerButton.performClick()
                    true
                } else {
                    false
                }
            }


            // Llamar al método con todos los campos
            registrarUsuario(nombre, apellidoPaterno, apellidoMaterno, fechaNacimiento, codigoSocio, email, telefono, password, puesto)
        }
    }

    private fun registrarUsuario(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        fechaNacimiento: String,
        codigoSocio: String,
        email: String,
        telefono: String,
        password: String,
        puesto: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = mapOf(
                            "uid" to it.uid,
                            "nombre" to nombre,
                            "apellidoPaterno" to apellidoPaterno,
                            "apellidoMaterno" to apellidoMaterno,
                            "fechaNacimiento" to fechaNacimiento,
                            "codigoSocio" to codigoSocio,
                            "email" to it.email,
                            "telefono" to telefono,
                            "puesto" to puesto
                        )
                        database.reference.child("users").child(it.uid).setValue(userData)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    // Mostrar Toast inmediatamente
                                    Toast.makeText(
                                        this,
                                        "¡¡¡Registro exitoso!!!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Redirigir después de 2s para que se vea el Toast
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }, 500)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error guardando datos: ${dbTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    }

                }
            }
    }
}