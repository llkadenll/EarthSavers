package com.example.earthsavers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.gms.maps.model.LatLng

class DBhelper(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,
    null, DATABASE_VER) {
    companion object {
        private val DATABASE_VER = 4
        private val DATABASE_NAME = "earthSavers.db"

        //Table
        private val TABLE_NAME = "Places"
        private val COL_ID = "Id"
        private val COL_LONGITUDE = "Longitude"
        private val COL_LATITUDE = "Latitude"
        private val COL_CREATED_AT = "Created_at"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE_QUERY =
            ("CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_LONGITUDE REAL, $COL_LATITUDE REAL, $COL_CREATED_AT INTEGER)")
        db!!.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db!!)
    }

    fun addPlace(position: LatLng){
        val db= this.writableDatabase
        val values = ContentValues()
        values.put(COL_LATITUDE, position.latitude)
        values.put(COL_LONGITUDE, position.longitude)
        values.put(COL_CREATED_AT, System.currentTimeMillis()/1000)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun deletePlace(position: LatLng) {
        val db= this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_NAME+ " WHERE "+ COL_LATITUDE+"=" + position.latitude + " AND " + COL_LONGITUDE + "=" + position.longitude);
    }

    val allPlaces:List<Place>
        get() {
            val places = ArrayList<Place>()
            val selectQuery = "SELECT * FROM $TABLE_NAME"
            val db = this.writableDatabase
            val cursor =  db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do {
                    val place = Place()
                    place.id = cursor.getInt(cursor.getColumnIndex(COL_ID))
                    place.position = LatLng(cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE)))
                    place.created_at = cursor.getInt(cursor.getColumnIndex(COL_CREATED_AT))

                    places.add(place)
                } while (cursor.moveToNext())
            }
            db.close()
            return places
        }
}