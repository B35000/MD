package com.color.mattdriver.Utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.color.mattdriver.Constants
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val locationResult = LocationResult.extractResult(intent) ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        val wayLatitude = location.latitude
                        val wayLongitude = location.longitude
                        Log.e(TAG,"wayLatitude: ${wayLatitude} longitude: ${wayLongitude}")
                        Constants().SharedPreferenceManager(context).store_location(Gson().toJson(LatLng(wayLatitude,wayLongitude)))

//                        val b_intent = Intent("INTERNET_LOST")
//                        intent.action = "com.example.Broadcast"
//                        intent.putExtra("loc", Gson().toJson(location))
//                        intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
//                        context.sendBroadcast(b_intent)

                    }
                }
            }
        }
    }

    companion object {
        private val TAG = "LUBroadcastReceiver"
        internal val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.sample.locationupdatespendingintent.action" + ".PROCESS_UPDATES"
    }
}