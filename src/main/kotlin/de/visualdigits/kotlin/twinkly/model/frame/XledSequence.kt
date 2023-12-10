package de.visualdigits.kotlin.twinkly.model.frame

import com.madgag.gif.fmsware.GifDecoder
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.frame.transition.TransitionType
import de.visualdigits.kotlin.twinkly.model.scene.Scene
import de.visualdigits.kotlin.twinkly.model.scene.SceneType
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class XledSequence(
    var frameDelay: Long = 100,
    private val frames: MutableList<Playable> = mutableListOf()
) : Playable, MutableList<Playable> by frames {

    private val log = LoggerFactory.getLogger(XledSequence::class.java)

    private var running: Boolean = false

    override fun toString(): String {
        val frames =  frames
            .map { it.toString().split("\n") }
        val sb = StringBuilder()
        for (y in 0 until frames[0].size) {
            sb.append(frames.map { it[y] }.joinToString(" ") + "\n")
        }
        return sb.toString()
    }

    override fun play(
        xled: XLed,
        loop: Int,
        random: Boolean,
        transitionType: TransitionType?,
        transitionDirection: TransitionDirection?,
        transitionBlendMode: BlendMode?,
        transitionDuration: Long,
        verbose: Boolean
    ) {
        val repetitions = max(1, frameDelay / 5000)
        var frameLoopCount = loop
        var lastPlayable: Playable? = null

        running = true
        while (running && (frameLoopCount == -1 || frameLoopCount > 0)) {
            for (j in 0 until frames.size) {
                val playable = nextPlayable(j, lastPlayable, random)
                if (verbose) log.info("\n$playable")

                if (lastPlayable != null && transitionType != TransitionType.STRAIGHT) {
                    showTransition(
                        xled = xled,
                        source = lastPlayable,
                        target = playable,
                        transitionType = transitionType,
                        transitionDirection = transitionDirection,
                        transitionBlendMode = transitionBlendMode,
                        transitionDuration = transitionDuration
                    )
                }

                showPlayable(xled, playable, repetitions)

                lastPlayable = playable
            }

            if (frameLoopCount != -1) frameLoopCount--
        }
    }

    private fun showTransition(
        xled: XLed,
        source: Playable,
        target: Playable,
        transitionType: TransitionType?,
        transitionDirection: TransitionDirection?,
        transitionBlendMode: BlendMode?,
        transitionDuration: Long
    ) {
        val transType = transitionType ?: TransitionType.random()
        val transitionSequence = transType.transitionSequence(
            source = source,
            target = target,
            transitionDirection = transitionDirection ?: transType.supportedTransitionDirections().random(),
            blendMode = transitionBlendMode ?: BlendMode.random(),
            duration = transitionDuration
        )
        xled.showRealTimeSequence(transitionSequence, loop = 1)
    }

    private fun showPlayable(
        xled: XLed,
        playable: Playable,
        repetitions: Long
    ) {
        when (playable) {
            is XledFrame -> {
                for (i in 0 until repetitions) {
                    xled.showRealTimeFrame(playable)
                    if (!running) break
                    Thread.sleep(min(5000, frameDelay))
                }
            }
            is XledSequence -> {
                xled.showRealTimeSequence(
                    frameSequence = playable,
                    loop = (frameDelay / playable.frameDelay / playable.size).toInt()
                )
            }
        }
    }

    private fun nextPlayable(
        j: Int,
        lastPlayable: Playable?,
        random: Boolean
    ): Playable {
        val playable = if (random) {
            random(lastPlayable)
        }
        else {
            frames[j]
        }
        return playable
    }

    private fun random(lastPlayable: Playable?): Playable {
        var nextPlayable = frames.random()
        while (nextPlayable == lastPlayable) {
            nextPlayable = frames.random()
        }
        return nextPlayable
    }

    override fun stop() {
        running = false
    }

    fun append(other: XledSequence): XledSequence {
        other.frames.forEach { this.frames.add(it) }
        return this
    }

    override fun frames(): List<Playable> = frames

    override fun toByteArray(bytesPerLed: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        forEach { baos.write(it.toByteArray(bytesPerLed)) }
        return baos.toByteArray()
    }

    companion object {
        fun fromDirectory(
            directory: File,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0),
            frameDelay: Long = 1000
        ): XledSequence {
            if (!directory.isDirectory) throw IllegalArgumentException("Given file is not a directory")
            val sequence = XledSequence(frameDelay = frameDelay)
            directory
                .listFiles { file -> file.isDirectory }
                ?.sortedBy { it.name.lowercase() }
                ?.take(maxFrames)
                ?.forEach { sceneDirectory ->
                    val sceneFile = File(sceneDirectory, "scene.json")
                    if (sceneFile.exists()) {
                        val scene = Scene.unmarshall(sceneFile)
                        val images = sceneDirectory.listFiles { file -> file.isFile && file.name.lowercase().endsWith(".png") }
                        if (images != null) {
                            when (scene.type) {
                                SceneType.frame -> {
                                    sequence.add(XledFrame.fromImage(images.first(), initialColor))
                                }
                                SceneType.sequence -> {
                                    val subSequence = XledSequence()
                                    scene.frameDelay?.let { subSequence.frameDelay = it }
                                    images.forEach { image -> subSequence.add(XledFrame.fromImage(image, initialColor)) }
                                    sequence.add(subSequence)
                                }
                            }
                        }
                    }
                }
            return sequence
        }

        fun fromAnimatedGif(
            file: File,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            return fromAnimatedGif(FileInputStream(file), maxFrames, initialColor)
        }

        fun fromAnimatedGif(
            ins: InputStream,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            val sequence = XledSequence()
            val gifDecoder = GifDecoder()
            gifDecoder.read(ins)
            for (f in 0 until min(gifDecoder.frameCount, maxFrames)) {
                sequence.add(
                    XledFrame.fromImage(gifDecoder.getFrame(f), initialColor)
                )
            }
            return sequence
        }

        fun fromText(
            text: String,
            fontName: String,
            fontSize: Int,
            backgroundColor: Color<*>,
            textColor: Color<*>,
            targetWidth: Int,
            targetHeight: Int
        ): XledSequence {
            val banner = XledFrame.fromText(
                text = text,
                fontName = fontName,
                fontSize = fontSize,
                backgroundColor = backgroundColor,
                textColor = textColor
            )

            return scrollingBanner(banner, targetWidth, targetHeight)
        }

        fun fromTexts(
            fontName: String,
            fontSize: Int,
            targetWidth: Int,
            targetHeight: Int,
            vararg texts: Triple<String, Color<*>, Color<*>>
        ): XledSequence {
            val banner = XledFrame.fromTexts(
                fontName = fontName,
                fontSize = fontSize,
                texts = texts
            )

            return scrollingBanner(banner, targetWidth, targetHeight)
        }

        fun scrollingBanner(
            banner: XledFrame,
            targetWidth: Int,
            targetHeight: Int
        ): XledSequence {
            val sequence = XledSequence(frameDelay = 100)

            // move in
            for (x in 0 until targetWidth - 1) {
                val frame = banner.subFrame(0, 0, x, min(banner.height, targetHeight))
                val canvas = XledFrame(targetWidth, targetHeight, RGBColor(0, 0, 0))
                canvas.replaceSubFrame(frame, targetWidth - 1 - x, 0)
                sequence.add(canvas)
            }

            // scroll text
            for (x in 0 until banner.width - targetWidth) {
                val frame = banner.subFrame(x, 0, targetWidth, min(banner.height, targetHeight))
                val canvas = XledFrame(targetWidth, targetHeight, RGBColor(0, 0, 0))
                canvas.replaceSubFrame(frame, 0, 0)
                sequence.add(canvas)
            }

            // move out
            for (x in targetWidth - 1 downTo 0) {
                val frame = banner.subFrame(banner.width - x, 0, x, min(banner.height, targetHeight))
                val canvas = XledFrame(targetWidth, targetHeight, RGBColor(0, 0, 0))
                canvas.replaceSubFrame(frame, 0, 0)
                sequence.add(canvas)
            }

            return sequence
        }
    }
}
