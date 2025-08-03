package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodAmbient(
    override val index: Int,
    override val label: String
): MoodsEffect {

    Shuffle(-1, "Shuffle"),
    Sparkles(0, "Sparkles"),
    Tranquil(1, "Tranquil"),
    Plasma(3, "Plasma");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodAmbient.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodAmbient.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Ambient.index
}