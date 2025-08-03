package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodPop(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "Pop"),
    BPMHue(0, "BPM Hue", "Pop"),
    VuFreq(1, "Vu Freq", "Pop"),
    Bounce(2, "Bounce", "Pop"),
    AngelFade(3, "Angel Fade", "Pop"),
    Clockwork(5, "Clockwork", "Pop"),
    Signal(6, "Signal", "Pop"),
    Oscillator(7, "Oscillator", "Pop");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodPop.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodPop.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Pop.index
}