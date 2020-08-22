package com.color.mattdriver.Activities

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.color.mattdriver.Constants
import com.color.mattdriver.Fragments.*
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.R
import com.color.mattdriver.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    Welcome.WelcomeInterface,
    JoinOrganisation.JoinOrganisationInterface,
    CreateOrganisation.CreateOrganisationInterface,
    OrganisationPasscode.OrganisationPasscodeInterface,
    ViewOrganisation.viewOrganisationInterface, GoogleMap.OnMyLocationClickListener {
    val TAG = "MapsActivity"
    val _welcome = "_welcome"
    val _join_organisation = "_join_organisation"
    val _create_organisation = "_create_organisation"
    val _organisation_passcode =  "_organisation_passcode"
    val _view_organisation = "_view_organisation"
    val _settings = "_settings"

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 1000
    private var wayLatitude = 0.0
    private var wayLongitude = 0.0
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    val constants = Constants()
    var is_loading = false

    val db = Firebase.firestore
    var organisations: ArrayList<organisation> = ArrayList()
    var my_organisations: ArrayList<String> = ArrayList()
    var mapView: View? = null
    val ZOOM = 17f
    var has_set_my_location = false


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e(TAG,"onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar = supportActionBar!!
        actionBar.hide()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapView = mapFragment.view
        mapFragment.getMapAsync(this)

        set_up_getting_my_location()
        set_network_change_receiver()

//        if(constants.SharedPreferenceManager(applicationContext).isFirstTimeLaunch()){
//            open_welcome_fragment()
//        }
//
        binding.continueLayout.setOnClickListener {
            constants.touch_vibrate(applicationContext)
            val orgs = Gson().toJson(organisation.organisation_list(organisations))
            supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id,JoinOrganisation.newInstance("","", orgs),_join_organisation).commit()
        }

        binding.settings.setOnClickListener {
            constants.touch_vibrate(applicationContext)
            supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id,MainSettings.newInstance("",""),_settings).commit()
//            binding.settings.visibility = View.GONE
        }

        if(constants.SharedPreferenceManager(applicationContext).get_current_data().equals("")){
            //first time, data has to be loaded
            Log.e(TAG,"loading data from firestore")
            load_organisations()
            load_my_organisations()
        }else{
            Log.e(TAG,"setting session data")
            set_session_data()
            Log.e("MapsAct","organisations are : ${organisations.size}")
        }

        val currentFragPos = supportFragmentManager.fragments.size-1

        if(supportFragmentManager.findFragmentByTag(_settings)!=null){
            Log.e(TAG,"Were in settings, hiding settings btn")
//            binding.settings.visibility = View.GONE
        }

    }

    fun open_welcome_fragment(){
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(binding.money.id,Welcome.newInstance("",""),_welcome).commit()
    }

    override fun OnContinueSelected() {
        constants.SharedPreferenceManager(applicationContext).setFirstTimeLaunch(false)
        onBackPressed()

    }

    override fun onBackPressed() {
        if(supportFragmentManager.fragments.size>1){
            val trans = supportFragmentManager.beginTransaction()
            trans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            val currentFragPos = supportFragmentManager.fragments.size-1

            removing_fragment_notifier(supportFragmentManager.fragments.get(currentFragPos).tag!!)
            trans.remove(supportFragmentManager.fragments.get(currentFragPos))
            trans.commit()
            supportFragmentManager.popBackStack()

        }else super.onBackPressed()
    }

    fun removing_fragment_notifier(tag: String){
//        if(tag.equals(_settings)){
//            binding.settings.visibility = View.VISIBLE
//        } else
            if(tag.equals(_view_organisation)){
            show_normal_home_items()
        }
    }



    fun set_network_change_receiver(){
        val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network?) {
                // network available
                whenNetworkAvailable()
            }

            override fun onLost(network: Network?) {
                // network unavailable
                whenNetworkLost()
            }
        }

        val connectivityManager: ConnectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request: NetworkRequest = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }

        if(!constants.isOnline(applicationContext)){
            whenNetworkLost()
        }
    }

    fun whenNetworkLost(){
        val alpha_hidden = constants.dp_to_px(-20f, applicationContext)
        val alpha_shown = 0f
        val duration = 200L
        val delay = 1000L
        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message?) {
                binding.networkRelative.visibility = View.VISIBLE
                binding.noInternetText.text = getString(R.string.no_internet_connection)
                binding.noInternetText.setBackgroundColor(resources.getColor(R.color.red))
                val valueAnimator = ValueAnimator.ofFloat(alpha_hidden, alpha_shown)
                val listener = ValueAnimator.AnimatorUpdateListener{
                    val value = it.animatedValue as Float
                    binding.networkRelative.translationY = value
                }
                valueAnimator.addUpdateListener(listener)
                valueAnimator.interpolator = LinearOutSlowInInterpolator()
                valueAnimator.duration = duration
                valueAnimator.start()
            }
        }

        Timer().schedule(100){
            val message = mHandler.obtainMessage()
            message.sendToTarget()
        }
    }

    fun whenNetworkAvailable(){
        val alpha_hidden = constants.dp_to_px(-20f, applicationContext)
        val alpha_shown = 0f
        val duration = 200L
        val delay = 1000L

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message?) {
                binding.noInternetText.text = getString(R.string.back_online)
                binding.noInternetText.setBackgroundColor(resources.getColor(R.color.green))
                val valueAnimator = ValueAnimator.ofFloat(alpha_shown, alpha_hidden)
                val listener = ValueAnimator.AnimatorUpdateListener{
                    val value = it.animatedValue as Float
                    binding.networkRelative.translationY = value
                    if(value==alpha_hidden){
                        binding.networkRelative.visibility = View.GONE
                    }
                }
                valueAnimator.addUpdateListener(listener)
                valueAnimator.interpolator = LinearOutSlowInInterpolator()
                valueAnimator.duration = duration
                valueAnimator.startDelay = delay
                valueAnimator.start()
            }
        }

        Timer().schedule(100){
            val message = mHandler.obtainMessage()
            message.sendToTarget()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationRequestCode -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    set_up_getting_my_location()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun set_up_getting_my_location(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), locationRequestCode)
        } else{
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationRequest = LocationRequest.create()
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationRequest.setInterval(10 * 1000)


            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult == null) {
                        return
                    }
                    for (location in locationResult.locations) {
                        if (location != null) {
                            wayLatitude = location.latitude
                            wayLongitude = location.longitude
                            Log.e(TAG,"wayLatitude: ${wayLatitude} longitude: ${wayLongitude}")
                            if(!has_set_my_location){
                                has_set_my_location = true
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,
                                    location.longitude), ZOOM))
                            }
                        }
                    }
                }
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)

        }
    }

    override fun onDestroy() {
        Log.e(TAG,"onDestroy")
        super.onDestroy()
        if (this.mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback)
        }
        store_session_data()
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"onStop")

    }

    override fun onStart() {
        Log.e(TAG,"onStart")
        super.onStart()
        set_session_data()
    }

    override fun onPause() {
        Log.e(TAG,"onPause")
        super.onPause()
    }

    override fun onResume() {
        Log.e(TAG,"onResume")
        super.onResume()
    }

    fun hideLoadingScreen(){
        is_loading = false
        binding.mapsLoadingScreen.visibility = View.GONE
    }

    fun showLoadingScreen(){
        is_loading = true
        binding.mapsLoadingScreen.visibility = View.VISIBLE
        binding.mapsLoadingScreen.setOnTouchListener { v, _ -> true }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        if(Constants().SharedPreferenceManager(applicationContext).isDarkModeOn()) {
            val success = googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json)))
            if (!success) {
                Log.e("mapp", "Style parsing failed.")
            }
        }

        mMap.setOnMyLocationClickListener(this)
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        mMap.setIndoorEnabled(false)
        mMap.setBuildingsEnabled(false)
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        if (mapView!!.findViewById<View>("1".toInt()) != null) {
            // Get the button view
            val locationButton = (mapView!!.findViewById<View>("1".toInt())
                .getParent() as View).findViewById<View>("2".toInt())
            // and next place it, on bottom right (as Google Maps app)
            val layoutParams: RelativeLayout.LayoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, dpToPx(10), dpToPx(100))
        }

