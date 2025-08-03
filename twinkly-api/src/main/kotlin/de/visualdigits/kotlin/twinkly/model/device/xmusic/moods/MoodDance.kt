package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodDance(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "Dance"),
    Nova(0, "Nova", "Dance"),
    Omega(1, "Omega", "Dance"),
    DiamondWeave(2, "Diamond Weave", "Dance"),
    Oscillator(3, "Oscillator", "Dance"),
    March(5, "March", "Dance"),
    PsycoMarch(6, "Psyco March", "Dance"),
    Chevron(7, "Chevron", "Dance");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodDance.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodDance.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Dance.index
}