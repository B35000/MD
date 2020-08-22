package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.R
import com.google.gson.Gson


class OrganisationPasscode : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private lateinit var the_organisation: organisation
    private lateinit var listener: OrganisationPasscodeInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            the_organisation = Gson().fromJson(it.getString(ARG_ORGANISATION), organisation::class.java)
        }
    }

    var didPasscodeFail: () -> Unit = {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OrganisationPasscodeInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_organisation_passcode, container, false)

        val create_layout: RelativeLayout = va.findViewById(R.id.create_layout)
        val editText: EditText = va.findViewById(R.id.editText)
        val org_name: TextView = va.findViewById(R.id.org_name)
        val creation_country: TextView = va.findViewById(R.id.creation_country)
        val money: RelativeLayout = va.findViewById(R.id.money)

        money.setOnTouchListener { v, event -> true }

        org_name.text = the_organisation.name
        creation_country.text = the_organisation.country

        create_layout.setOnClickListener {
            val code = editText.text.toString().trim()
            if(code.equals("")){
                editText.setError(getString(R.string.say_something))
            }else{
                Constants().touch_vibrate(context)
                listener.submitOrganisationPasscode(code.toLong(),the_organisation)
            }
        }

        didPasscodeFail = {
            editText.setError(getString(R.string.that_didnt_work))
        }

        return va
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String, organisation: String) =
            OrganisationPasscode().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGANISATION,organisation)
                }
            }
    }


    interface OrganisationPasscodeInterface{
        fun submitOrganisationPasscode(code: Long, organisation: organisation)
    }

}