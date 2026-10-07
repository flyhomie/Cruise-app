package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiDriverAlert
import com.example.data.ai.GeminiDriverAssistant
import com.example.data.local.*
import com.example.data.repository.CruizeRepository
import com.example.ui.components.NavDestination
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class CruizeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CruizeDatabase.getDatabase(application)
    val repository = CruizeRepository(db)

    // Flow states from repository
    val currentUser: StateFlow<UserEntity?> = repository.loggedInUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allHazards: StateFlow<List<RoadHazardEntity>> = repository.allHazards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allReviews: StateFlow<List<HazardReviewEntity>> = repository.allReviews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appSettings: StateFlow<AppSettingsEntity?> = repository.appSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val tripLogs: StateFlow<List<ScannedTripLogEntity>> = repository.allTripLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI & Navigation State
    private val _currentDestination = MutableStateFlow(NavDestination.HOME)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    private val _currentSpeedMph = MutableStateFlow(78)
    val currentSpeedMph: StateFlow<Int> = _currentSpeedMph.asStateFlow()

    private val _currentRoadName = MutableStateFlow("Thika Superhighway & Survey, Nairobi")
    val currentRoadName: StateFlow<String> = _currentRoadName.asStateFlow()

    private val _is3dPerspective = MutableStateFlow(true)
    val is3dPerspective: StateFlow<Boolean> = _is3dPerspective.asStateFlow()

    private val _isTopBarExpanded = MutableStateFlow(true)
    val isTopBarExpanded: StateFlow<Boolean> = _isTopBarExpanded.asStateFlow()

    private val _isAiVoiceOn = MutableStateFlow(true)
    val isAiVoiceOn: StateFlow<Boolean> = _isAiVoiceOn.asStateFlow()

    private val _isSimulatingDrive = MutableStateFlow(false)
    val isSimulatingDrive: StateFlow<Boolean> = _isSimulatingDrive.asStateFlow()

    private val _aiAlertState = MutableStateFlow<AiDriverAlert?>(null)
    val aiAlertState: StateFlow<AiDriverAlert?> = _aiAlertState.asStateFlow()

    private val _isLoadingAiAlert = MutableStateFlow(false)
    val isLoadingAiAlert: StateFlow<Boolean> = _isLoadingAiAlert.asStateFlow()

    private val _selectedHazard = MutableStateFlow<RoadHazardEntity?>(null)
    val selectedHazard: StateFlow<RoadHazardEntity?> = _selectedHazard.asStateFlow()

    private val _showReportDialog = MutableStateFlow(false)
    val showReportDialog: StateFlow<Boolean> = _showReportDialog.asStateFlow()

    private val _selectedMapFilter = MutableStateFlow("ALL")
    val selectedMapFilter: StateFlow<String> = _selectedMapFilter.asStateFlow()

    private val _selectedSeverityFilter = MutableStateFlow("ALL")
    val selectedSeverityFilter: StateFlow<String> = _selectedSeverityFilter.asStateFlow()

    private val _showCctvLayer = MutableStateFlow(true)
    val showCctvLayer: StateFlow<Boolean> = _showCctvLayer.asStateFlow()

    private val _inspectedCctvHazard = MutableStateFlow<RoadHazardEntity?>(null)
    val inspectedCctvHazard: StateFlow<RoadHazardEntity?> = _inspectedCctvHazard.asStateFlow()

    private val _showCctvMappingDialog = MutableStateFlow(false)
    val showCctvMappingDialog: StateFlow<Boolean> = _showCctvMappingDialog.asStateFlow()

    fun toggleCctvLayer() {
        _showCctvLayer.value = !_showCctvLayer.value
        speakText(if (_showCctvLayer.value) "CCTV Surveillance layer enabled." else "CCTV Surveillance layer hidden.")
    }

    fun inspectCctvFeed(hazard: RoadHazardEntity?) {
        _inspectedCctvHazard.value = hazard
        if (hazard != null) {
            speakText("Accessing live telemetry for ${hazard.title}.")
        }
    }

    fun setShowCctvMappingDialog(show: Boolean) {
        _showCctvMappingDialog.value = show
    }

    private val _selectedRadiusFilterMeters = MutableStateFlow(0) // 0 means ALL distances
    val selectedRadiusFilterMeters: StateFlow<Int> = _selectedRadiusFilterMeters.asStateFlow()

    // Gemini Chatbot State
    data class ChatMessage(val sender: String, val text: String, val timestamp: Long = System.currentTimeMillis())

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "Cruize AI Co-Pilot",
                text = "Jambo! I am your Cruize East Africa Co-Pilot. Ask me about NTSA speed cameras, traffic police traps, speed humps, or road conditions across Kenya & East Africa!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // User's own reported hazards
    val userReportingHistory: StateFlow<List<RoadHazardEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getUserHazards(user.username)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _proximityAlertRangeMeters = MutableStateFlow(300)
    val proximityAlertRangeMeters: StateFlow<Int> = _proximityAlertRangeMeters.asStateFlow()

    private val _speechFeedbackMessage = MutableStateFlow<String?>(null)
    val speechFeedbackMessage: StateFlow<String?> = _speechFeedbackMessage.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        // Pre-populate sample hazards & user
        viewModelScope.launch {
            repository.initializePrepopulatedData()
            triggerAiRoadAnalysis()
        }

        // Collect persistent App Settings from Room database to keep local UI state synchronized
        viewModelScope.launch {
            repository.appSettings.collectLatest { settings ->
                if (settings != null) {
                    _isAiVoiceOn.value = settings.isAiVoiceOn
                    _proximityAlertRangeMeters.value = settings.proximityAlertRangeMeters
                    _is3dPerspective.value = settings.is3dPerspective
                }
            }
        }

        // Initialize Android TextToSpeech engine safely for real audio alert output!
        try {
            tts = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                }
            }
        } catch (e: Exception) {
            // Speech fallback
        }
    }

    fun navigateTo(dest: NavDestination) {
        _currentDestination.value = dest
    }

    fun setMapFilter(filter: String) {
        _selectedMapFilter.value = filter
    }

    fun setSeverityFilter(severity: String) {
        _selectedSeverityFilter.value = severity
    }

    fun setRadiusFilter(meters: Int) {
        _selectedRadiusFilterMeters.value = meters
    }

    fun sendCopilotMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        val userMsg = ChatMessage(sender = "You", text = userPrompt)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val responseText = GeminiDriverAssistant.askCopilot(
                userPrompt = userPrompt,
                roadContext = _currentRoadName.value
            )
            val aiMsg = ChatMessage(sender = "Cruize AI Co-Pilot", text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isChatLoading.value = false

            if (_isAiVoiceOn.value) {
                speakText(responseText.take(120))
            }
        }
    }

    fun updateProfile(username: String, bio: String, vehicle: String, avatar: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateUserProfile(user.id, username, bio, vehicle, avatar)
                speakText("Driver profile updated successfully.")
            }
        }
    }

    fun deleteReport(hazard: RoadHazardEntity) {
        viewModelScope.launch {
            repository.deleteHazard(hazard)
            speakText("Incident report retracted.")
        }
    }

    fun selectHazard(hazard: RoadHazardEntity?) {
        _selectedHazard.value = hazard
    }

    fun toggle3dPerspective() {
        _is3dPerspective.value = !_is3dPerspective.value
    }

    fun toggleTopBar() {
        _isTopBarExpanded.value = !_isTopBarExpanded.value
    }

    fun toggleAiVoice() {
        _isAiVoiceOn.value = !_isAiVoiceOn.value
    }

    private val _isAutoDashcamMapping = MutableStateFlow(true)
    val isAutoDashcamMapping: StateFlow<Boolean> = _isAutoDashcamMapping.asStateFlow()

    fun toggleAutoDashcamMapping() {
        _isAutoDashcamMapping.value = !_isAutoDashcamMapping.value
    }

    fun readIncomingAlert(hazard: RoadHazardEntity, distanceMeters: Int = 500) {
        val alertPhrase = when (hazard.type) {
            "SPEED_BUMP" -> "Caution: Steep speed hump reported ${distanceMeters}m ahead on ${hazard.roadName}."
            "COP_TRAP" -> "Alert: Kenya Traffic Police checkpoint reported ${distanceMeters}m ahead on ${hazard.roadName}."
            "STATIONARY_SPEED_CAM" -> "Caution: NTSA Stationary Speed Camera reported ${distanceMeters}m ahead on ${hazard.roadName}. Keep speed under 80 KM/H."
            "MOBILE_SPEED_CAM" -> "Alert: NTSA Mobile Radar Speed Van reported ${distanceMeters}m ahead on ${hazard.roadName}."
            "SPEED_CAMERA" -> "Notice: Automated Speed camera ${distanceMeters}m ahead on ${hazard.roadName}."
            "POTHOLE" -> "Danger: Deep pothole reported ${distanceMeters}m ahead on ${hazard.roadName}."
            "ACCIDENT" -> "Warning: Traffic accident reported ${distanceMeters}m ahead on ${hazard.roadName}."
            else -> "Notice: Road hazard reported ${distanceMeters}m ahead on ${hazard.roadName}."
        }
        speakText(alertPhrase)
    }

    private val _pinnedLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val pinnedLocation: StateFlow<Pair<Double, Double>?> = _pinnedLocation.asStateFlow()

    private val _pinnedRoadName = MutableStateFlow<String?>(null)
    val pinnedRoadName: StateFlow<String?> = _pinnedRoadName.asStateFlow()

    fun setPinnedLocation(lat: Double, lng: Double, roadName: String) {
        _pinnedLocation.value = Pair(lat, lng)
        _pinnedRoadName.value = roadName
    }

    fun clearPinnedLocation() {
        _pinnedLocation.value = null
        _pinnedRoadName.value = null
    }

    fun scanDashcamAndAutoMap(
        detectedType: HazardType,
        title: String,
        roadName: String,
        distanceMeters: Int = 300
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val reporterName = user?.username ?: "Dashcam AI Vision"
            val severity = when (detectedType) {
                HazardType.COP_TRAP -> HazardSeverity.HIGH
                HazardType.ACCIDENT -> HazardSeverity.CRITICAL
                HazardType.SPEED_BUMP -> HazardSeverity.MEDIUM
                HazardType.POTHOLE -> HazardSeverity.MEDIUM
                else -> HazardSeverity.LOW
            }

            repository.addHazard(
                type = detectedType,
                title = title,
                description = "Auto-mapped via Dashcam AI Vision Camera Scanner",
                lat = -1.2863 + ((-40..40).random() / 10000.0),
                lng = 36.8172 + ((-40..40).random() / 10000.0),
                roadName = roadName,
                severity = severity,
                reporter = reporterName
            )

            // Persistently log the trip scan in Room Database
            repository.logTripScan(
                ScannedTripLogEntity(
                    hazardType = detectedType.name,
                    label = title,
                    roadName = roadName,
                    driverSpeedKmh = _currentSpeedMph.value,
                    confidence = (88..98).random(),
                    distanceMeters = distanceMeters,
                    actionTaken = "Voice Alert & Mapped to Radar"
                )
            )

            val alertSpeech = "Dashcam AI detected $title ${distanceMeters}m ahead on $roadName. Hazard mapped to Cruize 3D radar."
            speakText(alertSpeech)
            triggerAiRoadAnalysis()
        }
    }

    fun toggleSimulateDrive() {
        _isSimulatingDrive.value = !_isSimulatingDrive.value
        if (_isSimulatingDrive.value) {
            _currentSpeedMph.value = (52..68).random()
            triggerAiRoadAnalysis()
        } else {
            _currentSpeedMph.value = 45
        }
    }

    fun setShowReportDialog(show: Boolean) {
        _showReportDialog.value = show
    }

    fun triggerAiRoadAnalysis() {
        viewModelScope.launch {
            _isLoadingAiAlert.value = true
            val hazardsList = allHazards.value
            val alert = GeminiDriverAssistant.analyzeRoadAhead(
                currentSpeedMph = _currentSpeedMph.value,
                roadName = _currentRoadName.value,
                nearbyHazards = hazardsList.take(3)
            )
            _aiAlertState.value = alert
            _isLoadingAiAlert.value = false

            if (_isAiVoiceOn.value && alert.voiceWarning.isNotBlank()) {
                speakText(alert.voiceWarning)
            }
        }
    }

    fun speakCurrentAlert() {
        val alert = _aiAlertState.value
        if (alert != null) {
            speakText(alert.voiceWarning)
        } else {
            speakText("Cruize radar active. All clear on ${_currentRoadName.value}.")
        }
    }

    fun speakText(text: String) {
        _speechFeedbackMessage.value = "🔊 Audio Warning: $text"
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cruize_alert_id")
        } catch (e: Exception) {
            // Speech engine fallback
        }
    }

    fun clearSpeechFeedback() {
        _speechFeedbackMessage.value = null
    }

    fun postHazardReport(
        type: HazardType,
        title: String,
        description: String,
        roadName: String,
        severity: HazardSeverity,
        speedLimit: Int = 50,
        cameraType: String = "TRAFFIC_SURVEILLANCE",
        cameraDirection: String = "BOTH_WAYS",
        cameraStatus: String = "ACTIVE_RECORDING"
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val reporterName = user?.username ?: "Anonymous Cruize Driver"
            val pin = _pinnedLocation.value
            val latVal = pin?.first ?: (-1.2863 + ((-50..50).random() / 10000.0))
            val lngVal = pin?.second ?: (36.8172 + ((-50..50).random() / 10000.0))

            val id = repository.addHazard(
                type = type,
                title = title,
                description = if (pin != null) "$description (Manual Map Pin Drop)" else description,
                lat = latVal,
                lng = lngVal,
                roadName = roadName,
                severity = severity,
                reporter = reporterName,
                speedLimit = speedLimit,
                cameraType = cameraType,
                cameraDirection = cameraDirection,
                cameraStatus = cameraStatus
            )
            _showReportDialog.value = false
            _showCctvMappingDialog.value = false
            clearPinnedLocation()
            val feedback = if (type == HazardType.CCTV_CAMERA) "CCTV camera mapped! Added to Cruize radar." else "Hazard reported! Thank you for alerting Cruize drivers."
            speakText(feedback)
            triggerAiRoadAnalysis()
        }
    }

    fun mapCctvCamera(
        title: String,
        roadName: String,
        cameraType: String,
        cameraDirection: String,
        cameraStatus: String,
        speedLimit: Int,
        description: String = "Community Mapped CCTV Surveillance & Enforcement Unit"
    ) {
        postHazardReport(
            type = HazardType.CCTV_CAMERA,
            title = title,
            description = description,
            roadName = roadName,
            severity = if (cameraType == "RED_LIGHT" || cameraType == "SPEED_GANTRY") HazardSeverity.HIGH else HazardSeverity.MEDIUM,
            speedLimit = speedLimit,
            cameraType = cameraType,
            cameraDirection = cameraDirection,
            cameraStatus = cameraStatus
        )
    }

    fun upvoteHazard(id: Long) {
        viewModelScope.launch {
            repository.upvote(id)
        }
    }

    fun downvoteHazard(id: Long) {
        viewModelScope.launch {
            repository.downvote(id)
        }
    }

    fun addReview(hazardId: Long, rating: Float, comment: String) {
        viewModelScope.launch {
            val username = currentUser.value?.username ?: "CruizeRider"
            repository.addReview(hazardId, username, rating, comment)
            speakText("Review posted. Driver community updated.")
        }
    }

    fun signUp(username: String, email: String, pass: String, vehicle: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.signUp(username, email, pass, vehicle)
            res.fold(
                onSuccess = { onResult(null) },
                onFailure = { onResult(it.message ?: "Sign up failed") }
            )
        }
    }

    fun signIn(email: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.signIn(email, pass)
            res.fold(
                onSuccess = { onResult(null) },
                onFailure = { onResult(it.message ?: "Sign in failed") }
            )
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            repository.loginAsGuest()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _currentDestination.value = NavDestination.PROFILE
        }
    }

    fun setProximityRange(meters: Int) {
        _proximityAlertRangeMeters.value = meters
        viewModelScope.launch {
            repository.updateProximityRange(meters)
        }
    }

    fun updateSpeedUnitSetting(isMph: Boolean) {
        viewModelScope.launch {
            repository.updateSpeedUnit(isMph)
            speakText(if (isMph) "Speed unit set to Miles Per Hour" else "Speed unit set to Kilometers Per Hour")
        }
    }

    fun updateAiVoiceSetting(enabled: Boolean) {
        _isAiVoiceOn.value = enabled
        viewModelScope.launch {
            repository.updateAiVoice(enabled)
            if (enabled) speakText("Cruize AI voice warnings activated")
        }
    }

    fun update3dPerspectiveSetting(is3d: Boolean) {
        _is3dPerspective.value = is3d
        viewModelScope.launch {
            repository.update3dPerspective(is3d)
        }
    }

    fun updateThemeModeSetting(nightMode: Boolean) {
        viewModelScope.launch {
            repository.updateThemeMode(nightMode)
        }
    }

    fun updateRegionSetting(region: String) {
        viewModelScope.launch {
            repository.updateRegion(region)
            speakText("Corridor region set to $region")
        }
    }

    fun updateBrandAccentSetting(accent: String) {
        viewModelScope.launch {
            repository.updateBrandAccent(accent)
        }
    }

    fun clearTripLogs() {
        viewModelScope.launch {
            repository.clearTripLogs()
            speakText("Trip scan logs cleared")
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Cleanup
        }
    }
}
