package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EmbeddedNote
import com.example.data.EmbeddedNoteRepository
import com.example.data.GeminiClient
import com.example.data.GeminiContent
import com.example.data.GeminiPart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- Course Data Models ---
data class Lesson(
    val id: Int,
    val title: String,
    val description: String,
    val content: String,
    val codeExample: String,
    val exampleExplanation: String,
    val systemRegisterPreset: String? = null, // Associates a direct simulation preset
    val codingProblemQuestion: String,
    val codingProblemInitialCode: String,
    val codingProblemSolution: String,
    val codingProblemHint: String
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val detailedExplanation: String
)

// --- Register Simulator Models ---
data class SimulatedRegisters(
    val ddrb: Int = 0x00,    // Data Direction Register B (0 = Input, 1 = Output)
    val portb: Int = 0x00,   // Port B Output Data Register (If output, 1 = High. If input, 1 = Pullup)
    val pinb: Int = 0x04     // Pin B Input Register (Reads hardware states. Default 0x04 means Pin 2 Switch is High/Released)
)

data class SimulatorCodeLine(
    val lineCode: String,
    val comment: String,
    val effectOnRegisters: (SimulatedRegisters) -> SimulatedRegisters
)

data class SimulationPreset(
    val name: String,
    val icon: String,
    val description: String,
    val sourceCode: String,
    val codeLines: List<SimulatorCodeLine>
)

class EmbeddedViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EmbeddedNoteRepository(db.embeddedNoteDao())

    // --- Offline Notes (Room Flow) ---
    val savedNotes: StateFlow<List<EmbeddedNote>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Notes editing states
    private val _noteTitle = MutableStateFlow("")
    val noteTitle = _noteTitle.asStateFlow()

    private val _noteCode = MutableStateFlow("")
    val noteCode = _noteCode.asStateFlow()

    private val _noteText = MutableStateFlow("")
    val noteText = _noteText.asStateFlow()

    private val _editingNoteId = MutableStateFlow<Int?>(null)
    val editingNoteId = _editingNoteId.asStateFlow()

    fun updateNoteFields(title: String, code: String, notes: String, editingId: Int? = null) {
        _noteTitle.value = title
        _noteCode.value = code
        _noteText.value = notes
        _editingNoteId.value = editingId
    }

    fun saveCurrentNote() {
        val title = _noteTitle.value.trim()
        val code = _noteCode.value.trim()
        val notes = _noteText.value.trim()

        if (title.isEmpty()) return

        viewModelScope.launch {
            val id = _editingNoteId.value
            if (id == null) {
                repository.insert(EmbeddedNote(title = title, codeSnippet = code, notes = notes))
            } else {
                repository.update(EmbeddedNote(id = id, title = title, codeSnippet = code, notes = notes))
            }
            clearNoteFields()
        }
    }

    fun deleteNote(note: EmbeddedNote) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun clearNoteFields() {
        _noteTitle.value = ""
        _noteCode.value = ""
        _noteText.value = ""
        _editingNoteId.value = null
    }

    // --- 1. Lessons Data ---
    val lessons = listOf(
        Lesson(
            id = 1,
            title = "Introduction: Bare-metal C Structure",
            description = "Start from scratch. Understand memory constraints & infinite loops.",
            content = """
                Welcome to Embedded C programming! 
                
                Standard C runs on high-power general computer processors. It executes on top of an Operating System like Windows or Linux, managed safely by memory managers.
                
                Embedded C executes directly on low-power Microcontrollers (MCUs) (like AVR, STM32, PIC, ESP32) with no OS! You interact directly with physical silicon gates and copper trace pins.
                
                Key Architectural Constraints you must know at 0%:
                • Minimal SRAM vs Flash (MCUs often have less than 2KB of RAM!)
                • Hardware address registries map memory direct to physical electrical pins.
                • Continuous Infinite loops: While general computer programs exit when finished, embedded units run 'while(1)' indefinitely until physical power is disconnected.
                
                The Compile Pipeline:
                1. Preprocessor: Resolves #defines and imports.
                2. Compiler: Translates C to Assembler.
                3. Assembler: Translates Assembly to Binary machine code.
                4. Linker: Positions the compiled code blocks into physical memory addresses, generating a HEX file.
            """.trimIndent(),
            codeExample = """
                #include <avr/io.h> // Physical register definitions
                
                int main(void) {
                    // 1. Initialization section
                    // Set pins as outputs or inputs here
                    
                    while (1) {
                        // 2. Loop forever
                        // Continuously monitor inputs and set outputs
                    }
                    return 0; // Never actually reached in embedded
                }
            """.trimIndent(),
            exampleExplanation = "This is the bare skeleton. The main function contains an initialization block followed by an infinite while(1) loop to ensure the MCU never terminates its run.",
            systemRegisterPreset = "LED Blinker",
            codingProblemQuestion = "Write an infinite loop statement in Embedded C using the 'while' construct to execute code continuously.",
            codingProblemInitialCode = "while (...) { }",
            codingProblemSolution = "while(1)",
            codingProblemHint = "Any non-zero integer evaluated inside while(...) counts as true. The most common standard is 1."
        ),
        Lesson(
            id = 2,
            title = "Variables & Fixed-width Integer Types",
            description = "Why standard 'int' is dangerous. Introduce precise definitions.",
            content = """
                In standard computer programming, a standard 'int' can span 16, 32, or 64 bits depending entirely on the machine. This is a critical hazard for Microcontrollers!
                
                If an integer overflows, your timers and control lines fail. To prevent this, Embedded C relies on clear, fixed-width integers declared in <stdint.h>:
                • uint8_t: Unsigned 8-bit integer (Range: 0 to 255)
                • int8_t: Signed 8-bit integer (Range: -128 to 127)
                • uint16_t: Unsigned 16-bit integer (Range: 0 to 65,535)
                • uint32_t: Unsigned 32-bit integer (Range: 0 to ~4.2 Billion)
                • float: Standard decimal floating-width (avoid on tiny MCUs without hardware floating-point units!)
                
                Always declare the smallest integer container you need. This leaves vital bytes open in SRAM limit pools.
            """.trimIndent(),
            codeExample = """
                #include <stdint.h>
                
                uint8_t ledSetting = 120; // Uses exactly 1 byte
                uint16_t loopCounter = 10000; // Uses exactly 2 bytes
                
                void process() {
                    // Safe from 8-bit overflow errors
                    ledSetting = ledSetting + 1; 
                }
            """.trimIndent(),
            exampleExplanation = "Using explicit uint8_t and uint16_t guarantees that your memory size remains identical whether you compile for an 8-bit ATmega or a 32-bit ARM chip.",
            systemRegisterPreset = "LED Blinker",
            codingProblemQuestion = "Declare an unsigned 8-bit integer named 'motorSpeed' and initialize it with a status value of 150.",
            codingProblemInitialCode = "... motorSpeed = 150;",
            codingProblemSolution = "uint8_t motorSpeed = 150;",
            codingProblemHint = "Use fixed-width 'uint8_t' for an unsigned 8-bit variable container."
        ),
        Lesson(
            id = 3,
            title = "Bitwise Operations: OR Masks",
            description = "Set specific port pins high without overwriting neighbors.",
            content = """
                A Microcontroller groups pins into 8-bit registers. Writing directly like 'PORTB = 0x20' forces Bit 5 high, but turns all other pins to 0! This destroys neighboring states.
                
                To configure one pin selectively, we use Bitwise Operators:
                • Shift Operator (<<): Shifts binary bits left. '1 << 5' generates binary '00100000' (making a mask on Bit 5).
                • Logical OR (|): Output is 1 if either bit is 1. Since 'X OR 0 = X' and 'X OR 1 = 1', ORing with a mask forces the target bit to 1, leaving other bits unchanged!
                
                Set pin formula: PORT_REG |= (1 << PIN_NUMBER);
            """.trimIndent(),
            codeExample = """
                #define ALARM_PIN 3
                
                void activateAlarm() {
                    // Set Alarm Pin 3 high. Leave other pins intact!
                    PORTB |= (1 << ALARM_PIN);
                }
            """.trimIndent(),
            exampleExplanation = "1 << 3 produces 00001000. ORing this mask onto PORTB changes Bit 3 high, keeping the status of Bits 7, 6, 5, 4, 2, 1, and 0 perfectly untouched.",
            systemRegisterPreset = "Active Alarm Sounder",
            codingProblemQuestion = "Write a C statement to set Bit 5 of PORTB register high using the bitwise OR assignment operator (|=).",
            codingProblemInitialCode = "PORTB ... (1 << 5);",
            codingProblemSolution = "PORTB |= (1 << 5);",
            codingProblemHint = "Use the '|=' operator to combine PORTB with the shifted mask."
        ),
        Lesson(
            id = 4,
            title = "Bitwise Operations: Clearing & Toggling",
            description = "Clear bits with AND-NOT masks. Toggle bits using XOR gates.",
            content = """
                We know how to set pins HIGH, but how do we clear them back to LOW (0V) without overriding neighboring port statuses?
                
                We combine the Bitwise AND (&) and Bitwise NOT (~) operators:
                • Bitwise NOT (~): Flips 1s to 0s and 0s to 1s. '~00100000' becomes '11011111'.
                • Bitwise AND (&): Output is 1 only if both bits are 1. Since 'X AND 1 = X' and 'X AND 0 = 0', ANDing PORTB with our inverted mask clears the target bit to 0, holding other bits intact!
                
                Clear pin formula: PORT_REG &= ~(1 << PIN_NUMBER);
                Toggle pin formula (flip state): PORT_REG ^= (1 << PIN_NUMBER);
            """.trimIndent(),
            codeExample = """
                #define STATUS_LED 4
                
                void controlPins() {
                    // 1. Force LED 4 low:
                    PORTB &= ~(1 << STATUS_LED);
                    
                    // 2. Toggle LED 4 (invert its current state):
                    PORTB ^= (1 << STATUS_LED);
                }
            """.trimIndent(),
            exampleExplanation = "The inverted mask preserves neighboring pins during the logical AND operation, forcing only LED Pin 4 low.",
            systemRegisterPreset = "Active Alarm Sounder",
            codingProblemQuestion = "Write a C statement to clear Bit 4 of PORTB register low using AND-NOT (&= ~).",
            codingProblemInitialCode = "PORTB &= ~... ;",
            codingProblemSolution = "PORTB &= ~(1 << 4);",
            codingProblemHint = "Invert the 1-shifted mask on position 4: ~(1 << 4)."
        ),
        Lesson(
            id = 5,
            title = "GPIO: Configuring Outputs",
            description = "Master DDR and PORT to control LEDs and external alarm modules.",
            content = """
                GPIO (General Purpose Input/Output) pins connect your code to physical devices! They are driven by three registers: DDRx, PORTx, and PINx.
                
                Let's configure outputs:
                • DDRx (Data Direction Register): Writing a '1' to a bit sets that physical pin as an OUTPUT channel. Writing '0' sets it as an INPUT.
                • PORTx (Port Output Data Register): Set's electrical output state. 
                     - 1 sends High voltage (VCC, 5.0V or 3.3V) through the line.
                     - 0 pulls it to Ground (GND, 0V).
                
                Configure Pin 5 as output, and turn it ON:
                DDRB |= (1 << 5);   // Direction Output
                PORTB |= (1 << 5);  // Pin High
            """.trimIndent(),
            codeExample = """
                #define LED_PIN 5
                
                void setup() {
                    DDRB |= (1 << LED_PIN); // Configure Pin 5 as Output
                }
                
                void loop() {
                    PORTB |= (1 << LED_PIN);  // Output 5V: Light ON
                    PORTB &= ~(1 << LED_PIN); // Output 0V: Light OFF
                }
            """.trimIndent(),
            exampleExplanation = "Setting DDRB Bit 5 closes the silicon transistor pathway. Then PORTB Bit 5 dictates whether to route high line current or GND through Pin 5.",
            systemRegisterPreset = "LED Blinker",
            codingProblemQuestion = "Write a C statement to configure Pin 3 of DDRB register as an output pin.",
            codingProblemInitialCode = "DDRB ... ;",
            codingProblemSolution = "DDRB |= (1 << 3);",
            codingProblemHint = "Set Bit 3 of DDRB register to 1 using the '|=' bit modifier."
        ),
        Lesson(
            id = 6,
            title = "GPIO: Buttons & Pull-up Resistors",
            description = "Read physical input states safely. Solve floating line noise.",
            content = """
                Input pins read external voltages. When you set DDRx Bit to 0, the pin acts as an input sensor.
                
                But if the input pin is disconnected (like an open switch), static electric fields make its voltage fluctuate wildly between 1 and 0. This is called a 'floating input'!
                
                To solve this, we activate the internal 'Pull-up Resistor' by writing 1 to the input PORTx bit. This pulls the pin state to 1 (HIGH) when the switch is open. 
                
                When the button is pressed, it connects the pin to GND, driving it low (0). We read this live state using the PINx register!
                
                Equation: if ( !(PINB & (1 << BUTTON_PIN)) ) { // Pressed! }
            """.trimIndent(),
            codeExample = """
                #define SWITCH_PIN 2
                
                void initializeButton() {
                    DDRB &= ~(1 << SWITCH_PIN); // Input Mode
                    PORTB |= (1 << SWITCH_PIN); // Pull-up ENABLED
                }
                
                uint8_t isButtonPressed() {
                    // Check if Pin reads LOW
                    if (!(PINB & (1 << SWITCH_PIN))) {
                        return 1; // Pressed
                    }
                    return 0; // Released
                }
            """.trimIndent(),
            exampleExplanation = "Because the pull-up keeps the open switch HIGH, pressing the switch grounds the signal to 0. We negate with '!' to translate this LOW state into a TRUE button action.",
            systemRegisterPreset = "Switch Reader",
            codingProblemQuestion = "Complete the C conditional expression to check if input Pin 2 of PINB register reads LOW (0).",
            codingProblemInitialCode = "if (!(PINB & ... ))",
            codingProblemSolution = "if (!(PINB & (1 << 2)))",
            codingProblemHint = "Verify if Bit 2 is grounded: extract Bit 2 with (1 << 2) and negate with '!'"
        ),
        Lesson(
            id = 7,
            title = "Memory Mapping & Volatile Pointers",
            description = "Bypass standard declarations, write directly to physical memory addresses.",
            content = """
                How does the compiler know what 'PORTB' is? 
                
                In Embedded systems, physical registers are mapped directly to set memory-mapped locations in memory! Memory spaces hold electrical latches wired right to current.
                
                For example, on an ATmega MCU, DDRB is wired to address '0x24' and PORTB is wired to address '0x25'. 
                
                You can write to physical pins directly by casting these addresses as pointers! However, because physical electrical currents can update registers outside of C code execution loops, pointers MUST be marked as 'volatile' to prevent compiler optimization bugs.
            """.trimIndent(),
            codeExample = """
                #include <stdint.h>
                
                // volatile uint8_t pointer mapped directly to address 0x25
                #define BARE_PORTB (*(volatile uint8_t *)(0x25))
                
                void triggerDirectPin() {
                    // Bypass build tools, set Bit 5 directly
                    BARE_PORTB |= (1 << 5); 
                }
            """.trimIndent(),
            exampleExplanation = "By using volatile, we inform the compiler: 'This memory location can change state externally! Always fetch its state direct from RAM, never cache it.'",
            systemRegisterPreset = "Active Alarm Sounder",
            codingProblemQuestion = "Assign address 0x24 (DDRB) as a volatile unsigned 8-bit pointer dereference named 'BARE_DDRB'.",
            codingProblemInitialCode = "#define BARE_DDRB (*(volatile ... *)(0x24))",
            codingProblemSolution = "#define BARE_DDRB (*(volatile uint8_t *)(0x24))",
            codingProblemHint = "Dereference a volatile uint8_t pointer cast: volatile uint8_t *"
        ),
        Lesson(
            id = 8,
            title = "Hardware Timers & Prescalers",
            description = "Avoid blocking delays. Count hardware clock ticks in the background.",
            content = """
                Standard delay loops like '_delay_ms()' block the entire CPU! It stalls everything, meaning sensor updates or incoming button clicks are completely missed.
                
                Hardware Timers solve this! They are physical counter modules built on silicon that increment their value with each CPU clock pulse in the background.
                
                If the counting happens too fast, we division-scale the CPU clock using a Prescaler. 
                
                By dividing the central clock down (division factors: 8, 64, 256, or 1024), we can schedule precise, asynchronous millisecond counts without stalling instruction executions!
            """.trimIndent(),
            codeExample = """
                void initTimer0() {
                    TCCR0A = 0x00; // Timer 0 Normal Mode
                    
                    // Set Prescaler division factor to 64:
                    // Set CS01 and CS00 bits in Clock Selector register
                    TCCR0B |= (1 << CS01) | (1 << CS00);
                }
            """.trimIndent(),
            exampleExplanation = "With a 16MHz clock divided by 64, Timer0 ticks exactly 250,000 times per second in the background.",
            systemRegisterPreset = "LED Blinker",
            codingProblemQuestion = "Configure bit CS01 of TCCR0B register to High using logical OR (|=) to modify clock selectors.",
            codingProblemInitialCode = "TCCR0B |= ... ;",
            codingProblemSolution = "TCCR0B |= (1 << CS01);",
            codingProblemHint = "Use the left shift mask (1 << CS01) to alter TCCR0B."
        ),
        Lesson(
            id = 9,
            title = "Interrupts & Event Routing",
            description = "Respond instantly to inputs without wasteful polling.",
            content = """
                Instead of checking if a button is clicked over and over in your loop (wasteful 'polling'), we can use Interrupts!
                
                When a pin triggers an interrupt, the hardware pauses your running main loop instantly, saves its current status, jumps to execute an Interrupt Service Routine (ISR), and then resumes main code nicely.
                
                Rules for safe ISR programming:
                1. Keep ISR functions short. Never use delays inside them.
                2. Declaring variables: Any variable modified inside an ISR and read by main MUST be defined as **'volatile'** to prevent optimization bugs.
            """.trimIndent(),
            codeExample = """
                #include <avr/interrupt.h>
                
                // volatile tells compilers to grab fresh values from RAM 
                volatile uint8_t alarmTriggered = 0;
                
                ISR(INT0_vect) {
                    alarmTriggered = 1; // Flag instant event
                }
                
                void initInterrupt() {
                    EIMSK |= (1 << INT0);  // Enable External INT0
                    sei();                 // Global interrupt flag ON
                }
            """.trimIndent(),
            exampleExplanation = "Interrupt INT0 monitors Pin 2. Clicking the switch halts the CPU, executes the fast ISR flag update, and returns.",
            systemRegisterPreset = "Switch Reader",
            codingProblemQuestion = "Declare a global volatile unsigned 8-bit variable named 'timerCount' and initialize it to 0.",
            codingProblemInitialCode = "... timerCount = 0;",
            codingProblemSolution = "volatile uint8_t timerCount = 0;",
            codingProblemHint = "Prefix string with 'volatile' followed by the 8-bit type 'uint8_t'."
        ),
        Lesson(
            id = 10,
            title = "Analog ADC & PWM Dimming",
            description = "Read sensor volt scales (analog) and dim physical output elements.",
            content = """
                Microcontrollers operate in binary (0V or 5V). But standard analog sensors return gradual voltage scales (like a pot knob or temperature resistor).
                
                Hardware peripherals resolve this:
                1. ADC (Analog to Digital Converter): Translates line voltage to a number. A 10-bit ADC translates 0V to 0 and 5V to 1023.
                2. PWM (Pulse Width Modulation): Simulates intermediate voltages by pulsing an output PIN on and off incredibly fast. The ratio of ON time to OFF time is the duty cycle.
                
                By scaling a 10-bit ADC reading down to fit an 8-bit PWM limit (0-255), we can directly map knobs to LED brightness or speaker volumes!
            """.trimIndent(),
            codeExample = """
                uint16_t readPotentiometer() {
                    ADCSRA |= (1 << ADSC); // Start read
                    while (ADCSRA & (1 << ADSC)); // Sync wait
                    return ADC; // Returns 10-bit value (0-1023)
                }
                
                void updateBrightness() {
                    uint16_t rawSensor = readPotentiometer();
                    // Scale 0-1023 down to 0-255 PWM range
                    OCR0A = rawSensor / 4; 
                }
            """.trimIndent(),
            exampleExplanation = "Dividing the 10-bit ADC channel reading by 4 scales it down to suit the 8-bit range of the PWM output register.",
            systemRegisterPreset = "Active Alarm Sounder",
            codingProblemQuestion = "Write a C statement to divide raw 10-bit integer 'analogIn' by 4 using the right bitwise shift operator for fast execution.",
            codingProblemInitialCode = "uint8_t duty = analogIn ... ;",
            codingProblemSolution = "uint8_t duty = analogIn >> 2;",
            codingProblemHint = "Right shifting an integer by 2 scales it down by 2^2 = 4."
        )
    )

    private val _currentLesson = MutableStateFlow(lessons.first())
    val currentLesson = _currentLesson.asStateFlow()

    fun selectLesson(lesson: Lesson) {
        _currentLesson.value = lesson
        // Automatically load simulation preset if associated
        lesson.systemRegisterPreset?.let { presetName ->
            simulationPresets.find { it.name == presetName }?.let { preset ->
                loadPreset(preset)
            }
        }
    }

    // --- 2. Bitwise Calculator Laboratory & Code Generator ---
    private val _bitwiseValA = MutableStateFlow(0b10100100) // Default 164
    val bitwiseValA = _bitwiseValA.asStateFlow()

    private val _bitwiseValB = MutableStateFlow(0b00111100) // Default 60
    val bitwiseValB = _bitwiseValB.asStateFlow()

    private val _selectedOperator = MutableStateFlow("&") // Options: &, |, ^, ~, <<, >>
    val selectedOperator = _selectedOperator.asStateFlow()

    private val _shiftAmount = MutableStateFlow(1)
    val shiftAmount = _shiftAmount.asStateFlow()

    fun toggleBitA(index: Int) {
        val current = _bitwiseValA.value
        _bitwiseValA.value = current xor (1 shl index)
    }

    fun toggleBitB(index: Int) {
        val current = _bitwiseValB.value
        _bitwiseValB.value = current xor (1 shl index)
    }

    fun setOperator(op: String) {
        _selectedOperator.value = op
    }

    fun setShiftAmount(amount: Int) {
        _shiftAmount.value = amount.coerceIn(1, 7)
    }

    fun getBitwiseResult(): Int {
        val a = _bitwiseValA.value
        val b = _bitwiseValB.value
        return when (_selectedOperator.value) {
            "&" -> a and b
            "|" -> a or b
            "^" -> a xor b
            "~" -> a.inv() and 0xFF // Keep to 8-bit
            "<<" -> (a shl _shiftAmount.value) and 0xFF
            ">>" -> a shr _shiftAmount.value
            else -> 0
        }
    }

    // --- 3. Microcontroller Simulator State ---
    private val _registers = MutableStateFlow(SimulatedRegisters())
    val registers = _registers.asStateFlow()

    private val _selectedPreset = MutableStateFlow<SimulationPreset?>(null)
    val selectedPreset = _selectedPreset.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(-1)
    val currentLineIndex = _currentLineIndex.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    private val _potentiometerValue = MutableStateFlow(128) // Simulated Potentiometer Value (0-255)
    val potentiometerValue = _potentiometerValue.asStateFlow()

    private val _isSwitchPressed = MutableStateFlow(false) // Pin 2 Switch: Pressed (connecting GND, active-low)
    val isSwitchPressed = _isSwitchPressed.asStateFlow()

    private var simulationJob: Job? = null

    // Real predefined Embedded presets with executable code line behaviors!
    val simulationPresets = listOf(
        SimulationPreset(
            name = "LED Blinker",
            icon = "flash_on",
            description = "Turns Pin 5 (Yellow LED) into Output and blinks it in a nested cycle.",
            sourceCode = """
                // Preset: LED Blinker
                #include <avr/io.h>
                
                int main(void) {
                    DDRB |= (1 << 5); // 1. Set Pin 5 as output
                    
                    while(1) {
                        PORTB |= (1 << 5);  // 2. Turn LED 5 ON (High)
                        delay();
                        PORTB &= ~(1 << 5); // 3. Turn LED 5 OFF (Low)
                        delay();
                    }
                }
            """.trimIndent(),
            codeLines = listOf(
                SimulatorCodeLine(
                    lineCode = "DDRB |= (1 << 5);",
                    comment = "DDRB is changed to 0x20. Register configuration maps Pin 5 as OUTPUT.",
                    effectOnRegisters = { reg -> reg.copy(ddrb = reg.ddrb or 0x20) }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB |= (1 << 5);",
                    comment = "Bit 5 is set in PORTB. Current is sent to the physical pin: Yellow Onboard LED turns ON!",
                    effectOnRegisters = { reg -> reg.copy(portb = reg.portb or 0x20) }
                ),
                SimulatorCodeLine(
                    lineCode = "delay();",
                    comment = "Wait routine execution pauses here. Hardware remains in configured state.",
                    effectOnRegisters = { reg -> reg }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB &= ~(1 << 5);",
                    comment = "Bit 5 is cleared to 0 in PORTB. Pin voltage goes to 0V: Yellow Onboard LED turns OFF!",
                    effectOnRegisters = { reg -> reg.copy(portb = reg.portb and 0x20.inv()) }
                ),
                SimulatorCodeLine(
                    lineCode = "delay();",
                    comment = "Wait routine execution pauses here. Yellow LED remains off.",
                    effectOnRegisters = { reg -> reg }
                )
            )
        ),
        SimulationPreset(
            name = "Switch Reader",
            icon = "toggle_on",
            description = "Reads Switch at Pin 2. When pressed (grounds input to 0), sets Pin 5 Onboard LED active.",
            sourceCode = """
                // Preset: Read Switch & Drive LED
                int main(void) {
                    DDRB |= (1 << 5);    // 1. LED-5 as Output
                    DDRB &= ~(1 << 2);   // 2. Button-2 as Input
                    PORTB |= (1 << 2);   // 3. Enable Pull-Up Resistor on button Pin 2
                    
                    while(1) {
                        // 4. Read physical PIN state
                        if ( !(PINB & (1 << 2)) ) { 
                            PORTB |= (1 << 5);  // 5. Button Pressed: LED ON
                        } else {
                            PORTB &= ~(1 << 5); // 6. Button Released: LED OFF
                        }
                    }
                }
            """.trimIndent(),
            codeLines = listOf(
                SimulatorCodeLine(
                    lineCode = "DDRB |= (1 << 5);",
                    comment = "Configure DDRB to make Pin 5 an Output Pin.",
                    effectOnRegisters = { reg -> reg.copy(ddrb = reg.ddrb or 0x20) }
                ),
                SimulatorCodeLine(
                    lineCode = "DDRB &= ~(1 << 2);",
                    comment = "Clear Bit 2 in DDRB. Configures Pin 2 as INPUT.",
                    effectOnRegisters = { reg -> reg.copy(ddrb = reg.ddrb and 0x04.inv()) }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB |= (1 << 2);",
                    comment = "High state on Input pin activates internal Pull-Up. Reads as High (1) by default.",
                    effectOnRegisters = { reg -> reg.copy(portb = reg.portb or 0x04) }
                ),
                SimulatorCodeLine(
                    lineCode = "if ( !(PINB & (1 << 2)) )",
                    comment = "Checks if Pin 2 reads 0V. We read the PINB register directly. PINB Bit 2 matches the physical switch state.",
                    effectOnRegisters = { reg -> reg }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB |= (1 << 5);",
                    comment = "Switch is pressed (PINB Bit 2 is 0). Logic matches, sending High trigger to PORTB LED Pin 5!",
                    effectOnRegisters = { reg -> if (reg.pinb and 0x04 == 0) reg.copy(portb = reg.portb or 0x20) else reg }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB &= ~(1 << 5);",
                    comment = "Switch is released (PINB Bit 2 is 1). Sets PORTB back to Low: pin-based LED dims.",
                    effectOnRegisters = { reg -> if (reg.pinb and 0x04 != 0) reg.copy(portb = reg.portb and 0x20.inv()) else reg }
                )
            )
        ),
        SimulationPreset(
            name = "Active Alarm Sounder",
            icon = "volume_up",
            description = "Turns Pin 4 (Red warning LED) and Pin 3 (Active Buzzer Probe) high in alternating frequencies.",
            sourceCode = """
                // Preset: Hardware Alarm Array
                #define WARNING_LED 4
                #define BUZZER_PIN 3
                
                int main(void) {
                    DDRB |= (1 << WARNING_LED) | (1 << BUZZER_PIN); // 1. Output warning array
                    
                    while(1) {
                        PORTB |= (1 << WARNING_LED) | (1 << BUZZER_PIN); // 2. Trigger alarm state
                        delay();
                        PORTB &= ~((1 << WARNING_LED) | (1 << BUZZER_PIN)); // 3. Mute array
                        delay();
                    }
                }
            """.trimIndent(),
            codeLines = listOf(
                SimulatorCodeLine(
                    lineCode = "DDRB |= (1 << 4) | (1 << 3);",
                    comment = "Enable corresponding direction registers for warning LED (Bit 4) and active sounder (Bit 3) as outputs.",
                    effectOnRegisters = { reg -> reg.copy(ddrb = reg.ddrb or 0x18) }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB |= (1 << 4) | (1 << 3);",
                    comment = "Turn Red Warning LED and Piezo Buzzer hardware channels matching output bits ON simultaneously!",
                    effectOnRegisters = { reg -> reg.copy(portb = reg.portb or 0x18) }
                ),
                SimulatorCodeLine(
                    lineCode = "delay();",
                    comment = "Alarm elements operate. Tone waves oscillate.",
                    effectOnRegisters = { reg -> reg }
                ),
                SimulatorCodeLine(
                    lineCode = "PORTB &= ~((1 << 4) | (1 << 3));",
                    comment = "Clear bits 4 and 3 in PORTB. Instantly mutes the Buzzer and turns off Red Alarm LED.",
                    effectOnRegisters = { reg -> reg.copy(portb = reg.portb and 0x18.inv()) }
                ),
                SimulatorCodeLine(
                    lineCode = "delay();",
                    comment = "Silence wait cycle.",
                    effectOnRegisters = { reg -> reg }
                )
            )
        )
    )

    init {
        // Load default preset
        loadPreset(simulationPresets.first())
    }

    fun loadPreset(preset: SimulationPreset) {
        stopSimulation()
        _selectedPreset.value = preset
        _currentLineIndex.value = 0
        _registers.value = SimulatedRegisters() // Reset register states
        syncInputSignals()
    }

    fun setPotentiometer(value: Int) {
        _potentiometerValue.value = value
    }

    fun setSwitchPressed(pressed: Boolean) {
        _isSwitchPressed.value = pressed
        syncInputSignals()
    }

    private fun syncInputSignals() {
        val currentReg = _registers.value
        // If switch is pressed: physical pin 2 connects to GND -> Bit 2 reads Low (0).
        // If switch is NOT pressed: Pull-up is enabled -> Bit 2 reads High (1).
        val newPinb = if (_isSwitchPressed.value) {
            currentReg.pinb and 0x04.inv()
        } else {
            currentReg.pinb or 0x04
        }
        _registers.value = currentReg.copy(pinb = newPinb)
    }

    fun stepSimulation() {
        val preset = _selectedPreset.value ?: return
        if (preset.codeLines.isEmpty()) return

        val nextIndex = (_currentLineIndex.value + 1) % preset.codeLines.size
        _currentLineIndex.value = nextIndex

        val currentLine = preset.codeLines[nextIndex]
        _registers.value = currentLine.effectOnRegisters(_registers.value)
        syncInputSignals()
    }

    fun startSimulation() {
        if (_isSimulating.value) return
        _isSimulating.value = true

        simulationJob = viewModelScope.launch {
            while (_isSimulating.value) {
                stepSimulation()
                delay(1200) // Give user readable time to scan register changes
            }
        }
    }

    fun stopSimulation() {
        _isSimulating.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    // --- 4. Interactive Quiz Progress ---
    val quizQuestions = listOf(
        QuizQuestion(
            id = 1,
            question = "Which C expression correctly SETS Bit 3 of PORTB to High without altering other pins?",
            options = listOf(
                "PORTB = 0x08;",
                "PORTB |= (1 << 3);",
                "PORTB &= ~(1 << 3);",
                "PORTB ^= (1 << 3);"
            ),
            correctAnswerIndex = 1,
            detailedExplanation = "Using '|=' perform a Bitwise OR with a mask of 1 shifted left by 3. Since 'anything OR 0 is standard state', and 'anything OR 1 is forced to 1', Bit 3 resolves to High while surrounding pins are not modified."
        ),
        QuizQuestion(
            id = 2,
            question = "What is the purpose of the 'volatile' keyword in Embedded C declarations?",
            options = listOf(
                "It makes compile speeds significantly faster.",
                "It prevents variables from changing state outside main loop structures.",
                "It forces compilers to read fresh values direct from RAM rather than cached registries.",
                "It allocates memory inside flash storage instead of volatile SRAM."
            ),
            correctAnswerIndex = 2,
            detailedExplanation = "'volatile' tells compiler optimizations that a variable's value can be changed by hardware interrupts or peripherals. It guarantees fresh evaluation fetches straight from hardware SRAM registers."
        ),
        QuizQuestion(
            id = 3,
            question = "If DDRB register is configured as '0x20' (00100000), what role does PORTB Pin 5 play?",
            options = listOf(
                "Pin 5 represents a Digital Output.",
                "Pin 5 represents a Digital Input with no Pullup.",
                "Pin 5 represents a Digital Input with enabled Internal Pull-Up.",
                "Pin 5 is fully muted and un-addressable."
            ),
            correctAnswerIndex = 0,
            detailedExplanation = "A '1' in the Data Direction Register (DDRx) declares a pin as an OUTPUT Pin. Bit 5 is set, so PORTB Pin 5 drives output voltage."
        ),
        QuizQuestion(
            id = 4,
            question = "How do you check if Digital Input Pin 2 in PINB reads Low (GND)?",
            options = listOf(
                "if (PINB == 0) { ... }",
                "if (PINB & (1 << 2)) { ... }",
                "if ( !(PINB & (1 << 2)) ) { ... }",
                "PINB |= (1 << 2);"
            ),
            correctAnswerIndex = 2,
            detailedExplanation = "'PINB & (1 << 2)' performs an AND operation. If Pin 2 is active Low (GND / 0V), this evaluates to 0. Negating it with '!' results in a true condition, verifying active Low state."
        )
    )

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex = _currentQuizIndex.asStateFlow()

    private val _selectedQuizAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedQuizAnswerIndex = _selectedQuizAnswerIndex.asStateFlow()

    private val _quizStatus = MutableStateFlow<QuizStatus>(QuizStatus.Unanswered)
    val quizStatus = _quizStatus.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    sealed interface QuizStatus {
        object Unanswered : QuizStatus
        data class Answered(val isCorrect: Boolean, val rightIdx: Int, val explanation: String) : QuizStatus
    }

    fun selectQuizAnswer(index: Int) {
        if (_quizStatus.value is QuizStatus.Answered) return // Prevent duplicate choices
        _selectedQuizAnswerIndex.value = index
    }

    fun submitQuizAnswer() {
        val ansIdx = _selectedQuizAnswerIndex.value ?: return
        val currentQ = quizQuestions[_currentQuizIndex.value]
        val isCorrect = ansIdx == currentQ.correctAnswerIndex

        if (isCorrect) {
            _quizScore.value += 1
        }

        _quizStatus.value = QuizStatus.Answered(
            isCorrect = isCorrect,
            rightIdx = currentQ.correctAnswerIndex,
            explanation = currentQ.detailedExplanation
        )
    }

    fun nextQuizQuestion() {
        val nextIdx = _currentQuizIndex.value + 1
        if (nextIdx < quizQuestions.size) {
            _currentQuizIndex.value = nextIdx
            _selectedQuizAnswerIndex.value = null
            _quizStatus.value = QuizStatus.Unanswered
        } else {
            // Restart quiz
            _currentQuizIndex.value = 0
            _selectedQuizAnswerIndex.value = null
            _quizStatus.value = QuizStatus.Unanswered
            _quizScore.value = 0
        }
    }

    // --- 5. Gemini AI Embedded Tutor Companion Chat ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = Sender.Tutor,
                message = "Greetings future engineer! I am your AI Embedded Systems & C Tutor. I can write and explain bitwise masking, explain volatile vs static keywords, debug your register setup, or unpack interrupt timers. Ask me any embedded programming question from scratch!"
            )
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    private val _chatInputText = MutableStateFlow("")
    val chatInputText = _chatInputText.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading = _chatLoading.asStateFlow()

    fun updateChatInput(text: String) {
        _chatInputText.value = text
    }

    fun sendChatMessage() {
        val userPrompt = _chatInputText.value.trim()
        if (userPrompt.isEmpty()) return

        // Clear input bar
        _chatInputText.value = ""

        // Append to local message array
        val userMessage = ChatMessage(Sender.User, userPrompt)
        _chatMessages.value = _chatMessages.value + userMessage
        _chatLoading.value = true

        viewModelScope.launch {
            // Prepare history format matching gemini REST endpoint guidelines
            val history = _chatMessages.value.map { msg ->
                GeminiContent(
                    parts = listOf(GeminiPart(msg.message)),
                    role = if (msg.sender == Sender.User) "user" else "model"
                )
            }

            // Custom prompt injected as a hardware context background role mapping
            val systemPrompt = """
                You are a patient, expert professor of Embedded Systems, Firmware Architecture, and C Programming.
                Design Principles:
                1. Always focus on explain hardware interaction (pins, DDR, PORT, SRAM, registers, registers masking).
                2. Standard C is not Embedded C. Explain the hardware constraints (limited stack/heap, direct memory-mapped IO).
                3. Use simple, interactive markdown blocks with explanatory comments inside C snippets.
                4. Keep explanations approachable, clear, and focused on physical effects (voltage pins toggling 1/0).
                5. If appropriate, reference Arduino, AVR ATmega, or STM32 architecture for clarity.
            """.trimIndent()

            val responseText = GeminiClient.generateSolution(systemPrompt, history)

            _chatMessages.value = _chatMessages.value + ChatMessage(Sender.Tutor, responseText)
            _chatLoading.value = false
        }
    }

    fun populateChatPrompt(prompt: String) {
        _chatInputText.value = prompt
    }
}

// Chat helper structures
enum class Sender { User, Tutor }
data class ChatMessage(
    val sender: Sender,
    val message: String,
    val time: Long = System.currentTimeMillis()
)
