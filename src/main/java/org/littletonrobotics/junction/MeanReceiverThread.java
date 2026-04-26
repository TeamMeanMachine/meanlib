// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

class MeanReceiverThread extends Thread {
    private final BlockingQueue<MeanLogTable> queue;
    private List<MeanLogDataReceiver> dataReceivers = new ArrayList<>();

    MeanReceiverThread(BlockingQueue<MeanLogTable> queue) {
        super("AdvantageKit_LogReceiver");
        this.setDaemon(true);
        this.queue = queue;
    }

    void addDataReceiver(MeanLogDataReceiver dataReceiver) {
        dataReceivers.add(dataReceiver);
    }

    public void run() {
        // Start data receivers
        for (int i = 0; i < dataReceivers.size(); i++) {
            dataReceivers.get(i).start();
        }

        try {
            while (true) {
                MeanLogTable entry = queue.take(); // Wait for data

                // Send data to receivers
                for (int i = 0; i < dataReceivers.size(); i++) {
                    dataReceivers.get(i).putTable(entry);
                }
            }
        } catch (InterruptedException exception) {

            // End all data receivers
            for (int i = 0; i < dataReceivers.size(); i++) {
                dataReceivers.get(i).end();
            }
        }
    }
}
