package com.color.mattdriver.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.color.mattdriver.Utilities.GpsUtils
import com.color.mattdriver.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    val TAG = "SplashScreen"
    private lateinit var binding: ActivitySplashBinding
    var ACCESS_FINE_LOCATION_CODE = 3310


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GpsUtils(this).turnGPSOn(object : GpsUtils.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                openMap()
            }
        })

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACCESS_FINE_LOCATION_CODE) {
                openMap() // flag maintain before get location
            }
        }
    }

    fun openMap(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}