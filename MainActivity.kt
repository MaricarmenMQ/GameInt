package com.example.mycafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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
                    JuegoCompletoMejorado()
                }
            }
        }
    }
}

@Composable
fun JuegoCompletoMejorado() {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Estados principales
    var pantallaActual by remember { mutableStateOf("cafe") }
    var mostrarMenu by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf(GameState()) }
    var necesidades by remember { mutableStateOf(Necesidades()) }
    var clientesActivos by remember { mutableStateOf(listOf<Cliente>()) }
    var clienteSeleccionado by remember { mutableStateOf<Int?>(null) }
    var notificacion by remember { mutableStateOf("") }
    var tiempoNotificacion by remember { mutableStateOf(0) }

    // Timer de degradación de energía y necesidades (mejorado)
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Cada 3 segundos, más dinámico

            if (pantallaActual == "cafe") {
                // En el café, desgaste más rápido pero con beneficios sociales
                val nuevoPersonaje = gameState.personaje.copy(
                    energia = (gameState.personaje.energia - 2.5f).coerceAtLeast(0f)
                ).also { it.actualizarEstado() }

                val nuevasNecesidades = necesidades.copy(
                    hambre = (necesidades.hambre - 1.5f).coerceAtLeast(0f),
                    sueño = (necesidades.sueño - 1f).coerceAtLeast(0f),
                    higiene = (necesidades.higiene - 0.4f).coerceAtLeast(0f),
                    diversion = (necesidades.diversion - 0.8f).coerceAtLeast(0f),
                    social = (necesidades.social + 1f).coerceAtMost(100f) // Trabajar es social
                )

                gameState = gameState.copy(personaje = nuevoPersonaje)
                necesidades = nuevasNecesidades
            } else {
                // En casa, desgaste lento
                val nuevoPersonaje = gameState.personaje.copy(
                    energia = (gameState.personaje.energia - 0.8f).coerceAtLeast(0f)
                ).also { it.actualizarEstado() }

                val nuevasNecesidades = necesidades.copy(
                    hambre = (necesidades.hambre - 0.8f).coerceAtLeast(0f),
                    higiene = (necesidades.higiene - 0.3f).coerceAtLeast(0f),
                    diversion = (necesidades.diversion - 0.5f).coerceAtLeast(0f)
                )

                gameState = gameState.copy(personaje = nuevoPersonaje)
                necesidades = nuevasNecesidades
            }

            // Sistema de alertas mejorado
            val alertaCritica = necesidades.necesidadCritica()
            if (alertaCritica != null && notificacion.isEmpty()) {
                notificacion = alertaCritica
                tiempoNotificacion = 5000 // 5 segundos para críticas
            }
        }
    }

    // Generador de clientes inteligente
    LaunchedEffect(pantallaActual, gameState.hora) {
        while (pantallaActual == "cafe" && gameState.esHoraTrabajo()) {
            val tiempoEspera = when (gameState.hora) {
                in 8..10 -> 12000L // Mañana tranquila
                in 11..13 -> 6000L  // Rush del almuerzo
                in 14..16 -> 10000L // Tarde moderada
                in 17..19 -> 7000L  // Rush de la tarde
                else -> 15000L      // Cierre tranquilo
            }

            delay(tiempoEspera)

            if (clientesActivos.size < gameState.cafeUpgrades.capacidadClientes) {
                val nuevoId = (clientesActivos.maxOfOrNull { it.id } ?: 0) + 1
                val nuevoCliente = ClienteFactory.crearClienteAleatorio(nuevoId)
                clientesActivos = clientesActivos + nuevoCliente
            }
        }
    }

    // Timer de paciencia optimizado
    LaunchedEffect(clientesActivos.size) {
        while (clientesActivos.isNotEmpty()) {
            delay(1000)
            clientesActivos = clientesActivos.mapNotNull { cliente ->
                val factorPaciencia = when (cliente.tipoCliente) {
                    TipoCliente.IMPACIENTE -> 2.2f
                    TipoCliente.VIP -> 0.6f
                    TipoCliente.GENEROSO -> 0.9f
                    else -> 1.2f
                }

                val nuevaPaciencia = (cliente.paciencia - factorPaciencia).coerceAtLeast(0f)

                if (nuevaPaciencia <= 0f) {
                    val penalizacion = when (cliente.tipoCliente) {
                        TipoCliente.VIP -> 20
                        TipoCliente.IMPACIENTE -> 12
                        TipoCliente.GENEROSO -> 8
                        else -> 6
                    }
                    gameState = gameState.copy(dinero = (gameState.dinero - penalizacion).coerceAtLeast(0))
                    notificacion = "😤 Cliente ${cliente.emoji} se fue molesto (-$$penalizacion)"
                    tiempoNotificacion = 4000
                    null
                } else {
                    cliente.copy(paciencia = nuevaPaciencia)
                }
            }
        }
    }

    // Timer de tiempo mejorado (45 segundos = 1 hora del juego)
    LaunchedEffect(Unit) {
        while (true) {
            delay(45000) // Tiempo más dinámico
            gameState = gameState.avanzarHora()

            // Eventos especiales según la hora
            when (gameState.hora) {
                8 -> {
                    if (pantallaActual == "cafe") {
                        notificacion = "🌅 ¡Buenos días! El café abre sus puertas"
                        tiempoNotificacion = 3000
                    }
                }
                12 -> {
                    notificacion = "🍽️ Hora del almuerzo - ¡Más clientes hambrientos!"
                    tiempoNotificacion = 3000
                }
                15 -> {
                    notificacion = "☕ Hora del café de la tarde"
                    tiempoNotificacion = 2000
                }
                20 -> {
                    notificacion = "🌆 Últimas horas del día en el café"
                    tiempoNotificacion = 3000
                }
                22 -> {
                    notificacion = "🌙 El café cierra. ¡Ve a casa a descansar!"
                    tiempoNotificacion = 4000
                }
                0 -> {
                    notificacion = "💤 Es muy tarde, deberías dormir"
                    tiempoNotificacion = 3000
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior ultra mejorada
            BarraSuperiorUltraMejorada(
                gameState = gameState,
                necesidades = necesidades,
                pantallaActual = pantallaActual,
                onCambiarPantalla = { pantallaActual = it },
                onMostrarMenu = { mostrarMenu = true },
                isTablet = isTablet
            )

            // Sistema de notificaciones mejorado
            if (notificacion.isNotEmpty()) {
                NotificacionMejorada(
                    mensaje = notificacion,
                    tipo = determinarTipoNotificacion(notificacion)
                )
            }

            // Pantallas principales
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
                        onNotificacion = {
                            notificacion = it
                            tiempoNotificacion = 4000
                        }
                    )
                }
                "casa" -> {
                    CasaScreen(
                        gameState = gameState,
                        necesidades = necesidades,
                        onGameStateChanged = { gameState = it },
                        onNecesidadesChanged = { necesidades = it },
                        onNotificacion = {
                            notificacion = it
                            tiempoNotificacion = 3000
                        }
                    )
                }
            }
        }

        // Menú overlay mejorado
        if (mostrarMenu) {
            MenuPrincipalUltraMejorado(
                gameState = gameState,
                necesidades = necesidades,
                onCerrarMenu = { mostrarMenu = false },
                onReiniciarJuego = {
                    gameState = GameState()
                    necesidades = Necesidades()
                    clientesActivos = emptyList()
                    clienteSeleccionado = null
                    mostrarMenu = false
                    notificacion = "🎮 ¡Nuevo juego iniciado! ¡A por ello!"
                    tiempoNotificacion = 3000
                },
                isTablet = isTablet
            )
        }
    }

    // Limpiar notificaciones con tiempo personalizado
    LaunchedEffect(notificacion, tiempoNotificacion) {
        if (notificacion.isNotEmpty() && tiempoNotificacion > 0) {
            delay(tiempoNotificacion.toLong())
            notificacion = ""
            tiempoNotificacion = 0
        }
    }
}

