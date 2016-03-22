package com.bitalino.sensordroid.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by sveinpg on 20.02.16.
 */
public class JSONHelper {

    public static JSONObject construct(int id, int[] ids, Object[] values) {
        // TODO: Check if lenght of ids and values match
        long millis = System.currentTimeMillis();
        //long millis = Calendar.getInstance().getTimeInMillis();
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        millis = millis % 1000;

        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);

        JSONObject res = new JSONObject();
        try {
            res.put("type", "data");
            res.put("id", id);
            res.put("time", time);

            Log.d("JsonHelper", "Id lenght" + ids.length);
            JSONArray data = new JSONArray();
            for (int i = 0; i < ids.length; i++) {
                JSONObject element = new JSONObject();
                Log.d("JsonHelper", "Id " + ids[i] + " value " + values[i]);
                element.put("id", ids[i]);
                element.put("value", values[i]);
                data.put(element);
            }
            res.put("data", data);
        }catch(JSONException je){
            je.printStackTrace();
        }
        return res;
    }

    public static JSONObject metadata(String name, int id, int[] ids, String[] dataTypes,
                                      String[] metrics, String[] descriptions) {

        JSONObject res = new JSONObject();
        try{
            res.put("type", "meta");
            res.put("name", name);
            res.put("id", id);

            JSONArray channels = new JSONArray();
            for(int i = 0; i <ids.length; i++){
                JSONObject element = new JSONObject();
                element.put("id", ids[i]);
                element.put("type", dataTypes[i]);
                element.put("metric", metrics[i]);
                element.put("description", descriptions[i]);
                channels.put(element);
            }
            res.put("channels", channels);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return res;
    }
}
