package com.cjamcu.psiphon

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import ca.psiphon.PsiphonTunnel
import java.util.concurrent.atomic.AtomicInteger


class PsiphonService : Service(), PsiphonTunnel.HostService {

    private val binder = LocalBinder()
    private var tunnel: PsiphonTunnel? = null

    var httpProxyPort: AtomicInteger? = null
    var isConnected: Boolean = false
    var status: PsiphonConnectionState = PsiphonConnectionState.disconnected
     var configText = ""

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        // Return this instance of PsiphonService so clients can call public methods
        val service = this@PsiphonService
    }

    override fun onCreate() {
        super.onCreate()

        logMessage("Servicio creado")
        httpProxyPort = AtomicInteger(0)
        tunnel = PsiphonTunnel.newPsiphonTunnel(this)




    }

    override fun onStartCommand(intencion: Intent, flags: Int, idArranque: Int): Int {
        val extras: Bundle = intencion.extras!!

        configText = extras["configText"] as String

        logMessage("Servicio reiniciado")

        try {
            this.tunnel!!.startTunneling("")
        } catch (e: PsiphonTunnel.Exception) {
            logMessage("failed to start Psiphon")
        }


        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isConnected = false
        status = PsiphonConnectionState.disconnected
        logMessage("Se cerro la conexion")
    }




    override fun getAppName(): String {
        return "Apretaste"
    }

    override fun getContext(): Context {
        return this
    }

    override fun getVpnService(): Any? {
        return null
    }

    override fun newVpnServiceBuilder(): Any? {
        return null
    }

    override fun getPsiphonConfig(): String {
        return  configText
    }

    private fun logMessage(status: String) {
        Log.e("PsiphonService", status)
    }

    override fun onDiagnosticMessage(message: String) {
        logMessage(message)
    }

    override fun onAvailableEgressRegions(regions: List<String>) {
        for (region in regions) {
            logMessage("Available egress region: $region")
        }
    }

    override fun onSocksProxyPortInUse(port: Int) {
        logMessage("Local SOCKS proxy port in use: " + Integer.toString(port))
    }

    override fun onHttpProxyPortInUse(port: Int) {
        logMessage("local HTTP proxy port in use: " + Integer.toString(port))
    }

    override fun onListeningSocksProxyPort(port: Int) {
        logMessage("local SOCKS proxy listening on port: " + Integer.toString(port))
    }

    override fun onListeningHttpProxyPort(port: Int) {
        logMessage("local HTTP proxy listening on port: " + Integer.toString(port))
        httpProxyPort!!.set(port)

    }

    override fun onUpstreamProxyError(message: String) {
        logMessage("Upstream proxy error: $message")
    }

    override fun onConnecting() {
        logMessage("Connecting...")
        status = PsiphonConnectionState.connecting
    }

    override fun onConnected() {
        logMessage("Connected")
        isConnected = true
        status = PsiphonConnectionState.connected

    }

    override fun onHomepage(url: String) {
        logMessage("home page: $url")
    }

    override fun onClientUpgradeDownloaded(filename: String) {
        logMessage("clientRequest upgrade downloaded")
    }

    override fun onClientIsLatestVersion() {

    }

    override fun onSplitTunnelRegion(region: String) {
        logMessage("split tunnel region: $region")
    }

    override fun onUntunneledAddress(address: String) {
        logMessage("untunneled address: $address")
    }

    override fun onBytesTransferred(sent: Long, received: Long) {
        logMessage("bytes sent: " + java.lang.Long.toString(sent))
        logMessage("bytes received: " + java.lang.Long.toString(received))
    }

    override fun onStartedWaitingForNetworkConnectivity() {
        logMessage("waiting for network connectivity...")
        isConnected = false
        status = PsiphonConnectionState.waitingForNetwork
    }

    override fun onActiveAuthorizationIDs(authorizations: List<String>) {}

    override fun onExiting() {
        isConnected = false
        status = PsiphonConnectionState.disconnected
    }

    override fun onClientRegion(region: String) {
        logMessage("clientRequest region: $region")
    }


}

enum class PsiphonConnectionState {
    disconnected,
    connecting,
    connected,
    waitingForNetwork
}