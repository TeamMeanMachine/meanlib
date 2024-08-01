package org.team2471.frc.lib.vision

import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.photonvision.common.dataflow.structures.Packet
import org.photonvision.targeting.PhotonPipelineResult


interface PhotonVisionCameraIO {
    class PhotonVisionCameraInputs(val name: String): LoggableInputs {
        var isConnected: Boolean = false

        var cameraResult: PhotonPipelineResult = PhotonPipelineResult()
        var aPacketSerde: PhotonPipelineResult.APacketSerde? = null



        override fun toLog(table: LogTable) {
            table.put("$name/IsConnected", isConnected)

            val packet = Packet(cameraResult.packetSize)
            aPacketSerde = PhotonPipelineResult.APacketSerde()
            aPacketSerde!!.pack(packet, cameraResult)
            table.put("$name/CameraResultData", packet.data)
        }

        override fun fromLog(table: LogTable) {
            isConnected = table.get("$name/IsConnected", isConnected)
            val cameraResultData = table["CameraResultData"]
            if (cameraResultData != null) {
                cameraResultData.raw
                cameraResult = PhotonPipelineResult.serde.unpack(Packet(cameraResultData.raw))
            }
        }
    }
    fun updateInputs(inputs: PhotonVisionCameraInputs) {}
    fun reset() {}
}