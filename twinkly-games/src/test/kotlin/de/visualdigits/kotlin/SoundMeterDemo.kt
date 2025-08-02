package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import org.hid4java.HidManager
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

class SoundMeterDemo : XledArrayTest() {

    private val hidDevice = HidManager.getHidServices().attachedHidDevices.find { it.vendorId == 0x64bd && it.productId == 0x74e3 }
    private val reportFetchData = intArrayOf(0xB3, 0x00, 0x00, 0x00, 0x08, 0xFD, 0x19, 0x00).map { it.toByte() }.toByteArray()

    @Test
    fun soundMeter() {
        var frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 0)
        )

        val heightYellow = xledArray.height - (xledArray.height / 100.0 * 50.0).roundToInt()
        val heightRed = xledArray.height - (xledArray.height / 100.0 * 70.0).roundToInt()

        frame.fillRect(0, heightYellow, 1, xledArray.height - 1, RGBColor(0,255,0))
        frame.fillRect(0, heightRed, 1, heightYellow - 1, RGBColor(255,255,0))
        frame.fillRect(0, 0, 1, heightRed - 1, RGBColor(255,0,0))

        xledArray.setLedMode(LedMode.rt)

        hidDevice?.also { device ->
            device.open()
            while (true) {
                device.write(reportFetchData, reportFetchData.size, 0x00)
                val bytes = device.read(3, 500)
                val result = bytes.map { it.toInt() and 0xff }
                if (result.isNotEmpty()) {
                    val db = ((result[0] and 0x7 shl 8) or result[1]) / 10.0
                    val y = xledArray.height - ((db - 30.0) / 100.0 * xledArray.height).roundToInt()
                    if (db > 100) {
                        frame.fillRect(5, heightYellow, xledArray.width - 5, xledArray.height - 1, RGBColor(0,255,0))
                        frame.fillRect(5, heightRed, xledArray.width - 5, heightYellow - 1, RGBColor(255,255,0))
                        frame.fillRect(5, y, xledArray.width - 5, heightRed - 1, RGBColor(255,0,0))
                    } else if (db > 80) {
                        frame.fillRect(5, heightYellow, xledArray.width - 5, xledArray.height - 1, RGBColor(0,255,0))
                        frame.fillRect(5, y, xledArray.width - 5, heightYellow - 1, RGBColor(255,255,0))
                    } else {
                        frame.fillRect(5, y, xledArray.width - 5, xledArray.height - 1, RGBColor(0,255,0))
                    }
                }
                xledArray.showRealTimeFrame(frame)
                Thread.sleep(100)
                frame = XledFrame(
                    width = xledArray.width,
                    height = xledArray.height,
                    initialColor = RGBColor(0, 0, 0)
                )
                frame.fillRect(0, heightYellow, 1, xledArray.height - 1, RGBColor(0,255,0))
                frame.fillRect(0, heightRed, 1, heightYellow - 1, RGBColor(255,255,0))
                frame.fillRect(0, 0, 1, heightRed - 1, RGBColor(255,0,0))
            }
        }
    }
}
