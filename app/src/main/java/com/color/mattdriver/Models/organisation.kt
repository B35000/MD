package com.color.mattdriver.Models

import java.io.Serializable

class organisation(var name: String, var creation_time: Long): Serializable{
    var country: String? = null
    var org_id: String? = null
    var admins: admin = admin()

    class organisation_list(var organisations: ArrayList<organisation>):Serializable
    class admin(var admins: ArrayList<String> = ArrayList())
}