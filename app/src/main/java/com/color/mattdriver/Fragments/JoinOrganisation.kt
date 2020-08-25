package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.R
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.android.synthetic.main.recycler_item_organisation_item.*
import java.util.*
import kotlin.collections.ArrayList


class JoinOrganisation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGS = "ARG_ORGS"
    private lateinit var listener: JoinOrganisationInterface
    var organisations: ArrayList<organisation> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organisations = Gson().fromJson(it.getString(ARG_ORGS), organisation.organisation_list::class.java).organisations
        }
    }

    var onOrganisationListUpdated: (ArrayList<organisation>) -> Unit = {}

    var onOrganisationListReloaded: (ArrayList<organisation>) -> Unit = {}

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
        val no_orgs_layout: RelativeLayout = va.findViewById(R.id.no_orgs_layout)

        val money: RelativeLayout = va.findViewById(R.id.money)
        money.setOnTouchListener { v, event -> true }

        val swipeContainer = va.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)

        swipeContainer.setColorSchemeResources(
            R.color.blue,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        swipeContainer.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                listener.whenReloadOrganisations()
                Constants().touch_vibrate(context)

            }
        })

        onOrganisationListReloaded = {
            swipeContainer.setRefreshing(false)
            organisations = it
            organisation_recyclerview.adapter!!.notifyDataSetChanged()

            if(organisations.isNotEmpty()){
                no_orgs_layout.visibility = View.GONE
            }else{
                no_orgs_layout.visibility = View.VISIBLE
            }
        }

        create_layout.setOnClickListener {
            listener.whenCreateOrganisation()
            Constants().touch_vibrate(context)
        }

        organisation_recyclerview.adapter = OrganisationsListAdapter()
        organisation_recyclerview.layoutManager = LinearLayoutManager(context)

        if(organisations.isNotEmpty()){
            no_orgs_layout.visibility = View.GONE
        }else{
            no_orgs_layout.visibility = View.VISIBLE
        }

        onOrganisationListUpdated = {
            organisations = it
            if(organisations.isNotEmpty()){
                no_orgs_layout.visibility = View.GONE
                organisation_recyclerview.adapter!!.notifyDataSetChanged()
            }else{
                no_orgs_layout.visibility = View.VISIBLE
            }
        }

        return va
    }

    internal inner class OrganisationsListAdapter : RecyclerView.Adapter<ViewHolderOrganisations>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderOrganisations {
            val vh = ViewHolderOrganisations(LayoutInflater.from(context).inflate(R.layout.recycler_item_organisation_item, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(v: ViewHolderOrganisations, position: Int) {
            val organisation = organisations.get(position)

            val t_diff = Calendar.getInstance().timeInMillis-organisation.creation_time
            v.creation_country.text = "${organisation.country}"
            v.org_name.text = organisation.name

            v.join_layout.setOnClickListener {
                Constants().touch_vibrate(context)
                listener.joinOrganisation(organisation)
            }
        }

        override fun getItemCount():Int {
            return organisations.size
        }

    }

    internal inner class ViewHolderOrganisations (view: View) : RecyclerView.ViewHolder(view) {
        val join_layout: RelativeLayout = view.findViewById(R.id.join_layout)
        val creation_country: TextView = view.findViewById(R.id.creation_country)
        val org_name: TextView = view.findViewById(R.id.org_name)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, organisations: String) =
            JoinOrganisation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGS, organisations)
                }
            }
    }

    interface JoinOrganisationInterface{
        fun whenReloadOrganisations()
        fun whenCreateOrganisation()
        fun joinOrganisation(organisation: organisation)
    }
}