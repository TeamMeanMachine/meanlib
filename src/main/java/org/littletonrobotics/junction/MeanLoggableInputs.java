package org.littletonrobotics.junction;

public interface MeanLoggableInputs {
    /**
     * Updates a LogTable with the data to log.
     *
     * @param table The table to which data should be written.
     */
    public void toLog(MeanLogTable table);

    /**
     * Updates data based on a LogTable.
     *
     * @param table The table from which data should be read.
     */
    public void fromLog(MeanLogTable table);
}
