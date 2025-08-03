package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodPop(
    override val index: Int,
    override val label: String
): MoodsEffect {

    Shuffle(-1, "Shuffle"),
    BPMHue(0, "BPM Hue"),
    VuFreq(1, "Vu Freq"),
    Bounce(2, "Bounce"),
    AngelFade(3, "Angel Fade"),
    Clockwork(5, "Clockwork"),
    Signal(6, "Signal"),
    Oscillator(7, "Oscillator");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodPop.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodPop.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Pop.index
}