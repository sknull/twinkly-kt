package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedArray
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledMatrixDevice
import de.visualdigits.kotlin.twinkly.model.device.xmusic.XMusic

abstract class XledArrayTest {

    protected val twinklyMusic = XMusic.instance("192.168.178.41")

    protected val xledMatrix = XledMatrixDevice.instance("192.168.178.39", "matrix", 10, 50)

    val curtain1 = XLedDevice.instance(ipAddress = "192.168.178.38", name = "curtain1", width = 10, height = 21)
    val curtain2 = XLedDevice.instance(ipAddress = "192.168.178.52", name = "curtain2", width = 10, height = 21)
    val curtain3 = XLedDevice.instance(ipAddress = "192.168.178.58", name = "curtain3", width = 10, height = 21)
    val curtain4 = XLedDevice.instance(ipAddress = "192.168.178.60", name = "curtain4", width = 10, height = 21)

    protected val xledArray = XLedArray.instance(
        mutableListOf(
            mutableListOf(
                curtain1,
                curtain2,
            ),
            mutableListOf(
                curtain3,
                curtain4
            )
        )
    )

    protected val xledArrayLandscapeLeft = XLedArray.instance(
        mutableListOf(
            mutableListOf(
                curtain1,
                curtain2,
            ),
            mutableListOf(
                curtain3,
                curtain4
            )
        ),
        DeviceOrigin.BOTTOM_LEFT
    )

    protected val xledArrayLandscapeRight = XLedArray.instance(
        xLedDevices = mutableListOf(
            mutableListOf(
                curtain1,
                curtain2,
            ),
            mutableListOf(
                curtain3,
                curtain4
            )
        ),
        deviceOrigin = DeviceOrigin.TOP_RIGHT
    )
}
