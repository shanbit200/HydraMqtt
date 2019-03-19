# HydraMqtt

Mqtt Android Client
        
         /*
         * Connecting
         * */
        val hydraMqtt = HydraMqtt.getInstance().connect(applicationcontext, "URL")

        /*
         * Subscribe data
         */
        socket.subscribe(dataToPush)
        
        /*
         * unSubscribe data
         */
        socket.unSubscribe(dataToPush)

        /*
         * Getting Callbacks
         */
         
        hydraMqtt!.onMessage(IMessageCallback {
            Log.e(TAG, "message : " + it)
        }).onOpen(ISocketOpenCallback {
            Log.e(TAG, "open")
        }).onClose(ISocketCloseCallback {
            Log.e(TAG, "close")
        }).onError(IErrorCallback {
            Log.e(TAG, "error")
        })
