package com.example.mycafe.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycafe.data.Personaje
import com.example.mycafe.data.Necesidades
import com.example.mycafe.data.GameState
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke

@Composable
fun CasaScreen(
    gameState: GameState,
    necesidades: Necesidades,
    onGameStateChanged: (GameState) -> Unit,
    onNecesidadesChanged: (Necesidades) -> Unit,
    onNotificacion: (String) -> Unit
) {
    var actividadEnProceso by remember { mutableStateOf<EfectoActividad?>(null) }
    var progresoActividad by remember { mutableStateOf(0f) }

    // Colores de ambiente según la hora
    val coloresAmbiente = when {
        gameState.hora in 6..11 -> listOf(Color(0xFFFFF8E1), Color(0xFFFFE0B2)) // Mañana
        gameState.hora in 12..17 -> listOf(Color(0xFFE8F5E8), Color(0xFFC8E6C9)) // Tarde
        gameState.hora in 18..21 -> listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)) // Atardecer
        else -> listOf(Color(0xFF3E2723), Color(0xFF5D4037)) // Noche
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = coloresAmbiente)
            )
            .padding(16.dp)
    ) {
        // Header con saludo personalizado
        SaludoPersonalizado(gameState, necesidades)

        Spacer(modifier = Modifier.height(12.dp))

        // Personaje central animado
        PersonajeAnimadoMejorado(gameState.personaje, necesidades)

        Spacer(modifier = Modifier.height(12.dp))

        // Panel de necesidades estilo Los Sims mejorado
        NecesidadesPanelMejorado(necesidades)

        Spacer(modifier = Modifier.height(16.dp))

        // Progreso de actividad si está en proceso
        if (actividadEnProceso != null) {
            ActividadEnProcesoCard(
                actividad = actividadEnProceso!!,
                progreso = progresoActividad
            )

            // Lógica del progreso
            LaunchedEffect(actividadEnProceso) {
                val duracion = obtenerDuracionActividad(actividadEnProceso!!)
                val pasos = 100
                repeat(pasos) {
                    delay(duracion / pasos)
                    progresoActividad = (it + 1) / pasos.toFloat()
                }

                if (actividadEnProceso != null) {
                    completarActividad(
                        efecto = actividadEnProceso!!,
                        gameState = gameState,
                        necesidades = necesidades,
                        onGameStateChanged = onGameStateChanged,
                        onNecesidadesChanged = onNecesidadesChanged,
                        onNotificacion = onNotificacion
                    )
                    actividadEnProceso = null
                    progresoActividad = 0f
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Grid de actividades mejorado
        Text(
            text = "🏠 Actividades en casa:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (gameState.hora >= 22 || gameState.hora <= 6) Color.White else Color(0xFF5D4037)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(obtenerActividadesDisponibles(gameState, necesidades)) { actividad ->
                ActividadCardMejorada(
                    actividad = actividad,
                    enabled = actividad.puedeUsar && actividadEnProceso == null,
                    onClick = {
                        if (actividadEnProceso == null) {
                            actividadEnProceso = actividad.efecto
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SaludoPersonalizado(gameState: GameState, necesidades: Necesidades) {
    val estadoGeneral = necesidades.calcularEstadoGeneral()
    val saludo = when {
        gameState.hora in 5..11 -> "🌅 Buenos días"
        gameState.hora in 12..17 -> "☀️ Buenas tardes"
        gameState.hora in 18..21 -> "🌆 Buenas tardes"
        else -> "🌙 Buenas noches"
    }

    val mensaje = when {
        estadoGeneral > 80f -> "¡Te ves genial hoy!"
        estadoGeneral > 60f -> "Te ves bien"
        estadoGeneral > 40f -> "Pareces algo cansado"
        else -> "¡Necesitas cuidarte más!"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                gameState.hora >= 22 || gameState.hora <= 6 -> Color(0xFF37474F)
                else -> Color.White.copy(alpha = 0.9f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = saludo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameState.hora >= 22 || gameState.hora <= 6) Color.White else Color(0xFF5D4037)
                )
                Text(
                    text = mensaje,
                    fontSize = 12.sp,
                    color = when {
                        estadoGeneral > 60f -> Color(0xFF4CAF50)
                        estadoGeneral > 40f -> Color(0xFFFF9800)
                        else -> Color(0xFFE91E63)
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${gameState.hora}:00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameState.hora >= 22 || gameState.hora <= 6) Color.White else Color(0xFF757575)
                )
                Text(
                    text = "Día ${gameState.dia}",
                    fontSize = 10.sp,
                    color = if (gameState.hora >= 22 || gameState.hora <= 6) Color.White else Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun PersonajeAnimadoMejorado(personaje: Personaje, necesidades: Necesidades) {
    val estadoGeneral = necesidades.calcularEstadoGeneral()

    // Animaciones según el estado
    val movimientoX by rememberInfiniteTransition().animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    estadoGeneral > 70f -> 2000
                    estadoGeneral > 40f -> 3000
                    else -> 4000
                }
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val escala by animateFloatAsState(
        targetValue = when {
            estadoGeneral > 80f -> 1.1f
            estadoGeneral > 60f -> 1.0f
            else -> 0.9f
        },
        animationSpec = spring(dampingRatio = 0.8f)
    )

    val rotacion by animateFloatAsState(
        targetValue = when {
            estadoGeneral < 30f -> 10f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = 0.6f)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Personaje principal
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = when {
                                estadoGeneral > 70f -> listOf(Color(0xFFE8F5E8), Color(0xFFC8E6C9))
                                estadoGeneral > 40f -> listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                                else -> listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                            }
                        )
                    )
                    .border(3.dp, Color.White, CircleShape)
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = personaje.humor,
                    fontSize = 48.sp,
                    modifier = Modifier
                        .offset(x = movimientoX.dp)
                        .scale(escala)
                        .rotate(rotacion)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Estado del personaje con efectos
            Text(
                text = "Estado: ${personaje.estado}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    estadoGeneral > 70f -> Color(0xFF2E7D32)
                    estadoGeneral > 40f -> Color(0xFFE65100)
                    else -> Color(0xFFC62828)
                }
            )

            // Barra de energía visual
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(personaje.energia / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = when {
                                        personaje.energia > 70f -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                                        personaje.energia > 40f -> listOf(Color(0xFFFF9800), Color(0xFFFFC107))
                                        else -> listOf(Color(0xFFE91E63), Color(0xFFFF5722))
                                    }
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${personaje.energia.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NecesidadesPanelMejorado(necesidades: Necesidades) {
    val estadoGeneral = necesidades.calcularEstadoGeneral()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                estadoGeneral > 70f -> Color(0xFFE8F5E8)
                estadoGeneral > 40f -> Color(0xFFFFF3E0)
                else -> Color(0xFFFFEBEE)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header del panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤️ Estado de Bienestar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            estadoGeneral > 70f -> Color(0xFF4CAF50)
                            estadoGeneral > 40f -> Color(0xFFFF9800)
                            else -> Color(0xFFE91E63)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${estadoGeneral.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barras de necesidades mejoradas
            BarraNecesidadMejorada("🍽️", "Hambre", necesidades.hambre)
            BarraNecesidadMejorada("😴", "Sueño", necesidades.sueño)
            BarraNecesidadMejorada("🚿", "Higiene", necesidades.higiene)
            BarraNecesidadMejorada("🎮", "Diversión", necesidades.diversion)
            BarraNecesidadMejorada("👥", "Social", necesidades.social)

            // Alerta crítica mejorada
            necesidades.necesidadCritica()?.let { alerta ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE57373)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alerta,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarraNecesidadMejorada(emoji: String, nombre: String, valor: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = nombre,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(55.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(valor / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = when {
                                valor > 70f -> listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                                valor > 40f -> listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                                else -> listOf(Color(0xFFE91E63), Color(0xFFEC407A))
                            }
                        )
                    )
            )
        }

        Text(
            text = "${valor.toInt()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(25.dp),
            textAlign = TextAlign.End
        )
    }
}

data class ActividadInfo(
    val icono: String,
    val titulo: String,
    val descripcion: String,
    val beneficios: List<String>,
    val tiempo: String,
    val costo: Int,
    val puedeUsar: Boolean,
    val efecto: EfectoActividad,
    val colorTema: Color
)

fun obtenerActividadesDisponibles(gameState: GameState, necesidades: Necesidades): List<ActividadInfo> {
    return listOf(
        ActividadInfo(
            icono = "🛏️",
            titulo = "Dormir",
            descripcion = "Sueño reparador",
            beneficios = listOf("+80 Sueño", "+30 Energía"),
            tiempo = "8h",
            costo = 0,
            puedeUsar = gameState.hora >= 22 || gameState.hora <= 6,
            efecto = EfectoActividad.DORMIR,
            colorTema = Color(0xFF3F51B5)
        ),
        ActividadInfo(
            icono = "🚿",
            titulo = "Ducharse",
            descripcion = "Higiene personal",
            beneficios = listOf("+60 Higiene", "+10 Energía"),
            tiempo = "30min",
            costo = 0,
            puedeUsar = necesidades.higiene < 90f,
            efecto = EfectoActividad.DUCHA,
            colorTema = Color(0xFF00BCD4)
        ),
        ActividadInfo(
            icono = "🍳",
            titulo = "Cocinar",
            descripcion = "Preparar comida",
            beneficios = listOf("+70 Hambre", "+15 Diversión"),
            tiempo = "1h",
            costo = 10,
            puedeUsar = gameState.dinero >= 10 && necesidades.hambre < 85f,
            efecto = EfectoActividad.COCINAR,
            colorTema = Color(0xFFFF9800)
        ),
        ActividadInfo(
            icono = "📺",
            titulo = "Ver TV",
            descripcion = "Entretenimiento",
            beneficios = listOf("+40 Diversión", "+20 Energía"),
            tiempo = "2h",
            costo = 0,
            puedeUsar = necesidades.diversion < 80f,
            efecto = EfectoActividad.TV,
            colorTema = Color(0xFF9C27B0)
        ),
        ActividadInfo(
            icono = "☕",
            titulo = "Café Express",
            descripcion = "Energía rápida",
            beneficios = listOf("+40 Energía"),
            tiempo = "15min",
            costo = 15,
            puedeUsar = gameState.dinero >= 15 && gameState.personaje.energia < 80f,
            efecto = EfectoActividad.CAFE,
            colorTema = Color(0xFF795548)
        ),
        ActividadInfo(
            icono = "📱",
            titulo = "Redes Sociales",
            descripcion = "Socializar online",
            beneficios = listOf("+30 Social", "+20 Diversión"),
            tiempo = "1h",
            costo = 0,
            puedeUsar = necesidades.social < 80f,
            efecto = EfectoActividad.SOCIAL,
            colorTema = Color(0xFF4CAF50)
        )
    )
}

@Composable
fun ActividadCardMejorada(
    actividad: ActividadInfo,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val escala by animateFloatAsState(
        targetValue = if (enabled) 1.0f else 0.95f,
        animationSpec = spring(dampingRatio = 0.8f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .scale(escala)
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                actividad.colorTema.copy(alpha = 0.1f)
            } else {
                Color(0xFFE0E0E0)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (enabled) {
            BorderStroke(2.dp, actividad.colorTema.copy(alpha = 0.3f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono principal con animación
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) actividad.colorTema.copy(alpha = 0.2f) else Color(0xFFBDBDBD)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actividad.icono,
                    fontSize = 24.sp
                )
            }

            // Título y descripción
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = actividad.titulo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) actividad.colorTema else Color(0xFF757575),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = actividad.descripcion,
                    fontSize = 9.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center
                )
            }

            // Beneficios
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                actividad.beneficios.take(2).forEach { beneficio ->
                    Text(
                        text = beneficio,
                        fontSize = 8.sp,
                        color = if (enabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Footer con tiempo y costo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰${actividad.tiempo}",
                    fontSize = 8.sp,
                    color = Color(0xFF757575)
                )

                if (actividad.costo > 0) {
                    Text(
                        text = "$${actividad.costo}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) Color(0xFF2E7D32) else Color(0xFFE91E63)
                    )
                } else {
                    Text(
                        text = "Gratis",
                        fontSize = 8.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun ActividadEnProcesoCard(
    actividad: EfectoActividad,
    progreso: Float
) {
    val info = obtenerInfoActividad(actividad)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = info.icono,
                fontSize = 32.sp
            )

            Text(
                text = "✨ ${info.titulo} en proceso...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = info.colorTema
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(info.colorTema, info.colorTema.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(progreso * 100).toInt()}% completado",
                fontSize = 10.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

fun obtenerInfoActividad(efecto: EfectoActividad): ActividadInfo {
    return when (efecto) {
        EfectoActividad.DORMIR -> ActividadInfo(
            "🛏️", "Dormir", "Descansando", emptyList(), "8h", 0, true, efecto, Color(0xFF3F51B5)
        )
        EfectoActividad.DUCHA -> ActividadInfo(
            "🚿", "Ducharse", "Limpiándose", emptyList(), "30min", 0, true, efecto, Color(0xFF00BCD4)
        )
        EfectoActividad.COCINAR -> ActividadInfo(
            "🍳", "Cocinar", "Cocinando", emptyList(), "1h", 10, true, efecto, Color(0xFFFF9800)
        )
        EfectoActividad.TV -> ActividadInfo(
            "📺", "Ver TV", "Relajándose", emptyList(), "2h", 0, true, efecto, Color(0xFF9C27B0)
        )
        EfectoActividad.CAFE -> ActividadInfo(
            "☕", "Café Express", "Bebiendo café", emptyList(), "15min", 15, true, efecto, Color(0xFF795548)
        )
        EfectoActividad.SOCIAL -> ActividadInfo(
            "📱", "Redes Sociales", "Socializando", emptyList(), "1h", 0, true, efecto, Color(0xFF4CAF50)
        )
    }
}

fun obtenerDuracionActividad(efecto: EfectoActividad): Long {
    return when (efecto) {
        EfectoActividad.DORMIR -> 8000L    // 8 segundos = 8 horas
        EfectoActividad.DUCHA -> 2000L     // 2 segundos = 30 minutos
        EfectoActividad.COCINAR -> 3000L   // 3 segundos = 1 hora
        EfectoActividad.TV -> 4000L        // 4 segundos = 2 horas
        EfectoActividad.CAFE -> 1000L      // 1 segundo = 15 minutos
        EfectoActividad.SOCIAL -> 3000L    // 3 segundos = 1 hora
    }
}

enum class EfectoActividad {
    DORMIR, DUCHA, COCINAR, TV, CAFE, SOCIAL
}

private fun completarActividad(
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
                hambre = (necesidades.hambre - 15f).coerceAtLeast(0f)
            )
            val nuevoPersonaje = gameState.personaje.copy(
                energia = (gameState.personaje.energia + 50f).coerceAtMost(100f)
            ).also { it.actualizarEstado() }

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(
                gameState.copy(personaje = nuevoPersonaje)
                    .avanzarHora().avanzarHora().avanzarHora().avanzarHora()
                    .avanzarHora().avanzarHora().avanzarHora().avanzarHora()
            )
            onNotificacion("😴 ¡Dormiste genial! Te sientes renovado")
        }

        EfectoActividad.DUCHA -> {
            val nuevasNecesidades = necesidades.copy(
                higiene = (necesidades.higiene + 70f).coerceAtMost(100f)
            )
            val nuevoPersonaje = gameState.personaje.copy(
                energia = (gameState.personaje.energia + 15f).coerceAtMost(100f)
            ).also { it.actualizarEstado() }

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(gameState.copy(personaje = nuevoPersonaje))
            onNotificacion("🚿 ¡Qué fresco! Te sientes limpio y renovado")
        }

        EfectoActividad.COCINAR -> {
            if (gameState.dinero >= 10) {
                val nuevasNecesidades = necesidades.copy(
                    hambre = (necesidades.hambre + 80f).coerceAtMost(100f),
                    diversion = (necesidades.diversion + 20f).coerceAtMost(100f)
                )

                onNecesidadesChanged(nuevasNecesidades)
                onGameStateChanged(gameState.copy(dinero = gameState.dinero - 10).avanzarHora())
                onNotificacion("🍳 ¡Delicioso! Cocinaste tu comida favorita")
            } else {
                onNotificacion("💰 No tienes suficiente dinero para cocinar")
            }
        }

        EfectoActividad.TV -> {
            val nuevasNecesidades = necesidades.copy(
                diversion = (necesidades.diversion + 50f).coerceAtMost(100f),
                social = (necesidades.social + 10f).coerceAtMost(100f)
            )
            val nuevoPersonaje = gameState.personaje.copy(
                energia = (gameState.personaje.energia + 25f).coerceAtMost(100f)
            ).also { it.actualizarEstado() }

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(gameState.copy(personaje = nuevoPersonaje).avanzarHora().avanzarHora())
            onNotificacion("📺 ¡Qué entretenido! Viste tu serie favorita")
        }

        EfectoActividad.CAFE -> {
            if (gameState.dinero >= 15) {
                val nuevoPersonaje = gameState.personaje.copy(
                    energia = (gameState.personaje.energia + 45f).coerceAtMost(100f)
                ).also { it.actualizarEstado() }

                onGameStateChanged(gameState.copy(
                    personaje = nuevoPersonaje,
                    dinero = gameState.dinero - 15
                ))
                onNotificacion("☕ ¡Energía instantánea! El café te activó")
            } else {
                onNotificacion("💰 No tienes dinero para el café")
            }
        }

        EfectoActividad.SOCIAL -> {
            val nuevasNecesidades = necesidades.copy(
                social = (necesidades.social + 60f).coerceAtMost(100f),
                diversion = (necesidades.diversion + 30f).coerceAtMost(100f)
            )

            onNecesidadesChanged(nuevasNecesidades)
            onGameStateChanged(gameState.avanzarHora())
            onNotificacion("📱 ¡Qué divertido! Conectaste con tus amigos")
        }
    }
}