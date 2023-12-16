package de.visualdigits.kotlin.twinkly.games.conway

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import java.io.File

class Conway(
    val preset: File,
    val xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed
) {

    fun run() {
        val initialFrame = XledFrame(preset)
        var matrix = Matrix(xled.width, xled.height)
        xled.mode(DeviceMode.rt)
        for (y in 0 until xled.height) {
            for (x in 0 until xled.width) {
                if (!initialFrame[x, y].isBlack()) {
                    initialFrame[x, y] = RGBColor(0, 0, 255)
                    matrix[x][y] = 1
                }
            }
        }
        xled.showRealTimeFrame(initialFrame)
        Thread.sleep(100)
        var result = ConwayResult(matrix)
        while (result.changes > 0) {
            result = nextGeneration(matrix, result)
            matrix = result.matrix
            val frame = XledFrame(xled.width, xled.height)
            val color = determineColor(result)
            for (y in 0 until xled.height) {
                for (x in 0 until xled.width) {
                    if (matrix[x][y] != 0) {
                        frame[x, y] = color
                    }
                }
            }
            xled.showRealTimeFrame(frame)
            Thread.sleep(100)
        }
    }

    /**
     * green : Normal growth
     * yellow: Cycle period 2
     * violet: Cycle period 3
     * teal  : Cycle period 4
     * white : Cycle period > 4
     * blue  : Static (no change at all, but life cells)
     * red   : Dead (no more life cells at all)
     */
    fun determineColor(result: ConwayResult): RGBColor {
        return if (result.life > 0) {
            if (result.changes > 0) {
                when (result.cycle) {
                    0 -> RGBColor(0, 255, 0)
                    2 -> RGBColor(255, 255, 0)
                    3 -> RGBColor(255, 0, 255)
                    4 -> RGBColor(0, 255, 255)
                    else -> RGBColor(255, 255, 255)
                }
            } else {
                RGBColor(0, 0, 255)
            }
        } else {
            RGBColor(255, 0, 0)
        }
    }

    fun nextGeneration(frame: Matrix, result: ConwayResult): ConwayResult {
        val nextFrame = frame.clone()
        var changes = 0
        var life = 0
        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                var v = frame[x][y]
                val n = numberOfNeighbors(frame, x, y)
                if (v == 1 && n < 2 || n > 3) {
                    v = 0
                    changes += 1
                } else if (v == 0 && n == 3) {
                    v = 1
                    changes += 1
                }
                nextFrame[x][y] = v
                life += v
            }
        }
        val g = result.generations.size
        var cycle = 0
        loop@ for (i in 2 until g) {
            if (nextFrame == result.generations[g - i]) {
                cycle = i
                break@loop
            }
        }
        result.generations.add(nextFrame)
        val nextGenerations = if (cycle > 0) {
            result.generations.subList(result.generations.size - cycle, result.generations.size)
        } else result.generations
        return ConwayResult(nextFrame, life, changes, cycle, nextGenerations)
    }

    fun numberOfNeighbors(frame: Matrix, x: Int, y: Int): Int {
        var n = 0
        for (py in y - 1 until y + 2) {
            for (px in x - 1 until x + 2) {
                if (py != y || px != x) {
                    val v = frame[clip(px, frame.width - 1)][clip(py, frame.height - 1)]
                    n += v
                }
            }
        }
        return n
    }

    fun clip(n: Int, max: Int): Int {
        return if(n > max) {
            n - max
        } else if (n < 0) {
            max + n
        } else {
            n
        }
    }

    data class ConwayResult(
        val matrix: Matrix,
        val life: Int = 0,
        val changes: Int = 1,
        val cycle: Int = 0,
        val generations: MutableList<Matrix> = mutableListOf()
    )
}
