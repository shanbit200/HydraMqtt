package com.example.mqttexample.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class HydraMqtt internal  constructor(){
       companion object {
           private val mInstance: HydraMqtt =   HydraMqtt()
           @Synchronized
           fun getInstance(): HydraMqtt {
               return mInstance
           }
       }
    private var TAG: String = "MqttClient"
    private var heartbeatInterval: Int? = 2000   // *** default 2 seconds heartbeat time ****
  private var heartbeatData: String? = null
    private var timer: Timer? = null
    private var context: Context? = null
    private var reconnectTimerTask: TimerTask? = null
    private var heartbeatTimerTask: TimerTask? = null
    private val RECONNECT_INTERVAL_MS = 5000
    private val sendBuffer = LinkedBlockingQueue<String>()


    private val clientCloseCallbacks = Collections
        .newSetFromMap(HashMap<IClientCloseCallback, Boolean>())

    private val clientOpenCallbacks = Collections
        .newSetFromMap(HashMap<IClientOpenCallback, Boolean>())

    private val messageCallbacks = Collections
        .newSetFromMap(HashMap<IMessageCallback, Boolean>())

    private val errorCallbacks = Collections
        .newSetFromMap(HashMap<IErrorCallback, Boolean>())

    /*
    *endpointUri is the url for the mqtt client
    * */
    private var endpointUri: String = ""

    private var mqttClient: MqttAndroidClient? =null
    private var iMqttToken: IMqttToken? = null

    private var reconnectOnFailure = true


    public fun getData(){
               if(mqttClient==null && iMqttToken==null){
                   connect(context!!,endpointUri)
               }  else{
                     iMqttToken!!.actionCallback= (object : IMqttActionListener{
                         override fun onSuccess(asyncActionToken: IMqttToken?) {
                           Log.e(TAG,"Hydra Mqtt Connection Successfull !!")

                             cancelReconnectTimer()
                             //startHeartbeatTimer()

                             for (callback in clientOpenCallbacks) {
                                 callback.onOpen()
                             }

                             this@HydraMqtt.flushSendBuffer()

                             mqttClient!!.setCallback(object : MqttCallbackExtended, IMqttActionListener {

                                 @Throws(Exception::class)    /*Connection Lost*/
                                 override fun connectionLost(cause: Throwable?) {
                                 }

                                 override fun onSuccess(asyncActionToken: IMqttToken?) {
                                     Log.e(TAG, "Success---")
                                 }

                                 override fun onFailure(
                                     asyncActionToken: IMqttToken?,
                                     exception: Throwable?
                                 ) {
                                     Log.e(TAG, "failur--e---")

                                 }

                                 override fun connectComplete(reconnect: Boolean, serverURI: String) {
                                     Log.e(TAG, "Connected to: $serverURI")
                                 }



                                 @Throws(Exception::class)
                                 override fun messageArrived(topic: String, message: MqttMessage) {
                                     for (callback in messageCallbacks) {
                                         callback.onMessage(message)
                                     }
                                 }

                                 override fun deliveryComplete(token: IMqttDeliveryToken) {
                                     Log.e(TAG, "Done")
                                 }
                             })
                             
                             
                             
                             
                         }

                         override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                             Log.e(TAG,"Hydra Mqtt Connection Failure !!")
                             for (callback in errorCallbacks) {
                                 callback.onError(exception!!.message)
                             }
                             if (this@HydraMqtt.mqttClient != null) {
                                 try {
                                     this@HydraMqtt.mqttClient!!.disconnect().actionCallback=(object :IMqttActionListener {
                                         override fun onSuccess(asyncActionToken: IMqttToken?) {
                                         }

                                         override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                                         }

                                     })
                                 } finally {
                                     this@HydraMqtt.mqttClient = null
                                 }
                             }
                             if (reconnectOnFailure) {
                                 scheduleReconnectTimer()
                             }
                         }
                     })
               }
    }

    /**
     * **************Connect to Mqtt Client ***********
     * **/
    @Throws(IOException::class)
    public fun connect(context: Context, endpointUri: String) {
        Log.e(TAG, "Connecting to $endpointUri")
        this.endpointUri = endpointUri
        this.context = context
        this.timer = Timer("Reconnect Timer for $endpointUri")
        val mqttClientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(context, endpointUri, mqttClientId)
        iMqttToken=mqttClient!!.connect()
        getData()
    }

    /**
     * Disconnect to socket
     * **/
    @Throws(IOException::class)
    public fun disconnect() {
        Log.e(TAG, "disconnect")
        try{
        if (mqttClient != null) {
            mqttClient!!.disconnect()
            mqttClient = null
        }
      //  cancelHeartbeatTimer()
        cancelReconnectTimer()
        }catch (e:java.lang.Exception) {} }


    /**
     * @return true if the client connection is connected
     **/
    fun isConnected(): Boolean {
        return mqttClient != null
    }


    /**
     * Sets up and schedules a timer task to make repeated reconnect attempts at configured
     * intervals
     **/
    private fun scheduleReconnectTimer() {
        cancelReconnectTimer()
      //  cancelHeartbeatTimer()

        this@HydraMqtt.reconnectTimerTask = object : TimerTask() {
            override fun run() {
                Log.e(TAG, "reconnectTimerTask run")
                try {
                    this@HydraMqtt.connect(context!!,endpointUri)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reconnect to $e")
                }

            }
        }
        timer!!.schedule(this@HydraMqtt.reconnectTimerTask, RECONNECT_INTERVAL_MS.toLong())
    }

    private fun startHeartbeatTimer() {
        this@HydraMqtt.heartbeatTimerTask = object : TimerTask() {
            override fun run() {
                Log.e(TAG, "heartbeatTimerTask run")
                if (this@HydraMqtt.isConnected()) {
                    try {
                        /**
                         * Send Heartbeat here
                         **/
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send heartbeat :  " + e)
                    }
                }
            }
        }

        timer!!.schedule(
            this@HydraMqtt.heartbeatTimerTask, this@HydraMqtt.heartbeatInterval!!.toLong(),
            this@HydraMqtt.heartbeatInterval!!.toLong()
        )
    }

    private fun cancelHeartbeatTimer() {
        if (this@HydraMqtt.heartbeatTimerTask != null) {
            this@HydraMqtt.heartbeatTimerTask!!.cancel()
        }
    }

    private fun cancelReconnectTimer() {
        if (this@HydraMqtt.reconnectTimerTask != null) {
            this@HydraMqtt.reconnectTimerTask!!.cancel()
        }
    }

    private fun flushSendBuffer() {
        while (this.isConnected() && !this.sendBuffer.isEmpty()) {
            val body = this.sendBuffer.remove()
            this.mqttClient!!.subscribe(body.toString(),2)
        }
    }

    /**
     *  subscription to backend
     * **/
    public fun subscribe(text: String) {
        if (this.isConnected()) {
            try {
                mqttClient!!.subscribe(text,2)
            } catch (e: Exception) {
                Log.e(TAG,e.toString())
                System.gc()
            }
        } else {
            this.sendBuffer!!.add(text)
        }
    }

    /**
     *  unsubscription to backend
     * **/
    public fun unSubscribe(text: String) {
        if (this.isConnected()) {
            try {
                mqttClient!!.unsubscribe(text)
            } catch (e: Exception) {
                Log.e(TAG,e.toString())
                System.gc()
            }
        }
    }

    fun onClose(callback: IClientCloseCallback): HydraMqtt {
        this.clientCloseCallbacks.add(callback)
        return this
    }

    fun onError(callback: IErrorCallback): HydraMqtt {
        this.errorCallbacks.add(callback)
        return this
    }

    fun onMessage(callback: IMessageCallback): HydraMqtt {
        this.messageCallbacks.add(callback)
        return this
    }

    fun onOpen(callback: IClientOpenCallback): HydraMqtt {
        cancelReconnectTimer()
        this.clientOpenCallbacks.add(callback)
        return this
    }
}