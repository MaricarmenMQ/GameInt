package com.example.mycafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.mycafe.data.Personaje
import com.example.mycafe.data.Cliente
import com.example.mycafe.data.ClienteFactory

import com.example.mycafe.data.GameState
import com.example.mycafe.data.Necesidades
import com.example.mycafe.data.*
import com.example.mycafe.screens.CasaScreen
import com.example.mycafe.screens.CafeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFF8E1)
                ) {
                    JuegoCompleto()
                }
            }
        }
    }
}

@Composable
fun JuegoCompleto() {
    // Estados principales actualizados
    var pantallaActual by remember { mutableStateOf("cafe") }
    var mostrarMenu by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf(GameState()) }
    var necesidades by remember { mutableStateOf(Necesidades()) }
    var clientesActivos by remember { mutableStateOf(listOf<Cliente>()) }
    var clienteSeleccionado by remember { mutableStateOf<Int?>(null) }
    var notificacion by remember { mutableStateOf("") }

    // Timer de degradación de energía y necesidades
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000) // Cada 4 segundos

            if (pantallaActual == "cafe") {
                // En el café, energía y necesidades bajan más rápido
                val nuevoPersonaje = gameState.personaje.copy(
                    energia = (gameState.personaje.energia - 3f).coerceAtLeast(0f)
                ).also { it.actualizarEstado() }

                val nuevasNecesidades = necesidades.copy(
                    hambre = (necesidades.hambre - 2f).coerceAtLeast(0f),
                    sueño = (necesidades.sueño - 1f).coerceAtLeast(0f),
                    higiene = (necesidades.higiene - 0.5f).coerceAtLeast(0f),
                    diversion = (necesidades.diversion - 1f).coerceAtLeast(0f),
                    social = (necesidades.social + 0.5f).coerceAtMost(100f) // Trabajar es social
                )

                gameState = gameState.copy(personaje = nuevoPersonaje)
                necesidades = nuevasNecesidades
            } else {
                // En casa, solo baja lento
                val nuevoPersonaje = gameState.personaje.copy(
                    energia = (gameState.personaje.energia - 1f).coerceAtLeast(0f)
                ).also { it.actualizarEstado() }

                val nuevasNecesidades = necesidades.copy(
                    hambre = (necesidades.hambre - 1f).coerceAtLeast(0f),
                    higiene = (necesidades.higiene - 0.3f).coerceAtLeast(0f)
                )

                gameState = gameState.copy(personaje = nuevoPersonaje)
                necesidades = nuevasNecesidades
            }

            // Alertas de necesidades críticas
            val alertaCritica = necesidades.necesidadCritica()
            if (alertaCritica != null && notificacion.isEmpty()) {
                notificacion = alertaCritica
            }
        }
    }

    // Generador de clientes mejorado (solo cuando esté en el café y sea horario de trabajo)
    LaunchedEffect(pantallaActual, gameState.hora) {
        while (pantallaActual == "cafe" && gameState.esHoraTrabajo()) {
            delay(8000) // Cada 8 segundos
            if (clientesActivos.size < gameState.cafeUpgrades.capacidadClientes) {
                val nuevoId = (clientesActivos.maxOfOrNull { it.id } ?: 0) + 1
                val nuevoCliente = ClienteFactory.crearClienteAleatorio(nuevoId)
                clientesActivos = clientesActivos + nuevoCliente
            }
        }
    }

    // Timer de paciencia de clientes mejorado
    LaunchedEffect(clientesActivos.size) {
        while (clientesActivos.isNotEmpty()) {
            delay(1000) // Cada segundo
            clientesActivos = clientesActivos.mapNotNull { cliente ->
                val factorPaciencia = when (cliente.tipoCliente) {
                    TipoCliente.IMPACIENTE -> 2f
                    TipoCliente.VIP -> 0.5f
                    else -> 1f
                }

                val nuevaPaciencia = (cliente.paciencia - factorPaciencia).coerceAtLeast(0f)

                if (nuevaPaciencia <= 0f) {
                    val penalizacion = when (cliente.tipoCliente) {
                        TipoCliente.VIP -> 15
                        TipoCliente.IMPACIENTE -> 8
                        else -> 5
                    }
                    gameState = gameState.copy(dinero = (gameState.dinero - penalizacion).coerceAtLeast(0))
                    notificacion = "Cliente ${cliente.emoji} se fue. -$$penalizacion"
                    null
                } else {
                    cliente.copy(paciencia = nuevaPaciencia)
                }
            }
        }
    }

    // Timer de tiempo del juego (avanza cada minuto real = 1 hora del juego)
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000) // 1 minuto real = 1 hora del juego
            gameState = gameState.avanzarHora()

            // Notificaciones especiales según la hora
            when (gameState.hora) {
                8 -> if (pantallaActual == "cafe") notificacion = "¡Buenos días! El café abre"
                12 -> notificacion = "Hora del almuerzo - más clientes"
                20 -> notificacion = "Últimas horas del café"
                22 -> notificacion = "El café cierra. Ve a casa a descansar"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior mejorada
            BarraSuperiorMejorada(
                gameState = gameState,
                necesidades = necesidades,
                pantallaActual = pantallaActual,
                onCambiarPantalla = { pantallaActual = it },
                onMostrarMenu = { mostrarMenu = true }
            )

            // Notificación mejorada
            if (notificacion.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            notificacion.contains("¡") -> Color(0xFF4CAF50)
                            notificacion.contains("-$") -> Color(0xFFFF5722)
                            else -> Color(0xFF2196F3)
                        }
                    )
                ) {
                    Text(
                        text = notificacion,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Pantallas usando los nuevos screens
            when (pantallaActual) {
                "cafe" -> {
                    CafeScreen(
                        gameState = gameState,
                        necesidades = necesidades,
                        clientesActivos = clientesActivos,
                        clienteSeleccionado = clienteSeleccionado,
                        onGameStateChanged = { gameState = it },
                        onClientesChanged = { clientesActivos = it },
                        onClienteSeleccionado = { clienteSeleccionado = it },
                        onNotificacion = { notificacion = it }
                    )
                }
                "casa" -> {
                    CasaScreen(
                        gameState = gameState,
                        necesidades = necesidades,
                        onGameStateChanged = { gameState = it },
                        onNecesidadesChanged = { necesidades = it },
                        onNotificacion = { notificacion = it }
                    )
                }
            }
        }

        // Menú superpuesto mejorado
        if (mostrarMenu) {
            MenuPrincipalMejorado(
                gameState = gameState,
                onCerrarMenu = { mostrarMenu = false },
                onReiniciarJuego = {
                    gameState = GameState()
                    necesidades = Necesidades()
                    clientesActivos = emptyList()
                    clienteSeleccionado = null
                    mostrarMenu = false
                    notificacion = "Nuevo juego iniciado"
                }
            )
        }
    }

    // Limpiar notificaciones
    LaunchedEffect(notificacion) {
        if (notificacion.isNotEmpty()) {
            delay(4000)
            notificacion = ""
        }
    }
}

