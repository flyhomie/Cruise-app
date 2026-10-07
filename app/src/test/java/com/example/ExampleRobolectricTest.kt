package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppSettingsEntity
import com.example.data.local.CruizeDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cruize", appName)
  }

  @Test
  fun `verify Room app settings persistence`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, CruizeDatabase::class.java).build()
    val dao = db.appSettingsDao()

    val initialSettings = AppSettingsEntity(
      id = 1,
      speedUnitMph = false,
      isAiVoiceOn = true,
      proximityAlertRangeMeters = 500,
      is3dPerspective = true,
      nightDriveTheme = true,
      selectedRegion = "Kenya (Nairobi / Mombasa Corridors)",
      brandAccentTheme = "NEON_CYAN"
    )
    dao.insertOrUpdate(initialSettings)

    val stored = dao.getAppSettingsDirect()
    assertNotNull(stored)
    assertEquals(false, stored?.speedUnitMph)
    assertEquals("Kenya (Nairobi / Mombasa Corridors)", stored?.selectedRegion)
    db.close()
  }
}

