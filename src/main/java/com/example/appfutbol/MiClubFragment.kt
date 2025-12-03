package com.example.appfutbol

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appfutbol.data.model.Club
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.navigation.fragment.NavHostFragment

class MyClubFragment : Fragment() {

    private lateinit var logoImage: CircleImageView
    private lateinit var clubTitle: TextView
    private lateinit var gridIcon: ImageView
    private lateinit var fieldIcon: ImageView
    private lateinit var iconGrouping: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var adminIcon: ImageView

    private lateinit var clubDescription: TextView
    private lateinit var adminName: TextView
    private lateinit var adminEmail: TextView

    private lateinit var adminInfoLayout: LinearLayout
    private lateinit var subtitleAdmin: TextView
    private lateinit var subtitleInfo: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_myclub, container, false)

        // Image View boton back
        val botonBackFragment = view.findViewById<ImageButton>(R.id.botonBack_fragment)
        botonBackFragment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PartidosFragment())
                .commit()
        }

        // Enlaza con el layout
        clubDescription = view.findViewById(R.id.clubDescription)
        adminName = view.findViewById(R.id.adminName)
        adminEmail = view.findViewById(R.id.adminEmail)

        // Inicializar views
        logoImage = view.findViewById(R.id.logoImage)
        clubTitle = view.findViewById(R.id.clubTitle)
        adminIcon = view.findViewById(R.id.adminIcon)
        adminInfoLayout = view.findViewById(R.id.adminInfoLayout)

        gridIcon = view.findViewById(R.id.btnGrid)
        iconGrouping = view.findViewById(R.id.btnUser)
        //Icono de Pincel
        fieldIcon = view.findViewById(R.id.btnSettings)
        subtitleAdmin = view.findViewById(R.id.subtitleAdmin)
        //icono de borrar o salir del grupo
        settingsIcon = view.findViewById(R.id.btnFIeld) // Verifica que coincida con tu XML
        subtitleInfo = view.findViewById(R.id.subtitleInfo)

        // Apenas carga se abre el fragment - cargar el club por defecto
        cargarDatosClub("club_bosque")

        // Eventos
        logoImage.setOnClickListener {
            Toast.makeText(requireContext(), "Logo del club pulsado ⚽", Toast.LENGTH_SHORT).show()
        }

        gridIcon.setOnClickListener {
            mostrarClub()
        }

        // Icono del grupo de personas
        iconGrouping.setOnClickListener {
            val fragment = InscripcionesFragment()
            fragment.arguments = Bundle().apply {
                putString("tipo", "Inscripciones")
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            marcarIconoSeleccionado(iconGrouping)

        }

        // Click en icono de pencil para ir a fragment de inscripciones
        fieldIcon.setOnClickListener {
            mostrarPartidos()
            // Pintar el icono en verde
            marcarIconoSeleccionado(fieldIcon)
        }


        // Click en icono de basura para salir de la app
        settingsIcon.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Salir del club")
                .setMessage("¿Estás seguro que deseas salir del club?")
                .setPositiveButton("Salir") { dialog, _ ->
                    salirDelClub()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Forzar que apenas cargue se ejecute activarGrid()
        gridIcon.post {
            gridIcon.performClick()
        }

        return view
    }

    private fun salirDelClub() {
        AlertDialog.Builder(requireContext())
            .setTitle("Salir del club")
            .setMessage("Saliste del grupo")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun cargarDatosClub(clubId: String) {
        db.collection("club").document(clubId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MyClubFragment", "Error al escuchar cambios en el club", e)
                    Toast.makeText(requireContext(), "Error al obtener el club", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val club = snapshot.toObject(Club::class.java)
                    club?.let {
                        // Formatear la fecha
                        val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a", Locale.getDefault())
                        val fecha = it.fechaCreacion?.toDate()
                        val fechaFormateada = fecha?.let { f -> sdf.format(f) } ?: "Fecha no disponible"
                        clubDescription.text = "Club creado el $fechaFormateada"

                        // Mostrar admin
                        it.admin?.let { admin ->
                            adminName.text = if (admin.nombre.isNullOrEmpty()) "Sin nombre" else admin.nombre
                            adminEmail.text = if (admin.correo.isNullOrEmpty()) "Sin correo" else admin.correo
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Club no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun marcarIconoSeleccionado(icon: ImageView) {
        // Limpiar todos
        gridIcon.clearColorFilter()
        iconGrouping.clearColorFilter()
        fieldIcon.clearColorFilter()
        settingsIcon.clearColorFilter()

        // Pintar el seleccionado
        icon.setColorFilter(Color.parseColor("#4CAF50"), PorterDuff.Mode.SRC_IN)
    }

    private fun mostrarClub(){
        // Mostrar vistas principales
        logoImage.visibility = View.VISIBLE
        clubTitle.visibility = View.VISIBLE
        clubDescription.visibility = View.VISIBLE
        adminInfoLayout.visibility = View.VISIBLE
        adminName.visibility = View.VISIBLE
        adminEmail.visibility = View.VISIBLE
        adminIcon.visibility = View.VISIBLE
        subtitleInfo.visibility = View.VISIBLE
        clubDescription.visibility = View.VISIBLE

        // Ocultar el contenedor de partidos
        val contenedor = view?.findViewById<FrameLayout>(R.id.contenedorPartidos)
        contenedor?.visibility = View.GONE

        // Cargar datos del club
        cargarDatosClub("club_bosque")
        marcarIconoSeleccionado(gridIcon)
    }

    private fun mostrarPartidos(){
        // 1. Ocultar los elementos que no quieres ver mientras muestras la lista de partidos
        clubDescription.visibility = View.GONE
        adminInfoLayout.visibility = View.GONE
        subtitleAdmin.visibility = View.GONE
        subtitleInfo.visibility = View.GONE
        adminName.visibility = View.GONE
        adminEmail.visibility = View.GONE
        adminIcon.visibility = View.GONE

        // 2. Mostrar el contenedor de partidos
        val contenedor = view?.findViewById<FrameLayout>(R.id.contenedorPartidos)
        contenedor?.visibility = View.VISIBLE

        // 3. Cargar el fragment dentro del contenedor
        val fragment = ListadoMyClubPartidosFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.contenedorPartidos, fragment)
            .commit()
    }


}
