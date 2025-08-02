package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.device.xled.XledMatrixDevice
import de.visualdigits.kotlin.twinkly.model.device.xmusic.XMusic

abstract class XledArrayTest {

    val twinklyMusic = XMusic("192.168.178.39")

    val curtain1 = XLedDevice("192.168.178.35", 10, 21)
    val curtain2 = XLedDevice("192.168.178.58", 10, 21)
    val curtain3 = XLedDevice("192.168.178.52", 10, 21)
    val curtain4 = XLedDevice("192.168.178.60", 10, 21)

    protected val xledArray = XledArray(
        arrayOf(
            arrayOf(
                curtain1,
                curtain2,
            ),
            arrayOf(
                curtain3,
                curtain4
            )
        )
    )

    protected val xledArrayLandscapeLeft = XledArray(
        arrayOf(
            arrayOf(
                curtain1,
                curtain2,
            ),
            arrayOf(
                curtain3,
                XLedDevice("192.168.178.60", 10, 21)
            )
        ),
        DeviceOrigin.BOTTOM_LEFT
    )

    protected val xledArrayLandscapeRight = XledArray(
        arrayOf(
            arrayOf(
                curtain1,
                curtain2,
            ),
            arrayOf(
                curtain3,
                XLedDevice("192.168.178.60", 10, 21)
            )
        ),
        DeviceOrigin.TOP_RIGHT
    )

    protected val xledMatrix = XledMatrixDevice("192.168.178.34", 10, 50)
}