@Composable
fun BarraSuperiorUltraMejorada(
    gameState: GameState,
    necesidades: Necesidades,
    pantallaActual: String,
    onCambiarPantalla: (String) -> Unit,
    onMostrarMenu: () -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6D4C41) // Marrón café rico
        ),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isTablet) 16.dp else 12.dp)
        ) {
            // Primera fila: Control principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón menú mejorado
                Card(
                    modifier = Modifier
                        .size(if (isTablet) 50.dp else 45.dp)
                        .clickable { onMostrarMenu() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E342E)),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("≡", fontSize = if (isTablet) 20.sp else 18.sp, color = Color.White)
                    }
                }

                // Navegación mejorada
                Row {
                    BotonesNavegacionMejorados(
                        pantallaActual = pantallaActual,
                        gameState = gameState,
                        onCambiarPantalla = onCambiarPantalla,
                        isTablet = isTablet
                    )
                }

                // Estado financiero y nivel
                EstadoFinancieroMejorado(gameState, isTablet)
            }

            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

            // Segunda fila: Estado del personaje y tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado del personaje
                EstadoPersonajeMejorado(gameState, isTablet)

                // Reloj del juego mejorado
                RelojDelJuegoMejorado(gameState, isTablet)

                // Progreso de XP mejorado
                ProgresoXPMejorado(gameState, isTablet)
            }

            Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 6.dp))

            // Tercera fila: Barras de estado principales
            EstadoGeneralMejorado(gameState, necesidades, isTablet)
        }
    }
}

