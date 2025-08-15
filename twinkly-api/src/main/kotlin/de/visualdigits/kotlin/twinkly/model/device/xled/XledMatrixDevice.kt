package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.playable.XledFrame

class XledMatrixDevice private constructor(
    ipAddress: String = "",
    name: String = "",
    width: Int = 0,
    height: Int = 0
): XLedDevice(
    ipAddress = ipAddress,
    name = name,
    width = width,
    height = height,
    transformation = { frame ->
    val transformed = XledFrame(frame.width, frame.height)

    for (y in 0 until frame.height) {
        val transformRow = transformationMatrix[y]
        for (x in 0 until frame.width) {
            val transformValue = transformRow[x]
            transformed[transformValue.first, transformValue.second] = frame[x, y]
        }
    }

    transformed
}) {

    companion object {

        private val cache = mutableMapOf<String, XledMatrixDevice>()

        fun instance(
            ipAddress: String,
            name: String,
            width: Int,
            height: Int
        ): XledMatrixDevice {
            return cache.computeIfAbsent(ipAddress) {
                XledMatrixDevice(
                    ipAddress,
                    name,
                    width,
                    height
                )
            }
        }

        private val transformationMatrix = arrayOf(
            arrayOf(Pair( 4,  0), Pair( 3, 49), Pair( 2,  0), Pair( 1, 49), Pair( 0,  0), Pair( 5,  0), Pair( 6, 49), Pair( 7,  0), Pair( 8, 49), Pair( 9,  0)),
            arrayOf(Pair( 4,  1), Pair( 3, 48), Pair( 2,  1), Pair( 1, 48), Pair( 0,  1), Pair( 5,  1), Pair( 6, 48), Pair( 7,  1), Pair( 8, 48), Pair( 9,  1)),
            arrayOf(Pair( 4,  2), Pair( 3, 47), Pair( 2,  2), Pair( 1, 47), Pair( 0,  2), Pair( 5,  2), Pair( 6, 47), Pair( 7,  2), Pair( 8, 47), Pair( 9,  2)),
            arrayOf(Pair( 4,  3), Pair( 3, 46), Pair( 2,  3), Pair( 1, 46), Pair( 0,  3), Pair( 5,  3), Pair( 6, 46), Pair( 7,  3), Pair( 8, 46), Pair( 9,  3)),
            arrayOf(Pair( 4,  4), Pair( 3, 45), Pair( 2,  4), Pair( 1, 45), Pair( 0,  4), Pair( 5,  4), Pair( 6, 45), Pair( 7,  4), Pair( 8, 45), Pair( 9,  4)),
            arrayOf(Pair( 4,  5), Pair( 3, 44), Pair( 2,  5), Pair( 1, 44), Pair( 0,  5), Pair( 5,  5), Pair( 6, 44), Pair( 7,  5), Pair( 8, 44), Pair( 9,  5)),
            arrayOf(Pair( 4,  6), Pair( 3, 43), Pair( 2,  6), Pair( 1, 43), Pair( 0,  6), Pair( 5,  6), Pair( 6, 43), Pair( 7,  6), Pair( 8, 43), Pair( 9,  6)),
            arrayOf(Pair( 4,  7), Pair( 3, 42), Pair( 2,  7), Pair( 1, 42), Pair( 0,  7), Pair( 5,  7), Pair( 6, 42), Pair( 7,  7), Pair( 8, 42), Pair( 9,  7)),
            arrayOf(Pair( 4,  8), Pair( 3, 41), Pair( 2,  8), Pair( 1, 41), Pair( 0,  8), Pair( 5,  8), Pair( 6, 41), Pair( 7,  8), Pair( 8, 41), Pair( 9,  8)),
            arrayOf(Pair( 4,  9), Pair( 3, 40), Pair( 2,  9), Pair( 1, 40), Pair( 0,  9), Pair( 5,  9), Pair( 6, 40), Pair( 7,  9), Pair( 8, 40), Pair( 9,  9)),

            arrayOf(Pair( 4, 10), Pair( 3, 39), Pair( 2, 10), Pair( 1, 39), Pair( 0, 10), Pair( 5, 10), Pair( 6, 39), Pair( 7, 10), Pair( 8, 39), Pair( 9, 10)),
            arrayOf(Pair( 4, 11), Pair( 3, 38), Pair( 2, 11), Pair( 1, 38), Pair( 0, 11), Pair( 5, 11), Pair( 6, 38), Pair( 7, 11), Pair( 8, 38), Pair( 9, 11)),
            arrayOf(Pair( 4, 12), Pair( 3, 37), Pair( 2, 12), Pair( 1, 37), Pair( 0, 12), Pair( 5, 12), Pair( 6, 37), Pair( 7, 12), Pair( 8, 37), Pair( 9, 12)),
            arrayOf(Pair( 4, 13), Pair( 3, 36), Pair( 2, 13), Pair( 1, 36), Pair( 0, 13), Pair( 5, 13), Pair( 6, 36), Pair( 7, 13), Pair( 8, 36), Pair( 9, 13)),
            arrayOf(Pair( 4, 14), Pair( 3, 35), Pair( 2, 14), Pair( 1, 35), Pair( 0, 14), Pair( 5, 14), Pair( 6, 35), Pair( 7, 14), Pair( 8, 35), Pair( 9, 14)),
            arrayOf(Pair( 4, 15), Pair( 3, 34), Pair( 2, 15), Pair( 1, 34), Pair( 0, 15), Pair( 5, 15), Pair( 6, 34), Pair( 7, 15), Pair( 8, 34), Pair( 9, 15)),
            arrayOf(Pair( 4, 16), Pair( 3, 33), Pair( 2, 16), Pair( 1, 33), Pair( 0, 16), Pair( 5, 16), Pair( 6, 33), Pair( 7, 16), Pair( 8, 33), Pair( 9, 16)),
            arrayOf(Pair( 4, 17), Pair( 3, 32), Pair( 2, 17), Pair( 1, 32), Pair( 0, 17), Pair( 5, 17), Pair( 6, 32), Pair( 7, 17), Pair( 8, 32), Pair( 9, 17)),
            arrayOf(Pair( 4, 18), Pair( 3, 31), Pair( 2, 18), Pair( 1, 31), Pair( 0, 18), Pair( 5, 18), Pair( 6, 31), Pair( 7, 18), Pair( 8, 31), Pair( 9, 18)),
            arrayOf(Pair( 4, 19), Pair( 3, 30), Pair( 2, 19), Pair( 1, 30), Pair( 0, 19), Pair( 5, 19), Pair( 6, 30), Pair( 7, 19), Pair( 8, 30), Pair( 9, 19)),

            arrayOf(Pair( 4, 20), Pair( 3, 29), Pair( 2, 20), Pair( 1, 29), Pair( 0, 20), Pair( 5, 20), Pair( 6, 29), Pair( 7, 20), Pair( 8, 29), Pair( 9, 20)),
            arrayOf(Pair( 4, 21), Pair( 3, 28), Pair( 2, 21), Pair( 1, 28), Pair( 0, 21), Pair( 5, 21), Pair( 6, 28), Pair( 7, 21), Pair( 8, 28), Pair( 9, 21)),
            arrayOf(Pair( 4, 22), Pair( 3, 27), Pair( 2, 22), Pair( 1, 27), Pair( 0, 22), Pair( 5, 22), Pair( 6, 27), Pair( 7, 22), Pair( 8, 27), Pair( 9, 22)),
            arrayOf(Pair( 4, 23), Pair( 3, 26), Pair( 2, 23), Pair( 1, 26), Pair( 0, 23), Pair( 5, 23), Pair( 6, 26), Pair( 7, 23), Pair( 8, 26), Pair( 9, 23)),
            arrayOf(Pair( 4, 24), Pair( 3, 25), Pair( 2, 24), Pair( 1, 25), Pair( 0, 24), Pair( 5, 24), Pair( 6, 25), Pair( 7, 24), Pair( 8, 25), Pair( 9, 24)),
            arrayOf(Pair( 4, 25), Pair( 3, 24), Pair( 2, 25), Pair( 1, 24), Pair( 0, 25), Pair( 5, 25), Pair( 6, 24), Pair( 7, 25), Pair( 8, 24), Pair( 9, 25)),
            arrayOf(Pair( 4, 26), Pair( 3, 23), Pair( 2, 26), Pair( 1, 23), Pair( 0, 26), Pair( 5, 26), Pair( 6, 23), Pair( 7, 26), Pair( 8, 23), Pair( 9, 26)),
            arrayOf(Pair( 4, 27), Pair( 3, 22), Pair( 2, 27), Pair( 1, 22), Pair( 0, 27), Pair( 5, 27), Pair( 6, 22), Pair( 7, 27), Pair( 8, 22), Pair( 9, 27)),
            arrayOf(Pair( 4, 28), Pair( 3, 21), Pair( 2, 28), Pair( 1, 21), Pair( 0, 28), Pair( 5, 28), Pair( 6, 21), Pair( 7, 28), Pair( 8, 21), Pair( 9, 28)),
            arrayOf(Pair( 4, 29), Pair( 3, 20), Pair( 2, 29), Pair( 1, 20), Pair( 0, 29), Pair( 5, 29), Pair( 6, 20), Pair( 7, 29), Pair( 8, 20), Pair( 9, 29)),

            arrayOf(Pair( 4, 30), Pair( 3, 19), Pair( 2, 30), Pair( 1, 19), Pair( 0, 30), Pair( 5, 30), Pair( 6, 19), Pair( 7, 30), Pair( 8, 19), Pair( 9, 30)),
            arrayOf(Pair( 4, 31), Pair( 3, 18), Pair( 2, 31), Pair( 1, 18), Pair( 0, 31), Pair( 5, 31), Pair( 6, 18), Pair( 7, 31), Pair( 8, 18), Pair( 9, 31)),
            arrayOf(Pair( 4, 32), Pair( 3, 17), Pair( 2, 32), Pair( 1, 17), Pair( 0, 32), Pair( 5, 32), Pair( 6, 17), Pair( 7, 32), Pair( 8, 17), Pair( 9, 32)),
            arrayOf(Pair( 4, 33), Pair( 3, 16), Pair( 2, 33), Pair( 1, 16), Pair( 0, 33), Pair( 5, 33), Pair( 6, 16), Pair( 7, 33), Pair( 8, 16), Pair( 9, 33)),
            arrayOf(Pair( 4, 34), Pair( 3, 15), Pair( 2, 34), Pair( 1, 15), Pair( 0, 34), Pair( 5, 34), Pair( 6, 15), Pair( 7, 34), Pair( 8, 15), Pair( 9, 34)),
            arrayOf(Pair( 4, 35), Pair( 3, 14), Pair( 2, 35), Pair( 1, 14), Pair( 0, 35), Pair( 5, 35), Pair( 6, 14), Pair( 7, 35), Pair( 8, 14), Pair( 9, 35)),
            arrayOf(Pair( 4, 36), Pair( 3, 13), Pair( 2, 36), Pair( 1, 13), Pair( 0, 36), Pair( 5, 36), Pair( 6, 13), Pair( 7, 36), Pair( 8, 13), Pair( 9, 36)),
            arrayOf(Pair( 4, 37), Pair( 3, 12), Pair( 2, 37), Pair( 1, 12), Pair( 0, 37), Pair( 5, 37), Pair( 6, 12), Pair( 7, 37), Pair( 8, 12), Pair( 9, 37)),
            arrayOf(Pair( 4, 38), Pair( 3, 11), Pair( 2, 38), Pair( 1, 11), Pair( 0, 38), Pair( 5, 38), Pair( 6, 11), Pair( 7, 38), Pair( 8, 11), Pair( 9, 38)),
            arrayOf(Pair( 4, 39), Pair( 3, 10), Pair( 2, 39), Pair( 1, 10), Pair( 0, 39), Pair( 5, 39), Pair( 6, 10), Pair( 7, 39), Pair( 8, 10), Pair( 9, 39)),

            arrayOf(Pair( 4, 40), Pair( 3,  9), Pair( 2, 40), Pair( 1,  9), Pair( 0, 40), Pair( 5, 40), Pair( 6,  9), Pair( 7, 40), Pair( 8,  9), Pair( 9, 40)),
            arrayOf(Pair( 4, 41), Pair( 3,  8), Pair( 2, 41), Pair( 1,  8), Pair( 0, 41), Pair( 5, 41), Pair( 6,  8), Pair( 7, 41), Pair( 8,  8), Pair( 9, 41)),
            arrayOf(Pair( 4, 42), Pair( 3,  7), Pair( 2, 42), Pair( 1,  7), Pair( 0, 42), Pair( 5, 42), Pair( 6,  7), Pair( 7, 42), Pair( 8,  7), Pair( 9, 42)),
            arrayOf(Pair( 4, 43), Pair( 3,  6), Pair( 2, 43), Pair( 1,  6), Pair( 0, 43), Pair( 5, 43), Pair( 6,  6), Pair( 7, 43), Pair( 8,  6), Pair( 9, 43)),
            arrayOf(Pair( 4, 44), Pair( 3,  5), Pair( 2, 44), Pair( 1,  5), Pair( 0, 44), Pair( 5, 44), Pair( 6,  5), Pair( 7, 44), Pair( 8,  5), Pair( 9, 44)),
            arrayOf(Pair( 4, 45), Pair( 3,  4), Pair( 2, 45), Pair( 1,  4), Pair( 0, 45), Pair( 5, 45), Pair( 6,  4), Pair( 7, 45), Pair( 8,  4), Pair( 9, 45)),
            arrayOf(Pair( 4, 46), Pair( 3,  3), Pair( 2, 46), Pair( 1,  3), Pair( 0, 46), Pair( 5, 46), Pair( 6,  3), Pair( 7, 46), Pair( 8,  3), Pair( 9, 46)),
            arrayOf(Pair( 4, 47), Pair( 3,  2), Pair( 2, 47), Pair( 1,  2), Pair( 0, 47), Pair( 5, 47), Pair( 6,  2), Pair( 7, 47), Pair( 8,  2), Pair( 9, 47)),
            arrayOf(Pair( 4, 48), Pair( 3,  1), Pair( 2, 48), Pair( 1,  1), Pair( 0, 48), Pair( 5, 48), Pair( 6,  1), Pair( 7, 48), Pair( 8,  1), Pair( 9, 48)),
            arrayOf(Pair( 4, 49), Pair( 3,  0), Pair( 2, 49), Pair( 1,  0), Pair( 0, 49), Pair( 5, 49), Pair( 6,  0), Pair( 7, 49), Pair( 8,  0), Pair( 9, 49)),
        )
    }
}
