package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.math.NumberUtils.min
import kotlin.random.Random

open class TetrisBlock(width: Int, height: Int, initialColor: Color<*>) : XledFrame(width, height, initialColor) {

    private var xled: XLed = XLedDevice("")
    private var board: XledFrame = XledFrame(0, 0)
    private var posX: Int = 0
    private var posY: Int = 0
    private var oldFrame: XledFrame = XledFrame(0, 0)
    private var running: Boolean = true

    suspend fun start(xled: XLed, board: XledFrame) {
        this.xled = xled
        this.board = board
        posX = Random(System.currentTimeMillis()).nextInt(0, board.width - width)
        oldFrame = board.subFrame(posX, posY, width, height)
        if (oldFrame.frame.any { row -> row.any { color ->!color.isBlack() } }) {
            println("#### all blocked - not starting block '${this::class.simpleName}'")
            return
        }
        board.replaceSubFrame(this, posX, posY)

        xled.showRealTimeFrame(board)

        move()
    }

    fun stop() {
        running = false
    }

    suspend fun move() {
        while (running) {
            val lineAhead = board.subFrame(posX, posY + height, width, 1)
            if (lineAhead.frame.any { row -> row.any { color ->!color.isBlack() } }) {
                println("collision - stopping block '${this::class.simpleName}'")
                running = false
                break
            }
            delay(300)
            println("moving block '${this::class.simpleName}'")
            board.replaceSubFrame(oldFrame, posX, posY, BlendMode.REPLACE)
            posY++
            oldFrame = board.subFrame(posX, posY, width, height)
            board.replaceSubFrame(this, posX, posY)
            xled.showRealTimeFrame(board)
        }
    }
}
