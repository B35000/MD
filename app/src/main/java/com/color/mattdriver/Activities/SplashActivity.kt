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
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.Models.route
import com.color.mattdriver.R
import com.color.mattdriver.Utilities.GpsUtils
import com.color.mattdriver.databinding.ActivitySplashBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import java.util.*

class SplashActivity : AppCompatActivity() {
    val TAG = "SplashScreen"
    private lateinit var binding: ActivitySplashBinding
    var ACCESS_FINE_LOCATION_CODE = 3310
    private var mAuth: FirebaseAuth? = null
    val constants = Constants()
    var organisations: ArrayList<organisation> = ArrayList()
    var my_organisations: ArrayList<String> = ArrayList()
    var routes: ArrayList<route> = ArrayList()
    val db = Firebase.firestore


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
            if(constants.SharedPreferenceManager(applicationContext).get_current_data().equals("")){
                //theres no data, we nee to load stuff first
                load_organisations()
            }else {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
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

    fun load_organisations(){
        val user = constants.SharedPreferenceManager(applicationContext).getPersonalInfo()!!
        organisations.clear()
        db.collection(constants.organisations)
            .document(user.phone.country_name)
            .collection(constants.country_organisations)
            .get().addOnSuccessListener {
                if(it.documents.isNotEmpty()){
                    for(item in it.documents){
                        val org_id = item["org_id"] as String
                        val org_name = item["name"] as String
                        val country = item["country"] as String
                        val creation_time = item["creation_time"] as Long

                        val org = organisation(org_name,creation_time)
                        org.org_id = org_id
                        org.country = country

                        organisations.add(org)
                    }
                }
                load_my_organisations()
            }
    }

    fun load_my_organisations(){
        my_organisations.clear()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        db.collection(constants.coll_users).document(uid)
            .collection(constants.my_organisations).get()
            .addOnSuccessListener {
                if(it.documents.isNotEmpty()){
                    for(item in it.documents){
                        val org_id = item["org_id"] as String
                        my_organisations.add(org_id)
                    }
                }
                load_routes()
            }
    }

    fun load_routes(){
        val user = constants.SharedPreferenceManager(applicationContext).getPersonalInfo()!!
        routes.clear()
        db.collection(constants.organisations)
            .document(user.phone.country_name)
            .collection(constants.country_routes)
            .get().addOnSuccessListener {
                if(it.documents.isNotEmpty()){
                    for(item in it.documents) {
                        val organisation_id = item["organisation_id"] as String
                        val creation_time = item["creation_time"] as Long
                        val route_id = item["route_id"] as String
                        val country = item["country"] as String
                        val creater = item["creater"] as String

                        val route = Gson().fromJson(item["route"].toString(), route::class.java)

                        routes.add(route)
                    }
                }
                store_session_data()
                openMap()
            }
    }

    fun store_session_data(){
        val session = Gson().toJson(
            MapsActivity.session_data(
                organisations, "","",
                my_organisations, routes
            )
        )
        constants.SharedPreferenceManager(applicationContext).store_current_data(session)
    }

    fun set_session_data(){
        val session = constants.SharedPreferenceManager(applicationContext).get_current_data()
        if(!session.equals("")){
            //its not empty
            var session_obj = Gson().fromJson(session, MapsActivity.session_data::class.java)
            organisations = session_obj.organisations
            my_organisations = session_obj.my_organisations
            routes = session_obj.routes
        }
    }
}