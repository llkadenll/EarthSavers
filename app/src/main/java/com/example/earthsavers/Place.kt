package com.example.earthsavers

import com.google.android.gms.maps.model.LatLng
import java.sql.Date

class Place {
    var id : Int = 0
    lateinit var position : LatLng
    var created_at: Int = 0

    constructor(){}

    constructor(id:Int, position: LatLng, created_at: Int){
        this.id = id
        this.position = position
        this.created_at = created_at
    }
}