package de.visualdigits.kotlin

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openqa.selenium.chrome.ChromeDriver

@Disabled("only for local testing")
class LiveScoreTest {

    @Test
    fun liveScore() {
        val result = getLiveScore()
        println(result)
    }

    private fun getLiveScore(): List<Pair<String, String>> {
        val url = "https://de.uefa.com/euro2024/fixtures-results/"
        val driver = ChromeDriver()
        driver.get(url)
        Thread.sleep(1000)
        val html = driver.pageSource
        driver.quit()
//        val html = File(ClassLoader.getSystemResource("html/scrape-pending.html").toURI()).readText()
        return "Live-Ergebnisse - (.*?)\"".toRegex()
            .find(html)
            ?.let { a ->
                a.groups[1]?.value
                    ?.split(" - ")
                    ?.let { b ->
                        "${b[0]} (\\d+)-(\\d+) ${b[1]}".toRegex()
                            .find(html)
                            ?.let { c ->
                                listOf(
                                    Pair(b[0], c.groups[1]?.value ?: ""),
                                    Pair(b[1], c.groups[2]?.value ?: "")
                                )
                            }?:listOf(Pair(b[0], "0"),Pair(b[1], "0"))
                    }
            } ?: "Nächstes Spiel - (.*?)\"".toRegex().find(html)?.let { d ->
            d.groups[1]?.value?.split(" - ")?.let { e -> listOf(Pair(e[0], "?"), Pair(e[1], "?")) }
        } ?: listOf()
    }
}
