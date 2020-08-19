package com.color.mattdriver

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.color.mattdriver.Models.number
import java.io.Serializable

class Constants {
    val vib_time: Long = 2
    val coll_users = "users"
    val first_time_launch = "first_time_launch"
    val dark_mode = "dark_mode"
    val unknown_email = "unknown_email"

    fun touch_vibrate(context: Context?){
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(Constants().vib_time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(vib_time)
        }
    }

    inner class SharedPreferenceManager(val applicationContext: Context){
        fun setPersonalInfo(phone: number, email: String, name: String, sign_up_time: Long, uid: String){
            val user = user(phone,email,name, sign_up_time,uid)
            val pref: SharedPreferences = applicationContext.getSharedPreferences(coll_users, Context.MODE_PRIVATE)
            pref.edit().clear().putString(coll_users, Gson().toJson(user)).apply()
        }

        fun setPerson(usr: user){
            val pref: SharedPreferences = applicationContext.getSharedPreferences(coll_users, Context.MODE_PRIVATE)
            pref.edit().clear().putString(coll_users, Gson().toJson(usr)).apply()
        }

        fun isFirstTimeLaunch(): Boolean{
            val pref: SharedPreferences = applicationContext.getSharedPreferences(first_time_launch, Context.MODE_PRIVATE)
            val va = pref.getBoolean(first_time_launch, true)

            return va
        }

        fun setFirstTimeLaunch(value: Boolean){
            val pref: SharedPreferences = applicationContext.getSharedPreferences(first_time_launch, Context.MODE_PRIVATE)
            pref.edit().putBoolean(first_time_launch,value).apply()
        }

        fun getPersonalInfo(): user?{
            val pref: SharedPreferences = applicationContext.getSharedPreferences(coll_users, Context.MODE_PRIVATE)
            val user_str = pref.getString(coll_users, "")

            if(user_str==""){
                return null
            }else{
                return Gson().fromJson(user_str, user::class.java)
            }
        }

        fun isDarkModeOn(): Boolean{
            val pref: SharedPreferences = applicationContext.getSharedPreferences(dark_mode, Context.MODE_PRIVATE)
            return pref.getBoolean(dark_mode, false)
        }

        fun setDarkMode(is_dark_on: Boolean){
            val pref: SharedPreferences = applicationContext.getSharedPreferences(dark_mode, Context.MODE_PRIVATE)
            pref.edit().putBoolean(dark_mode, is_dark_on).apply()
        }


    }

    inner class user(var phone: number, val email: String, var name: String, val sign_up_time: Long, val uid: String): Serializable


    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isConnected
    }

    fun dark_mode(context: Context){
        if(!SharedPreferenceManager(context).isDarkModeOn()){
            setGucciTheme(AppCompatDelegate.MODE_NIGHT_YES)
            SharedPreferenceManager(context).setDarkMode(true)
        }else{
            setGucciTheme(AppCompatDelegate.MODE_NIGHT_NO)
            SharedPreferenceManager(context).setDarkMode(false)
        }
    }

    fun maintain_theme(context: Context){
        if(SharedPreferenceManager(context).isDarkModeOn()){
            setGucciTheme(AppCompatDelegate.MODE_NIGHT_YES)
            SharedPreferenceManager(context).setDarkMode(true)
        }else{
            setGucciTheme(AppCompatDelegate.MODE_NIGHT_NO)
            SharedPreferenceManager(context).setDarkMode(false)
        }
    }

    fun setGucciTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun dp_to_px(dip: Float, context: Context): Float {
        val r: Resources = context.getResources()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics())
    }


}