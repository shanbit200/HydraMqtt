package com.example.mqttexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mqttexample.mqtt.*
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = MainActivity::class.java.name
    }

    private val utils = Utils.getInstance()
    private var hydraMqtt = HydraMqtt.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        hydraMqtt!!.onMessage(IMessageCallback {
            Log.e(TAG, "Incoming message from : $it")
        }).onOpen(IClientOpenCallback {
            Log.e(TAG, "open")
        }).onClose(IClientCloseCallback {
            Log.e(TAG, "close")
        }).onError(IErrorCallback {
            Log.e(TAG, "error")
        })

        tv.setOnClickListener {
            Log.e(TAG, "*******************CLick*******************")
            if (hydraMqtt.isConnected()) {
              hydraMqtt.disconnect()
            }   else{
                hydraMqtt!!.connect(application, "URL")
            }

        }
    }

}


