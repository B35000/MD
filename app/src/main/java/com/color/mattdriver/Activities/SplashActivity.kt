package com.color.mattdriver.Activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.number
import com.color.mattdriver.R
import com.color.mattdriver.Utilities.GpsUtils
import com.color.mattdriver.databinding.ActivitySplashBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hbb20.CountryCodePicker
import java.util.*

class SplashActivity : AppCompatActivity() {
    val TAG = "SplashScreen"
    private lateinit var binding: ActivitySplashBinding
    var ACCESS_FINE_LOCATION_CODE = 3310
    private var mAuth: FirebaseAuth? = null
    val constants = Constants()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        GpsUtils(this).turnGPSOn(object : GpsUtils.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                openMap()
            }
        })
        Constants().maintain_theme(applicationContext)
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
        if(constants.SharedPreferenceManager(applicationContext).getPersonalInfo()!=null){
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }else{
            startAnonymousSignUp()
        }

    }


    fun startAnonymousSignUp(){
        if(isOnline()) {
            mAuth!!.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user = mAuth!!.currentUser

                        val tm: TelephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        val locale: String = tm.getNetworkCountryIso()
                        val ccp = CountryCodePicker(applicationContext)
                        ccp.setDefaultCountryUsingNameCode(locale)
                        ccp.setAutoDetectedCountry(true)

                        val the_number = number(0,
                            ccp.selectedCountryCodeWithPlus,
                            ccp.selectedCountryName,
                            ccp.selectedCountryNameCode
                        )
                        constants.SharedPreferenceManager(applicationContext)
                            .setPersonalInfo(the_number,constants.unknown_email,"", Calendar.getInstance().timeInMillis,user!!.uid)
                        openMap()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInAnonymously:failure", task.exception)
                    }
                }
        }else{
            Snackbar.make(binding.root,getString(R.string.please_check_on_your_internet_connection),
                Snackbar.LENGTH_SHORT).show()
        }
    }

    fun isOnline(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isConnected
    }

}