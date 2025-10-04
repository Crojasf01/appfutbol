package com.example.appfutbol.data.model

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appfutbol.R
import com.example.appfutbol.data.model.Partido
import com.example.appfutbol.util.Constants
import java.text.SimpleDateFormat
import java.util.Locale


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

            // Formateadores de fecha y hora en español
            val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale("ES"))
            val sdfHora = SimpleDateFormat("HH:mm", Locale("ES"))

            // Mostrar fecha formateada
            findViewById<TextView>(R.id.tvDia).text = it.fechaPartido?.toDate()?.let { fecha ->
                sdfFecha.format(fecha)
            } ?: "Sin fecha"
            //Mostrar fecha formateada
            findViewById<TextView>(R.id.tvMes).text = it.fechaPartido?.toDate()?.let { fecha ->
                sdfFecha.format(fecha)
            } ?: "Sin fecha"


            // Mostrar hora formateada
            findViewById<TextView>(R.id.tvHora).text = it.fechaPartido?.toDate()?.let { fecha ->
                sdfHora.format(fecha)
            } ?: "Sin hora"

            // Mostrar cupos / inscritos si lo tienes
        }

    }
}
