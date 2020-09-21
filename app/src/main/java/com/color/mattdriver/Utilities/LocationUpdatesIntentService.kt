package com.color.mattdriver.Utilities

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class LocationUpdatesIntentService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val locationResult = LocationResult.extractResult(intent) ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        val wayLatitude = location.latitude
                        val wayLongitude = location.longitude
                        Log.e(TAG,"wayLatitude: ${wayLatitude} longitude: ${wayLongitude}")
                    }
                }
            }
        }
    }

    companion object {
        private val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.sample.locationupdatespendingintent.action" + ".PROCESS_UPDATES"
        private val TAG = LocationUpdatesIntentService::class.java.simpleName
    }
}