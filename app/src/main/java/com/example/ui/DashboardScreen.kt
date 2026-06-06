package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.EmbeddedNote

// Cybernetic Lab Theme Color Palettes
val CyberTeal = Color(0xFF00F0FF)      // High voltage indicator cyan
val SemiconductorAmber = Color(0xFFFF9E00) // Warming orange/amber logic pulse
val LaserRed = Color(0xFFFF0D57)       // Warning logic alert red
val SiliconGray = Color(0xFF1B222A)    // Dark board background
val CircuitGreen = Color(0xFF00E676)   // Ready indicator green
val BreadboardYellow = Color(0xFFFFEE58) // Warning blink Yellow
val BoardTraceColor = Color(0x3300F0FF) // Cyber traces

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: EmbeddedViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("Curriculum", Icons.Default.Menu),
        TabItem("Simulation Lab", Icons.Default.PlayArrow),
        TabItem("Bitwise Lab", Icons.Default.Settings),
        TabItem("AI Tutor", Icons.Default.Face),
        TabItem("Lab Notebook", Icons.Default.Star)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0C1015),
                contentColor = Color.White,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selectedTab == index) CyberTeal else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color.White else Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF1B222A)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFF070B0E)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Lab Bench Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1015))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .border(1.dp, Color(0x1A00F0FF), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CircuitGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EMBEDDED C BASICS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = "Interactive Local Hardware Lab Bench",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Voltage indicators / Status badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141F2B)),
                    border = BorderStroke(1.dp, Color(0x3300F0FF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VCC: 5.0V",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SemiconductorAmber
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> CurriculumTab(viewModel) { selectedTab = 1 } // callback to show simulator
                    1 -> SimulationLabTab(viewModel)
                    2 -> BitwiseLabTab(viewModel)
                    3 -> TutorChatTab(viewModel)
                    4 -> NotebookTab(viewModel)
                }
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)

