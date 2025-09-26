package com.example.mycafe.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycafe.data.Personaje
import com.example.mycafe.data.Necesidades
import com.example.mycafe.data.GameState
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun CasaScreen(
    gameState: GameState,
    necesidades: Necesidades,
    onGameStateChanged: (GameState) -> Unit,
    onNecesidadesChanged: (Necesidades) -> Unit,
    onNotificacion: (String) -> Unit
) {
    // Animaciones para el personaje
    val personajeRotacion by animateFloatAsState(
        targetValue = when {
            necesidades.calcularEstadoGeneral() > 80f -> 0f
            necesidades.calcularEstadoGeneral() > 50f -> 5f
            else -> 10f
        },
        animationSpec = tween(1000)
    )

    val personajeEscala by animateFloatAsState(
        targetValue = when {
            necesidades.calcularEstadoGeneral() > 70f -> 1.0f
            else -> 0.9f
        },
        animationSpec = tween(500)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título con hora del día
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    gameState.hora < 12 -> "🌅 Buenos días"
                    gameState.hora < 18 -> "☀️ Buenas tardes"
                    else -> "🌙 Buenas noches"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${gameState.hora}:00 - Día ${gameState.dia}",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personaje animado
        Text(
            text = gameState.personaje.humor,
            fontSize = 80.sp,
            modifier = Modifier
                .rotate(personajeRotacion)
                .scale(personajeEscala)
        )

        Text(
            text = "Estado: ${gameState.personaje.estado}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        // Barras de necesidades estilo Sims
        NecesidadesPanel(necesidades)

        Spacer(modifier = Modifier.height(20.dp))

        // Actividades organizadas por tipo
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dormir
            item {
                ActividadCard(
                    icono = "🛏️",
                    titulo = "Dormir",
                    descripcion = "8 horas de sueño",
                    beneficios = listOf("+80 Sueño", "+30 Energía"),
                    tiempo = "8h",
                    costo = 0,
                    puedeUsar = gameState.hora >= 22 || gameState.hora <= 6,
                    efecto = EfectoActividad.DORMIR,
                    onClick = { efecto ->
                        realizarActividad(
                            efecto = efecto,
                            gameState = gameState,
                            necesidades = necesidades,
                            onGameStateChanged = onGameStateChanged,
                            onNecesidadesChanged = onNecesidadesChanged,
                            onNotificacion = onNotificacion
                        )
                    }
                )
            }

            // Bañarse
            item {
                ActividadCard(
                    icono = "🚿",
                    titulo = "Ducharse",
                    descripcion = "Higiene personal",
                    beneficios = listOf("+60 Higiene", "+10 Energía"),
                    tiempo = "30min",
                    costo = 0,
                    puedeUsar = necesidades.higiene < 90f,
                    efecto = EfectoActividad.DUCHA,
                    onClick = { efecto ->
                        realizarActividad(
                            efecto = efecto,
                            gameState = gameState,
                            necesidades = necesidades,
                            onGameStateChanged = onGameStateChanged,
                            onNecesidadesChanged = onNecesidadesChanged,
                            onNotificacion = onNotificacion
                        )
                    }
                )
            }

            // Comer
            item {
                ActividadCard(
                    icono = "🍳",
                    titulo = "Cocinar",
                    descripcion = "Preparar comida",
                    beneficios = listOf("+70 Hambre", "+15 Diversión"),
                    tiempo = "1h",
                    costo = 10,
                    puedeUsar = gameState.dinero >= 10 && necesidades.hambre < 85f,
                    efecto = EfectoActividad.COCINAR,
                    onClick = { efecto ->
                        realizarActividad(
                            efecto = efecto,
                            gameState = gameState,
                            necesidades = necesidades,
                            onGameStateChanged = onGameStateChanged,
                            onNecesidadesChanged = onNecesidadesChanged,
                            onNotificacion = onNotificacion
                        )
                    }
                )
            }

            // Ver TV
            item {
                ActividadCard(
                    icono = "📺",
                    titulo = "Ver TV",
                    descripcion = "Relajarse",
                    beneficios = listOf("+40 Diversión", "+20 Energía"),
                    tiempo = "2h",
                    costo = 0,
                    puedeUsar = necesidades.diversion < 80f,
                    efecto = EfectoActividad.TV,
                    onClick = { efecto ->
                        realizarActividad(
                            efecto = efecto,
                            gameState = gameState,
                            necesidades = necesidades,
                            onGameStateChanged = onGameStateChanged,
                            onNecesidadesChanged = onNecesidadesChanged,
                            onNotificacion = onNotificacion
                        )
                    }
                )
            }

            // Café energizante
            item {
                ActividadCard(
                    icono = "☕",
                    titulo = "Café Fuerte",
                    descripcion = "Energía rápida",
                    beneficios = listOf("+40 Energía"),
                    tiempo = "15min",
                    costo = 15,
                    puedeUsar = gameState.dinero >= 15 && gameState.personaje.energia < 80f,
                    efecto = EfectoActividad.CAFE,
                    onClick = { efecto ->
                        realizarActividad(
                            efecto = efecto,
                            gameState = gameState,
                            necesidades = necesidades,
                            onGameStateChanged = onGameStateChanged,
                            onNecesidadesChanged = onNecesidadesChanged,
                            onNotificacion = onNotificacion
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun NecesidadesPanel(necesidades: Necesidades) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Estado General: ${necesidades.calcularEstadoGeneral().toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    necesidades.calcularEstadoGeneral() > 70f -> Color(0xFF4CAF50)
                    necesidades.calcularEstadoGeneral() > 40f -> Color(0xFFFF9800)
                    else -> Color(0xFFE91E63)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Barras de necesidades
            BarraNecesidad("🍽️ Hambre", necesidades.hambre)
            BarraNecesidad("😴 Sueño", necesidades.sueño)
            BarraNecesidad("🚿 Higiene", necesidades.higiene)
            BarraNecesidad("🎮 Diversión", necesidades.diversion)
            BarraNecesidad("👥 Social", necesidades.social)

            // Alerta de necesidad crítica
            necesidades.necesidadCritica()?.let { alerta ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Text(
                        text = "⚠️ $alerta",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun BarraNecesidad(nombre: String, valor: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nombre,
            fontSize = 12.sp,
            modifier = Modifier.width(80.dp)
        )

        LinearProgressIndicator(
            progress = valor / 100f,
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = when {
                valor > 70f -> Color(0xFF4CAF50)
                valor > 40f -> Color(0xFFFF9800)
                else -> Color(0xFFE91E63)
            }
        )

        Text(
            text = "${valor.toInt()}%",
            fontSize = 10.sp,
            modifier = Modifier.width(35.dp)
        )
    }
}

enum class EfectoActividad {
    DORMIR, DUCHA, COCINAR, TV, CAFE
}

@Composable
fun ActividadCard(
    icono: String,
    titulo: String,
    descripcion: String,
    beneficios: List<String>,
    tiempo: String,
    costo: Int,
    puedeUsar: Boolean,
    efecto: EfectoActividad,
    onClick: (EfectoActividad) -> Unit
) {
    var realizando by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable { if (puedeUsar && !realizando) onClick(efecto) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                realizando -> Color(0xFFFFEB3B)
                puedeUsar -> Color(0xFFE8F5E8)
                else -> Color(0xFFE0E0E0)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = icono, fontSize = 32.sp)

            Text(
                text = titulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = descripcion,
                fontSize = 10.sp,
                color = Color(0xFF757575)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                beneficios.forEach { beneficio ->
                    Text(
                        text = beneficio,
                        fontSize = 9.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Text(
                text = "⏰ $tiempo",
                fontSize = 8.sp,
                color = Color(0xFF757575)
            )

            if (costo > 0) {
                Text(
                    text = "💰 $$costo",
                    fontSize = 10.sp,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

private fun realizarActividad(
    efecto: EfectoActividad,
    gameState: GameState,
    necesidades: Necesidades,
    onGameStateChanged: (GameState) -> Unit,
    onNecesidadesChanged: (Necesidades) -> Unit,
    onNotificacion: (String) -> Unit
) {
    when (efecto) {
        EfectoActividad.DORMIR -> {
            val nuevasNecesidades = necesidades.copy(
                sueño = (necesidades.sueño + 80f).coerceAtMost(100f),
                hambre = (necesidades.hambre - 20f).coerceAtLeast(0f)
            )
            val nuevoPersonaje = gameState.personaje.copy(
                energia = (gameState.personaje.energia + 30f).coerceAtMost(100f)
            ).also { it.actualizarEstado() }

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(gameState.copy(personaje = nuevoPersonaje).avanzarHora().avanzarHora())
            onNotificacion("Dormiste bien. +80 Sueño, +30 Energía")
        }

        EfectoActividad.DUCHA -> {
            val nuevasNecesidades = necesidades.copy(
                higiene = (necesidades.higiene + 60f).coerceAtMost(100f)
            )
            val nuevoPersonaje = gameState.personaje.copy(
                energia = (gameState.personaje.energia + 10f).coerceAtMost(100f)
            ).also { it.actualizarEstado() }

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(gameState.copy(personaje = nuevoPersonaje))
            onNotificacion("Te sientes limpio y fresco")
        }

        EfectoActividad.COCINAR -> {
            if (gameState.dinero >= 10) {
                val nuevasNecesidades = necesidades.copy(
                    hambre = (necesidades.hambre + 70f).coerceAtMost(100f),
                    diversion = (necesidades.diversion + 15f).coerceAtMost(100f)
                )

                onNecesidadesChanged(nuevasNecesidades)
                onGameStateChanged(gameState.copy(dinero = gameState.dinero - 10).avanzarHora())
                onNotificacion("Cocinaste una deliciosa comida")
            }
        }

        EfectoActividad.TV -> {
            val nuevasNecesidades = necesidades.copy(
                diversion = (necesidades.diversion + 40f).coerceAtMost(100f)
            )
            val nuevoPersonaje = gameState.personaje.copy(
                energia = (gameState.personaje.energia + 20f).coerceAtMost(100f)
            ).also { it.actualizarEstado() }

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(gameState.copy(personaje = nuevoPersonaje).avanzarHora().avanzarHora())
            onNotificacion("Viste tu programa favorito")
        }

        EfectoActividad.CAFE -> {
            if (gameState.dinero >= 15) {
                val nuevoPersonaje = gameState.personaje.copy(
                    energia = (gameState.personaje.energia + 40f).coerceAtMost(100f)
                ).also { it.actualizarEstado() }

                onGameStateChanged(gameState.copy(
                    personaje = nuevoPersonaje,
                    dinero = gameState.dinero - 15
                ))
                onNotificacion("Café fuerte. +40 Energía")
            }
        }
    }
}