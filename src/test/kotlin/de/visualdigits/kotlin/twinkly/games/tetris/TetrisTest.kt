package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
        runBlocking {
            while (true) {
                val b = Random(System.currentTimeMillis()).nextInt(0, n)
                val block = blocks[b].createInstance()
                println("starting block '${block::class.simpleName}'")
                async(Dispatchers.Default) {
                    block.start(xledArray, board)
                }
                delay(2000)
            }
        }
    }
}
