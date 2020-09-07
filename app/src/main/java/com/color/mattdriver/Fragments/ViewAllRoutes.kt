package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.Models.route
import com.color.mattdriver.R
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


class ViewAllRoutes : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ROUTES = "ARG_ROUTES"
    private val ARG_PICKED_ROUTE = "ARG_PICKED_ROUTE"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private lateinit var listener: ViewAllRoutesInterface
    private var routes: ArrayList<route> = ArrayList()
    private lateinit var organ: organisation
    private var set_route : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organ = Gson().fromJson(it.getString(ARG_ORGANISATION),organisation::class.java)
            routes = sorted_routes(Gson().fromJson(it.getString(ARG_ROUTES), route.route_list::class.java).routes)
            set_route = it.getString(ARG_PICKED_ROUTE)!!
        }
    }

    var when_route_data_updated: (routes: ArrayList<route>) -> Unit = {}

    var when_route_picked: (route: String) -> Unit = {}

    var onOrganisationReloaded: (organisation) -> Unit = {}

    var reset_view: () -> Unit = {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is ViewAllRoutesInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_view_all_routes, container, false)
        val title: TextView = va.findViewById(R.id.title)
        val swipeContainer = va.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)
        val created_routes_recyclerview: RecyclerView = va.findViewById(R.id.created_routes_recyclerview)

        val money: RelativeLayout = va.findViewById(R.id.money)
        money.setOnTouchListener { v, event -> true }

        reset_view = {
            if(routes.isNotEmpty()){
                created_routes_recyclerview.adapter = RoutesListAdapter()
                created_routes_recyclerview.layoutManager = LinearLayoutManager(context)
            }
            title.text  = organ.name
            when_route_data_updated = {
                routes = it

                created_routes_recyclerview.adapter = RoutesListAdapter()
                created_routes_recyclerview.layoutManager = LinearLayoutManager(context)
                swipeContainer.setRefreshing(false)
            }

            when_route_picked = {
                set_route = it
                when_route_data_updated(routes)
            }
            if(!set_route.equals("")){
                when_route_picked(set_route)
            }
        }
        reset_view()
        swipeContainer.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                listener.whenViewAllRoutesReloadRoutes()
                Constants().touch_vibrate(context)

            }
        })

        onOrganisationReloaded = {
            organ = it
            reset_view()
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
                listener.whenViewAllRoutesViewRoute(route,organ)
            }
        }

        override fun getItemCount():Int {
            return routes.size
        }

    }

    fun sorted_routes(loaded_routes: ArrayList<route>): ArrayList<route>{
        val sortedList = loaded_routes.sortedWith(compareBy({ it.creation_time }))
        val ar = ArrayList<route>()
        ar.addAll(sortedList.reversed())
        return ar
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
            ViewAllRoutes().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGANISATION, organisation)
                    putString(ARG_ROUTES, routes)
                    putString(ARG_PICKED_ROUTE,picked_route)
                }
            }
    }

    interface ViewAllRoutesInterface{
        fun whenViewAllRoutesReloadRoutes()
        fun whenViewAllRoutesViewRoute(route:route, organisation: organisation)
    }
}