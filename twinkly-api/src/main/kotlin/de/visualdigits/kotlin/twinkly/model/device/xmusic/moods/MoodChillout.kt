package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodChillout(
    override val index: Int,
    val label: String
): MoodsEffect {

    Shuffle(-1, "Shuffle"),
    Sparkles(0, "Sparkles"),
    Bubbles(1, "Bubbles"),
    PsycoSparkles(2, "Psyco Sparkles"),
    Nova(3, "Nova");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodChillout.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodChillout.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Chillout.index
}