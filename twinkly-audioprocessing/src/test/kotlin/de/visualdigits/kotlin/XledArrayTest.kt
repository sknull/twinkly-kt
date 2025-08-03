package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray

abstract class XledArrayTest {

    protected val xledArray = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice(ipAddress = "192.168.178.35", width = 10, height = 21),
                XLedDevice(ipAddress = "192.168.178.58", width = 10, height = 21),
            ),
            arrayOf(
                XLedDevice(ipAddress = "192.168.178.52", width = 10, height = 21),
                XLedDevice(ipAddress = "192.168.178.60", width = 10, height = 21)
            )
        )
    )

    protected val xledArrayLandscapeLeft = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice(ipAddress = "192.168.178.35", width = 10, height = 21),
                XLedDevice(ipAddress = "192.168.178.58", width = 10, height = 21),
            ),
            arrayOf(
                XLedDevice(ipAddress = "192.168.178.52", width = 10, height = 21),
                XLedDevice(ipAddress = "192.168.178.60", width = 10, height = 21)
            )
        ),
        DeviceOrigin.BOTTOM_LEFT
    )

    protected val xledArrayLandscapeRight = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice(ipAddress = "192.168.178.35", width = 10, height = 21),
                XLedDevice(ipAddress = "192.168.178.58", width = 10, height = 21),
            ),
            arrayOf(
                XLedDevice(ipAddress = "192.168.178.52", width = 10, height = 21),
                XLedDevice(ipAddress = "192.168.178.60", width = 10, height = 21)
            )
        ),
        DeviceOrigin.TOP_RIGHT
    )
}
