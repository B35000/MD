package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.color.mattdriver.Constants
import com.color.mattdriver.R



class Welcome : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: WelcomeInterface


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is WelcomeInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mView = inflater.inflate(R.layout.fragment_welcome, container, false)

        val dark_mode_text: TextView = mView.findViewById(R.id.dark_mode_text)
        val night_mode_switch: Switch = mView.findViewById(R.id.night_mode_switch)
        val begin_layout = mView.findViewById<RelativeLayout>(R.id.begin_layout)

        if(Constants().SharedPreferenceManager(context!!).isDarkModeOn()){
            dark_mode_text.text = getString(R.string.turn_off_dark_mode)
        }
        night_mode_switch.isChecked = Constants().SharedPreferenceManager(context!!).isDarkModeOn()

        night_mode_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            Constants().dark_mode(context!!)
            if(isChecked) dark_mode_text.text = getString(R.string.turn_off_dark_mode)
            else dark_mode_text.text = getString(R.string.turn_on_dark_mode)
        }

        begin_layout.setOnClickListener{
            listener.OnContinueSelected()
            Constants().touch_vibrate(context)
        }

        return mView
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Welcome().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    interface WelcomeInterface{
        fun OnContinueSelected()
    }

}