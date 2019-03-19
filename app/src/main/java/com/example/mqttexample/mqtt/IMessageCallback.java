package com.example.mqttexample.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMessageCallback {
    void onMessage(MqttMessage message);
}
