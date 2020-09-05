package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.driver
import com.color.mattdriver.Models.organisation
import com.color.mattdriver.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.recycler_item_driver.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


class Drivers : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private lateinit var organ: organisation
    private lateinit var listener: DriversInterface


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organ = Gson().fromJson(it.getString(ARG_ORGANISATION),organisation::class.java)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is DriversInterface){
            listener = context
        }
    }



    var onOrganisationReloaded: (organisation) -> Unit = {}

    var reset_view: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_drivers, container, false)
        val money: RelativeLayout = va.findViewById(R.id.money)
        val drivers_recyclerview: RecyclerView = va.findViewById(R.id.drivers_recyclerview)

        money.setOnTouchListener { v, event -> true }

        reset_view = {
            drivers_recyclerview.adapter = DriversListAdapter()
            drivers_recyclerview.layoutManager = LinearLayoutManager(context)
        }
        reset_view()

        onOrganisationReloaded = {
            organ = it
            reset_view()
        }

        return va
    }

    internal inner class DriversListAdapter : RecyclerView.Adapter<ViewHolderDrivers>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderDrivers {
            val vh = ViewHolderDrivers(LayoutInflater.from(context).inflate(R.layout.recycler_item_driver, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(v: ViewHolderDrivers, position: Int) {
            val drivr = organ.drivers.get(position)

            if(drivr.driver_id.equals(Constants().pass)){
                v.root.visibility = View.GONE
            }

            v.creation_time.text = "Joined ${Constants().get_formatted_time(drivr.joining_time)}"
            v.coutry_text.text = organ.country

            if(organ.admins!=null && organ.admins.admins.contains(drivr.driver_id)){
                v.admin_switch.isChecked = true

                //admins cant be deactivated or removed here
                v.deactivate_driver_layout.visibility = View.GONE
                v.admin_switch.visibility = View.GONE
            }
            if(organ.deactivated_drivers!=null && organ.deactivated_drivers.deactivated_drivers.contains(drivr.driver_id)){
                v.deactivate_switch.isChecked = true
            }

            v.admin_switch.setOnCheckedChangeListener { compoundButton, b ->
                Constants().touch_vibrate(context)
                listener.whenDriverSetAdmin(organ,drivr,b)
            }
            v.deactivate_switch.setOnCheckedChangeListener { compoundButton, b ->
                Constants().touch_vibrate(context)
                listener.whenDriverDeActivated(organ,drivr,b)
            }

        }

        override fun getItemCount():Int {
            return organ.drivers.size
        }

    }

    internal inner class ViewHolderDrivers (view: View) : RecyclerView.ViewHolder(view) {
        val root: RelativeLayout = view.findViewById(R.id.root)
        val creation_time: TextView = view.findViewById(R.id.creation_time)
        val coutry_text: TextView = view.findViewById(R.id.coutry_text)
        val deactivate_switch: Switch = view.findViewById(R.id.deactivate_switch)
        val deactivate_driver_layout: RelativeLayout = view.findViewById(R.id.deactivate_driver_layout)
        val admin_switch: Switch = view.findViewById(R.id.admin_switch)

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, organisation: String) =
            Drivers().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGANISATION, organisation)
                }
            }
    }

    interface DriversInterface{
        fun whenDriverDeActivated(organ: organisation, drivr: driver, value: Boolean)
        fun whenDriverSetAdmin(organ: organisation, drivr: driver, value: Boolean)

    }

}