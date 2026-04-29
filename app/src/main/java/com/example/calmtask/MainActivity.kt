package com.example.calmtask

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.Locale

data class TaskItem(
    val title: String,
    val status: String = "active"
)

enum class Screen {
    ONBOARDING,
    HOME,
    NIGHT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private var textToSpeech: TextToSpeech? = null

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= 33) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestCameraPermission.launch(Manifest.permission.CAMERA)
        requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        textToSpeech = TextToSpeech(this) {}

        setContent {
            CalmTaskApp(
                speak = { text, languageCode ->
                    speakText(text, languageCode)
                }
            )
        }
    }

    private fun speakText(text: String, languageCode: String) {
        val locale = when (languageCode) {
            "English" -> Locale.ENGLISH
            "Hindi" -> Locale("hi", "IN")
            "Spanish" -> Locale("es", "ES")
            "French" -> Locale.FRENCH
            "Arabic" -> Locale("ar")
            "Portuguese" -> Locale("pt", "BR")
            "German" -> Locale.GERMAN
            else -> Locale.ENGLISH
        }

        textToSpeech?.language = locale
        textToSpeech?.setSpeechRate(0.92f)
        textToSpeech?.speak(text, TextToSpeech.QUEUEFLUSH, null, "calmtaskvoice")
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun CalmTaskApp(
    speak: (String, String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("calmtaskprefs", Context.MODE_PRIVATE)
    }

    var screen by remember {
        mutableStateOf(
            if (prefs.getBoolean("onboarded", false)) Screen.HOME else Screen.ONBOARDING
        )
    }

    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var country by remember { mutableStateOf(prefs.getString("country", "United States") ?: "United States") }
    var language by remember { mutableStateOf(prefs.getString("language", "English") ?: "English") }
    var gender by remember { mutableStateOf(prefs.getString("gender", "Prefer not to say") ?: "Prefer not to say") }
    var mood by remember { mutableStateOf(prefs.getString("mood", "Calm") ?: "Calm") }
    var voiceEnabled by remember { mutableStateOf(prefs.getBoolean("voiceEnabled", true)) }
    var profilePicture by remember { mutableStateOf(prefs.getString("profilePicture", "") ?: "") }

    val tasks = remember {
        mutableStateListOf<TaskItem>().apply {
            val savedTasks = prefs.getString("tasks", "")
            if (!savedTasks.isNullOrBlank()) {
                savedTasks.split("|||").forEach { raw ->
                    val parts = raw.split(":::")
                    if (parts.size == 2) {
                        add(TaskItem(parts[0], parts[1]))
                    }
                }
            }
        }
    }

    fun saveAll() {
        prefs.edit()
            .putString("name", name)
            .putString("country", country)
            .putString("language", language)
            .putString("gender", gender)
            .putString("mood", mood)
            .putBoolean("voiceEnabled", voiceEnabled)
            .putBoolean("onboarded", true)
            .putString("profilePicture", profilePicture)
            .putString("tasks", tasks.joinToString("|||") { "${it.title}:::${it.status}" })
            .apply()
    }

    fun say(text: String) {
        if (voiceEnabled) {
            speak(text, language)
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = WarmBackground
        ) {
            when (screen) {
                Screen.ONBOARDING -> OnboardingScreen(
                    name = name,
                    onNameChange = { name = it },
                    country = country,
                    onCountryChange = { country = it },
                    language = language,
                    onLanguageChange = { language = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    mood = mood,
                    onMoodChange = { mood = it },
                    profilePicture = profilePicture,
                    onProfilePictureChange = { profilePicture = it },
                    onFinish = {
                        saveAll()
                        say("Welcome. Let's keep today simple.")
                        screen = Screen.HOME
                    }
                )

                Screen.HOME -> HomeScreen(
                    name = name,
                    language = language,
                    mood = mood,
                    profilePicture = profilePicture,
                    tasks = tasks,
                    voiceEnabled = voiceEnabled,
                    onAddTask = { title ->
                        if (title.isNotBlank()) {
                            tasks.add(TaskItem(title.trim()))
                            saveAll()
                            say("Added.")
                        }
                    },
                    onDone = { index ->
                        tasks[index] = tasks[index].copy(status = "completed")
                        saveAll()
                        say("Done. That's one less thing to carry.")
                    },
                    onLater = { index ->
                        saveAll()
                        say("Okay. I'll keep it for later.")
                    },
                    onSkip = { index ->
                        tasks[index] = tasks[index].copy(status = "skipped")
                        saveAll()
                        say("Skipped.")
                    },
                    onMorningGreeting = {
                        val active = tasks.count { it.status == "active" }
                        val phrase = greetingForMood(mood, active)
                        say(phrase)
                    },
                    onVoiceCommand = { command ->
                        handleSimpleCommand(
                            command = command,
                            tasks = tasks,
                            save = { saveAll() },
                            say = { say(it) }
                        )
                    },
                    goNight = { screen = Screen.NIGHT },
                    goSettings = { screen = Screen.SETTINGS }
                )

                Screen.NIGHT -> NightScreen(
                    tasks = tasks,
                    onMoveAllActiveToTomorrow = {
                        saveAll()
                        say("Saved for tomorrow.")
                    },
                    onDeleteSkipped = {
                        tasks.removeAll { it.status == "skipped" }
                        saveAll()
                        say("Cleared skipped tasks.")
                    },
                    onBack = { screen = Screen.HOME }
                )

                Screen.SETTINGS -> SettingsScreen(
                    name = name,
                    country = country,
                    language = language,
                    gender = gender,
                    mood = mood,
                    voiceEnabled = voiceEnabled,
                    profilePicture = profilePicture,
                    onNameChange = {
                        name = it
                        saveAll()
                    },
                    onCountryChange = {
                        country = it
                        saveAll()
                    },
                    onLanguageChange = {
                        language = it
                        saveAll()
                    },
                    onGenderChange = {
                        gender = it
                        saveAll()
                    },
                    onMoodChange = {
                        mood = it
                        saveAll()
                    },
                    onVoiceEnabledChange = {
                        voiceEnabled = it
                        saveAll()
                    },
                    onProfilePictureChange = {
                        profilePicture = it
                        saveAll()
                    },
                    onClearTasks = {
                        tasks.clear()
                        saveAll()
                    },
                    onBack = { screen = Screen.HOME }
                )
            }
        }
    }
}

val WarmBackground = Color(0xFFF8F5EF)
val PrimaryBlue = Color(0xFF4F7DF3)
val CalmGreen = Color(0xFF5CB85C)
val WarmAmber = Color(0xFFF5A623)
val Charcoal = Color(0xFF222222)
val MutedGray = Color(0xFF777777)
val CardWhite = Color(0xFFFFFFFF)
val LightGray = Color(0xFFEEEEEE)

@Composable
fun OnboardingScreen(
    name: String,
    onNameChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    mood: String,
    onMoodChange: (String) -> Unit,
    profilePicture: String,
    onProfilePictureChange: (String) -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to CalmTask",
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "I'll help you choose what matters today without annoying you.",
            color = MutedGray
        )

        ProfilePictureUpload(
            picture = profilePicture,
            onPictureChange = onProfilePictureChange
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth()
        )

        SimpleDropdown(
            label = "Country",
            selected = country,
            options = listOf(
                "United States",
                "India",
                "United Kingdom",
                "Canada",
                "Australia",
                "Spain",
                "France",
                "Saudi Arabia",
                "Germany",
                "Brazil",
                "Mexico",
                "Japan",
                "China",
                "Other"
            ),
            onSelected = onCountryChange
        )

        SimpleDropdown(
            label = "Language",
            selected = language,
            options = listOf(
                "English",
                "Hindi",
                "Spanish",
                "French",
                "Arabic",
                "Portuguese",
                "German"
            ),
            onSelected = onLanguageChange
        )

        SimpleDropdown(
            label = "Gender",
            selected = gender,
            options = listOf(
                "Female",
                "Male",
                "Non-binary",
                "Prefer not to say"
            ),
            onSelected = onGenderChange
        )

        Text(
            text = "How are you starting today?",
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )

        MoodButtons(
            selectedMood = mood,
            onMoodChange = onMoodChange
        )

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start")
        }
    }
}

