package com.example.mycafe.data

data class Cliente(
    val id: Int,
    val pedido: String,
    val emoji: String,
    val pasos: List<String>,
    var pasoActual: Int = 0,
    var paciencia: Float = 100f,
    val precio: Int,
    val tipoCliente: TipoCliente = TipoCliente.NORMAL
)

enum class TipoCliente {
    NORMAL,      // Cliente regular
    IMPACIENTE,  // Pierde paciencia más rápido, paga más
    GENEROSO,    // Paga propina si lo atiendes bien
    VIP          // Cliente especial con pedidos complejos
}

object ClienteFactory {
    private val tiposPedido = listOf(
        // Bebidas básicas
        Triple("Café Solo", "🙋‍♂️", listOf("☕") to 8),
        Triple("Café con Leche", "🙋‍♀️", listOf("☕", "🥛") to 12),
        Triple("Cappuccino", "👨‍💼", listOf("☕", "🥛", "🫧") to 18),
        Triple("Té", "👩‍🎓", listOf("🫖") to 6),
        Triple("Chocolate Caliente", "🧒", listOf("🍫", "🥛") to 14),

        // Postres (nuevos!)
        Triple("Cupcake", "👩‍🍳", listOf("🧁", "🎂") to 20),
        Triple("Galletas", "👨‍🌾", listOf("🍪") to 10),
        Triple("Pastel de Chocolate", "💼", listOf("🍫", "🎂", "🍰") to 35),
        Triple("Donut", "🎯", listOf("🍩") to 12),

        // Combos
        Triple("Desayuno Completo", "🌅", listOf("☕", "🥐", "🧈") to 25),
        Triple("Merienda Dulce", "🍽️", listOf("🫖", "🧁", "🍪") to 28)
    )

    fun crearClienteAleatorio(id: Int): Cliente {
        val pedidoData = tiposPedido.random()
        val tipoCliente = when ((1..100).random()) {
            in 1..70 -> TipoCliente.NORMAL
            in 71..85 -> TipoCliente.IMPACIENTE
            in 86..95 -> TipoCliente.GENEROSO
            else -> TipoCliente.VIP
        }

        val multiplicadorPrecio = when (tipoCliente) {
            TipoCliente.IMPACIENTE -> 1.3f
            TipoCliente.GENEROSO -> 1.1f
            TipoCliente.VIP -> 1.5f
            else -> 1.0f
        }

        return Cliente(
            id = id,
            pedido = pedidoData.first,
            emoji = ajustarEmojiPorTipo(pedidoData.second, tipoCliente),
            pasos = pedidoData.third.first,
            precio = (pedidoData.third.second * multiplicadorPrecio).toInt(),
            tipoCliente = tipoCliente
        )
    }

    private fun ajustarEmojiPorTipo(emojiBase: String, tipo: TipoCliente): String {
        return when (tipo) {
            TipoCliente.IMPACIENTE -> "😤"
            TipoCliente.GENEROSO -> "😊"
            TipoCliente.VIP -> "👑"
            else -> emojiBase
        }
    }
}