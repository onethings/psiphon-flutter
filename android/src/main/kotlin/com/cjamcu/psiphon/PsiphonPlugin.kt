package com.cjamcu.psiphon

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.NonNull;
import com.google.gson.Gson

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*

/** PsiphonPlugin */
class PsiphonPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel


    lateinit var applicationContext: Context
    var psiphonService: PsiphonService? = null
    var psiphonBound = false
    private var psiphonAttempts: Int = 0
    private var maxPsiphonAttempts: Int = 30
    private lateinit var activity: Activity
    private lateinit var assets: FlutterPlugin.FlutterAssets


    private val psiphonConnectionFactory = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            psiphonBound = false

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PsiphonService.LocalBinder
            psiphonService = binder.service
            psiphonBound = true

        }

        override fun onBindingDied(name: ComponentName?) {
            psiphonBound = false
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "psiphon")
        channel.setMethodCallHandler(this);


        assets = flutterPluginBinding.flutterAssets;

        applicationContext = flutterPluginBinding.applicationContext;
    }

    override fun onDetachedFromActivity() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity

        bindPsiphon()
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }


    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "psiphon")
            channel.setMethodCallHandler(PsiphonPlugin())

        }
    }



    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        when (call.method) {
            "connect" -> connectToPsiphon(call,result)
            "stop" -> stop(result)
            "connectionState" -> getConnectionState(result)
            else -> result.notImplemented()
        }
    }

    private fun getConnectionState(result: MethodChannel.Result) {
        result.success(psiphonService?.status?.ordinal)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun connectToPsiphon(call: MethodCall, result: Result) {

        if (psiphonIsConnected()) {
            result.success(getJsonConnect(true, psiphonService!!.httpProxyPort!!.toInt()))
        } else {

            val psiphonIntent = Intent(this.applicationContext, PsiphonService::class.java)
            val config = call.argument<String>("configText")
            psiphonIntent.putExtra("configText", config)
            activity.startService(psiphonIntent)


            val timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    when {
                        psiphonIsConnected() -> activity.runOnUiThread {
                            timer.cancel()
                            timer.purge()

                            result.success(getJsonConnect(true, psiphonService!!.httpProxyPort!!.toInt()))

                        }
                        psiphonAttempts > maxPsiphonAttempts -> activity.runOnUiThread {
                            timer.cancel()
                            timer.purge()
                            psiphonAttempts = 0
                            when (maxPsiphonAttempts) {
                                30 -> maxPsiphonAttempts = 45
                                45 -> maxPsiphonAttempts = 60
                            }
                            result.success(getJsonConnect(false, 0))
                            unbindPsiphon()


                        }
                        else -> psiphonAttempts++
                    }
                }
            }, 0, 1000)
        }


    }

    private fun psiphonIsConnected(): Boolean {
        return psiphonBound && psiphonService?.status == PsiphonConnectionState.connected;
    }

    private fun getJsonConnect(connected: Boolean, port: Int): String? {
        val connectStatus = ConnectStatus(connected, port)
        return Gson().toJson(connectStatus);
    }


    fun bindPsiphon() {

        val intent = Intent(this.applicationContext, PsiphonService::class.java)
        var bind = activity.bindService(intent, psiphonConnectionFactory, Context.BIND_AUTO_CREATE)
    }

    fun unbindPsiphon() {
        if (psiphonBound && psiphonService != null) {
            try {
                activity.unbindService(psiphonConnectionFactory)
            } catch (e: IllegalArgumentException) {
                println("Psiphon service not registered")
            }
        }
    }


    private fun stop(result: Result) {
        unbindPsiphon()
        result.success(null)
    }
}




