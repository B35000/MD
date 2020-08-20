package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.color.mattdriver.Constants
import com.color.mattdriver.R



class JoinOrganisation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: JoinOrganisationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is JoinOrganisationInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_join_organisation, container, false)
        val create_layout: RelativeLayout = va.findViewById(R.id.create_layout)
        val organisation_recyclerview: RecyclerView = va.findViewById(R.id.organisation_recyclerview)

        create_layout.setOnClickListener {
            listener.whenCreateOrganisation()
            Constants().touch_vibrate(context)
        }

        return va
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinOrganisation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    interface JoinOrganisationInterface{
        fun whenCreateOrganisation()
    }
}