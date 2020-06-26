package com.ccasey.getitdone.storage

import android.content.Context
import android.content.SharedPreferences
import com.ccasey.getitdone.model.Run
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferenceHelper(context: Context) {
    val sharedPrefs: SharedPreferences = context.getSharedPreferences("GetItDone", 0)
    val editor: SharedPreferences.Editor = sharedPrefs.edit()

    fun getCurrentRunMetrics(): Run {
        val distance = sharedPrefs.getFloat(CURRENT_RUN_DISTANCE, 0f)
        val duration = sharedPrefs.getLong(CURRENT_RUN_DURATION, 0L)
        val mileSplit = sharedPrefs.getFloat(CURRENT_RUN_MILE_SPLIT, 0f)
        val dateTime = sharedPrefs.getLong(CURRENT_RUN_DATE_TIME, 0L)
        val routeLineXCoords = getList(CURRENT_RUN_ROUTE_LINE_X)
        val routeLineYCoords = getList(CURRENT_RUN_ROUTE_LINE_Y)

        return Run(distance, duration, mileSplit, dateTime, routeLineXCoords, routeLineYCoords)
    }

    fun setCurrentRunMetrics(run: Run) {
        editor.putFloat(CURRENT_RUN_DISTANCE, run.distance)
        editor.putLong(CURRENT_RUN_DURATION, run.duration)
        editor.putFloat(CURRENT_RUN_MILE_SPLIT, run.mileSplit)
        editor.putLong(CURRENT_RUN_DATE_TIME, run.dateTime)
        setList(CURRENT_RUN_ROUTE_LINE_X, run.routeLineX)
        setList(CURRENT_RUN_ROUTE_LINE_Y, run.routeLineY)
        editor.commit();
    }

    fun getDurationLeft(): Long {
        return sharedPrefs.getLong(CURRENT_RUN_DURATION_LEFT, 0L)
    }

    fun setDurationLeft(left: Long) {
        editor.putLong(CURRENT_RUN_DURATION_LEFT, left)
        editor.commit()
    }

    fun getList(key: String): List<Float> {
        val arrayItems: List<Float>
        val serializedObject = sharedPrefs.getString(key, "")
        if (serializedObject != null) {
            val gson = Gson()
            val type =
                object : TypeToken<List<Float>>() {}.type
            arrayItems = gson.fromJson<List<Float>>(serializedObject, type)
            return arrayItems
        }
        return emptyList()
    }

    fun <T> setList(key: String?, list: List<T>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json);
    }

    companion object {
        const val CURRENT_RUN_DISTANCE = "CURRENT_RUN_DISTANCE"
        const val CURRENT_RUN_DURATION = "CURRENT_RUN_DURATION"
        const val CURRENT_RUN_MILE_SPLIT = "CURRENT_RUN_MILE_SPLIT"
        const val CURRENT_RUN_DATE_TIME = "CURRENT_RUN_DATE_TIME"
        const val CURRENT_RUN_ROUTE_LINE_X = "CURRENT_RUN_ROUTE_LINE_X"
        const val CURRENT_RUN_ROUTE_LINE_Y = "CURRENT_RUN_ROUTE_LINE_Y"

        const val CURRENT_RUN_DURATION_LEFT = "CURRENT_RUN_DURATION_LEFT"
    }
}