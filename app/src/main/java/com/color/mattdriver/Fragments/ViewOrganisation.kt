package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.Models.route
import com.color.mattdriver.R
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList


class ViewOrganisation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ROUTES = "ARG_ROUTES"
    private val ARG_PICKED_ROUTE = "ARG_PICKED_ROUTE"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private lateinit var listener: viewOrganisationInterface
    private var routes: ArrayList<route> = ArrayList()
    private lateinit var organ: organisation
    private var set_route : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organ = Gson().fromJson(it.getString(ARG_ORGANISATION),organisation::class.java)
            routes = Gson().fromJson(it.getString(ARG_ROUTES), route.route_list::class.java).routes
            set_route = it.getString(ARG_PICKED_ROUTE)!!
        }
    }

    var isPasscodeSet: () -> Unit = {}

    var when_route_data_updated: (routes: ArrayList<route>) -> Unit = {}

    var when_route_picked: (route: String) -> Unit = {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is viewOrganisationInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_view_organisation, container, false)
        val title: TextView = va.findViewById(R.id.title)
        val join_organisation_layout: RelativeLayout = va.findViewById(R.id.join_organisation_layout)
        val new_route_layout: RelativeLayout = va.findViewById(R.id.new_route_layout)
        val create_route_layout: RelativeLayout = va.findViewById(R.id.create_route_layout)
        val new_code: TextView = va.findViewById(R.id.new_code)
        val passcode_layout: RelativeLayout = va.findViewById(R.id.passcode_layout)
        val generate_passcode_layout: RelativeLayout = va.findViewById(R.id.generate_passcode_layout)
        val created_routes_recyclerview: RecyclerView = va.findViewById(R.id.created_routes_recyclerview)
        val selected_route_card: CardView = va.findViewById(R.id.selected_route_card)
        val creation_time: TextView = va.findViewById(R.id.creation_time)
        val source_text: TextView = va.findViewById(R.id.source_text)
        val destination_text: TextView = va.findViewById(R.id.destination_text)
        val see_route_layout :RelativeLayout = va.findViewById(R.id.see_route_layout)
        val swipeContainer = va.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)

        val money: RelativeLayout = va.findViewById(R.id.money)

        money.setOnTouchListener { v, event -> true }

        passcode_layout.visibility = View.VISIBLE
        new_route_layout.visibility = View.VISIBLE

        if(routes.isNotEmpty()){
            created_routes_recyclerview.adapter = RoutesListAdapter()
            created_routes_recyclerview.layoutManager = LinearLayoutManager(context)
        }

        swipeContainer.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                listener.whenReloadRoutes()
                Constants().touch_vibrate(context)

            }
        })

        title.text  = organ.name
        create_route_layout.setOnClickListener {
            Constants().touch_vibrate(context)
            listener.createNewRouteClicked(organ)
        }
        generate_passcode_layout.setOnClickListener {
            //a new random passcode
            val i = (Random().nextInt(900000) + 100000).toLong()
            new_code.visibility = View.VISIBLE
            new_code.text = "Code : ${i}"

            Constants().touch_vibrate(context)
            listener.generatePasscodeClicked(organ, i)
        }

        isPasscodeSet = {
            Handler().postDelayed({
                new_code.visibility = View.GONE
                new_code.text = ""
            }, Constants().otp_expiration_time)
        }

        when_route_data_updated = {
            routes = it

            created_routes_recyclerview.adapter = RoutesListAdapter()
            created_routes_recyclerview.layoutManager = LinearLayoutManager(context)
            swipeContainer.setRefreshing(false)
        }

        when_route_picked = {
            set_route = it
            if(set_route.equals("")){
                selected_route_card.visibility = View.GONE
            }
            for(item in routes){
                if(item.route_id.equals(set_route)){
                    //found the route
                    selected_route_card.visibility = View.VISIBLE
                    creation_time.text = Constants().get_formatted_time(item.creation_time)
                    source_text.text = item.starting_pos_desc
                    destination_text.text = item.ending_pos_desc

                    see_route_layout.setOnClickListener {
                        Constants().touch_vibrate(context)
                        listener.viewRoute(item, organ)
                    }
                }
            }
            when_route_data_updated(routes)
        }

        if(!set_route.equals("")){
            when_route_picked(set_route)
        }

        join_organisation_layout.setOnClickListener{
            Constants().touch_vibrate(context)
            listener.onChangeOrganisation()
        }

        return va
    }


    internal inner class RoutesListAdapter : RecyclerView.Adapter<ViewHolderRoutes>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderRoutes {
            val vh = ViewHolderRoutes(LayoutInflater.from(context).inflate(R.layout.recycler_item_route, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(v: ViewHolderRoutes, position: Int) {
            val route = routes.get(position)
            if(route.route_id.equals(set_route)){
                v.root_cardview.visibility = View.GONE
            }
            v.creation_time.text = "${Constants().get_formatted_time(route.creation_time)}"
            v.source_text.text = route.starting_pos_desc
            v.destination_text.text = route.ending_pos_desc

            v.view_layout.setOnClickListener {
                Constants().touch_vibrate(context)
                listener.viewRoute(route,organ)
            }
        }

        override fun getItemCount():Int {
            return routes.size
        }

    }

    internal inner class ViewHolderRoutes (view: View) : RecyclerView.ViewHolder(view) {
        val view_layout: RelativeLayout = view.findViewById(R.id.view_layout)
        val creation_time: TextView = view.findViewById(R.id.creation_time)
        val source_text: TextView = view.findViewById(R.id.source_text)
        val destination_text: TextView = view.findViewById(R.id.destination_text)
        val root_cardview: CardView = view.findViewById(R.id.root_cardview)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, organisation: String, routes: String, picked_route: String) =
            ViewOrganisation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGANISATION, organisation)
                    putString(ARG_ROUTES, routes)
                    putString(ARG_PICKED_ROUTE,picked_route)
                }
            }
    }


    interface viewOrganisationInterface{
        fun createNewRouteClicked(organisation: organisation)
        fun generatePasscodeClicked(organisation: organisation, code: Long)
        fun viewRoute(route:route, organisation: organisation)
        fun whenReloadRoutes()
        fun onChangeOrganisation()
    }

}