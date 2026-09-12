package com.example

import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.DeviceBrand
import com.example.data.network.NsdCastScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NsdScannerTest {

    @Test
    fun targetServiceTypes_containsSmartTvProtocols() {
        val serviceTypes = NsdCastScanner.TARGET_SERVICE_TYPES.map { it.first }
        assertTrue(serviceTypes.contains("_googlecast._tcp."))
        assertTrue(serviceTypes.contains("_airplay._tcp."))
        assertTrue(serviceTypes.contains("_dial-multiscreen-org._tcp."))
        assertTrue(serviceTypes.contains("_smartview._tcp."))
        assertTrue(serviceTypes.contains("_upnp._tcp."))
    }

    @Test
    fun castDevice_defaultValues_areValid() {
        val device = CastDevice(
            id = "nsd_living_room_cast",
            name = "Living Room TV",
            brand = DeviceBrand.SAMSUNG,
            model = "Neo QLED 4K",
            ipAddress = "192.168.1.100",
            room = "Salon",
            protocol = CastProtocol.SAMSUNG_SMART_VIEW,
            port = 8001,
            isNsdDiscovered = true
        )

        assertEquals("Living Room TV", device.name)
        assertEquals(8001, device.port)
        assertTrue(device.isNsdDiscovered)
        assertTrue(device.isOnline)
        assertNotNull(device.resolutionSupport)
    }
}