// ==========================================
// 1. CURRICULUM TAB
// ==========================================
@Composable
fun CurriculumTab(
    viewModel: EmbeddedViewModel,
    onNavigateToSimulator: () -> Unit
) {
    val lessons = viewModel.lessons
    val currentLesson by viewModel.currentLesson.collectAsStateWithLifecycle()
    var showFullExplanation by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Lesson navigation column
        LazyColumn(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .background(Color(0xFF0A0F14)),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(lessons) { lesson ->
                val isSelected = lesson.id == currentLesson.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectLesson(lesson) }
                        .background(if (isSelected) Color(0xFF1B222A) else Color.Transparent)
                        .border(
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF00F0FF) else Color.Transparent
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LEVEL ${lesson.id}",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) CyberTeal else Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when(lesson.id) {
                            1 -> "Intro"
                            2 -> "Types"
                            3 -> "Bitwise-1"
                            4 -> "Bitwise-2"
                            5 -> "GPIO Out"
                            6 -> "GPIO In"
                            7 -> "Pointers"
                            8 -> "Timers"
                            9 -> "Interrupts"
                            10 -> "ADC/PWM"
                            else -> "Module"
                        },
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) Color.White else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Divider(color = Color(0xFF141A20))
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = Color(0xFF1A222A)
        )

        // Active Lesson Panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "LEVEL ${currentLesson.id}: ${currentLesson.title}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = currentLesson.description,
                color = CyberTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Divider(color = Color(0x3300F0FF), modifier = Modifier.padding(bottom = 12.dp))

            // Body text
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E141B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                border = BorderStroke(1.dp, Color(0xFF1C2836))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = currentLesson.content,
                        fontSize = 13.sp,
                        color = Color(0xFFD1D8E0),
                        lineHeight = 20.sp
                    )
                }
            }

            // Interactive C Code Console
            Text(
                text = "PRACTICAL HARDWARE ASSEMBLY CODE",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = SemiconductorAmber,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Code box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030609)),
                border = BorderStroke(1.dp, Color(0xFF1F3547)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "firmware.c",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SemiconductorAmber)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentLesson.codeExample,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CircuitGreen,
                        lineHeight = 18.sp
                    )
                }
            }

            // Code Explanation
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141F2B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            tint = CyberTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "How this affects Circuitry:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentLesson.exampleExplanation,
                        fontSize = 12.sp,
                        color = Color(0xFFABB9C7),
                        lineHeight = 18.sp
                    )
                }
            }

            // Quick launch simulation preset
            if (currentLesson.systemRegisterPreset != null) {
                Button(
                    onClick = {
                        onNavigateToSimulator()
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("curriculum_simulate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007A8A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberTeal)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Simulate",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST IN THE SIMULATOR NOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Interactive coding challenge card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D161E)),
                border = BorderStroke(1.dp, SemiconductorAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("interactive_challenge_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SemiconductorAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INTERACTIVE TOPIC CHALLENGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SemiconductorAmber
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currentLesson.codingProblemQuestion,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Input text field for their solution code
                    var userSolution by remember(currentLesson.id) { mutableStateOf(currentLesson.codingProblemInitialCode) }
                    var hasChecked by remember(currentLesson.id) { mutableStateOf(false) }
                    var isCorrect by remember(currentLesson.id) { mutableStateOf(false) }
                    var showHint by remember(currentLesson.id) { mutableStateOf(false) }
                    
                    TextField(
                        value = userSolution,
                        onValueChange = { 
                            userSolution = it
                            hasChecked = false 
                        },
                        placeholder = { Text("Complete the C statement...", fontSize = 12.sp, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("challenge_input"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = CircuitGreen,
                            unfocusedTextColor = CircuitGreen,
                            focusedContainerColor = Color(0xFF040B0F),
                            unfocusedContainerColor = Color(0xFF040B0F),
                            focusedIndicatorColor = SemiconductorAmber,
                            unfocusedIndicatorColor = Color(0xFF1F3547)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showHint = !showHint },
                            modifier = Modifier.testTag("challenge_hint_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (showHint) "HIDE HINT" else "NEED A HINT?",
                                color = CyberTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Button(
                            onClick = {
                                hasChecked = true
                                // Normalize comparison for Whitespace differences and trailing semicolons
                                val normalizedUser = userSolution.replace(" ", "").replace(";", "").replace("\n", "").trim().lowercase()
                                val normalizedSolution = currentLesson.codingProblemSolution.replace(" ", "").replace(";", "").replace("\n", "").trim().lowercase()
                                isCorrect = normalizedUser == normalizedSolution
                            },
                            modifier = Modifier.testTag("challenge_verify_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SemiconductorAmber),
                            shape = RoundedCornerShape(4.dp),
                            enabled = userSolution.trim().isNotEmpty()
                        ) {
                            Text("COMPILE & VERIFY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    
                    if (showHint) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14202B)),
                            border = BorderStroke(0.5.dp, CyberTeal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Hint: ${currentLesson.codingProblemHint}",
                                modifier = Modifier.padding(10.dp),
                                color = Color(0xFFABB9C7),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    if (hasChecked) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) Color(0xFF10261A) else Color(0xFF2E1218)
                            ),
                            border = BorderStroke(1.dp, if (isCorrect) CircuitGreen else LaserRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = "verify status",
                                        tint = if (isCorrect) CircuitGreen else LaserRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCorrect) "VERIFICATION MATCHED!" else "COMPILER ERROR / FAULT",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) CircuitGreen else LaserRed,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCorrect) {
                                        "Beautiful! Your C instruction compiles successfully and perfectly configures the hardware gates."
                                    } else {
                                        "Expected instruction: `${currentLesson.codingProblemSolution}`. Review the syntax, spacing, and casing!"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. SIMULATION LAB TAB
// ==========================================
@Composable
fun SimulationLabTab(viewModel: EmbeddedViewModel) {
    val presets = viewModel.simulationPresets
    val selectedPreset by viewModel.selectedPreset.collectAsStateWithLifecycle()
    val registers by viewModel.registers.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
    val currentLineIndex by viewModel.currentLineIndex.collectAsStateWithLifecycle()
    val isSwitchPressed by viewModel.isSwitchPressed.collectAsStateWithLifecycle()
    val potentiometerValue by viewModel.potentiometerValue.collectAsStateWithLifecycle()

    var showPresetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Preset selector card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F151C)),
            border = BorderStroke(1.dp, Color(0xFF1F2B39)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF172330)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedPreset?.name) {
                                "LED Blinker" -> Icons.Default.PlayArrow
                                "Switch Reader" -> Icons.Default.Check
                                "Active Alarm Sounder" -> Icons.Default.Notifications
                                else -> Icons.Default.Settings
                            },
                            contentDescription = "Preset icon",
                            tint = CyberTeal
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = selectedPreset?.name ?: "No Preset Loaded",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = selectedPreset?.description ?: "",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = { showPresetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2936)),
                    border = BorderStroke(1.dp, Color(0x6600F0FF)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("PRESETS", color = CyberTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- HARDWARE VISUALIZATION BOARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141B22)),
            border = BorderStroke(1.dp, Color(0xFF283647)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = "HARDWARE DEVELOPMENT BOARD",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Breadboard simulation layouts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090F14))
                        .padding(12.dp)
                ) {
                    // Traces or graphics inside. Let's align MCU in central block, LEDs & Buzzer on right
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Virtual Integrated Circuit (Microcontroller Chip styling)
                            Row(
                                modifier = Modifier
                                    .border(1.5.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .background(Color(0xFF111111))
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ATMEGA328P",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Tiny notch
                                Box(
                                    modifier = Modifier
                                        .size(6.dp, 12.dp)
                                        .background(Color.DarkGray)
                                )
                            }

                            // Output physical components visual arrays
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Onboard LED 5 (Yellow)
                                val isLed5On = (registers.ddrb and 0x20 != 0) && (registers.portb and 0x20 != 0)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isLed5On) BreadboardYellow else Color(0xFF222510))
                                            .border(
                                                2.dp,
                                                if (isLed5On) BreadboardYellow else Color.Gray,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("LED 5", fontSize = 9.sp, color = if (isLed5On) BreadboardYellow else Color.Gray)
                                }

                                // Warning Red LED 4
                                val isLed4On = (registers.ddrb and 0x10 != 0) && (registers.portb and 0x10 != 0)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isLed4On) LaserRed else Color(0xFF330910))
                                            .border(2.dp, if (isLed4On) LaserRed else Color.Gray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("LED 4", fontSize = 9.sp, color = if (isLed4On) LaserRed else Color.Gray)
                                }

                                // Interactive Active sounder buzzer on Pin 3
                                val isBuzzerOn = (registers.ddrb and 0x08 != 0) && (registers.portb and 0x08 != 0)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isBuzzerOn) CyberTeal else Color(0xFF0C2630))
                                            .border(2.dp, if (isBuzzerOn) CyberTeal else Color.Gray, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "🔊",
                                            fontSize = 11.sp,
                                            color = if (isBuzzerOn) Color.Black else Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("BUZZ 3", fontSize = 9.sp, color = if (isBuzzerOn) CyberTeal else Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interaction elements inside development board: Slide switch and potentiometer dials
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Slide switch input
                            Column {
                                Text(
                                    text = "PHYSICAL SWITCH INPUT (Pin 2 / Pullup active)",
                                    fontSize = 9.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Button(
                                    onClick = { viewModel.setSwitchPressed(!isSwitchPressed) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSwitchPressed) LaserRed else Color(0xFF1B2936)
                                    ),
                                    border = BorderStroke(1.dp, if (isSwitchPressed) LaserRed else CyberTeal),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = if (isSwitchPressed) "SWITCH: GND (PRESSED/0)" else "SWITCH: OPEN (RELEASED/1)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSwitchPressed) Color.White else CyberTeal
                                    )
                                }
                            }

                            // Potentiometer Analog input (level gauge)
                            Column(
                                modifier = Modifier.width(130.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("POT (Pin 1/A0)", fontSize = 9.sp, color = Color.Gray)
                                    Text("${(potentiometerValue * 1023 / 255)}", fontSize = 10.sp, color = SemiconductorAmber, fontFamily = FontFamily.Monospace)
                                }
                                Slider(
                                    value = potentiometerValue.toFloat(),
                                    onValueChange = { viewModel.setPotentiometer(it.toInt()) },
                                    valueRange = 0f..255f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = SemiconductorAmber,
                                        activeTrackColor = SemiconductorAmber.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.height(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- DIRECT REGISTER BIT-MAP MAP ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121820)),
            border = BorderStroke(1.dp, Color(0xFF223041)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "I/O BIT REGISTER MAP - PORTB DIRECTORY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Registers rows: DDRB, PORTB, PINB
                RegisterVisualRow(
                    label = "DDRB (Direction)",
                    value = registers.ddrb,
                    subtext = "1=Out, 0=In"
                )
                Spacer(modifier = Modifier.height(8.dp))
                RegisterVisualRow(
                    label = "PORTB (Data Out)",
                    value = registers.portb,
                    subtext = "Output Volts"
                )
                Spacer(modifier = Modifier.height(8.dp))
                RegisterVisualRow(
                    label = "PINB (Data In)",
                    value = registers.pinb,
                    subtext = "Physical Read"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Small map showing Bit indexes to physical attachments
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF091016))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Pin Attachments: ", fontSize = 9.sp, color = Color.LightGray, fontFamily = FontFamily.Monospace)
                    Text("Bit 5 -> LED5", fontSize = 9.sp, color = BreadboardYellow, fontFamily = FontFamily.Monospace)
                    Text("Bit 4 -> LED4", fontSize = 9.sp, color = LaserRed, fontFamily = FontFamily.Monospace)
                    Text("Bit 3 -> Buzzer", fontSize = 9.sp, color = CyberTeal, fontFamily = FontFamily.Monospace)
                    Text("Bit 2 -> Switch", fontSize = 9.sp, color = CircuitGreen, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // --- EMULATOR SOURCE CONSOLE INTERFACE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030508)),
            border = BorderStroke(1.dp, Color(0xFF1B2C3C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSimulating) CircuitGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interactive Compiler Source Code Console",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }

                    // Simulation controllers
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.stepSimulation() },
                            enabled = !isSimulating,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF141D26), RoundedCornerShape(4.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Step",
                                tint = if (isSimulating) Color.DarkGray else CyberTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isSimulating) viewModel.stopSimulation() else viewModel.startSimulation()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (isSimulating) LaserRed.copy(alpha = 0.2f) else CyberTeal.copy(
                                        alpha = 0.2f
                                    ), RoundedCornerShape(4.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                tint = if (isSimulating) LaserRed else CyberTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The lines of source code console, highlighting currently active index
                val presetLines = selectedPreset?.codeLines ?: emptyList()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF05090F))
                        .padding(8.dp)
                        .border(1.dp, Color(0xFF14202E), RoundedCornerShape(4.dp))
                ) {
                    presetLines.forEachIndexed { idx, line ->
                        val isCurrentLine = idx == currentLineIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isCurrentLine) Color(0xFF102130) else Color.Transparent)
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCurrentLine) "▶ " else "  ",
                                color = SemiconductorAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = line.lineCode,
                                color = if (isCurrentLine) Color.White else CircuitGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isCurrentLine) {
                            // Print debug description of registers alter action
                            Text(
                                text = "  // ACTION: " + line.comment,
                                color = SemiconductorAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 18.dp, bottom = 4.dp, end = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Presets Dialog
    if (showPresetDialog) {
        Dialog(onDismissRequest = { showPresetDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C121A)),
                border = BorderStroke(1.dp, CyberTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELECT HARDWARE PRESET",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    presets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loadPreset(preset)
                                    showPresetDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1B2836)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (preset.name) {
                                        "LED Blinker" -> Icons.Default.PlayArrow
                                        "Switch Reader" -> Icons.Default.Check
                                        "Active Alarm Sounder" -> Icons.Default.Notifications
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = "icon",
                                    tint = CyberTeal
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(preset.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(preset.description, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Divider(color = Color(0xFF1A222C))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showPresetDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CLOSE", color = CyberTeal)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterVisualRow(label: String, value: Int, subtext: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1016))
            .border(1.dp, Color(0xFF192534), RoundedCornerShape(4.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text(subtext, color = Color.Gray, fontSize = 9.sp)
        }

        // Show entire 8 bits visually
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 7 downTo 0) {
                val bitState = (value shr i) and 1
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (bitState == 1) CyberTeal else Color(0xFF141F2B))
                        .border(
                            1.dp,
                            if (bitState == 1) CyberTeal else Color.DarkGray,
                            RoundedCornerShape(2.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$bitState",
                        color = if (bitState == 1) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Hex display
        Text(
            text = "0x" + String.format("%02X", value),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SemiconductorAmber
        )
    }
}


// ==========================================
// 3. BITWISE LAB TAB
// ==========================================
@Composable
fun BitwiseLabTab(viewModel: EmbeddedViewModel) {
    val valA by viewModel.bitwiseValA.collectAsStateWithLifecycle()
    val valB by viewModel.bitwiseValB.collectAsStateWithLifecycle()
    val operator by viewModel.selectedOperator.collectAsStateWithLifecycle()
    val shiftAmount by viewModel.shiftAmount.collectAsStateWithLifecycle()
    val result = viewModel.getBitwiseResult()

    val ops = listOf("&", "|", "^", "~", "<<", ">>")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11171E)),
            border = BorderStroke(1.dp, Color(0xFF263345)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "BITWISE OPERATOR GATE PORTAL",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "Click to toggle individual bits of Registers A & B, then select an operator to observe simulated logical evaluations.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // REGISTER A INTERACTION Row
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F151C)),
            border = BorderStroke(1.dp, Color(0xFF1E2F40)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("REGISTER A", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyberTeal, fontFamily = FontFamily.Monospace)
                    Text("DEC: $valA  |  HEX: 0x${String.format("%02X", valA)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SemiconductorAmber)
                }
                Spacer(modifier = Modifier.height(8.dp))
                BitInteractiveRow(value = valA, onBitToggled = { viewModel.toggleBitA(it) })
            }
        }

        // SELECT OPERATOR ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ops.forEach { op ->
                Button(
                    onClick = { viewModel.setOperator(op) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (operator == op) CyberTeal else Color(0xFF1B232D)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (operator == op) CyberTeal else Color.Gray),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = op,
                        fontWeight = FontWeight.Bold,
                        color = if (operator == op) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (operator == "<<" || operator == ">>") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bit shift amount:", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..4) {
                        Button(
                            onClick = { viewModel.setShiftAmount(i) },
                            modifier = Modifier.size(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (shiftAmount == i) SemiconductorAmber else Color(0xFF1B232D)
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("$i", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // REGISTER B INTERACTION ROW (Only if NOT a unary NOT operation)
        if (operator != "~") {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F151C)),
                border = BorderStroke(1.dp, Color(0xFF1E2F40)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("REGISTER B", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyberTeal, fontFamily = FontFamily.Monospace)
                        Text("DEC: $valB  |  HEX: 0x${String.format("%02X", valB)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SemiconductorAmber)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BitInteractiveRow(value = valB, onBitToggled = { viewModel.toggleBitB(it) })
                }
            }
        }

        // RESULT DISPLAY
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF15221B)),
            border = BorderStroke(1.dp, CircuitGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MATH OUTPUT RESULT",
                        fontWeight = FontWeight.Bold,
                        color = CircuitGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DEC: $result  |  HEX: 0x${String.format("%02X", result)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bit result block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 7 downTo 0) {
                        val bitState = (result shr i) and 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(30.dp)
                        ) {
                            Text("B$i", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (bitState == 1) CircuitGreen else Color(0xFF0F1411))
                                    .border(1.dp, if (bitState == 1) CircuitGreen else Color.DarkGray)
                            ) {
                                Text(
                                    text = "$bitState",
                                    color = if (bitState == 1) Color.Black else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }

        // EQUIVALENT C CODE MASK CONFIGURATOR
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020406)),
            border = BorderStroke(1.dp, Color(0xFF1E2E3C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "AUTO-GENERATED EMBEDDED C SYNTAX",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SemiconductorAmber,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val codeSyntax = when (operator) {
                    "&" -> """
                        // Bitwise masking: Clear bits using Register A & B
                        uint8_t result = REG_A & REG_B; // resolves to 0x${String.format("%02X", result)}
                    """.trimIndent()
                    "|" -> """
                        // Force set bits using bitwise OR logic
                        PORTB |= 0x${String.format("%02X", valA)}; // Set pins matching Register A
                    """.trimIndent()
                    "^" -> """
                        // Toggle pins matching mask on of Port B
                        PORTB ^= 0x${String.format("%02X", valA)}; // Toggle Pin lines
                    """.trimIndent()
                    "~" -> """
                        // Bitwise NEGATE mask logic
                        uint8_t inverted = ~0x${String.format("%02X", valA)}; // Resolves to 0x${String.format("%02X", result)}
                    """.trimIndent()
                    "<<" -> """
                        // Multiply/Shift logic leftward
                        uint8_t shifted = 0x${String.format("%02X", valA)} << $shiftAmount; // Resolves to 0x${String.format("%02X", result)}
                    """.trimIndent()
                    ">>" -> """
                        // Divide/Shift logic rightward
                        uint8_t shifted = 0x${String.format("%02X", valA)} >> $shiftAmount; // Resolves to 0x${String.format("%02X", result)}
                    """.trimIndent()
                    else -> ""
                }

                Text(
                    text = codeSyntax,
                    color = CircuitGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF05080C))
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun BitInteractiveRow(value: Int, onBitToggled: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 7 downTo 0) {
            val bitState = (value shr i) and 1
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Bit $i",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (bitState == 1) CyberTeal else Color(0xFF14202D))
                        .border(
                            1.dp,
                            if (bitState == 1) CyberTeal else Color.Gray,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onBitToggled(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$bitState",
                        color = if (bitState == 1) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}


// ==========================================
// 4. AI TUTOR CHAT TAB
// ==========================================
@Composable
fun TutorChatTab(viewModel: EmbeddedViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatInputText by viewModel.chatInputText.collectAsStateWithLifecycle()
    val chatLoading by viewModel.chatLoading.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    // Scroll to bottom when messages list size updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = listOf(
        "Explain volatile vs static",
        "How do I set DDRB registers?",
        "Explain hardware Interrupts",
        "How Timer prescales work"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Conversation Feed
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                val isTutor = message.sender == Sender.Tutor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = if (isTutor) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTutor) Color(0xFF1B232E) else Color(0xFF005F6A)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isTutor) Color(0xFF283647) else CyberTeal
                        ),
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = if (isTutor) 0.dp else 8.dp,
                            bottomEnd = if (isTutor) 8.dp else 0.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Header label
                            Text(
                                text = if (isTutor) "⚡ AI MCU INSTRUCTOR" else "STUDENT",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isTutor) CyberTeal else BreadboardYellow,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = message.message,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            if (chatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A22)),
                            modifier = Modifier.width(140.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = CyberTeal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MCU active...", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Suggestions chips row
        Text(
            text = "SUGGESTED STUDY TOPICS:",
            fontSize = 9.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            items(suggestionChips) { chip ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { viewModel.populateChatPrompt(chip) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111922)),
                    border = BorderStroke(1.dp, Color(0xFF233547))
                ) {
                    Text(
                        text = chip,
                        fontSize = 11.sp,
                        color = CyberTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Input send frame (touch sizes are at least 48dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0F14))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = chatInputText,
                onValueChange = { viewModel.updateChatInput(it) },
                placeholder = { Text("Ask about registers, bits...", fontSize = 13.sp, color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF141A22),
                    unfocusedContainerColor = Color(0xFF141A22),
                    focusedIndicatorColor = CyberTeal
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.sendChatMessage() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberTeal),
                enabled = !chatLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}


// ==========================================
// 5. LAB NOTEBOOK & NOTES TAB
// ==========================================
@Composable
fun NotebookTab(viewModel: EmbeddedViewModel) {
    val notesList by viewModel.savedNotes.collectAsStateWithLifecycle()
    val noteTitle by viewModel.noteTitle.collectAsStateWithLifecycle()
    val noteCode by viewModel.noteCode.collectAsStateWithLifecycle()
    val noteText by viewModel.noteText.collectAsStateWithLifecycle()
    val editingNoteId by viewModel.editingNoteId.collectAsStateWithLifecycle()

    var showQuizDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Quick Quiz CTA banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1C0E)),
            border = BorderStroke(1.dp, SemiconductorAmber),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TEST YOUR HARDWARE LOGIC",
                        fontWeight = FontWeight.Bold,
                        color = SemiconductorAmber,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Take a quick interactive assessment on bitwise masking and registers.",
                        fontSize = 11.sp,
                        color = Color.White,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showQuizDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SemiconductorAmber),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("QUIZ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // CREATE NEW NOTE FORM CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F151C)),
            border = BorderStroke(1.dp, Color(0xFF1F2B39)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (editingNoteId == null) "SAVE NEW LAB WORK SNIPPET" else "EDIT LAB WORK SNIPPET",
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Title field of touch size
                TextField(
                    value = noteTitle,
                    onValueChange = { viewModel.updateNoteFields(it, noteCode, noteText, editingNoteId) },
                    label = { Text("Note Title (e.g. Blinking configuration)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .heightIn(min = 48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141F2B)
                    )
                )

                // Register code snippet configurations
                TextField(
                    value = noteCode,
                    onValueChange = { viewModel.updateNoteFields(noteTitle, it, noteText, editingNoteId) },
                    label = { Text("Register Code (e.g. DDRB |= 0x20;)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .heightIn(min = 48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = CircuitGreen,
                        unfocusedTextColor = CircuitGreen,
                        focusedContainerColor = Color(0xFF040B0F)
                    )
                )

                // Comments column Note block
                TextField(
                    value = noteText,
                    onValueChange = { viewModel.updateNoteFields(noteTitle, noteCode, it, editingNoteId) },
                    label = { Text("Personal Learning Notes / Explanations", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .heightIn(min = 80.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141F2B)
                    )
                )

                // Action controls: Save/Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (editingNoteId != null) {
                        TextButton(
                            onClick = { viewModel.clearNoteFields() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("CANCEL", color = LaserRed)
                        }
                    }
                    Button(
                        onClick = { viewModel.saveCurrentNote() },
                        colors = ButtonDefaults.buttonColors(containerColor = CircuitGreen),
                        shape = RoundedCornerShape(4.dp),
                        enabled = noteTitle.trim().isNotEmpty()
                    ) {
                        Text("SAVE NOTE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // PREVIOUS ENTRIES LOG
        Text(
            text = "LAB HISTORY DATA ENTRIES: " + notesList.size,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (notesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E13))
                    .padding(24.dp)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved notes yet. Document your bit-mask configurations, register values, or custom C routines here!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            notesList.forEach { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11171E)),
                    border = BorderStroke(1.dp, Color(0xFF1E2F40)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.updateNoteFields(
                                            note.title,
                                            note.codeSnippet,
                                            note.notes,
                                            note.id
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "edit",
                                        tint = CyberTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteNote(note) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "delete",
                                        tint = LaserRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (note.codeSnippet.isNotEmpty()) {
                            Text(
                                text = note.codeSnippet,
                                color = CircuitGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF04070A))
                                    .padding(6.dp)
                                    .border(1.dp, Color(0xFF162330))
                                    .padding(4.dp)
                            )
                        }

                        if (note.notes.isNotEmpty()) {
                            Text(
                                text = note.notes,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Quiz modal overlay dialog
    if (showQuizDialog) {
        QuizDialog(viewModel) { showQuizDialog = false }
    }
}


@Composable
fun QuizDialog(viewModel: EmbeddedViewModel, onDismiss: () -> Unit) {
    val qIdx by viewModel.currentQuizIndex.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val status by viewModel.quizStatus.collectAsStateWithLifecycle()
    val answerIdx by viewModel.selectedQuizAnswerIndex.collectAsStateWithLifecycle()

    val currentQ = viewModel.quizQuestions[qIdx]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E141C)),
            border = BorderStroke(1.5.dp, SemiconductorAmber),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header progress tracking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARDWARE READINESS EVALUATION",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SemiconductorAmber
                    )
                    Text(
                        text = "Score: $score/${viewModel.quizQuestions.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question indicator
                Text(
                    text = "Question ${qIdx + 1} of ${viewModel.quizQuestions.size}",
                    fontSize = 12.sp,
                    color = CyberTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentQ.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Options layout
                currentQ.options.forEachIndexed { idx, opt ->
                    val isSelected = answerIdx == idx
                    val isCorrectColorState = if (status is EmbeddedViewModel.QuizStatus.Answered) {
                        val activeStatus = status as EmbeddedViewModel.QuizStatus.Answered
                        if (idx == activeStatus.rightIdx) CircuitGreen else if (isSelected && !activeStatus.isCorrect) LaserRed else Color(0xFF1B2635)
                    } else if (isSelected) CyberTeal else Color(0xFF1B2635)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable(enabled = status is EmbeddedViewModel.QuizStatus.Unanswered) {
                                viewModel.selectQuizAnswer(idx)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (status is EmbeddedViewModel.QuizStatus.Answered) {
                                val activeStatus = status as EmbeddedViewModel.QuizStatus.Answered
                                if (idx == activeStatus.rightIdx) CircuitGreen.copy(alpha = 0.15f)
                                else if (isSelected && !activeStatus.isCorrect) LaserRed.copy(alpha = 0.15f)
                                else Color(0xFF182330)
                            } else if (isSelected) CyberTeal.copy(alpha = 0.15f) else Color(0xFF141F2B)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (status is EmbeddedViewModel.QuizStatus.Answered) {
                                val activeStatus = status as EmbeddedViewModel.QuizStatus.Answered
                                if (idx == activeStatus.rightIdx) CircuitGreen
                                else if (isSelected && !activeStatus.isCorrect) LaserRed
                                else Color.DarkGray
                            } else if (isSelected) CyberTeal else Color.DarkGray
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (idx) {
                                    0 -> "A.  "
                                    1 -> "B.  "
                                    2 -> "C.  "
                                    3 -> "D.  "
                                    else -> "  "
                                },
                                color = if (isSelected) CyberTeal else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = opt,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Answer explanation blocks
                if (status is EmbeddedViewModel.QuizStatus.Answered) {
                    val activeStatus = status as EmbeddedViewModel.QuizStatus.Answered
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132029)),
                        border = BorderStroke(1.dp, Color(0xFF223547)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (activeStatus.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = "status",
                                    tint = if (activeStatus.isCorrect) CircuitGreen else LaserRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (activeStatus.isCorrect) "CORRECT LOGIC FLAG MATCHED!" else "HARDWARE FAULT DETECTED!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (activeStatus.isCorrect) CircuitGreen else LaserRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeStatus.explanation,
                                fontSize = 12.sp,
                                color = Color(0xFFC5D1DE),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Call to actions (submit or proceed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CLOSE LAB ASSESS", color = Color.Gray, fontSize = 12.sp)
                    }

                    if (status is EmbeddedViewModel.QuizStatus.Unanswered) {
                        Button(
                            onClick = { viewModel.submitQuizAnswer() },
                            enabled = answerIdx != null,
                            colors = ButtonDefaults.buttonColors(containerColor = SemiconductorAmber),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("SUBMIT RESPONSE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            val isLast = qIdx + 1 == viewModel.quizQuestions.size
                            Text(
                                text = if (isLast) "RESTART LAB EVAL" else "NEXT QUESTION",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
