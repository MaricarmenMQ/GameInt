package com.example.mycafe.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycafe.data.*
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.lazy.LazyRow
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Panel de clientes mejorado
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Estado del café y estadísticas
            CafeStatsCard(
                gameState = gameState,
                eficiencia = eficiencia,
                clientesActivos = clientesActivos.size
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de clientes con animaciones
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(clientesActivos) { cliente ->
                    ClienteCardAnimated(
                        cliente = cliente,
                        isSelected = clienteSeleccionado == cliente.id,
                        onClick = { onClienteSeleccionado(cliente.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inventario
            InventarioCard(gameState.inventario)

            // Botón para comprar ingredientes
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
                        onNotificacion("Ingredientes comprados por $50")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🛒 Comprar Ingredientes ($50)", fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Estación de trabajo mejorada
        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
        ) {
            val clienteActual = clientesActivos.find { it.id == clienteSeleccionado }

            if (clienteActual != null) {
                EstacionTrabajoMejorada(
                    cliente = clienteActual,
                    eficiencia = eficiencia,
                    inventario = gameState.inventario,
                    onPasoCompletado = { clienteActualizado ->
                        if (clienteActualizado.pasoActual >= clienteActualizado.pasos.size) {
                            // Cliente completado - calcular propina
                            val tiempoServicio = 3000L // Simulado por ahora
                            val propina = GameLogic.calcularPropina(tiempoServicio, clienteActualizado)
                            val gananciaTotal = clienteActualizado.precio + propina
                            val experienciaGanada = when (clienteActualizado.tipoCliente) {
                                TipoCliente.VIP -> 15
                                TipoCliente.IMPACIENTE -> 10
                                else -> 5
                            }

                            // Consumir ingredientes
                            val nuevoInventario = gameState.inventario.consumirIngredientes(clienteActualizado.pasos)

                            // Actualizar estado
                            val nuevoGameState = gameState.copy(
                                dinero = gameState.dinero + gananciaTotal,
                                experiencia = gameState.experiencia + experienciaGanada,
                                clientesServidos = gameState.clientesServidos + 1,
                                inventario = nuevoInventario
                            ).subirNivel()

                            onGameStateChanged(nuevoGameState)
                            onClientesChanged(clientesActivos.filter { it.id != clienteActualizado.id })
                            onClienteSeleccionado(null)

                            val mensaje = if (propina > 0) {
                                "+$${gananciaTotal} (incluye $${propina} propina!)"
                            } else {
                                "+$${gananciaTotal} ganados"
                            }
                            onNotificacion(mensaje)
                        } else {
                            onClientesChanged(
                                clientesActivos.map {
                                    if (it.id == clienteActualizado.id) clienteActualizado else it
                                }
                            )
                        }
                    }
                )
            } else {
                EstacionVaciaAnimada()
            }
        }
    }
}

@Composable
fun CafeStatsCard(
    gameState: GameState,
    eficiencia: Float,
    clientesActivos: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                eficiencia > 0.8f -> Color(0xFFE8F5E8)
                eficiencia > 0.5f -> Color(0xFFFFF3E0)
                else -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "☕ Mi Café - Nivel ${gameState.nivel}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "XP: ${gameState.experiencia}/${gameState.calcularExperienciaParaSiguienteNivel()}",
                fontSize = 10.sp
            )

            LinearProgressIndicator(
                progress = gameState.experiencia.toFloat() / gameState.calcularExperienciaParaSiguienteNivel(),
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Clientes: $clientesActivos/3 | Eficiencia: ${(eficiencia * 100).toInt()}%",
                fontSize = 10.sp
            )

            Text(
                text = "Servidos hoy: ${gameState.clientesServidos}",
                fontSize = 10.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun ClienteCardAnimated(
    cliente: Cliente,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val escala by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(300)
    )

    val colorFondo by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFFFFEB3B)
            cliente.paciencia < 20 -> Color(0xFFFFCDD2)
            cliente.tipoCliente == TipoCliente.VIP -> Color(0xFFE1BEE7)
            else -> Color(0xFFE8F5E8)
        },
        animationSpec = tween(500)
    )

    Card(
        modifier = Modifier
            .width(90.dp)
            .height(120.dp)
            .scale(escala)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = cliente.emoji, fontSize = 24.sp)

            Text(
                text = cliente.pedido,
                fontSize = 8.sp,
                maxLines = 2,
                fontWeight = if (cliente.tipoCliente == TipoCliente.VIP) FontWeight.Bold else FontWeight.Normal
            )

            Row {
                Text(text = "$${cliente.precio}", fontSize = 10.sp, color = Color(0xFF2E7D32))
                if (cliente.tipoCliente == TipoCliente.GENEROSO || cliente.tipoCliente == TipoCliente.VIP) {
                    Text(text = " 💰", fontSize = 8.sp)
                }
            }

            LinearProgressIndicator(
                progress = cliente.paciencia / 100f,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = when {
                    cliente.paciencia > 60 -> Color.Green
                    cliente.paciencia > 30 -> Color(0xFFFF9800)
                    else -> Color.Red
                }
            )

            // Indicador de tipo de cliente
            Text(
                text = when (cliente.tipoCliente) {
                    TipoCliente.IMPACIENTE -> "⚡"
                    TipoCliente.GENEROSO -> "😊"
                    TipoCliente.VIP -> "👑"
                    else -> ""
                },
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun InventarioCard(inventario: Inventario) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "📦 Inventario",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("☕ Básicos: ${inventario.ingredientesBasicos}", fontSize = 9.sp)
                Text("🍫 Premium: ${inventario.ingredientesPremium}", fontSize = 9.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🎂 Pasteles: ${inventario.pasteles}", fontSize = 9.sp)
                Text("🍪 Galletas: ${inventario.galletas}", fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun EstacionTrabajoMejorada(
    cliente: Cliente,
    eficiencia: Float,
    inventario: Inventario,
    onPasoCompletado: (Cliente) -> Unit
) {
    var preparando by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf(0f) }
    val puedePreparar = inventario.puedePreparar(cliente.pasos)

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (puedePreparar) Color(0xFFF8F8F8) else Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Info del cliente y pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Preparando: ${cliente.pedido}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Cliente: ${cliente.emoji}", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${cliente.precio}", fontSize = 14.sp, color = Color(0xFF2E7D32))
                    Text(
                        when (cliente.tipoCliente) {
                            TipoCliente.VIP -> "👑 VIP"
                            TipoCliente.IMPACIENTE -> "⚡ Rápido"
                            TipoCliente.GENEROSO -> "💰 Generoso"
                            else -> "😊 Normal"
                        },
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pasos visuales con animación
            LazyRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cliente.pasos.size) { index ->
                    val paso = cliente.pasos[index]
                    val escalaAnimada by animateFloatAsState(
                        targetValue = when {
                            index < cliente.pasoActual -> 0.9f
                            index == cliente.pasoActual && preparando -> 1.2f
                            index == cliente.pasoActual -> 1.1f
                            else -> 1.0f
                        },
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 100f)
                    )

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .scale(escalaAnimada)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < cliente.pasoActual -> Color(0xFF4CAF50)
                                    index == cliente.pasoActual && preparando -> Color(0xFFFFEB3B)
                                    index == cliente.pasoActual -> Color(0xFF2196F3)
                                    else -> Color(0xFFE0E0E0)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = paso, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso si está preparando
            if (preparando) {
                Text("Preparando...", fontSize = 12.sp)
                LinearProgressIndicator(
                    progress = progreso,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF4CAF50)
                )

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

            Spacer(modifier = Modifier.height(12.dp))

            // Controles
            if (!puedePreparar) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Text(
                        text = "⚠️ Sin ingredientes suficientes",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            } else if (!preparando) {
                if (cliente.pasoActual < cliente.pasos.size) {
                    Button(
                        onClick = { preparando = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (cliente.tipoCliente) {
                                TipoCliente.VIP -> Color(0xFF9C27B0)
                                TipoCliente.IMPACIENTE -> Color(0xFFFF5722)
                                else -> Color(0xFF2196F3)
                            }
                        )
                    ) {
                        Text("Preparar ${cliente.pasos[cliente.pasoActual]}")
                    }
                } else {
                    Button(
                        onClick = { onPasoCompletado(cliente.copy(pasoActual = cliente.pasoActual + 1)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("✨ Servir Cliente")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info de eficiencia
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
fun EstacionVaciaAnimada() {
    val rotacion by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔧",
                fontSize = 36.sp,
                modifier = Modifier.rotate(rotacion)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Selecciona un cliente para empezar",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tip: Clientes VIP 👑 y Generosos 💰 dan más dinero",
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}