@Composable
fun BarraSuperiorMejorada(
    gameState: GameState,
    necesidades: Necesidades,
    pantallaActual: String,
    onCambiarPantalla: (String) -> Unit,
    onMostrarMenu: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8D6E63))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Primera fila: Menú, navegación, dinero
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menú hamburguesa
                Button(
                    onClick = onMostrarMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                ) {
                    Text("≡", fontSize = 16.sp, color = Color.White)
                }

                // Botones de navegación
                Row {
                    Button(
                        onClick = { onCambiarPantalla("cafe") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pantallaActual == "cafe") Color(0xFF5D4037) else Color(0xFFBCAAA4)
                        ),
                        enabled = gameState.esHoraTrabajo() || pantallaActual == "cafe"
                    ) {
                        Text("☕ Café", fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onCambiarPantalla("casa") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pantallaActual == "casa") Color(0xFF5D4037) else Color(0xFFBCAAA4)
                        )
                    ) {
                        Text("🏠 Casa", fontSize = 10.sp)
                    }
                }

                // Estado del dinero y nivel
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Nivel ${gameState.nivel}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💰 $${gameState.dinero}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Segunda fila: Estado del personaje y tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Personaje y estado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${gameState.personaje.humor} ${gameState.personaje.estado}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tiempo del juego
                Text(
                    text = when {
                        gameState.hora < 12 -> "🌅 ${gameState.hora}:00"
                        gameState.hora < 18 -> "☀️ ${gameState.hora}:00"
                        else -> "🌙 ${gameState.hora}:00"
                    } + " | Día ${gameState.dia}",
                    color = Color.White,
                    fontSize = 10.sp
                )

                // XP
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "XP: ${gameState.experiencia}/${gameState.calcularExperienciaParaSiguienteNivel()}",
                        fontSize = 8.sp,
                        color = Color.White
                    )
                    LinearProgressIndicator(
                        progress = gameState.experiencia.toFloat() / gameState.calcularExperienciaParaSiguienteNivel(),
                        modifier = Modifier.width(60.dp).height(3.dp),
                        color = Color(0xFFFFEB3B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tercera fila: Barras de energía y estado general
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Barra de energía
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 10.sp)
                    LinearProgressIndicator(
                        progress = gameState.personaje.energia / 100f,
                        modifier = Modifier.width(80.dp).height(4.dp),
                        color = if (gameState.personaje.energia > 50) Color.Green else Color.Red
                    )
                    Text(
                        "${gameState.personaje.energia.toInt()}%",
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }

                // Estado general de necesidades
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", fontSize = 10.sp)
                    LinearProgressIndicator(
                        progress = necesidades.calcularEstadoGeneral() / 100f,
                        modifier = Modifier.width(80.dp).height(4.dp),
                        color = when {
                            necesidades.calcularEstadoGeneral() > 70f -> Color(0xFF4CAF50)
                            necesidades.calcularEstadoGeneral() > 40f -> Color(0xFFFF9800)
                            else -> Color(0xFFE91E63)
                        }
                    )
                    Text(
                        "${necesidades.calcularEstadoGeneral().toInt()}%",
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MenuPrincipalMejorado(
    gameState: GameState,
    onCerrarMenu: () -> Unit,
    onReiniciarJuego: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(350.dp)
                .height(500.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "☕ Mi Café",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )

                // Estadísticas del juego
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Estadísticas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nivel: ${gameState.nivel}")
                        Text("Dinero: $${gameState.dinero}")
                        Text("Clientes servidos: ${gameState.clientesServidos}")
                        Text("Día actual: ${gameState.dia}")
                        Text("XP: ${gameState.experiencia}")
                    }
                }

                // Botones
                Button(
                    onClick = onCerrarMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Continuar Jugando", fontSize = 14.sp)
                }

                Button(
                    onClick = onReiniciarJuego,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text("Reiniciar Juego", fontSize = 14.sp)
                }

                // Instrucciones
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Cómo jugar:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🏠 Casa: Cuida tus necesidades (hambre, sueño, higiene)\n" +
                                    "☕ Café: Atiende clientes y gana dinero\n" +
                                    "👑 Clientes VIP: Más dinero, menos prisa\n" +
                                    "💰 Clientes generosos: Dan propina\n" +
                                    "⚡ Clientes impacientes: Se van rápido pero pagan más",
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}