import com.example.appfutbol.data.model.Inscripcion

sealed class InscripcionItem {

    data class HeaderPartido(val dia: String, val hora: String) : InscripcionItem()
    data class item(val inscripcion: Inscripcion, val index: Int?) : InscripcionItem()
}
