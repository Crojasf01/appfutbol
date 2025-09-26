package com.example.appfutbol.data.model

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appfutbol.R
import com.example.appfutbol.data.model.Partido
import com.example.appfutbol.util.Constants

class DetallePartidoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_partido)

        val partido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Constants.EXTRA_PARTIDO, Partido::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Partido>(Constants.EXTRA_PARTIDO)
        }

        partido?.let {
            findViewById<TextView>(R.id.tvTitulo).text = it.titulo
            findViewById<TextView>(R.id.tvLugar).text = it.lugar
            findViewById<TextView>(R.id.tvFecha).text = it.fecha
            findViewById<TextView>(R.id.tvHora).text = it.hora
            //findViewById<TextView>(R.id.tvInscritos).text = "Inscritos: ${it.inscritos}"
        }
    }
}
