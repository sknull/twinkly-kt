package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodAmbient(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "Ambient"),
    Sparkles(0, "Sparkles", "Ambient"),
    Tranquil(1, "Tranquil", "Ambient"),
    Plasma(3, "Plasma", "Ambient");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodAmbient.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodAmbient.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Ambient.index
}