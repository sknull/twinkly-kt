package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodClassic(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "Classic"),
    CandyLine(0, "Candy Line", "Classic"),
    Swirl(1, "Swirl", "Classic"),
    Weave(2, "Weave", "Classic"),
    Fizz(3, "Fizz", "Classic"),
    VuMeter(4, "Vu Meter", "Classic"),
    Radiate(5, "Radiate", "Classic"),
    DiamondTwist(6, "Diamond Twist", "Classic"),
    PsycoSparkle(7, "Psyco Sparkle", "Classic");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodClassic.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodClassic.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Classic.index
}