package de.visualdigits.kotlin.klanglicht.rest.yamaha.feign

import de.visualdigits.kotlin.klanglicht.model.yamaha.Menu

class YamahaReceiverClient(
    val yamahaReceiverUrl: String
) {

    var client: YamahaReceiverFeignClient? = null
    var unitDescription: de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription? = null

    init {
        client = YamahaReceiverFeignClient.Companion.client(yamahaReceiverUrl)
        unitDescription = client?.unitDescription
    }

    fun controlVolume(volume: Int) {
        val menu = unitDescription?.getMenu<Menu>("Main Zone/Volume/Level")
        val command = menu?.createCommand(volume.toString())
        client?.control(command)
    }

    fun controlSurroundProgram(program: String?) {
        val menu = unitDescription?.getMenu<Menu>("Main Zone/Setup/Surround/Program")
        val command = menu?.createCommand(program)
        client?.control(command)
    }
}
