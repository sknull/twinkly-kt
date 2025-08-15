package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("only for local testing")
class XledFrameTest {

    private val xledArray = XLedArray.instance(
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

    private val xledArrayLandscape = XLedArray.instance(
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

    @Test
    fun testSingleFrames() {
        val curtain1 = XLedDevice.instance(ipAddress = "192.168.178.38", width = 10, height = 21)
        val frame1 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame1[0, 0] = RGBColor(255, 0, 0)
        frame1[9, 0] = RGBColor(0, 255, 0)
        frame1[0, 20] = RGBColor(0, 0, 255)
        frame1[9, 20] = RGBColor(255, 0, 255)

        frame1[5, 5] = RGBColor(255, 0, 0)

        frame1.play(curtain1)

        val curtain2 = XLedDevice.instance(ipAddress = "192.168.178.58", width = 10, height = 21)
        val frame2 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame2[0, 0] = RGBColor(255, 0, 0)
        frame2[9, 0] = RGBColor(0, 255, 0)
        frame2[0, 20] = RGBColor(0, 0, 255)
        frame2[9, 20] = RGBColor(255, 0, 255)

        frame2[5, 5] = RGBColor(255, 0, 0)
        frame2[6, 5] = RGBColor(255, 0, 0)

        frame2.play(curtain2)

        val curtain3 = XLedDevice.instance(ipAddress = "192.168.178.52", width = 10, height = 21)
        val frame3 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame3[0, 0] = RGBColor(255, 0, 0)
        frame3[9, 0] = RGBColor(0, 255, 0)
        frame3[0, 20] = RGBColor(0, 0, 255)
        frame3[9, 20] = RGBColor(255, 0, 255)

        frame3[5, 5] = RGBColor(255, 0, 0)
        frame3[6, 5] = RGBColor(255, 0, 0)
        frame3[5, 6] = RGBColor(255, 0, 0)

        frame3.play(curtain3)

        val curtain4 = XLedDevice.instance(ipAddress = "192.168.178.60", width = 10, height = 21)
        val frame4 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame4[0, 0] = RGBColor(255, 0, 0)
        frame4[9, 0] = RGBColor(0, 255, 0)
        frame4[0, 20] = RGBColor(0, 0, 255)
        frame4[9, 20] = RGBColor(255, 0, 255)

        frame4[5, 5] = RGBColor(255, 0, 0)
        frame4[6, 5] = RGBColor(255, 0, 0)
        frame4[5, 6] = RGBColor(255, 0, 0)
        frame4[6, 6] = RGBColor(255, 0, 0)

        frame4.play(curtain4)
    }

    @Test
    fun testPurple() {
        val canvas = XledFrame(xledArray.width, xledArray.height, RGBColor(32, 0, 255))
        canvas.play(xledArray)
    }

    @Test
    fun testFrames() {
        val frame1 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame1[0, 0] = RGBColor(255, 0, 0)
        frame1[9, 0] = RGBColor(0, 255, 0)
        frame1[0, 20] = RGBColor(0, 0, 255)
        frame1[9, 20] = RGBColor(255, 0, 255)

        frame1[5, 5] = RGBColor(255, 0, 0)

        val frame2 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame2[0, 0] = RGBColor(255, 0, 0)
        frame2[9, 0] = RGBColor(0, 255, 0)
        frame2[0, 20] = RGBColor(0, 0, 255)
        frame2[9, 20] = RGBColor(255, 0, 255)

        frame2[5, 5] = RGBColor(255, 0, 0)
        frame2[6, 5] = RGBColor(255, 0, 0)

        val frame3 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame3[0, 0] = RGBColor(255, 0, 0)
        frame3[9, 0] = RGBColor(0, 255, 0)
        frame3[0, 20] = RGBColor(0, 0, 255)
        frame3[9, 20] = RGBColor(255, 0, 255)

        frame3[5, 5] = RGBColor(255, 0, 0)
        frame3[6, 5] = RGBColor(255, 0, 0)
        frame3[5, 6] = RGBColor(255, 0, 0)

        val frame4 = XledFrame(20, 21, RGBColor(0, 0, 0))
        frame4[0, 0] = RGBColor(255, 0, 0)
        frame4[9, 0] = RGBColor(0, 255, 0)
        frame4[0, 20] = RGBColor(0, 0, 255)
        frame4[9, 20] = RGBColor(255, 0, 255)

        frame4[5, 5] = RGBColor(255, 0, 0)
        frame4[6, 5] = RGBColor(255, 0, 0)
        frame4[5, 6] = RGBColor(255, 0, 0)
        frame4[6, 6] = RGBColor(255, 0, 0)

        val canvas = XledFrame(xledArray.width, xledArray.height)
        canvas.replaceSubFrame(frame1, 0, 0)
        canvas.replaceSubFrame(frame2, 10, 0)
        canvas.replaceSubFrame(frame3, 0,  21)
        canvas.replaceSubFrame(frame4, 10, 21)

        canvas.play(xledArray)
    }
}