@Composable
fun BotonesNavegacionMejorados(
    pantallaActual: String,
    gameState: GameState,
    onCambiarPantalla: (String) -> Unit,
    isTablet: Boolean
) {
    val puedeIrAlCafe = gameState.esHoraTrabajo() || pantallaActual == "cafe"

    // Botón Café
    Button(
        onClick = { onCambiarPantalla("cafe") },
        enabled = puedeIrAlCafe,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pantallaActual == "cafe") Color(0xFF8D6E63) else Color(0xFF5D4037),
            disabledContainerColor = Color(0xFF757575)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(width = if (isTablet) 100.dp else 80.dp, height = if (isTablet) 40.dp else 35.dp)
    ) {
        Text(
            "☕ Café",
            fontSize = if (isTablet) 12.sp else 10.sp,
            color = Color.White,
            fontWeight = if (pantallaActual == "cafe") FontWeight.Bold else FontWeight.Normal
        )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Botón Casa
    Button(
        onClick = { onCambiarPantalla("casa") },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pantallaActual == "casa") Color(0xFF8D6E63) else Color(0xFF5D4037)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(width = if (isTablet) 100.dp else 80.dp, height = if (isTablet) 40.dp else 35.dp)
    ) {
        Text(
            "🏠 Casa",
            fontSize = if (isTablet) 12.sp else 10.sp,
            color = Color.White,
            fontWeight = if (pantallaActual == "casa") FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EstadoFinancieroMejorado(gameState: GameState, isTablet: Boolean) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        // Nivel con icono
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", fontSize = if (isTablet) 12.sp else 10.sp)
            Text(
                text = "Nivel ${gameState.nivel}",
                color = Color(0xFFFFEB3B),
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Dinero con efecto
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("💰", fontSize = if (isTablet) 14.sp else 12.sp)
            Text(
                text = "${gameState.dinero}",
                color = Color(0xFF4CAF50),
                fontSize = if (isTablet) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EstadoPersonajeMejorado(gameState: GameState, isTablet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = gameState.personaje.humor,
            fontSize = if (isTablet) 24.sp else 20.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = gameState.personaje.estado,
            color = Color.White,
            fontSize = if (isTablet) 14.sp else 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RelojDelJuegoMejorado(gameState: GameState, isTablet: Boolean) {
    val iconoHora = when {
        gameState.hora < 6 -> "🌙"
        gameState.hora < 12 -> "🌅"
        gameState.hora < 18 -> "☀️"
        else -> "🌆"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(iconoHora, fontSize = if (isTablet) 16.sp else 14.sp)
            Text(
                text = "${gameState.hora}:00",
                color = Color.White,
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Día ${gameState.dia}",
            color = Color(0xFFFFE0B2),
            fontSize = if (isTablet) 10.sp else 8.sp
        )
    }
}

@Composable
fun ProgresoXPMejorado(gameState: GameState, isTablet: Boolean) {
    val xpActual = gameState.experiencia
    val xpNecesaria = gameState.calcularExperienciaParaSiguienteNivel()

    Column(horizontalAlignment = Alignment.End) {
        Text(
            "XP: $xpActual/$xpNecesaria",
            fontSize = if (isTablet) 10.sp else 8.sp,
            color = Color.White
        )

        Box(
            modifier = Modifier
                .width(if (isTablet) 80.dp else 60.dp)
                .height(if (isTablet) 6.dp else 4.dp)
                .clip(RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(xpActual.toFloat() / xpNecesaria)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                    .background(Color(0xFFFFEB3B))
            )
        }
    }
}

@Composable
fun EstadoGeneralMejorado(gameState: GameState, necesidades: Necesidades, isTablet: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Energía
        BarraEstadoCompacta(
            icono = "⚡",
            valor = gameState.personaje.energia,
            color = when {
                gameState.personaje.energia > 60 -> Color(0xFF4CAF50)
                gameState.personaje.energia > 30 -> Color(0xFFFF9800)
                else -> Color(0xFFE91E63)
            },
            isTablet = isTablet
        )

        // Estado general
        BarraEstadoCompacta(
            icono = "❤️",
            valor = necesidades.calcularEstadoGeneral(),
            color = when {
                necesidades.calcularEstadoGeneral() > 70f -> Color(0xFF4CAF50)
                necesidades.calcularEstadoGeneral() > 40f -> Color(0xFFFF9800)
                else -> Color(0xFFE91E63)
            },
            isTablet = isTablet
        )

        // Clientes servidos (solo visible en café)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = if (isTablet) 14.sp else 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "${gameState.clientesServidos}",
                fontSize = if (isTablet) 12.sp else 10.sp,
                color = Color(0xFFFFEB3B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BarraEstadoCompacta(
    icono: String,
    valor: Float,
    color: Color,
    isTablet: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icono, fontSize = if (isTablet) 14.sp else 12.sp)
        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .width(if (isTablet) 60.dp else 50.dp)
                .height(if (isTablet) 6.dp else 4.dp)
                .clip(RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(valor / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "${valor.toInt()}%",
            fontSize = if (isTablet) 10.sp else 8.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NotificacionMejorada(
    mensaje: String,
    tipo: TipoNotificacion
) {
    val colores = when (tipo) {
        TipoNotificacion.EXITO -> listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
        TipoNotificacion.ADVERTENCIA -> listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
        TipoNotificacion.ERROR -> listOf(Color(0xFFE91E63), Color(0xFFEC407A))
        TipoNotificacion.INFO -> listOf(Color(0xFF2196F3), Color(0xFF42A5F5))
        TipoNotificacion.CRITICA -> listOf(Color(0xFFE57373), Color(0xFFEF5350))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors = colores)
                )
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (tipo) {
                        TipoNotificacion.EXITO -> "✅"
                        TipoNotificacion.ADVERTENCIA -> "⚠️"
                        TipoNotificacion.ERROR -> "❌"
                        TipoNotificacion.INFO -> "ℹ️"
                        TipoNotificacion.CRITICA -> "🚨"
                    },
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = mensaje,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MenuPrincipalUltraMejorado(
    gameState: GameState,
    necesidades: Necesidades,
    onCerrarMenu: () -> Unit,
    onReiniciarJuego: () -> Unit,
    isTablet: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onCerrarMenu() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(if (isTablet) 450.dp else 350.dp)
                .heightIn(max = if (isTablet) 700.dp else 550.dp)
                .clickable { /* Prevenir cierre al hacer click dentro */ },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTablet) 24.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título del juego
                Text(
                    text = "☕ Mi Café Virtual",
                    fontSize = if (isTablet) 28.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )

                Text(
                    text = "Simulador de vida y negocio",
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    color = Color(0xFF8D6E63)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Panel de estadísticas mejorado
                EstadisticasDelJuegoMejoradas(gameState, necesidades, isTablet)

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Button(
                    onClick = onCerrarMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 50.dp else 45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar Jugando", fontSize = if (isTablet) 16.sp else 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onReiniciarJuego,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 50.dp else 45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reiniciar Juego", fontSize = if (isTablet) 16.sp else 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guía rápida mejorada
                GuiaRapidaMejorada(isTablet)
            }
        }
    }
}

@Composable
fun EstadisticasDelJuegoMejoradas(gameState: GameState, necesidades: Necesidades, isTablet: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 Tu Progreso",
                fontSize = if (isTablet) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem("⭐", "Nivel", "${gameState.nivel}", Color(0xFFFFEB3B), isTablet)
                EstadisticaItem("💰", "Dinero", "${gameState.dinero}", Color(0xFF4CAF50), isTablet)
                EstadisticaItem("👥", "Servidos", "${gameState.clientesServidos}", Color(0xFF2196F3), isTablet)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem("🗓️", "Día", "${gameState.dia}", Color(0xFF9C27B0), isTablet)
                EstadisticaItem("🎯", "XP", "${gameState.experiencia}", Color(0xFFFF9800), isTablet)
                EstadisticaItem("❤️", "Bienestar", "${necesidades.calcularEstadoGeneral().toInt()}%", Color(0xFFE91E63), isTablet)
            }
        }
    }
}

@Composable
fun EstadisticaItem(icono: String, titulo: String, valor: String, color: Color, isTablet: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icono, fontSize = if (isTablet) 20.sp else 18.sp)
        Text(
            text = titulo,
            fontSize = if (isTablet) 10.sp else 9.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = valor,
            fontSize = if (isTablet) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun GuiaRapidaMejorada(isTablet: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "🎮 Guía Rápida",
                fontSize = if (isTablet) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val consejos = listOf(
                "🏠 Casa: Mantén tus necesidades básicas",
                "☕ Café: Atiende clientes y gana dinero",
                "👑 Clientes VIP: Más dinero, más paciencia",
                "💰 Clientes generosos: Dan propina",
                "⚡ Clientes impacientes: Rápidos pero pagan más",
                "🎯 Sube de nivel ganando experiencia",
                "⏰ Trabaja de 8 AM a 8 PM"
            )

            consejos.forEach { consejo ->
                Text(
                    text = consejo,
                    fontSize = if (isTablet) 12.sp else 10.sp,
                    lineHeight = if (isTablet) 16.sp else 14.sp,
                    color = Color(0xFF1565C0)
                )
            }
        }
    }
}

enum class TipoNotificacion {
    EXITO, ADVERTENCIA, ERROR, INFO, CRITICA
}

fun determinarTipoNotificacion(mensaje: String): TipoNotificacion {
    return when {
        mensaje.contains("¡") && (mensaje.contains("+") || mensaje.contains("genial") || mensaje.contains("bien")) -> TipoNotificacion.EXITO
        mensaje.contains("⚠️") || mensaje.contains("Sin") || mensaje.contains("crítica") -> TipoNotificacion.CRITICA
        mensaje.contains("-$") || mensaje.contains("se fue") -> TipoNotificacion.ERROR
        mensaje.contains("Hora") || mensaje.contains("Buenos") || mensaje.contains("cierra") -> TipoNotificacion.INFO
        else -> TipoNotificacion.ADVERTENCIA
    }
}