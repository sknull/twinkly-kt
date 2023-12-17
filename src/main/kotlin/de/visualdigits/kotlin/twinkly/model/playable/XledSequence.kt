package de.visualdigits.kotlin.twinkly.model.playable

import com.madgag.gif.fmsware.GifDecoder
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType
import de.visualdigits.kotlin.twinkly.model.scene.Scene
import de.visualdigits.kotlin.twinkly.model.scene.SceneType
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class XledSequence(
    var frameDelay: Long = 100,
    private val frames: MutableList<Playable> = mutableListOf()
) : Playable, MutableList<Playable> by frames {

    private val log = LoggerFactory.getLogger(XledSequence::class.java)

    override var running: Boolean = false

    constructor(
        directory: File,
        maxFrames: Int = Int.MAX_VALUE,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        frameDelay: Long = 1000
    ) : this(frameDelay = frameDelay) {
        if (!directory.isDirectory) throw IllegalArgumentException("Given file is not a directory")
        if (!readSceneDirectory(directory, initialColor)) {
            directory
                .listFiles { file -> file.isDirectory }
                ?.sortedBy { it.name.lowercase() }
                ?.take(maxFrames)
                ?.forEach { sceneDirectory ->
                    readSceneDirectory(sceneDirectory, initialColor)
                }
        }
    }

    constructor(
        fontName: String,
        fontSize: Int,
        targetWidth: Int,
        targetHeight: Int,
        frameDelay: Long = 100,
        vararg texts: Triple<String, Color<*>, Color<*>>
    ) : this(frameDelay = frameDelay) {
        val banner = XledFrame(
            fontName = fontName,
            fontSize = fontSize,
            texts = texts
        )

        addScrollingBanner(banner, targetWidth, targetHeight)
    }

    constructor(
        fontName: String,
        targetWidth: Int,
        targetHeight: Int,
        frameDelay: Long = 100,
        vararg texts: Triple<String, Color<*>, Color<*>>
    ) : this(frameDelay = frameDelay) {
        val banner = XledFrame(
            fontName = fontName,
            texts = texts
        )

        addScrollingBanner(banner, targetWidth, targetHeight)
    }

    private fun readSceneDirectory(sceneDirectory: File, initialColor: Color<*>): Boolean {
        val sceneFile = File(sceneDirectory, "scene.json")
        val exists = sceneFile.exists()
        if (exists) {
            val scene = Scene.unmarshall(sceneFile)
            val images = sceneDirectory.listFiles { file -> file.isFile && file.name.lowercase().endsWith(".png") }
            if (images != null) {
                when (scene.type) {
                    SceneType.frame -> {
                        add(XledFrame(images.first(), initialColor))
                    }

                    SceneType.sequence -> {
                        val subSequence = XledSequence()
                        scene.frameDelay?.let { subSequence.frameDelay = it }
                        images.forEach { image -> subSequence.add(XledFrame(image, initialColor)) }
                        add(subSequence)
                    }
                }
            }
        }
        return exists
    }

    override fun toString(): String {
        val frames = frames
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

        xled.setMode(DeviceMode.rt)

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

    fun addScrollingBanner(
        banner: XledFrame,
        targetWidth: Int,
        targetHeight: Int
    ): XledSequence {
        // move in
        for (x in 0 until targetWidth - 1) {
            val frame = banner.subFrame(0, 0, x, min(banner.height, targetHeight))
            val canvas = XledFrame(targetWidth, targetHeight, RGBColor(0, 0, 0))
            canvas.replaceSubFrame(frame, targetWidth - 1 - x, 0)
            add(canvas)
        }

        // scroll text
        for (x in 0 until banner.width - targetWidth) {
            val frame = banner.subFrame(x, 0, targetWidth, min(banner.height, targetHeight))
            val canvas = XledFrame(targetWidth, targetHeight, RGBColor(0, 0, 0))
            canvas.replaceSubFrame(frame, 0, 0)
            add(canvas)
        }

        // move out
        for (x in targetWidth - 1 downTo 0) {
            val frame = banner.subFrame(banner.width - x, 0, x, min(banner.height, targetHeight))
            val canvas = XledFrame(targetWidth, targetHeight, RGBColor(0, 0, 0))
            canvas.replaceSubFrame(frame, 0, 0)
            add(canvas)
        }

        return this
    }

    fun addAnimatedGif(
        ins: InputStream,
        maxFrames: Int = Int.MAX_VALUE,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledSequence {
        val gifDecoder = GifDecoder()
        gifDecoder.read(ins)
        for (f in 0 until min(gifDecoder.frameCount, maxFrames)) {
            add(XledFrame(gifDecoder.getFrame(f), initialColor))
        }

        return this
    }

    private fun showTransition(
        xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed,
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
        xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed,
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
}
