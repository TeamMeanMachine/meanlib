package org.team2471.frc.lib.motion_profiling;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.team2471.frc.lib.motion_profiling.following.DrivetrainParameters;
import org.team2471.frc.lib.motion_profiling.following.RobotParameters;

import java.util.LinkedHashMap;
import java.util.Map;

public class Autonomi {
    public RobotParameters robotParameters;
    public DrivetrainParameters drivetrainParameters;

    public Map<String, Autonomous> mapAutonomous = new LinkedHashMap<>();

    private static JsonAdapter<Autonomi> jsonAdapter = new Moshi.Builder()
            .add(DrivetrainParameters.getMoshiAdapter())
            .build()
            .adapter(Autonomi.class).indent("\t");

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
        return jsonAdapter.toJson(this);
    }

    static public Autonomi fromJsonString(String json) {
        Autonomi autonomi;
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

    public void publishToNetworkTables(NetworkTableInstance networkTableInstance) {
        String json = toJsonString();
        NetworkTable table = networkTableInstance.getTable("PathVisualizer");
        NetworkTableEntry entry = table.getEntry("Autonomi");
        entry.setString(json);
    }
}
