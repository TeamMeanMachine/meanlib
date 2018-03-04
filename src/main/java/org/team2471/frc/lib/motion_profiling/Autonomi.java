package org.team2471.frc.lib.motion_profiling;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.HashMap;
import java.util.Map;

public class Autonomi {

    public Map<String, Autonomous> mapAutonomous = new HashMap<>();

    static Moshi moshi = new Moshi.Builder().build();
    static JsonAdapter<Autonomi> jsonAdapter = moshi.adapter(Autonomi.class);
    private static NetworkTableInstance networkTableInstance = NetworkTableInstance.create();

    public Autonomous get(String name) {
        return mapAutonomous.get(name);
    }

    public void put(Autonomous autonomous) {
        mapAutonomous.put(autonomous.name, autonomous);
    }

    public Path2D getPath(String autoName, String pathName) {
        Autonomous autonomous = get(autoName);
        return autonomous.get(pathName);
    }

    public String toJsonString() {
        String json = jsonAdapter.toJson(this);
        System.out.println(json);
        return json;
    }

    static public Autonomi fromJsonString(String json) {
        Autonomi autonomi = null;
        try {
            autonomi = jsonAdapter.fromJson(json);
        } catch (Exception e) {
            System.out.println("Constructing Autonomi class from json failed.");
            return null;
        }

        if (autonomi != null)
            autonomi.fixUpTailAndPrevPointers();

        return autonomi;
    }

    private void fixUpTailAndPrevPointers() {
        for (Map.Entry<String, Autonomous> entry : mapAutonomous.entrySet()) {
            entry.getValue().fixUpTailAndPrevPointers();
        }
    }
}
