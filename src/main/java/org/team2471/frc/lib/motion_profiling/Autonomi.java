package org.team2471.frc.lib.motion_profiling;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.HashMap;
import java.util.Map;

public class Autonomi {

    public Map<String, Autonomous> mapAutonomous = new HashMap<>();

    public Autonomous get(String name) {
        return mapAutonomous.get(name);
    }

    public void put(Autonomous autonomous) {
        mapAutonomous.put(autonomous.name, autonomous);
    }

    public String toJsonString() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Autonomi> jsonAdapter = moshi.adapter(Autonomi.class);
        String json = jsonAdapter.toJson(this);
        System.out.println(json);
        return json;
    }

    static public Autonomi fromJsonString(String json) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Autonomi> jsonAdapter = moshi.adapter(Autonomi.class);

        Autonomi autonomi = null;
        try {
            autonomi = jsonAdapter.fromJson(json);
        }
        catch (Exception e) {
            System.out.println("Constructing Autonomi class from json failed.");
        }
        return autonomi;
    }
}
