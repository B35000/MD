package com.color.mattdriver.Fragments

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.Models.route
import com.color.mattdriver.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
    private var routes_pos: HashMap<String,Int> = HashMap()
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

    var onOrganisationReloaded: (organisation) -> Unit = {}

    var reset_view: () -> Unit = {}

    var when_route_disabled: (is_disabled: Boolean, route: route) -> Unit = { b: Boolean, route: route -> }

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
        val selected_route_card: RelativeLayout = va.findViewById(R.id.selected_route_card)
        val creation_time: TextView = va.findViewById(R.id.creation_time)
        val source_text: TextView = va.findViewById(R.id.source_text)
        val destination_text: TextView = va.findViewById(R.id.destination_text)
        val see_route_layout :RelativeLayout = va.findViewById(R.id.see_route_layout)
        val swipeContainer = va.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)
        val view_drivers_layout: RelativeLayout = va.findViewById(R.id.view_drivers_layout)
        val view_drivers_button: RelativeLayout = va.findViewById(R.id.view_drivers_button)
        val refresh: TextView = va.findViewById(R.id.refresh)
        val view_all_routes_layout_button: RelativeLayout = va.findViewById(R.id.view_all_routes_layout_button)
        val view_all_routes_layout: RelativeLayout = va.findViewById(R.id.view_all_routes_layout)
        val stops_text: TextView = va.findViewById(R.id.stops_text)
        val auto_change_layout: RelativeLayout = va.findViewById(R.id.auto_change_layout)
        val auto_swap_switch: Switch = va.findViewById(R.id.auto_swap_switch)
        val swap_origin_textview: TextView = va.findViewById(R.id.swap_origin_textview)
        val swap_destination_textview: TextView = va.findViewById(R.id.swap_destination_textview)
        val money: RelativeLayout = va.findViewById(R.id.money)

        money.setOnTouchListener { v, event -> true }

        reset_view = {
            for(item in routes) {
                routes_pos.put(item.route_id, routes.indexOf(item))
            }

            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            if((organ.admins!=null && organ.admins.admins.contains(uid)) || uid.equals(Constants().pass)) {
                passcode_layout.visibility = View.VISIBLE
                new_route_layout.visibility = View.VISIBLE
            }

            if(routes.isNotEmpty()){
                created_routes_recyclerview.adapter = RoutesListAdapter()
                created_routes_recyclerview.layoutManager = LinearLayoutManager(context)
            }

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
                auto_swap_switch.setOnCheckedChangeListener { compoundButton, b ->}
                set_route = it
                if(set_route.equals("")){
                    selected_route_card.visibility = View.GONE
                    auto_change_layout.visibility = View.GONE
                }

                val item  = routes.get(routes_pos.get(set_route)!!)
                if(item.route_id.equals(set_route)){
                    //found the route
                    selected_route_card.visibility = View.VISIBLE
                    creation_time.text = Constants().get_formatted_time(item.creation_time)
                    source_text.text = "${item.starting_pos_desc}"
                    destination_text.text = "${item.ending_pos_desc}"

                    val mirror = get_mirror_route(item)
                    if(!mirror.route_id.equals(item.route_id)){
                        auto_change_layout.visibility = View.VISIBLE
                        swap_origin_textview.text = "From: ${mirror.starting_pos_desc}"
                        swap_destination_textview.text = "To: ${mirror.ending_pos_desc}"

                        auto_swap_switch.isChecked = Constants().SharedPreferenceManager(context!!).can_auto_swapp_route()
                        auto_swap_switch.setOnCheckedChangeListener { compoundButton, b ->
                            Constants().touch_vibrate(context)
                            Constants().SharedPreferenceManager(context!!).auto_swapp_route(b)
                        }
                    }

                    if(item.added_bus_stops.isNotEmpty()){
                        stops_text.text = item.added_bus_stops.size.toString()+" Stop"
                        if(item.added_bus_stops.size>1){
                            stops_text.text = item.added_bus_stops.size.toString()+" Stops"
                        }
                    }
                    see_route_layout.setOnClickListener {
                        Constants().touch_vibrate(context)
                        listener.viewRoute(item, organ)
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


            if(((organ.admins!= null && organ.admins.admins.contains(uid)) || uid.equals(Constants().pass)) && organ.drivers.isNotEmpty()){
                view_drivers_layout.visibility = View.VISIBLE
            }

            view_drivers_button.setOnClickListener {
                Constants().touch_vibrate(context)
                listener.viewDrivers(organ)
            }

        }
        reset_view()

        swipeContainer.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                listener.whenReloadRoutes()
                Constants().touch_vibrate(context)

            }
        })

        refresh.setOnClickListener {
            listener.whenReloadRoutes()
            Constants().touch_vibrate(context)
            swipeContainer.setRefreshing(true)
        }

        onOrganisationReloaded = {
            organ = it
            reset_view()
        }

        if(!routes.isEmpty()){
            view_all_routes_layout.visibility = View.VISIBLE
        }

        view_all_routes_layout_button.setOnClickListener {
            Constants().touch_vibrate(context)
            listener.viewAllRoutes()
        }

        when_route_disabled = { is_disabled: Boolean, route: route ->
            for(item in routes){
                if(item.route_id.equals(route.route_id)){
                    item.disabled = is_disabled
                }
            }
            if(routes.isNotEmpty()){
                created_routes_recyclerview.adapter = RoutesListAdapter()
                created_routes_recyclerview.layoutManager = LinearLayoutManager(context)
            }
        }

        return va
    }

    fun get_mirror_route(route: route): route{
        for(item in routes){
            if(!item.route_id.equals(route.route_id)){
                val start_end = Constants().distance_km(item.set_start_pos!!.latitude,item.set_start_pos!!.longitude,
                    route.set_end_pos!!.latitude,route.set_end_pos!!.longitude)
                Log.e("ViewOrg", "start_end: ${start_end}")

                val end_start =  Constants().distance_km(route.set_start_pos!!.latitude,route.set_start_pos!!.longitude,
                    item.set_end_pos!!.latitude,item.set_end_pos!!.longitude)
                Log.e("ViewOrg", "end_start: ${end_start}")

                if(start_end<=Constants().closeness_limit && end_start<=Constants().closeness_limit){
                    return item
                }
            }
        }
        return route
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
            return routes.size*0
        }

    }

    internal inner class ViewHolderRoutes (view: View) : RecyclerView.ViewHolder(view) {
        val view_layout: RelativeLayout = view.findViewById(R.id.view_layout)
        val creation_time: TextView = view.findViewById(R.id.creation_time)
        val source_text: TextView = view.findViewById(R.id.source_text)
        val destination_text: TextView = view.findViewById(R.id.destination_text)
        val root_cardview: RelativeLayout = view.findViewById(R.id.root_cardview)
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
        fun viewDrivers(organisation: organisation)
        fun viewAllRoutes()
    }

}