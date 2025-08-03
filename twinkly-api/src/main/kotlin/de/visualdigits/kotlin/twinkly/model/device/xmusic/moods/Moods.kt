package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
@OptIn(ExperimentalStdlibApi::class)
enum class Moods(
    val index: Int,
    val label: String,
    val icon: String,
    val color: String,
    val effects: Map<Int, MoodsEffect>
) {

    Dance(0, "Dance", "🕺", "#7E00B9", MoodDance.entries.associate { e -> Pair(e.index, e) }),
    Classic(1, "Classic", "🎻", "#1E3BE1", MoodClassic.entries.associate { e -> Pair(e.index, e) }),
    Ambient(2, "Ambient", "🌺", "#2291F1", MoodAmbient.entries.associate { e -> Pair(e.index, e) }),
    Fluo(3, "Fluo", "🔋", "#5BC0A3", MoodFluo.entries.associate { e -> Pair(e.index, e) }),
    Chillout(4, "Chillout", "🍸", "#F5C54D", MoodChillout.entries.associate { e -> Pair(e.index, e) }),
    Pop(5, "Pop", "🎸", "#CC3145", MoodPop.entries.associate { e -> Pair(e.index, e) });
    
    companion object {
        fun fromIndex(index: Int) = Moods.entries.find { e -> index == e.index }

        fun fromLabel(label: String) = Moods.entries.find { e -> label == e.label }
    }

    fun effectFromIndex(effectIndex: Int) = effects[effectIndex]
}