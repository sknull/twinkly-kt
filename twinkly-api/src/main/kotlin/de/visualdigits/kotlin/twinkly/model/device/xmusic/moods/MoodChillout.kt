package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodChillout(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "Chillout"),
    Sparkles(0, "Sparkles", "Chillout"),
    Bubbles(1, "Bubbles", "Chillout"),
    PsycoSparkles(2, "Psyco Sparkles", "Chillout"),
    Nova(3, "Nova", "Chillout");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodChillout.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodChillout.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Chillout.index
}