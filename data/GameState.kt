package com.example.mycafe.data

data class GameState(
    val personaje: Personaje = Personaje(),
    val dinero: Int = 100,
    val nivel: Int = 1,
    val experiencia: Int = 0,
    val clientesServidos: Int = 0,
    val dia: Int = 1,
    val hora: Int = 8, // 8 AM - 10 PM (8-22)
    val cafeUpgrades: CafeUpgrades = CafeUpgrades(),
    val casaUpgrades: CasaUpgrades = CasaUpgrades(),
    val inventario: Inventario = Inventario(),
    val logros: List<String> = emptyList()
) {
    fun calcularExperienciaParaSiguienteNivel(): Int {
        return nivel * 50 // Cada nivel necesita más XP
    }

    fun puedeSubirNivel(): Boolean {
        return experiencia >= calcularExperienciaParaSiguienteNivel()
    }

    fun subirNivel(): GameState {
        return if (puedeSubirNivel()) {
            copy(
                nivel = nivel + 1,
                experiencia = experiencia - calcularExperienciaParaSiguienteNivel()
            )
        } else this
    }

    fun esHoraTrabajo(): Boolean {
        return hora in 8..20 // 8 AM a 8 PM
    }

    fun avanzarHora(): GameState {
        val nuevaHora = if (hora >= 22) 8 else hora + 1
        val nuevoDia = if (hora >= 22) dia + 1 else dia
        return copy(hora = nuevaHora, dia = nuevoDia)
    }
}

data class CafeUpgrades(
    val maquinaCalidad: Int = 1, // 1-5, mejora calidad y precio
    val velocidadPreparacion: Int = 1, // 1-5, reduce tiempo
    val capacidadClientes: Int = 3, // Máximo de clientes simultáneos
    val decoracion: Int = 1, // Atrae más clientes
    val menuExpandido: Boolean = false // Desbloquea más productos
)

data class CasaUpgrades(
    val camaCalidad: Int = 1, // Mejora recuperación de energía
    val cocina: Int = 1, // Permite cocinar en casa
    val entretenimiento: Int = 1, // TV, videojuegos para diversión
    val bano: Int = 1 // Mejora higiene
)

data class Inventario(
    val ingredientesBasicos: Int = 20, // Café, leche, azúcar
    val ingredientesPremium: Int = 5, // Chocolate, vainilla, canela
    val pasteles: Int = 3,
    val galletas: Int = 5
) {
    fun puedePreparar(pasos: List<String>): Boolean {
        return when {
            pasos.contains("🍫") && ingredientesPremium < 1 -> false
            pasos.contains("🎂") && pasteles < 1 -> false
            pasos.contains("🍪") && galletas < 1 -> false
            pasos.any { it in listOf("☕", "🥛", "🫖") } && ingredientesBasicos < 1 -> false
            else -> true
        }
    }

    fun consumirIngredientes(pasos: List<String>): Inventario {
        var nuevoBasicos = ingredientesBasicos
        var nuevoPremium = ingredientesPremium
        var nuevosPasteles = pasteles
        var nuevasGalletas = galletas

        pasos.forEach { paso ->
            when (paso) {
                "☕", "🥛", "🫖" -> nuevoBasicos = (nuevoBasicos - 1).coerceAtLeast(0)
                "🍫" -> nuevoPremium = (nuevoPremium - 1).coerceAtLeast(0)
                "🎂" -> nuevosPasteles = (nuevosPasteles - 1).coerceAtLeast(0)
                "🍪" -> nuevasGalletas = (nuevasGalletas - 1).coerceAtLeast(0)
            }
        }

        return copy(
            ingredientesBasicos = nuevoBasicos,
            ingredientesPremium = nuevoPremium,
            pasteles = nuevosPasteles,
            galletas = nuevasGalletas
        )
    }
}

// Necesidades como Los Sims
data class Necesidades(
    val hambre: Float = 100f,
    val sueño: Float = 100f,
    val higiene: Float = 100f,
    val diversion: Float = 100f,
    val social: Float = 100f
) {
    fun calcularEstadoGeneral(): Float {
        return (hambre + sueño + higiene + diversion + social) / 5f
    }

    fun necesidadCritica(): String? {
        return when {
            hambre < 20f -> "¡Tienes mucha hambre!"
            sueño < 20f -> "¡Necesitas dormir!"
            higiene < 20f -> "¡Necesitas bañarte!"
            diversion < 20f -> "¡Necesitas divertirte!"
            social < 20f -> "¡Necesitas socializar!"
            else -> null
        }
    }
}

object GameLogic {
    fun calcularEficiencia(personaje: Personaje, necesidades: Necesidades): Float {
        val energiaFactor = personaje.energia / 100f
        val necesidadesFactor = necesidades.calcularEstadoGeneral() / 100f
        return ((energiaFactor * 0.6f) + (necesidadesFactor * 0.4f)).coerceAtLeast(0.1f)
    }

    fun calcularPropina(tiempoServicio: Long, cliente: Cliente): Int {
        return when (cliente.tipoCliente) {
            TipoCliente.GENEROSO -> {
                if (tiempoServicio < 3000) cliente.precio / 2 else 0
            }
            TipoCliente.VIP -> {
                if (tiempoServicio < 5000) cliente.precio / 3 else 0
            }
            else -> 0
        }
    }
}