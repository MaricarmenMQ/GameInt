package com.example.mycafe.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.mycafe.data.*
import kotlinx.coroutines.delay

@Composable
fun CafeScreen(
    gameState: GameState,
    necesidades: Necesidades,
    clientesActivos: List<Cliente>,
    clienteSeleccionado: Int?,
    onGameStateChanged: (GameState) -> Unit,
    onClientesChanged: (List<Cliente>) -> Unit,
    onClienteSeleccionado: (Int?) -> Unit,
    onNotificacion: (String) -> Unit
) {
    val eficiencia = GameLogic.calcularEficiencia(gameState.personaje, necesidades)

    // Layout en columna para mejor distribución en móvil
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8E1), // Crema claro
                        Color(0xFFFFE0B2)  // Naranja muy claro
                    )
                )
            )
            .padding(8.dp)
    ) {

        // 1. Panel superior compacto con stats
        CafeHeaderCompacto(gameState, eficiencia, clientesActivos.size)

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Fila de clientes - más visual y compacta
        if (clientesActivos.isNotEmpty()) {
            Text(
                "🧑‍🤝‍🧑 Clientes esperando:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(clientesActivos) { cliente ->
                    ClienteCardMejorada(
                        cliente = cliente,
                        isSelected = clienteSeleccionado == cliente.id,
                        onClick = { onClienteSeleccionado(cliente.id) }
                    )
                }
            }
        } else {
            // Estado vacío más atractivo
            EstadoSinClientesMejorado()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Estación de trabajo - ocupa el resto del espacio
        Box(modifier = Modifier.weight(1f)) {
            val clienteActual = clientesActivos.find { it.id == clienteSeleccionado }

            if (clienteActual != null) {
                EstacionTrabajoVisual(
                    cliente = clienteActual,
                    eficiencia = eficiencia,
                    inventario = gameState.inventario,
                    onPasoCompletado = { clienteActualizado ->
                        procesarClienteCompletado(
                            clienteActualizado = clienteActualizado,
                            clientesActivos = clientesActivos,
                            gameState = gameState,
                            onGameStateChanged = onGameStateChanged,
                            onClientesChanged = onClientesChanged,
                            onClienteSeleccionado = onClienteSeleccionado,
                            onNotificacion = onNotificacion
                        )
                    }
                )
            } else {
                EstacionVaciaAnimadaMejorada()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Panel inferior con inventario y acciones rápidas
        PanelInferiorCafe(
            gameState = gameState,
            onGameStateChanged = onGameStateChanged,
            onNotificacion = onNotificacion
        )
    }
}

@Composable
fun CafeHeaderCompacto(
    gameState: GameState,
    eficiencia: Float,
    clientesActivos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6D4C41) // Marrón café
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status café
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("☕", fontSize = 20.sp)
                Text(
                    "Nivel ${gameState.nivel}",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Clientes
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👥", fontSize = 20.sp)
                Text(
                    "$clientesActivos/3",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Eficiencia
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚡", fontSize = 20.sp)
                Text(
                    "${(eficiencia * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = if (eficiencia > 0.7f) Color(0xFF4CAF50) else Color(0xFFFFEB3B),
                    fontWeight = FontWeight.Bold
                )
            }

            // Servidos hoy
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 20.sp)
                Text(
                    "${gameState.clientesServidos}",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ClienteCardMejorada(
    cliente: Cliente,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFFEB3B) else Color.Transparent,
        animationSpec = tween(300)
    )

    val backgroundBrush = when {
        isSelected -> Brush.radialGradient(
            colors = listOf(Color(0xFFFFEB3B), Color(0xFFFFF59D))
        )
        cliente.paciencia < 30 -> Brush.radialGradient(
            colors = listOf(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
        )
        cliente.tipoCliente == TipoCliente.VIP -> Brush.radialGradient(
            colors = listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8))
        )
        else -> Brush.radialGradient(
            colors = listOf(Color(0xFFE8F5E8), Color(0xFFC8E6C9))
        )
    }

    Card(
        modifier = Modifier
            .width(85.dp)
            .height(110.dp)
            .scale(animatedScale)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .shadow(if (isSelected) 8.dp else 4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Emoji del cliente con animación si es VIP
                val clienteRotacion by rememberInfiniteTransition().animateFloat(
                    initialValue = if (cliente.tipoCliente == TipoCliente.VIP) -5f else 0f,
                    targetValue = if (cliente.tipoCliente == TipoCliente.VIP) 5f else 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Text(
                    text = cliente.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.rotate(clienteRotacion)
                )

                // Pedido (más compacto)
                Text(
                    text = cliente.pedido,
                    fontSize = 8.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontWeight = if (cliente.tipoCliente == TipoCliente.VIP) FontWeight.Bold else FontWeight.Normal
                )

                // Precio con efectos
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${cliente.precio}",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                    when (cliente.tipoCliente) {
                        TipoCliente.GENEROSO -> Text("💰", fontSize = 10.sp)
                        TipoCliente.VIP -> Text("👑", fontSize = 10.sp)
                        TipoCliente.IMPACIENTE -> Text("⚡", fontSize = 10.sp)
                        else -> {}
                    }
                }

                // Barra de paciencia mejorada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(cliente.paciencia / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    cliente.paciencia > 60 -> Color(0xFF4CAF50)
                                    cliente.paciencia > 30 -> Color(0xFFFF9800)
                                    else -> Color(0xFFE91E63)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoSinClientesMejorado() {
    val pulso by rememberInfiniteTransition().animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5) // Lavanda claro
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "☕",
                fontSize = 32.sp,
                modifier = Modifier.scale(pulso)
            )
            Text(
                text = "Esperando clientes...",
                fontSize = 14.sp,
                color = Color(0xFF6A1B9A),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if ((8..20).random() % 2 == 0) "¡La fama de tu café se extiende!" else "¡Pronto llegarán clientes!",
                fontSize = 10.sp,
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
fun EstacionTrabajoVisual(
    cliente: Cliente,
    eficiencia: Float,
    inventario: Inventario,
    onPasoCompletado: (Cliente) -> Unit
) {
    var preparando by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf(0f) }
    val puedePreparar = inventario.puedePreparar(cliente.pasos)

    // Colores temáticos según el cliente
    val colorTema = when (cliente.tipoCliente) {
        TipoCliente.VIP -> Color(0xFF9C27B0)
        TipoCliente.IMPACIENTE -> Color(0xFFFF5722)
        TipoCliente.GENEROSO -> Color(0xFF4CAF50)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (puedePreparar) Color.White else Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header del pedido estilizado
            Card(
                colors = CardDefaults.cardColors(containerColor = colorTema.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
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
                            text = cliente.pedido,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorTema
                        )
                        Text(
                            text = "Cliente ${cliente.emoji}",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${cliente.precio}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = when (cliente.tipoCliente) {
                                TipoCliente.VIP -> "👑 VIP"
                                TipoCliente.IMPACIENTE -> "⚡ Rápido"
                                TipoCliente.GENEROSO -> "💰 Generoso"
                                else -> "😊 Normal"
                            },
                            fontSize = 10.sp,
                            color = colorTema
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pasos de preparación visuales - Layout mejorado
            Text(
                text = "Proceso de preparación:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pasos en grid 2x2 o fila según cantidad
            if (cliente.pasos.size <= 4) {
                LazyRow(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cliente.pasos.size) { index ->
                        PasoVisualdeMejoradi(
                            paso = cliente.pasos[index],
                            index = index,
                            pasoActual = cliente.pasoActual,
                            preparando = preparando && index == cliente.pasoActual
                        )
                    }
                }
            } else {
                // Grid para pedidos complejos
                val rows = cliente.pasos.chunked(3)
                rows.forEach { rowPasos ->
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(rowPasos.size) { localIndex ->
                            val globalIndex = rows.indexOf(rowPasos) * 3 + localIndex
                            PasoVisualdeMejoradi(
                                paso = rowPasos[localIndex],
                                index = globalIndex,
                                pasoActual = cliente.pasoActual,
                                preparando = preparando && globalIndex == cliente.pasoActual
                            )
                        }
                    }
                    if (rowPasos != rows.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso si está preparando
            if (preparando) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✨ Preparando ${cliente.pasos[cliente.pasoActual]}...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progreso)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(colorTema, colorTema.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                        }
                    }
                }

                // Lógica de preparación
                LaunchedEffect(preparando) {
                    val tiempoBase = 2000
                    val tiempoTotal = (tiempoBase / eficiencia).toLong().coerceAtLeast(500)

                    val pasos = 100
                    repeat(pasos) {
                        delay(tiempoTotal / pasos)
                        progreso = (it + 1) / pasos.toFloat()
                    }

                    if (preparando) {
                        onPasoCompletado(cliente.copy(pasoActual = cliente.pasoActual + 1))
                        preparando = false
                        progreso = 0f
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones de acción mejorados
            if (!puedePreparar) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sin ingredientes suficientes para este pedido",
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            } else if (!preparando) {
                if (cliente.pasoActual < cliente.pasos.size) {
                    Button(
                        onClick = { preparando = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colorTema),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preparar ${cliente.pasos[cliente.pasoActual]}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onPasoCompletado(cliente.copy(pasoActual = cliente.pasoActual + 1)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎉", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Servir al Cliente!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Info adicional compacta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Velocidad: ${(eficiencia * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = if (eficiencia > 0.7f) Color(0xFF4CAF50) else Color(0xFFE91E63)
                )
                Text(
                    text = "Paciencia: ${cliente.paciencia.toInt()}%",
                    fontSize = 10.sp,
                    color = when {
                        cliente.paciencia > 60 -> Color(0xFF4CAF50)
                        cliente.paciencia > 30 -> Color(0xFFFF9800)
                        else -> Color(0xFFE91E63)
                    }
                )
            }
        }
    }
}

@Composable
fun PasoVisualdeMejoradi(
    paso: String,
    index: Int,
    pasoActual: Int,
    preparando: Boolean
) {
    val escalaAnimada by animateFloatAsState(
        targetValue = when {
            index < pasoActual -> 0.8f
            index == pasoActual && preparando -> 1.3f
            index == pasoActual -> 1.1f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 150f)
    )

    val rotacionAnimada by rememberInfiniteTransition().animateFloat(
        initialValue = if (preparando && index == pasoActual) -10f else 0f,
        targetValue = if (preparando && index == pasoActual) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(55.dp)
            .scale(escalaAnimada)
            .rotate(rotacionAnimada)
            .clip(CircleShape)
            .background(
                brush = when {
                    index < pasoActual -> Brush.radialGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                    )
                    index == pasoActual && preparando -> Brush.radialGradient(
                        colors = listOf(Color(0xFFFFEB3B), Color(0xFFFFC107))
                    )
                    index == pasoActual -> Brush.radialGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
                    )
                    else -> Brush.radialGradient(
                        colors = listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
                    )
                }
            )
            .border(
                width = 2.dp,
                color = when {
                    index < pasoActual -> Color(0xFF4CAF50)
                    index == pasoActual -> Color(0xFF2196F3)
                    else -> Color.Transparent
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = paso,
            fontSize = 20.sp
        )

        // Checkmark para pasos completados
        if (index < pasoActual) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 12.sp, color = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
fun EstacionVaciaAnimadaMejorada() {
    val rotacion by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val pulso by rememberInfiniteTransition().animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono animado
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE3F2FD),
                                Color(0xFFBBDEFB)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☕",
                    fontSize = 32.sp,
                    modifier = Modifier
                        .rotate(rotacion)
                        .scale(pulso)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Estación de trabajo lista",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Selecciona un cliente para comenzar",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tips aleatorios
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Text(
                    text = listOf(
                        "💡 Los clientes VIP 👑 pagan más pero son más exigentes",
                        "💡 Los clientes generosos 💰 dan propina si los atiendes rápido",
                        "💡 ¡Tu eficiencia mejora con mejor energía y ánimo!",
                        "💡 Los clientes impacientes ⚡ se van rápido pero pagan extra"
                    ).random(),
                    fontSize = 11.sp,
                    color = Color(0xFF6A1B9A),
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PanelInferiorCafe(
    gameState: GameState,
    onGameStateChanged: (GameState) -> Unit,
    onNotificacion: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Inventario compacto
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "📦 Stock",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IngredienteIndicador("☕", gameState.inventario.ingredientesBasicos)
                    IngredienteIndicador("🍫", gameState.inventario.ingredientesPremium)
                    IngredienteIndicador("🎂", gameState.inventario.pasteles)
                    IngredienteIndicador("🍪", gameState.inventario.galletas)
                }
            }
        }

        // Botón de compra mejorado
        if (gameState.dinero >= 50) {
            Button(
                onClick = {
                    val nuevoInventario = gameState.inventario.copy(
                        ingredientesBasicos = gameState.inventario.ingredientesBasicos + 20,
                        ingredientesPremium = gameState.inventario.ingredientesPremium + 10,
                        pasteles = gameState.inventario.pasteles + 5,
                        galletas = gameState.inventario.galletas + 8
                    )
                    onGameStateChanged(
                        gameState.copy(
                            inventario = nuevoInventario,
                            dinero = gameState.dinero - 50
                        )
                    )
                    onNotificacion("📦 ¡Stock reabastecido! -$50")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛒", fontSize = 16.sp)
                    Text("$50", fontSize = 10.sp)
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("💰", fontSize = 16.sp)
                    Text("Sin dinero", fontSize = 8.sp, color = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
fun IngredienteIndicador(emoji: String, cantidad: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(
            text = cantidad.toString(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                cantidad > 10 -> Color(0xFF4CAF50)
                cantidad > 5 -> Color(0xFFFF9800)
                else -> Color(0xFFE91E63)
            }
        )
    }
}

// Función auxiliar para procesar cliente completado
private fun procesarClienteCompletado(
    clienteActualizado: Cliente,
    clientesActivos: List<Cliente>,
    gameState: GameState,
    onGameStateChanged: (GameState) -> Unit,
    onClientesChanged: (List<Cliente>) -> Unit,
    onClienteSeleccionado: (Int?) -> Unit,
    onNotificacion: (String) -> Unit
) {
    if (clienteActualizado.pasoActual >= clienteActualizado.pasos.size) {
        // Cliente completado - calcular ganancias
        val tiempoServicio = 3000L // Simulado
        val propina = GameLogic.calcularPropina(tiempoServicio, clienteActualizado)
        val gananciaTotal = clienteActualizado.precio + propina
        val experienciaGanada = when (clienteActualizado.tipoCliente) {
            TipoCliente.VIP -> 15
            TipoCliente.IMPACIENTE -> 10
            TipoCliente.GENEROSO -> 8
            else -> 5
        }

        // Consumir ingredientes
        val nuevoInventario = gameState.inventario.consumirIngredientes(clienteActualizado.pasos)

        // Actualizar estado del juego
        val nuevoGameState = gameState.copy(
            dinero = gameState.dinero + gananciaTotal,
            experiencia = gameState.experiencia + experienciaGanada,
            clientesServidos = gameState.clientesServidos + 1,
            inventario = nuevoInventario
        ).subirNivel()

        onGameStateChanged(nuevoGameState)
        onClientesChanged(clientesActivos.filter { it.id != clienteActualizado.id })
        onClienteSeleccionado(null)

        // Mensaje personalizado según el resultado
        val mensaje = when {
            propina > 0 -> "🎉 +${gananciaTotal} (¡+${propina} propina!)"
            clienteActualizado.tipoCliente == TipoCliente.VIP -> "👑 Cliente VIP satisfecho: +${gananciaTotal}"
            else -> "✅ Cliente feliz: +${gananciaTotal}"
        }
        onNotificacion(mensaje)
    } else {
        // Actualizar paso del cliente
        onClientesChanged(
            clientesActivos.map {
                if (it.id == clienteActualizado.id) clienteActualizado else it
            }
        )
    }
}