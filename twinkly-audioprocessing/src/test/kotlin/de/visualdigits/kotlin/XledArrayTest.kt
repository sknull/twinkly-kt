package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedArray
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice

abstract class XledArrayTest {

    protected val xledArray = XLedArray.instance(
        mutableListOf(
            mutableListOf(
                XLedDevice.instance(ipAddress = "192.168.178.38", width = 10, height = 21),
                XLedDevice.instance(ipAddress = "192.168.178.52", width = 10, height = 21),
            ),
            mutableListOf(
                XLedDevice.instance(ipAddress = "192.168.178.58", width = 10, height = 21),
                XLedDevice.instance(ipAddress = "192.168.178.60", width = 10, height = 21)
            )
        )
    )

    protected val xledArrayLandscapeLeft = XLedArray.instance(
        mutableListOf(
            mutableListOf(
                XLedDevice.instance(ipAddress = "192.168.178.38", width = 10, height = 21),
                XLedDevice.instance(ipAddress = "192.168.178.52", width = 10, height = 21),
            ),
            mutableListOf(
                XLedDevice.instance(ipAddress = "192.168.178.58", width = 10, height = 21),
                XLedDevice.instance(ipAddress = "192.168.178.60", width = 10, height = 21)
            )
        ),
        DeviceOrigin.BOTTOM_LEFT
    )

    protected val xledArrayLandscapeRight = XLedArray.instance(
        mutableListOf(
            mutableListOf(
                XLedDevice.instance(ipAddress = "192.168.178.38", width = 10, height = 21),
                XLedDevice.instance(ipAddress = "192.168.178.52", width = 10, height = 21),
            ),
            mutableListOf(
                XLedDevice.instance(ipAddress = "192.168.178.58", width = 10, height = 21),
                XLedDevice.instance(ipAddress = "192.168.178.60", width = 10, height = 21)
            )
        ),
        DeviceOrigin.TOP_RIGHT
    )
}
