package org.team2471.frc.lib.comm

import edu.wpi.first.wpilibj.Timer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer

/**
 * A UDP server that sends the current time to any client who can provide the meaning of life.
 */
object ClockServer {
    private var initialized = false

    fun init(port: Int, timer: () -> Double = { Timer.getFPGATimestamp() }) {
        if(!initialized) initialized = true
        else throw IllegalStateException("A clock server has already been initialized!")

        Thread({
            val socket = DatagramSocket(port)

            val recvPacket = DatagramPacket(kotlin.ByteArray(1), 1)

            val buffer = ByteArray(8)
            val sendPacket = DatagramPacket(buffer, 8)

            while(!Thread.currentThread().isInterrupted) {
                socket.receive(recvPacket)
                if(recvPacket.data[0] == 42.toByte()) {
                    ByteBuffer.wrap(buffer).putDouble(timer())
                    sendPacket.address = recvPacket.address
                    sendPacket.port = recvPacket.port
                    socket.send(sendPacket)
                }
            }
            socket.close()
        }, "ClockServer").start()
    }
}
