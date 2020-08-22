package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import com.color.mattdriver.R
import com.hbb20.CountryCodePicker


class CreateOrganisation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: CreateOrganisationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is CreateOrganisationInterface){
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_create_organisation, container, false)
        val editText: EditText = va.findViewById(R.id.editText)
        val create_layout: RelativeLayout = va.findViewById(R.id.create_layout)
        val ccp: CountryCodePicker = va.findViewById(R.id.ccp)
        val money: RelativeLayout = va.findViewById(R.id.money)

        money.setOnTouchListener { v, event -> true }

        create_layout.setOnClickListener {
            var org_name = editText.text.toString().trim()
            val country_name = ccp.selectedCountryName
            if(org_name.equals("")){
                editText.setError("Name!")
            }else{
                listener.whenCreateOrganisationContinue(org_name,country_name)
            }
        }

        return va
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) = CreateOrganisation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    interface CreateOrganisationInterface{
        fun whenCreateOrganisationContinue(name: String, country_name: String)
    }

}