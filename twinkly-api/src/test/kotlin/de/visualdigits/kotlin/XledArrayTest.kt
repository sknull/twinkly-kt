package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedArray
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledMatrixDevice
import de.visualdigits.kotlin.twinkly.model.device.xmusic.XMusic

abstract class XledArrayTest {

    protected val twinklyMusic = XMusic.instance("192.168.178.39")

    protected val xledMatrix = XledMatrixDevice.instance("192.168.178.34", 10, 50)

    val curtain1 = XLedDevice.instance(ipAddress = "192.168.178.35", width = 10, height = 21)
    val curtain2 = XLedDevice.instance(ipAddress = "192.168.178.58", width = 10, height = 21)
    val curtain3 = XLedDevice.instance(ipAddress = "192.168.178.52", width = 10, height = 21)
    val curtain4 = XLedDevice.instance(ipAddress = "192.168.178.60", width = 10, height = 21)

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
