package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.function.Consumer
import java.util.regex.Pattern

class LMZones(
    val name: String? = null,
    val zones: MutableList<LMZone> = mutableListOf()
) : HtmlRenderable {

    fun add(lightmanagerUrl: String, markers: LMMarkers, zoneElem: Element) {
        val id: String = zoneElem.attr("id")
        if (id.startsWith("z")) {
            val headElem = zoneElem.select("div[class=bbHead]").first()!!
            val zone = LMZone(
                id = id.substring(1).toInt(),
                name = headElem.select("div[class=bbName]").first()?.text(),
                logo = headElem.select("div[class=bbLogo]").first()?.text(),
                tempChannel = headElem.select("div[class=ztemp]").first()?.attr("data-ch")?.toInt(),
                arrow = headElem.select("div[class=arrow]").first()?.text()
            )
            zoneElem
                .select("div[class=sbElement]")
                .forEach { actorElem -> addActor(lightmanagerUrl, markers, zone, actorElem) }
            if (!zone.actors.isEmpty()) {
                zones.add(zone)
            }
        }
    }

    private fun addActor(lightmanagerUrl: String, markers: LMMarkers, zone: LMZone, actorElem: Element) {
        val actor = LMActor()
        val aid: Int = actorElem.attr("id").substring(1).toInt()
        actor.id = aid
        val actorOff: String = actorElem.attr("data-aoff")
        var colorOff =
            if (StringUtils.isNotEmpty(actorOff)) actorOff.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1].trim { it <= ' ' }
            else ""
        val actorOn: String = actorElem.attr("data-aon")
        val colorOn = if (StringUtils.isNotEmpty(actorOn)) actorOn.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].trim { it <= ' ' }
        else ""
        var name = actorElem.child(0).text()
        val attributes = LMNamedAttributes(name, "color")
        if (attributes.matched()) {
            name = attributes.name
            colorOff = attributes["color"]
        }
        actor.colorOff = colorOff
        actor.colorOn = colorOn
        actor.name = name
        val dataMarker: String = actorElem.attr("data-marker")
        if (StringUtils.isNotEmpty(dataMarker)) {
            val mid = dataMarker.toInt()
            val marker = markers[mid]
            actor.addMarker(marker)
        }
        else {
            val actorMarkers = markers.getByActorId(aid)
            actorMarkers.forEach(Consumer { marker: LMMarker? -> actor.addMarker(marker) })
        }
        actor.actorOff = actorOff
        actor.actorOn = actorOn
        actor.isDimmer = !actorElem.select("div[class=myslider]").isEmpty()
        actorElem.children()
            .forEach { elem ->
                elem.children()
                    .forEach { child -> addRequest(lightmanagerUrl, actor, child) }
            }
        zone.addActor(actor)
    }

    private fun addRequest(lightmanagerUrl: String, actor: LMActor, child: Element) {
        if ("input" == child.tagName()) {
            val rq = LMDefaultRequest()
            val name: String = child.attr("value")
            rq.name = name
            val allParams = setUri(lightmanagerUrl, child, rq)
            val typ = getParams(allParams, "typ", 1)
            rq.type = if (typ.isNotEmpty()) RequestType.getByName(typ[0]) else RequestType.UNKNOWN
            val did = getParams(allParams, "did", 1)
            rq.deviceId = determineDeviceId(did)
            val lActorId = getParams(allParams, "aid", 1)
            rq.actorId = if (lActorId.isNotEmpty()) lActorId[0].toInt() else -1
            val acmd = getParams(allParams, "acmd", 1)
            rq.actorCommand = if (acmd.isNotEmpty()) acmd[0].toInt() else -1
            val seq = getParams(allParams, "seq", 1)
            rq.sequence = if (seq.isNotEmpty()) seq[0].toInt() else -1
            val lvl = getParams(allParams, "lvl", 1)
            rq.level = if (lvl.isNotEmpty()) lvl[0].toInt() else -1
            val lSmk = getParams(allParams, "smk", 2)
            rq.smk = if (lSmk.isNotEmpty()) intArrayOf(lSmk[0].toInt(), lSmk[1].toInt()) else IntArray(0)
            val lData = getParams(allParams, "dta", -1)
            rq.data = if (lData.isNotEmpty()) lData.toTypedArray() else arrayOf()
            actor.addRequest(name, rq)
        }
        else if ("a" == child.tagName()) {
            actor.addRequest("cam", LMCamRequest(child.attr("href")))
        }
    }

    private fun determineDeviceId(did: List<String>): Long {
        var deviceId = -1L
        if (!did.isEmpty()) {
            val sDeviceId = did[0]
            deviceId = try {
                sDeviceId.toLong()
            } catch (e: NumberFormatException) {
                sDeviceId.toLong(16)
            }
        }
        return deviceId
    }

    private fun setUri(lightmanagerUrl: String, child: Element, rq: LMDefaultRequest): List<String> {
        var allParams: List<String> = ArrayList()
        var request = ""
        try {
            request = URLDecoder.decode(child.attr("onclick"), StandardCharsets.UTF_8)
            request = request.substring(9, request.length - 2)
            allParams = ArrayList(Arrays.asList(*request.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
        } catch (e: UnsupportedEncodingException) {
            // ignore
        }
        val lUri = getParams(allParams, "uri", 1)
        var uri = ""
        try {
            uri = if (!lUri.isEmpty()) {
                lUri[0]
            } else {
                lightmanagerUrl + "?cmd=" + URLEncoder.encode(request, StandardCharsets.UTF_8)
            }
        } catch (e: UnsupportedEncodingException) {
            // ignore
        }
        if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
            uri = "http://$uri"
        }
        rq.uri = uri
        return allParams
    }

    private fun getParams(allParams: List<String>, name: String, numberOfParams: Int): List<String> {
        val params: List<String>
        val index = allParams.indexOf(name)
        params = if (index >= 0) {
            if (numberOfParams > 0) {
                allParams.subList(index + 1, index + 1 + numberOfParams)
            }
            else {
                allParams.subList(index + 1, allParams.size)
            }
        }
        else {
            emptyList()
        }
        return params
    }

    override fun toHtml(configHolder: ConfigHolder): String {
        val sb = StringBuilder()
        sb.append("<div class=\"title\" onclick=\"toggleFullScreen();\" alt=\"Toggle Fullscreen\" title=\"Toggle Fullscreen\">")
            .append(name)
            .append("</div>\n")
        sb.append("<div class=\"category\">\n")
        renderLabel(sb, "Z O N E S")
        zones.forEach(Consumer { zone: LMZone -> sb.append(zone.toHtml(configHolder)) })
        sb.append("</div><!-- zones -->\n\n")
        return sb.toString()
    }

    companion object {
        val P_COLOR = Pattern.compile("^([^{]*)\\{color:?([^}]*)\\}?$")
    }
}
