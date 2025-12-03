import com.example.appfutbol.data.model.Inscripcion

sealed class InscripcionItem {

    // Header principal (día)
    data class HeaderDia(val dia: String, val string: String?) : InscripcionItem()

    // Header secundario (hora)
    data class HeaderHora(val hora: String) : InscripcionItem()

    data class Item(val inscripcion: Inscripcion, val index: Int?) : InscripcionItem()
}
