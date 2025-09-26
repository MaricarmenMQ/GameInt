package com.example.mycafe.data

data class Personaje(
    var energia: Float = 100f,    // Solo energia para simplificar
    var humor: String = "😊",
    var estado: String = "Descansado"
) {
    fun actualizarEstado() {
        when {
            energia > 80f -> {
                humor = "😊"
                estado = "Descansado"
            }
            energia > 50f -> {
                humor = "🙂"
                estado = "Bien"
            }
            energia > 30f -> {
                humor = "😐"
                estado = "Cansado"
            }
            else -> {
                humor = "😫"
                estado = "Agotado"
            }
        }
    }
}