// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

import java.util.HashMap;
import java.util.Map;
import org.littletonrobotics.junction.MeanLogDataReceiver;
import org.littletonrobotics.junction.MeanLogTable;
import org.wpilib.networktables.*;

/** Publishes log data using NT4. */
public class MeanNT4Publisher implements MeanLogDataReceiver {
    private final NetworkTable akitTable;
    private MeanLogTable lastTable = new MeanLogTable(0);
    private final IntegerPublisher timestampPublisher;
    private final Map<String, GenericPublisher> publishers = new HashMap<>();

    /** Creates a new NT4Publisher. */
    public MeanNT4Publisher() {
        akitTable = NetworkTableInstance.getDefault().getTable("/AdvantageKit");
        timestampPublisher =
                akitTable.getIntegerTopic(timestampKey.substring(1)).publish(PubSubOption.SEND_ALL);
    }

    public void putTable(MeanLogTable table) {
        // Send timestamp
        timestampPublisher.set(table.getTimestamp(), table.getTimestamp());

        // Get old and new data
        Map<String, MeanLogTable.LogValue> newMap = table.getAll(false);
        Map<String, MeanLogTable.LogValue> oldMap = lastTable.getAll(false);

        // Encode new/changed fields
        for (Map.Entry<String, MeanLogTable.LogValue> field : newMap.entrySet()) {
            // Check if field has changed
            MeanLogTable.LogValue newValue = field.getValue();
            if (newValue.equals(oldMap.get(field.getKey()))) {
                continue;
            }

            // Create publisher if necessary
            String key = field.getKey().substring(1);
            GenericPublisher publisher = publishers.get(key);
            if (publisher == null) {
                publisher =
                        akitTable
                                .getTopic(key)
                                .genericPublish(field.getValue().getNT4Type(), PubSubOption.SEND_ALL);
                publishers.put(key, publisher);
            }

            // Write new data
            switch (field.getValue().type) {
                case Raw:
                    publisher.setRaw(field.getValue().getRaw(), table.getTimestamp());
                    break;
                case Boolean:
                    publisher.setBoolean(field.getValue().getBoolean(), table.getTimestamp());
                    break;
                case BooleanArray:
                    publisher.setBooleanArray(field.getValue().getBooleanArray(), table.getTimestamp());
                    break;
                case Integer:
                    publisher.setInteger(field.getValue().getInteger(), table.getTimestamp());
                    break;
                case IntegerArray:
                    publisher.setIntegerArray(field.getValue().getIntegerArray(), table.getTimestamp());
                    break;
                case Float:
                    publisher.setFloat(field.getValue().getFloat(), table.getTimestamp());
                    break;
                case FloatArray:
                    publisher.setFloatArray(field.getValue().getFloatArray(), table.getTimestamp());
                    break;
                case Double:
                    publisher.setDouble(field.getValue().getDouble(), table.getTimestamp());
                    break;
                case DoubleArray:
                    publisher.setDoubleArray(field.getValue().getDoubleArray(), table.getTimestamp());
                    break;
                case String:
                    publisher.setString(field.getValue().getString(), table.getTimestamp());
                    break;
                case StringArray:
                    publisher.setStringArray(field.getValue().getStringArray(), table.getTimestamp());
                    break;
            }
        }

        // Update last table
        lastTable = table;
    }
}
