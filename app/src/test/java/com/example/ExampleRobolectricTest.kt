package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppScreen
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceBrand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart TV Cast", appName)
  }

  @Test
  fun `cast screen navigation and connection status models`() {
    val state = CastState(
        status = ConnectionStatus.CONNECTED,
        connectedDevice = CastDevice(
            id = "test_chromecast",
            name = "Bedroom Chromecast",
            brand = DeviceBrand.CHROMECAST,
            model = "Chromecast with Google TV",
            ipAddress = "192.168.1.105",
            room = "Yatak Odası",
            protocol = CastProtocol.CHROMECAST
        ),
        volumePercent = 80
    )

    assertEquals(ConnectionStatus.CONNECTED, state.status)
    assertEquals("Bedroom Chromecast", state.connectedDevice?.name)
    assertEquals(AppScreen.CAST, AppScreen.valueOf("CAST"))
    assertTrue(state.progressFraction >= 0f)
  }
}
