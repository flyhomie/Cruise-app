package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CruizeRepository(private val db: CruizeDatabase) {
    val userDao = db.userDao()
    val hazardDao = db.roadHazardDao()
    val reviewDao = db.hazardReviewDao()
    val appSettingsDao = db.appSettingsDao()
    val scannedTripLogDao = db.scannedTripLogDao()

    val loggedInUser: Flow<UserEntity?> = userDao.getLoggedInUser()
    val allHazards: Flow<List<RoadHazardEntity>> = hazardDao.getAllHazards()
    val allReviews: Flow<List<HazardReviewEntity>> = reviewDao.getAllReviews()
    val appSettings: Flow<AppSettingsEntity?> = appSettingsDao.getAppSettings()
    val allTripLogs: Flow<List<ScannedTripLogEntity>> = scannedTripLogDao.getAllTripLogs()

    suspend fun initializePrepopulatedData() {
        if (hazardDao.getHazardCount() == 0) {
            val sampleHazards = listOf(
                RoadHazardEntity(
                    type = HazardType.CCTV_CAMERA.name,
                    title = "Thika Superhighway Roysambu ANPR CCTV",
                    description = "Automated number plate recognition & multi-lane traffic surveillance CCTV gantry with active speed monitoring.",
                    latitude = -1.2180,
                    longitude = 36.8850,
                    roadName = "Thika Superhighway Roysambu",
                    speedLimit = 80,
                    severity = HazardSeverity.HIGH.name,
                    reportedBy = "NTSA Highway Watch",
                    upvotes = 67,
                    downvotes = 0,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                    cameraType = "ANPR_PLATE_SCANNER",
                    cameraDirection = "ALL_LANES",
                    cameraStatus = "AI_ENFORCING"
                ),
                RoadHazardEntity(
                    type = HazardType.CCTV_CAMERA.name,
                    title = "Uhuru Highway Red Light CCTV",
                    description = "High-definition intersection surveillance & automated red light violation enforcement camera system.",
                    latitude = -1.2890,
                    longitude = 36.8200,
                    roadName = "Uhuru Highway & Haile Selassie Roundabout",
                    speedLimit = 50,
                    severity = HazardSeverity.CRITICAL.name,
                    reportedBy = "NairobiTrafficHQ",
                    upvotes = 84,
                    downvotes = 1,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    cameraType = "RED_LIGHT",
                    cameraDirection = "INTERSECTION_360",
                    cameraStatus = "RED_LIGHT_ACTIVE"
                ),
                RoadHazardEntity(
                    type = HazardType.CCTV_CAMERA.name,
                    title = "Nairobi Expressway Mlolongo Gantry CCTV",
                    description = "Overhead smart gantry surveillance CCTV monitoring high-speed express lanes and toll plaza flow.",
                    latitude = -1.3780,
                    longitude = 36.9380,
                    roadName = "Nairobi Expressway Mlolongo Section",
                    speedLimit = 120,
                    severity = HazardSeverity.HIGH.name,
                    reportedBy = "ExpresswayPatrol",
                    upvotes = 42,
                    downvotes = 0,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 50,
                    cameraType = "SPEED_GANTRY",
                    cameraDirection = "INBOUND",
                    cameraStatus = "SPEED_MONITORED"
                ),
                RoadHazardEntity(
                    type = HazardType.CCTV_CAMERA.name,
                    title = "Waiyaki Way Westlands Smart Traffic CCTV",
                    description = "Intelligent Traffic Management System (ITMS) camera scanning lane congestion, yellow box violations & illegal turns.",
                    latitude = -1.2640,
                    longitude = 36.8020,
                    roadName = "Waiyaki Way & Sarit Centre Junction",
                    speedLimit = 50,
                    severity = HazardSeverity.MEDIUM.name,
                    reportedBy = "CityCouncilRadar",
                    upvotes = 58,
                    downvotes = 2,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 80,
                    cameraType = "TRAFFIC_SURVEILLANCE",
                    cameraDirection = "BOTH_WAYS",
                    cameraStatus = "ACTIVE_RECORDING"
                ),
                RoadHazardEntity(
                    type = HazardType.COP_TRAP.name,
                    title = "Kenya Traffic Police Speed Trap",
                    description = "Traffic officers with laser radar gun stationed under bridge pillar checking inbound traffic.",
                    latitude = -1.2650,
                    longitude = 36.8050,
                    roadName = "Thika Superhighway & Survey",
                    speedLimit = 80,
                    severity = HazardSeverity.HIGH.name,
                    reportedBy = "NairobiRider",
                    upvotes = 38,
                    downvotes = 1,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 12
                ),
                RoadHazardEntity(
                    type = HazardType.STATIONARY_SPEED_CAM.name,
                    title = "NTSA Fixed Speed Camera (80 KM/H)",
                    description = "Active automated overhead NTSA speed enforcement camera near footbridge.",
                    latitude = -1.2863,
                    longitude = 36.8172,
                    roadName = "Mombasa Road & Nyayo Stadium",
                    speedLimit = 80,
                    severity = HazardSeverity.HIGH.name,
                    reportedBy = "SafariDriver",
                    upvotes = 45,
                    downvotes = 0,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45
                ),
                RoadHazardEntity(
                    type = HazardType.MOBILE_SPEED_CAM.name,
                    title = "NTSA Mobile Speed Radar Van",
                    description = "White NTSA surveillance van with automated radar system parked near bus stop.",
                    latitude = -1.2720,
                    longitude = 36.8110,
                    roadName = "Waiyaki Way & Westlands",
                    speedLimit = 50,
                    severity = HazardSeverity.HIGH.name,
                    reportedBy = "CruizeKenya",
                    upvotes = 29,
                    downvotes = 2,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 25
                ),
                RoadHazardEntity(
                    type = HazardType.SPEED_BUMP.name,
                    title = "Unmarked Steep Speed Hump",
                    description = "Very high speed hump recently built across both lanes without yellow reflector paint.",
                    latitude = -1.2920,
                    longitude = 36.8210,
                    roadName = "Ngong Road & City Centre",
                    speedLimit = 50,
                    severity = HazardSeverity.CRITICAL.name,
                    reportedBy = "MatatuPro",
                    upvotes = 52,
                    downvotes = 1,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60
                ),
                RoadHazardEntity(
                    type = HazardType.POTHOLE.name,
                    title = "Deep Crater Pothole Outer Lane",
                    description = "Deep rim-damaging pothole in outer lane near roundabout exit.",
                    latitude = -1.2980,
                    longitude = 36.8320,
                    roadName = "Southern Bypass Nairobi",
                    speedLimit = 100,
                    severity = HazardSeverity.HIGH.name,
                    reportedBy = "EastAfricaTransporter",
                    upvotes = 31,
                    downvotes = 1,
                    isVerified = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 120
                )
            )

            for (h in sampleHazards) {
                val id = hazardDao.insertHazard(h)
                reviewDao.insertReview(
                    HazardReviewEntity(
                        hazardId = id,
                        userName = "Driver99",
                        rating = 5.0f,
                        comment = "Spot on! Saved me from a ticket just now.",
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 5
                    )
                )
            }
        }

        // Initialize default demo user if none exists
        val existingUser = userDao.getUserByEmail("driver@cruize.app")
        if (existingUser == null) {
            val demoUser = UserEntity(
                username = "CruizeRider",
                email = "driver@cruize.app",
                passwordHash = "demo123",
                avatarUri = "avatar_driver",
                karmaPoints = 350,
                reportsCount = 12,
                preferredVehicle = "Tesla Model S / Sports Coupe",
                isLoggedIn = true
            )
            userDao.insertUser(demoUser)
        }

        // Initialize persistent App Settings if none exist
        if (appSettingsDao.getAppSettingsDirect() == null) {
            appSettingsDao.insertOrUpdate(
                AppSettingsEntity(
                    id = 1,
                    speedUnitMph = false,
                    isAiVoiceOn = true,
                    proximityAlertRangeMeters = 500,
                    is3dPerspective = true,
                    nightDriveTheme = true,
                    selectedRegion = "Kenya (Nairobi / Mombasa Corridors)",
                    brandAccentTheme = "NEON_CYAN"
                )
            )
        }
    }

    suspend fun signUp(username: String, email: String, pass: String, vehicle: String): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("Email is already registered"))
        }
        userDao.logoutAllUsers()
        val newObj = UserEntity(
            username = username,
            email = email,
            passwordHash = pass,
            preferredVehicle = vehicle,
            karmaPoints = 50,
            reportsCount = 0,
            isLoggedIn = true
        )
        val id = userDao.insertUser(newObj)
        return Result.success(newObj.copy(id = id))
    }

    suspend fun signIn(email: String, pass: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email)
            ?: return Result.failure(Exception("User not found"))
        if (user.passwordHash != pass) {
            return Result.failure(Exception("Invalid password"))
        }
        userDao.logoutAllUsers()
        userDao.setLoggedInUser(user.id)
        return Result.success(user.copy(isLoggedIn = true))
    }

    suspend fun loginAsGuest(): UserEntity {
        userDao.logoutAllUsers()
        var guest = userDao.getUserByUsername("GuestCruiser")
        if (guest == null) {
            guest = UserEntity(
                username = "GuestCruiser",
                email = "guest@cruize.app",
                passwordHash = "guest",
                karmaPoints = 20,
                reportsCount = 0,
                isLoggedIn = true
            )
            val id = userDao.insertUser(guest)
            return guest.copy(id = id)
        } else {
            userDao.setLoggedInUser(guest.id)
            return guest.copy(isLoggedIn = true)
        }
    }

    suspend fun signOut() {
        userDao.logoutAllUsers()
    }

    suspend fun addHazard(
        type: HazardType,
        title: String,
        description: String,
        lat: Double,
        lng: Double,
        roadName: String,
        severity: HazardSeverity,
        reporter: String,
        speedLimit: Int = 50,
        cameraType: String = "TRAFFIC_SURVEILLANCE",
        cameraDirection: String = "BOTH_WAYS",
        cameraStatus: String = "ACTIVE_RECORDING"
    ): Long {
        val newHazard = RoadHazardEntity(
            type = type.name,
            title = title,
            description = description,
            latitude = lat,
            longitude = lng,
            roadName = roadName,
            speedLimit = speedLimit,
            severity = severity.name,
            reportedBy = reporter,
            upvotes = 1,
            isVerified = true,
            timestamp = System.currentTimeMillis(),
            cameraType = cameraType,
            cameraDirection = cameraDirection,
            cameraStatus = cameraStatus
        )
        val id = hazardDao.insertHazard(newHazard)
        
        // Award karma to active user
        val user = loggedInUser.firstOrNull()
        if (user != null) {
            userDao.addKarma(user.id, 15)
        }
        return id
    }

    fun getUserHazards(username: String): Flow<List<RoadHazardEntity>> {
        return hazardDao.getHazardsByReporter(username)
    }

    suspend fun updateUserProfile(userId: Long, username: String, bio: String, vehicle: String, avatar: String) {
        userDao.updateProfile(userId, username, bio, vehicle, avatar)
    }

    suspend fun deleteHazard(hazard: RoadHazardEntity) {
        hazardDao.deleteHazard(hazard)
    }

    suspend fun upvote(id: Long) {
        hazardDao.upvoteHazard(id)
        val user = loggedInUser.firstOrNull()
        if (user != null) {
            userDao.adjustReputation(user.id, 10)
        }
    }

    suspend fun downvote(id: Long) {
        hazardDao.downvoteHazard(id)
        val user = loggedInUser.firstOrNull()
        if (user != null) {
            userDao.adjustReputation(user.id, -5)
        }
    }

    suspend fun addReview(hazardId: Long, username: String, rating: Float, comment: String) {
        reviewDao.insertReview(
            HazardReviewEntity(
                hazardId = hazardId,
                userName = username,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveAppSettings(settings: AppSettingsEntity) {
        appSettingsDao.insertOrUpdate(settings)
    }

    suspend fun updateSpeedUnit(isMph: Boolean) {
        appSettingsDao.updateSpeedUnit(isMph)
    }

    suspend fun updateAiVoice(enabled: Boolean) {
        appSettingsDao.updateAiVoice(enabled)
    }

    suspend fun updateProximityRange(rangeMeters: Int) {
        appSettingsDao.updateProximityRange(rangeMeters)
    }

    suspend fun update3dPerspective(is3d: Boolean) {
        appSettingsDao.update3dPerspective(is3d)
    }

    suspend fun updateThemeMode(nightMode: Boolean) {
        appSettingsDao.updateThemeMode(nightMode)
    }

    suspend fun updateRegion(region: String) {
        appSettingsDao.updateRegion(region)
    }

    suspend fun updateBrandAccent(accent: String) {
        appSettingsDao.updateBrandAccent(accent)
    }

    suspend fun logTripScan(log: ScannedTripLogEntity) {
        scannedTripLogDao.insertLog(log)
    }

    suspend fun clearTripLogs() {
        scannedTripLogDao.clearAllLogs()
    }
}