//        mMap.addMarker(MarkerOptions()
//            .position(LatLng(location.latitude, location.longitude))
////            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin",60,110)))
//        )
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), ZOOM))

    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
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
                store_session_data()
                if(supportFragmentManager.findFragmentByTag(_join_organisation)!=null){
                    (supportFragmentManager.findFragmentByTag(_join_organisation) as JoinOrganisation)
                        .onOrganisationListReloaded(organisations)
                }
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
                store_session_data()
            }
    }

    override fun whenReloadOrganisations() {
        load_organisations()
    }

    override fun whenCreateOrganisation() {
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .add(binding.money.id,CreateOrganisation.newInstance("",""),_create_organisation).commit()
    }

    override fun joinOrganisation(organisation: organisation) {
        if(my_organisations.contains(organisation.org_id)){
            //im a part of this
            var org_string = Gson().toJson(organisation)
            supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id,ViewOrganisation.newInstance("","",org_string),_view_organisation).commit()

        }else{
            val org = Gson().toJson(organisation)
            supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id,OrganisationPasscode.newInstance("","", org),_organisation_passcode).commit()
        }
    }

    override fun whenCreateOrganisationContinue(name: String, country_name: String) {
        showLoadingScreen()

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val org_ref = db.collection(constants.organisations)
            .document(country_name)
            .collection(constants.country_organisations)
            .document()

        val time = Calendar.getInstance().timeInMillis

        val data = hashMapOf(
            "name" to name,
            "org_id" to org_ref.id,
            "country" to country_name,
            "creater" to uid,
            "creation_time" to time
        )

        val new_org = organisation(name,time)
        new_org.org_id = org_ref.id
        new_org.country = country_name

        organisations.add(new_org)

        org_ref.set(data).addOnSuccessListener {
            Toast.makeText(applicationContext,"Done", Toast.LENGTH_SHORT).show()
            hideLoadingScreen()
            onBackPressed()

            my_organisations.add(new_org.org_id!!)
            var org_string = Gson().toJson(new_org)
            supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id,ViewOrganisation.newInstance("","",org_string),_view_organisation).commit()
        }


        if(supportFragmentManager.findFragmentByTag(_join_organisation)!=null){
            (supportFragmentManager.findFragmentByTag(_join_organisation) as JoinOrganisation)
                .onOrganisationListUpdated(organisations)
        }

        db.collection(constants.coll_users).document(uid)
            .collection(constants.my_organisations).document(org_ref.id)
            .set(hashMapOf(
                "name" to name,
                "org_id" to org_ref.id,
                "creation_time" to time
            ))
    }


    override fun submitOrganisationPasscode(code: Long, organisation: organisation) {
        showLoadingScreen()
        db.collection(constants.otp_codes)
            .document(organisation.org_id!!)
            .collection(constants.code_instances)
            .get().addOnSuccessListener {
                hideLoadingScreen()
                if(!it.documents.isEmpty()){
                    var does_code_work = false
                    for(item in it.documents){
                        val item_code = item["code"] as Long
                        val item_creation_time = item["creation_time"] as Long
                        val item_organisation = item["organisation"] as String
                        val time_difference = Calendar.getInstance().timeInMillis - item_creation_time

                        if(item_code==code && time_difference < constants.otp_expiration_time
                            && item_organisation.equals(organisation.org_id)){
                            //code works
                            does_code_work = true
                        }
                    }
                    if(does_code_work){
                        //if password is right
                        my_organisations.add(organisation.org_id!!)
                        var org_string = Gson().toJson(organisation)
                        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .add(binding.money.id,ViewOrganisation.newInstance("","",org_string),_view_organisation).commit()

                        if(!!constants.SharedPreferenceManager(applicationContext).getPersonalInfo()!!.email
                                .equals(constants.unknown_email)) {
                            //the user is a registered user.
                            val uid = FirebaseAuth.getInstance().currentUser!!.uid
                            db.collection(constants.coll_users).document(uid)
                                .collection(constants.my_organisations)
                                .document(organisation.org_id!!)
                                .set(hashMapOf(
                                        "name" to organisation.name,
                                        "org_id" to organisation.org_id,
                                        "creation_time" to organisation.creation_time
                                    ))
                        }

                    }else{
                        //if password is wrong
                        if(supportFragmentManager.findFragmentByTag(_organisation_passcode)!=null){
                            (supportFragmentManager.findFragmentByTag(_organisation_passcode) as OrganisationPasscode).didPasscodeFail()
                        }
                    }
                }else{
                    //if password is wrong
                    if(supportFragmentManager.findFragmentByTag(_organisation_passcode)!=null){
                        (supportFragmentManager.findFragmentByTag(_organisation_passcode) as OrganisationPasscode).didPasscodeFail()
                    }
                }

            }
    }

    override fun createNewRouteClicked(organisation: organisation) {
        openRouteCreater(organisation)
    }

    override fun generatePasscodeClicked(organisation: organisation, code: Long) {
        showLoadingScreen()
        db.collection(constants.otp_codes)
            .document(organisation.org_id!!)
            .collection(constants.code_instances)
            .document().set(hashMapOf(
                "code" to code,
                "organisation" to organisation.org_id,
                "creation_time" to Calendar.getInstance().timeInMillis
            )).addOnSuccessListener {
                hideLoadingScreen()
                Toast.makeText(applicationContext,"The password will only work for 1 min",Toast.LENGTH_SHORT).show()
                if(supportFragmentManager.findFragmentByTag(_view_organisation)!=null){
                    (supportFragmentManager.findFragmentByTag(_view_organisation) as ViewOrganisation).isPasscodeSet()
                }
            }

    }


    fun store_session_data(){
        val session = Gson().toJson(session_data(organisations,my_organisations))
        constants.SharedPreferenceManager(applicationContext).store_current_data(session)
    }

    fun set_session_data(){
        val session = constants.SharedPreferenceManager(applicationContext).get_current_data()
        if(!session.equals("")){
            //its not empty
            var session_obj = Gson().fromJson(session,session_data::class.java)
            organisations = session_obj.organisations
            my_organisations = session_obj.my_organisations
        }
    }

    class session_data(var organisations: ArrayList<organisation>,
                       var my_organisations: ArrayList<String>): Serializable


    fun openRouteCreater(organisation: organisation){
        hide_normal_home_items()
    }

    fun hide_normal_home_items(){
        binding.settings.visibility = View.GONE
        binding.bottomSheetLayout.visibility = View.GONE
        binding.money.visibility = View.GONE
    }

    fun show_normal_home_items(){
        binding.settings.visibility = View.VISIBLE
        binding.bottomSheetLayout.visibility = View.VISIBLE
        binding.money.visibility = View.VISIBLE
    }

    override fun onMyLocationClick(p0: Location) {

    }

}