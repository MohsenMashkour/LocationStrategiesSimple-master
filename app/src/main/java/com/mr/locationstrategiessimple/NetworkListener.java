package com.mr.locationstrategiessimple;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

class NetworkListener extends PhoneStateListener {
    private static NetworkListener instance;
    Context context;
    NetworkStateChangedListener listener;
    // Default constructor of the NetworkListener
    private NetworkListener(Context context){
        this.context = context;
    }
    // Method for retrieving the single instance of the NetworkListener
    public static NetworkListener getInstance(Context context){
        if(instance == null){
            instance = new NetworkListener(context);
        }
        return instance;
    }
    // Method for adding a listener to the updates received by NetworkListener
    public void addListener(NetworkStateChangedListener listener){
        this.listener = listener;
    }


    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        if(listener != null) {
            // If the listener exists send the signalStrength variable to the handler
            listener.signalStrengthsChangedHandler(signalStrength);
        }
    }

    // Interface used for creating a connection between NetworkListener
    // and the Activity that is using it.
    public interface NetworkStateChangedListener{

        void signalStrengthsChangedHandler(SignalStrength signalStrength);
    }
}