@Composable
fun ProfilePictureUpload(
    picture: String,
    onPictureChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val encoded = encodeImageToBase64(bitmap)
            onPictureChange(encoded)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val encoded = encodeImageToBase64(bitmap)
                onPictureChange(encoded)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (picture.isNotEmpty()) {
            val bitmap = decodeImageFromBase64(picture)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showDialog = true },
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(LightGray, CircleShape)
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text("Add Photo", color = MutedGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Tap to add profile picture", color = MutedGray, fontSize = 12.sp)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose photo source") },
            text = { Text("Take a new photo or choose from gallery?") },
            confirmButton = {
                Button(onClick = {
                    cameraLauncher.launch(null)
                    showDialog = false
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                Button(onClick = {
                    galleryLauncher.launch("image/*")
                    showDialog = false
                }) {
                    Text("Gallery")
                }
            }
        )
    }
}

fun encodeImageToBase64(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val imageBytes = baos.toByteArray()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}

fun decodeImageFromBase64(base64Str: String): Bitmap? {
    return try {
        val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun Image(
    bitmap: ImageBitmap,
    contentDescription: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    androidx.compose.foundation.Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun HomeScreen(
    name: String,
    language: String,
    mood: String,
    profilePicture: String,
    tasks: MutableList<TaskItem>,
    voiceEnabled: Boolean,
    onAddTask: (String) -> Unit,
    onDone: (Int) -> Unit,
    onLater: (Int) -> Unit,
    onSkip: (Int) -> Unit,
    onMorningGreeting: () -> Unit,
    onVoiceCommand: (String) -> Unit,
    goNight: () -> Unit,
    goSettings: () -> Unit
) {
    var newTask by remember { mutableStateOf("") }
    var commandText by remember { mutableStateOf("") }

    val activeTasks = tasks.withIndex().filter { it.value.status == "active" }
    val completedTasks = tasks.withIndex().filter { it.value.status == "completed" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBa
