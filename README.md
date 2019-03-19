# HydraMqtt
Mqtt Android Client

   hydraMqtt!!.connect(applicationcontext, "URL")

  hydraMqtt!!.onMessage(IMessageCallback {
            Log.e(TAG, "Incoming message from : $it")
        }).onOpen(IClientOpenCallback {
            Log.e(TAG, "open")
        }).onClose(IClientCloseCallback {
            Log.e(TAG, "close")
        }).onError(IErrorCallback {
            Log.e(TAG, "error")
        })
