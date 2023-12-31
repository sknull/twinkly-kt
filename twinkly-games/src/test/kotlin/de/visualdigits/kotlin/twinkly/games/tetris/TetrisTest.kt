package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.XledArrayTest
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import kotlin.random.Random
import kotlin.reflect.full.createInstance


@Disabled("for local testing only")
class TetrisTest : XledArrayTest() {

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
    fun testKeyEvents() {
        val listener = AWTEventListener { event ->
            val evt = event as KeyEvent
            if (evt.id == KeyEvent.KEY_PRESSED) {
                println("key pressed: ${evt.keyCode}")
            }
        }
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.KEY_EVENT_MASK)

        while (true) {
            Thread.sleep(10)
        }
    }


    @Test
    fun testBlock() {
        xledArray.setMode(DeviceMode.rt)

        val board = XledFrame(xledArray.width, xledArray.height)

        val block = BlockJ()

        block.draw(board, 1, 1)

        board.play(xledArray)
    }

    @Test
    fun testBlocks() {
        xledArray.setMode(DeviceMode.rt)

        val board = XledFrame(xledArray.width, xledArray.height)

        for (x in 0 until xledArray.width) {
            board[x, xledArray.height - 1] = RGBColor(255, 255, 255)
        }

        var y = 0
        blocks.forEach { b ->
            val block = b.createInstance()
            block.draw(board, 0, y)
            y += block.height + 1
        }

        y = 0
        blocks
            .map { b ->
                b.createInstance().rotateLeft()
            }.forEach { block ->
                block.draw(board, 5, y)
                y += block.height + 1
            }

        y = 0
        blocks
            .map { b ->
                b.createInstance().rotateRight()
            }.forEach { block ->
                block.draw(board, 10, y)
                y += block.height + 1
            }

        y = 0
        blocks
            .map { b ->
                b.createInstance().rotate180()
            }.forEach { block ->
                block.draw(board, 15, y)
                y += block.height + 1
            }

        board.play(xledArray)
    }

    fun blockFountain() {
        runBlocking {
            while (true) {
                val b = Random(System.currentTimeMillis()).nextInt(0, n)
                val block = blocks[b].createInstance()
                println("starting block '${block::class.simpleName}'")
                async(Dispatchers.Default) {
//                    block.start(xledArray, board)
                }
                delay(2000)
            }
        }
    }
}
