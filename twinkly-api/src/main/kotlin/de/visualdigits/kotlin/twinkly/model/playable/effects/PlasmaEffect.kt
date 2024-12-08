package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class PlasmaEffect(
    val angle: Double,
    val zoom: Double,
    val speed: Double,
    xled: XLed,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledEffect("Plasma Effect", xled, initialColor = initialColor) {

    private val mx = max(xled.width, xled.height) 
    private val vTexCoord = arrayOf(xled.width / 2.0 / mx, xled.height / 2.0 / mx)
    
    private val coords = vTexCoord
    private val sk = arrayOf(1.0, 1.0)
    private val coordScaling = 10.0
    private val fullZoom = fullScaleToZoom(zoom)
    private val fullAngle = fullScaleToAngle(angle)
    private val fullRad = angleFDegToRad(fullAngle)
    private val fullSpeed = fullScaleToSpeedF(speed)

    private var x = coords[0] / fullZoom
    private var y = coords[1] / fullZoom
    private var time = 0.0

    init {
        x = x * cos(fullRad) - y * sin(fullRad)
        y = x * sin(fullRad) - y * cos(fullRad)

        if (fullSpeed > 0.0) {
            time = ((time * 1000.0) * 2.0 / 1000.0) * fullSpeed
        } else {
            time = 0.0
        }
    }
    
    override fun getNextFrame() {
        val xx = sk[0] * x - sk[0] / 2.0
        val yy = sk[1] * y - sk[1] / 2.0
        var v = sin(xx * coordScaling + time)
        v += sin((yy * coordScaling + time) / 2.0)
        v += sin((xx * coordScaling + yy * coordScaling + time) / 2.0)
        val cx = (xx + 0.5 * sin(time / 5.0))
        val cy = (yy + 0.5 * cos(time / 3.0))

        v += sin(sqrt(100.0 * (cx * cx + cy * cy) + 1.0) + time)
        v /= 2.0

        RGBWColor(255, (255 * (sin(PI * v) + 1.0) / 2.0).roundToInt(), (255 * (cos(PI * v) + 1.0) / 2.0).roundToInt(), 255)
        println("$cx, $cy")

        time += 1
    }
    
    companion object {
        var FULL_SCALE: Double = 16384.0
        var ZOOM_MAX: Double = 5.0
        var ZOOM_SHIFT: Double = 0.2
        var ZOOM_RANGE: Double = ZOOM_MAX - ZOOM_SHIFT

        fun fullScaleToZoom(zoom: Double): Double {
            return (ZOOM_SHIFT * ZOOM_RANGE * (FULL_SCALE - zoom) + ZOOM_MAX * ZOOM_RANGE * zoom) / (ZOOM_RANGE * FULL_SCALE)
        }

        fun fullScaleToAngle(angleFs: Double): Double {
            return (angleFs / FULL_SCALE * 360.0)
        }

        fun angleFDegToRad(deg: Double): Double {
            return (deg * PI / 180.0)
        }

        fun fullScaleToSpeedF(speedFs: Double): Double {
            return speedFs / FULL_SCALE
        }

        fun getRemainder(num: Int, divisor: Int): Int {
            return (num - divisor * (num / divisor))
        }
    }
}
