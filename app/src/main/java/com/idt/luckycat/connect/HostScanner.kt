package com.idt.luckycat.connect

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class HostScanner {
    companion object {
        private const val TAG = "HostScanner"
    }

    private val _hosts = MutableStateFlow(emptyList<InetAddress>())
    val hosts = _hosts.asStateFlow()

    // find all hosts listening on the target port
    suspend fun findHosts(context: Context, port: Int) = withContext(Dispatchers.IO) {
        // clear list first
        _hosts.value = emptyList()

        Log.d(TAG, "Scan host started")

        val address = findBroadcastAddress(context)
        val socket = DatagramSocket()
        socket.soTimeout = 5_000
        socket.broadcast = true

        // construct packet to send
        val message = "hello from Android"
        val data = message.toByteArray()
        val packetSend = DatagramPacket(
            data,
            data.size,
            address,
            port
        )

        // construct packet to receive
        val buffer = ByteArray(1024)
        val packetReceive = DatagramPacket(buffer, buffer.size)

        while (isActive) {
            try {
                socket.send(packetSend)

                while (isActive) {
                    socket.receive(packetReceive)

                    // add hosts to list
                    val hostAddress = packetReceive.address
                    if (!_hosts.value.contains(hostAddress)) {
                        _hosts.update { (it + hostAddress) }
                    }
                }

            } catch (_: IOException) {
            }

            Log.d(TAG, "found ${_hosts.value.size} hosts:")
            _hosts.value.forEach { Log.d(TAG, it.hostName) }
        }
    }

    private fun findBroadcastAddress(context: Context): InetAddress {
        val wifi = context.getSystemService(WifiManager::class.java)
        val dhcp = wifi.dhcpInfo

        // find subnet from dhcp address and mask
        val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3) quads[k] = (broadcast shr k * 8 and 0xFF).toByte()

        return InetAddress.getByAddress(quads)
    }
}
