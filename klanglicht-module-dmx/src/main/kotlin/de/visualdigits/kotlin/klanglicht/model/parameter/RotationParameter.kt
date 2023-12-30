package de.visualdigits.kotlin.klanglicht.model.parameter

import de.visualdigits.kotlin.klanglicht.model.fixture.Fixture
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class RotationParameter(

    /** The fixture for this rotation (needed to calculate raw values). */
    val fixture: Fixture,

    val panDegrees: Double,

    val tiltDegrees: Double
) : Parameter<RotationParameter> {

    private val DEG2RAD: Double = (Math.PI / 180.0)

    private val RAD2DEG: Double = (180.0 / Math.PI)

    override fun parameterMap(): Map<String, Int> =
        fixture.panoParameterSet(panDegrees).parameterMap + fixture.tiltParameterSet(tiltDegrees).parameterMap

    override fun fade(other: Any, factor: Double): RotationParameter {
        return if (other is RotationParameter) {

            // use great circle to determine intermediate steps
            //  adapted from page http://compastic.blogspot.co.uk/2011/07/how-to-draw-great-circle-on-map-in.html
            //  which was adapted from page http://maps.forum.nu/gm_flight_path.html

            val lat1: Double = tiltDegrees * DEG2RAD
            val lon1: Double = panDegrees * DEG2RAD
            val lat2: Double = other.tiltDegrees * DEG2RAD
            val lon2: Double = other.panDegrees * DEG2RAD

            val d = 2 * asin(sqrt(sin((lat1 - lat2) / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin((lon1 - lon2) / 2).pow(2.0)))

            val A = sin((1 - factor) * d) / sin(d)
            val B = sin(factor * d) / sin(d)
            val x = A * cos(lat1) * cos(lon1) + B * cos(lat2) * cos(lon2)
            val y = A * cos(lat1) * sin(lon1) + B * cos(lat2) * sin(lon2)
            val z = A * sin(lat1) + B * sin(lat2)

            val latN = atan2(z, sqrt(x.pow(2.0) + y.pow(2.0)))
            val lonN = atan2(y, x)

            val panDegrees: Double = lonN * RAD2DEG
            val tiltDegrees: Double = latN * RAD2DEG

            RotationParameter(fixture, panDegrees, tiltDegrees)
        } else throw IllegalArgumentException("Cannot fade different parameter type")
    }
}
