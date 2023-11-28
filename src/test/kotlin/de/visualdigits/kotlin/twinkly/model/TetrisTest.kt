package de.visualdigits.kotlin.twinkly.model

import de.visualdigits.kotlin.twinkly.games.tetris.BlockI
import de.visualdigits.kotlin.twinkly.games.tetris.BlockJ
import de.visualdigits.kotlin.twinkly.games.tetris.BlockL
import de.visualdigits.kotlin.twinkly.games.tetris.BlockO
import de.visualdigits.kotlin.twinkly.games.tetris.BlockS
import de.visualdigits.kotlin.twinkly.games.tetris.BlockT
import de.visualdigits.kotlin.twinkly.games.tetris.BlockZ
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.reflect.full.createInstance

class TetrisTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35"),
        XLedDevice("192.168.178.52")
    ))

    private val board = XledFrame(xledArray.width, xledArray.height)

    private val blocks = listOf(
        BlockI::class,
        BlockJ::class,
        BlockL::class,
        BlockO::class,
        BlockS::class,
        BlockT::class,
        BlockZ::class
    )

    private val n = blocks.size

    @Test
    fun testBlocks() {
        xledArray.mode(DeviceMode.rt)

        for (x in 0 until xledArray.width) {
            board[x][xledArray.height - 1] = RGBColor(255, 255, 255)
        }

        blockFountain()
    }

    fun blockFountain() {
        runBlocking<Unit> {
            var count = 0
            while (true) {
                val b = Random(System.currentTimeMillis()).nextInt(0, n)
                val block = blocks[b].createInstance()
                println("starting block '${block::class.simpleName}'")
                async(Dispatchers.Default) {
                    count++
                    block.start(xledArray, board)
                    count--
                }
                println("### count: $count")
                delay(2000)
            }
        }
    }
}