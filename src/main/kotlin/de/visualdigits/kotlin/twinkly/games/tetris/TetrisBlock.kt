package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import kotlinx.coroutines.delay
import kotlin.random.Random

open class TetrisBlock(
    width: Int,
    height: Int,
    initialColor: Color<*>,
    val pixelsToCheck: List<Pair<Int, Int>>
) : XledFrame(width, height, initialColor) {

    private var xled: XLed = XLedDevice("")
    private var board: XledFrame = XledFrame(0, 0)
    private var posX: Int = 0
    private var posY: Int = 0
    private var oldFrame: XledFrame = XledFrame(0, 0)

    suspend fun start(xled: XLed, board: XledFrame) {
        this.xled = xled
        this.board = board
        posX = Random(System.currentTimeMillis()).nextInt(0, board.width - width)
        oldFrame = board.subFrame(posX, posY, width, height)
        if (oldFrame.frame.any { row -> row.any { color -> !color.isBlack() } }) {
            println("#### all blocked - not starting block '${this::class.simpleName}'")
            return
        }
        board.replaceSubFrame(this, posX, posY)

        xled.showRealTimeFrame(board)

        running = true
        while (running) {
            val collision = pixelsToCheck.any { p -> !this.board[posX + p.first][posY + p.second].isBlack() }
            if (collision) {
                println("collision - stopping block '${this::class.simpleName}'")
                running = false
                break
            }
            delay(300)
            println("moving block '${this::class.simpleName}'")
            this.board.replaceSubFrame(oldFrame, posX, posY, BlendMode.REPLACE)
            posY++
            oldFrame = this.board.subFrame(posX, posY, width, height)
            this.board.replaceSubFrame(this, posX, posY)
            this.xled.showRealTimeFrame(this.board)
        }
    }

}
