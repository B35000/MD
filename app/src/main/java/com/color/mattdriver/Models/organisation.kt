package com.color.mattdriver.Models

import java.io.Serializable

class organisation(var name: String, var creation_time: Long): Serializable{
    var country: String? = null
    var org_id: String? = null

    class organisation_list(var organisations: ArrayList<organisation>):Serializable
}