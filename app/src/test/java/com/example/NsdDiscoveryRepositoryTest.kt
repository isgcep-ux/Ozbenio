package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.DeviceBrand
import com.example.data.repository.NsdDiscoveryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NsdDiscoveryRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: NsdDiscoveryRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = NsdDiscoveryRepository(context)
    }

    @Test
    fun smartTvServiceTypes_coversAllKeyProtocols() {
        val serviceTypes = NsdDiscoveryRepository.SMART_TV_SERVICE_TYPES.map { it.first }

        // Google Cast & Android TV
        assertTrue("Google Cast should be covered", serviceTypes.contains("_googlecast._tcp."))
        assertTrue("Android TV remote should be covered", serviceTypes.contains("_androidtvremote._tcp."))

        // Apple AirPlay
        assertTrue("AirPlay should be covered", serviceTypes.contains("_airplay._tcp."))
        assertTrue("RAOP should be covered", serviceTypes.contains("_raop._tcp."))

        // Samsung Smart View
        assertTrue("Samsung Smart View should be covered", serviceTypes.contains("_smartview._tcp."))

        // LG webOS
        assertTrue("LG webOS should be covered", serviceTypes.contains("_webos-second-screen._tcp."))

        // Roku & DIAL
        assertTrue("DIAL protocol should be covered", serviceTypes.contains("_dial-multiscreen-org._tcp."))
        assertTrue("Roku RCP should be covered", serviceTypes.contains("_roku-rcp._tcp."))

        // DLNA / UPnP
        assertTrue("UPnP should be covered", serviceTypes.contains("_upnp._tcp."))
    }

    @Test
    fun registerManualDevice_updatesDiscoveredTvsFlow() {
        val sampleTv = CastDevice(
            id = "test_samsung_tv",
            name = "Salon Samsung QLED",
            brand = DeviceBrand.SAMSUNG,
            model = "QN90C 4K Smart Hub",
            ipAddress = "192.168.1.55",
            room = "Salon",
            protocol = CastProtocol.SAMSUNG_SMART_VIEW,
            port = 8001,
            isNsdDiscovered = true
        )

        repository.registerManualDevice(sampleTv)

        val list = repository.discoveredTvs.value
        assertEquals(1, list.size)
        assertEquals("test_samsung_tv", list[0].id)
        assertEquals("Salon Samsung QLED", list[0].name)
        assertEquals(CastProtocol.SAMSUNG_SMART_VIEW, list[0].protocol)
        assertEquals(8001, list[0].port)
    }

    @Test
    fun getDeviceById_returnsRegisteredDevice() {
        val lgTv = CastDevice(
            id = "test_lg_oled",
            name = "Yatak Odası LG OLED",
            brand = DeviceBrand.LG,
            model = "OLED C3 55\"",
            ipAddress = "192.168.1.60",
            room = "Yatak Odası",
            protocol = CastProtocol.LG_WEBOS,
            port = 3000,
            isNsdDiscovered = true
        )

        repository.registerManualDevice(lgTv)

        val retrieved = repository.getDeviceById("test_lg_oled")
        assertNotNull(retrieved)
        assertEquals("Yatak Odası LG OLED", retrieved?.name)
        assertEquals(DeviceBrand.LG, retrieved?.brand)
    }

    @Test
    fun clearDiscoveredDevices_emptiesList() {
        val appleTv = CastDevice(
            id = "test_apple_tv",
            name = "Ev Sineması Apple TV",
            brand = DeviceBrand.APPLE_TV,
            model = "Apple TV 4K",
            ipAddress = "192.168.1.70",
            room = "Ev Sineması",
            protocol = CastProtocol.AIRPLAY,
            port = 7000,
            isNsdDiscovered = true
        )

        repository.registerManualDevice(appleTv)
        assertEquals(1, repository.discoveredTvs.value.size)

        repository.clearDiscoveredDevices()
        assertEquals(0, repository.discoveredTvs.value.size)
    }

    @Test
    fun discoveryLifecycle_startAndStop_updatesScanningState() {
        // Initially not scanning
        assertFalse(repository.isScanning.value)

        // Starting discovery initiates scan state
        repository.startDiscovery()
        // Stop discovery resets scan state
        repository.stopDiscovery()
        assertFalse(repository.isScanning.value)
    }
}
