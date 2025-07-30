package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.device.xled.XledMatrixDevice

abstract class XledArrayTest {

    protected val xledArray = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice("192.168.178.35", 10, 21),
                XLedDevice("192.168.178.58", 10, 21),
            ),
            arrayOf(
                XLedDevice("192.168.178.52", 10, 21),
                XLedDevice("192.168.178.60", 10, 21)
            )
        )
    )

    protected val xledArrayLandscapeLeft = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice("192.168.178.35", 10, 21),
                XLedDevice("192.168.178.58", 10, 21),
            ),
            arrayOf(
                XLedDevice("192.168.178.52", 10, 21),
                XLedDevice("192.168.178.60", 10, 21)
            )
        ),
        DeviceOrigin.BOTTOM_LEFT
    )

    protected val xledArrayLandscapeRight = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice("192.168.178.35", 10, 21),
                XLedDevice("192.168.178.58", 10, 21),
            ),
            arrayOf(
                XLedDevice("192.168.178.52", 10, 21),
                XLedDevice("192.168.178.60", 10, 21)
            )
        ),
        DeviceOrigin.TOP_RIGHT
    )

    protected val xledMatrix = XledMatrixDevice("192.168.178.34", 10, 50)
}
