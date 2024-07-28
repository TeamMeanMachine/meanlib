//LimelightHelpers v1.9 (REQUIRES 2024.9.1)
package org.team2471.frc.lib.vision

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import edu.wpi.first.math.geometry.*
import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.DoubleArrayEntry
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object LimelightHelpers {
    private val doubleArrayEntries: MutableMap<String, DoubleArrayEntry> = ConcurrentHashMap()


    private var mapper: ObjectMapper? = null

    /**
     * Print JSON Parse time to the console in milliseconds
     */
    var profileJSON: Boolean = false

    fun sanitizeName(name: String?): String {
        if (name === "" || name == null) {
            return "limelight"
        }
        return name
    }

    fun toPose3D(inData: DoubleArray): Pose3d {
        if (inData.size < 6) {
            //System.err.println("Bad LL 3D Pose Data!");
            return Pose3d()
        }
        return Pose3d(
            Translation3d(inData[0], inData[1], inData[2]),
            Rotation3d(
                Units.degreesToRadians(inData[3]), Units.degreesToRadians(
                    inData[4]
                ),
                Units.degreesToRadians(inData[5])
            )
        )
    }

    fun toPose2D(inData: DoubleArray): Pose2d {
        if (inData.size < 6) {
            //System.err.println("Bad LL 2D Pose Data!");
            return Pose2d()
        }
        val tran2d = Translation2d(inData[0], inData[1])
        val r2d = Rotation2d(Units.degreesToRadians(inData[5]))
        return Pose2d(tran2d, r2d)
    }

    /**
     * Converts a Pose3d object to an array of doubles.
     *
     * @param pose The Pose3d object to convert.
     * @return The array of doubles representing the pose.
     */
    fun pose3dToArray(pose: Pose3d): DoubleArray {
        val result = DoubleArray(6)
        result[0] = pose.translation.x
        result[1] = pose.translation.y
        result[2] = pose.translation.z
        result[3] = Units.radiansToDegrees(pose.rotation.x)
        result[4] = Units.radiansToDegrees(pose.rotation.y)
        result[5] = Units.radiansToDegrees(pose.rotation.z)
        return result
    }

    /**
     * Converts a Pose2d object to an array of doubles.
     *
     * @param pose The Pose2d object to convert.
     * @return The array of doubles representing the pose.
     */
    fun pose2dToArray(pose: Pose2d): DoubleArray {
        val result = DoubleArray(6)
        result[0] = pose.translation.x
        result[1] = pose.translation.y
        result[2] = 0.0
        result[3] = Units.radiansToDegrees(0.0)
        result[4] = Units.radiansToDegrees(0.0)
        result[5] = Units.radiansToDegrees(pose.rotation.radians)
        return result
    }

    private fun extractArrayEntry(inData: DoubleArray, position: Int): Double {
        if (inData.size < position + 1) {
            return 0.0
        }
        return inData[position]
    }

    private fun getBotPoseEstimate(limelightName: String, entryName: String): PoseEstimate? {
        val poseEntry = getLimelightDoubleArrayEntry(limelightName, entryName)

        val tsValue = poseEntry.atomic
        val poseArray = tsValue.value
        val timestamp = tsValue.timestamp

        if (poseArray.size == 0) {
            // Handle the case where no data is available
            return null // or some default PoseEstimate
        }

        val pose = toPose2D(poseArray)
        val latency = extractArrayEntry(poseArray, 6)
        val tagCount = extractArrayEntry(poseArray, 7).toInt()
        val tagSpan = extractArrayEntry(poseArray, 8)
        val tagDist = extractArrayEntry(poseArray, 9)
        val tagArea = extractArrayEntry(poseArray, 10)

        // Convert server timestamp from microseconds to seconds and adjust for latency
        val adjustedTimestamp = (timestamp / 1000000.0) - (latency / 1000.0)

        val rawFiducials = arrayOfNulls<RawFiducial>(tagCount)
        val valsPerFiducial = 7
        val expectedTotalVals = 11 + valsPerFiducial * tagCount

        if (poseArray.size != expectedTotalVals) {
            // Don't populate fiducials
        } else {
            for (i in 0 until tagCount) {
                val baseIndex = 11 + (i * valsPerFiducial)
                val id = poseArray[baseIndex].toInt()
                val txnc = poseArray[baseIndex + 1]
                val tync = poseArray[baseIndex + 2]
                val ta = poseArray[baseIndex + 3]
                val distToCamera = poseArray[baseIndex + 4]
                val distToRobot = poseArray[baseIndex + 5]
                val ambiguity = poseArray[baseIndex + 6]
                rawFiducials[i] = RawFiducial(id, txnc, tync, ta, distToCamera, distToRobot, ambiguity)
            }
        }

        return PoseEstimate(pose, adjustedTimestamp, latency, tagCount, tagSpan, tagDist, tagArea, rawFiducials)
    }

    private fun getRawFiducials(limelightName: String): Array<RawFiducial?> {
        val entry = getLimelightNTTableEntry(limelightName, "rawfiducials")
        val rawFiducialArray = entry.getDoubleArray(DoubleArray(0))
        val valsPerEntry = 7
        if (rawFiducialArray.size % valsPerEntry != 0) {
            return arrayOfNulls(0)
        }

        val numFiducials = rawFiducialArray.size / valsPerEntry
        val rawFiducials = arrayOfNulls<RawFiducial>(numFiducials)

        for (i in 0 until numFiducials) {
            val baseIndex = i * valsPerEntry
            val id = extractArrayEntry(rawFiducialArray, baseIndex).toInt()
            val txnc = extractArrayEntry(rawFiducialArray, baseIndex + 1)
            val tync = extractArrayEntry(rawFiducialArray, baseIndex + 2)
            val ta = extractArrayEntry(rawFiducialArray, baseIndex + 3)
            val distToCamera = extractArrayEntry(rawFiducialArray, baseIndex + 4)
            val distToRobot = extractArrayEntry(rawFiducialArray, baseIndex + 5)
            val ambiguity = extractArrayEntry(rawFiducialArray, baseIndex + 6)

            rawFiducials[i] = RawFiducial(id, txnc, tync, ta, distToCamera, distToRobot, ambiguity)
        }

        return rawFiducials
    }

    fun getRawDetections(limelightName: String?): Array<RawDetection?> {
        val entry = getLimelightNTTableEntry(limelightName, "rawdetections")
        val rawDetectionArray = entry.getDoubleArray(DoubleArray(0))
        val valsPerEntry = 11
        if (rawDetectionArray.size % valsPerEntry != 0) {
            return arrayOfNulls(0)
        }

        val numDetections = rawDetectionArray.size / valsPerEntry
        val rawDetections = arrayOfNulls<RawDetection>(numDetections)

        for (i in 0 until numDetections) {
            val baseIndex = i * valsPerEntry // Starting index for this detection's data
            val classId = extractArrayEntry(rawDetectionArray, baseIndex).toInt()
            val txnc = extractArrayEntry(rawDetectionArray, baseIndex + 1)
            val tync = extractArrayEntry(rawDetectionArray, baseIndex + 2)
            val ta = extractArrayEntry(rawDetectionArray, baseIndex + 3)
            val corner0_X = extractArrayEntry(rawDetectionArray, baseIndex + 4)
            val corner0_Y = extractArrayEntry(rawDetectionArray, baseIndex + 5)
            val corner1_X = extractArrayEntry(rawDetectionArray, baseIndex + 6)
            val corner1_Y = extractArrayEntry(rawDetectionArray, baseIndex + 7)
            val corner2_X = extractArrayEntry(rawDetectionArray, baseIndex + 8)
            val corner2_Y = extractArrayEntry(rawDetectionArray, baseIndex + 9)
            val corner3_X = extractArrayEntry(rawDetectionArray, baseIndex + 10)
            val corner3_Y = extractArrayEntry(rawDetectionArray, baseIndex + 11)

            rawDetections[i] = RawDetection(
                classId,
                txnc,
                tync,
                ta,
                corner0_X,
                corner0_Y,
                corner1_X,
                corner1_Y,
                corner2_X,
                corner2_Y,
                corner3_X,
                corner3_Y
            )
        }

        return rawDetections
    }

    fun printPoseEstimate(pose: PoseEstimate?) {
        if (pose == null) {
            println("No PoseEstimate available.")
            return
        }

        System.out.printf("Pose Estimate Information:%n")
        System.out.printf("Timestamp (Seconds): %.3f%n", pose.timestampSeconds)
        System.out.printf("Latency: %.3f ms%n", pose.latency)
        System.out.printf("Tag Count: %d%n", pose.tagCount)
        System.out.printf("Tag Span: %.2f meters%n", pose.tagSpan)
        System.out.printf("Average Tag Distance: %.2f meters%n", pose.avgTagDist)
        System.out.printf("Average Tag Area: %.2f%% of image%n", pose.avgTagArea)
        println()

        if (pose.rawFiducials == null || pose.rawFiducials!!.size == 0) {
            println("No RawFiducials data available.")
            return
        }

        println("Raw Fiducials Details:")
        for (i in pose.rawFiducials!!.indices) {
            val fiducial = pose.rawFiducials!![i]
            System.out.printf(" Fiducial #%d:%n", i + 1)
            System.out.printf("  ID: %d%n", fiducial!!.id)
            System.out.printf("  TXNC: %.2f%n", fiducial.txnc)
            System.out.printf("  TYNC: %.2f%n", fiducial.tync)
            System.out.printf("  TA: %.2f%n", fiducial.ta)
            System.out.printf("  Distance to Camera: %.2f meters%n", fiducial.distToCamera)
            System.out.printf("  Distance to Robot: %.2f meters%n", fiducial.distToRobot)
            System.out.printf("  Ambiguity: %.2f%n", fiducial.ambiguity)
            println()
        }
    }

    fun getLimelightNTTable(tableName: String?): NetworkTable {
        return NetworkTableInstance.getDefault().getTable(sanitizeName(tableName))
    }

    fun Flush() {
        NetworkTableInstance.getDefault().flush()
    }

    fun getLimelightNTTableEntry(tableName: String?, entryName: String?): NetworkTableEntry {
        return getLimelightNTTable(tableName).getEntry(entryName)
    }

    fun getLimelightDoubleArrayEntry(tableName: String, entryName: String): DoubleArrayEntry {
        val key = "$tableName/$entryName"
        return doubleArrayEntries.computeIfAbsent(
            key
        ) { k: String? ->
            val table = getLimelightNTTable(tableName)
            table.getDoubleArrayTopic(entryName).getEntry(DoubleArray(0))
        }
    }

    fun getLimelightNTDouble(tableName: String?, entryName: String?): Double {
        return getLimelightNTTableEntry(tableName, entryName).getDouble(0.0)
    }

    fun setLimelightNTDouble(tableName: String?, entryName: String?, `val`: Double) {
        getLimelightNTTableEntry(tableName, entryName).setDouble(`val`)
    }

    fun setLimelightNTDoubleArray(tableName: String?, entryName: String?, `val`: DoubleArray?) {
        getLimelightNTTableEntry(tableName, entryName).setDoubleArray(`val`)
    }

    fun getLimelightNTDoubleArray(tableName: String?, entryName: String?): DoubleArray {
        return getLimelightNTTableEntry(tableName, entryName).getDoubleArray(DoubleArray(0))
    }


    fun getLimelightNTString(tableName: String?, entryName: String?): String {
        return getLimelightNTTableEntry(tableName, entryName).getString("")
    }

    fun getLimelightNTStringArray(tableName: String?, entryName: String?): Array<String> {
        return getLimelightNTTableEntry(tableName, entryName).getStringArray(arrayOfNulls(0))
    }


    fun getLimelightURLString(tableName: String?, request: String): URL? {
        val urlString = "http://" + sanitizeName(tableName) + ".local:5807/" + request
        val url: URL
        try {
            url = URL(urlString)
            return url
        } catch (e: MalformedURLException) {
            System.err.println("bad LL URL")
        }
        return null
    }

    /////
    /////
    fun getTX(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tx")
    }

    fun getTY(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "ty")
    }

    fun getTA(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "ta")
    }

    fun getT2DArray(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "t2d")
    }


    fun getTargetCount(limelightName: String?): Int {
        val t2d = getT2DArray(limelightName)
        if (t2d.size == 17) {
            return t2d[1].toInt()
        }
        return 0
    }

    fun getClassifierClassIndex(limelightName: String?): Int {
        val t2d = getT2DArray(limelightName)
        if (t2d.size == 17) {
            return t2d[10].toInt()
        }
        return 0
    }

    fun getDetectorClassIndex(limelightName: String?): Int {
        val t2d = getT2DArray(limelightName)
        if (t2d.size == 17) {
            return t2d[11].toInt()
        }
        return 0
    }

    fun getClassifierClass(limelightName: String?): String {
        return getLimelightNTString(limelightName, "tcclass")
    }

    fun getDetectorClass(limelightName: String?): String {
        return getLimelightNTString(limelightName, "tdclass")
    }


    fun getLatency_Pipeline(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tl")
    }

    fun getLatency_Capture(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "cl")
    }

    fun getCurrentPipelineIndex(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "getpipe")
    }

    fun getCurrentPipelineType(limelightName: String?): String {
        return getLimelightNTString(limelightName, "getpipetype")
    }

    fun getJSONDump(limelightName: String?): String {
        return getLimelightNTString(limelightName, "json")
    }

    /**
     * Switch to getBotPose
     *
     * @param limelightName
     * @return
     */
    @Deprecated("")
    fun getBotpose(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose")
    }

    /**
     * Switch to getBotPose_wpiRed
     *
     * @param limelightName
     * @return
     */
    @Deprecated("")
    fun getBotpose_wpiRed(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpired")
    }

    /**
     * Switch to getBotPose_wpiBlue
     *
     * @param limelightName
     * @return
     */
    @Deprecated("")
    fun getBotpose_wpiBlue(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpiblue")
    }

    fun getBotPose(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose")
    }

    fun getBotPose_wpiRed(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpired")
    }

    fun getBotPose_wpiBlue(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpiblue")
    }

    fun getBotPose_TargetSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_targetspace")
    }

    fun getCameraPose_TargetSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "camerapose_targetspace")
    }

    fun getTargetPose_CameraSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "targetpose_cameraspace")
    }

    fun getTargetPose_RobotSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "targetpose_robotspace")
    }

    fun getTargetColor(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "tc")
    }

    fun getFiducialID(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tid")
    }

    fun getNeuralClassID(limelightName: String?): String {
        return getLimelightNTString(limelightName, "tclass")
    }

    fun getRawBarcodeData(limelightName: String?): Array<String> {
        return getLimelightNTStringArray(limelightName, "rawbarcodes")
    }

    /////
    /////
    fun getBotPose3d(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose")
        return toPose3D(poseArray)
    }

    fun getBotPose3d_wpiRed(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose_wpired")
        return toPose3D(poseArray)
    }

    fun getBotPose3d_wpiBlue(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose_wpiblue")
        return toPose3D(poseArray)
    }

    fun getBotPose3d_TargetSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose_targetspace")
        return toPose3D(poseArray)
    }

    fun getCameraPose3d_TargetSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "camerapose_targetspace")
        return toPose3D(poseArray)
    }

    fun getTargetPose3d_CameraSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "targetpose_cameraspace")
        return toPose3D(poseArray)
    }

    fun getTargetPose3d_RobotSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "targetpose_robotspace")
        return toPose3D(poseArray)
    }

    fun getCameraPose3d_RobotSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "camerapose_robotspace")
        return toPose3D(poseArray)
    }

    /**
     * Gets the Pose2d for easy use with Odometry vision pose estimator
     * (addVisionMeasurement)
     *
     * @param limelightName
     * @return
     */
    fun getBotPose2d_wpiBlue(limelightName: String?): Pose2d {
        val result = getBotPose_wpiBlue(limelightName)
        return toPose2D(result)
    }

    /**
     * Gets the Pose2d and timestamp for use with WPILib pose estimator (addVisionMeasurement) when you are on the BLUE
     * alliance
     *
     * @param limelightName
     * @return
     */
    fun getBotPoseEstimate_wpiBlue(limelightName: String): PoseEstimate? {
        return getBotPoseEstimate(limelightName, "botpose_wpiblue")
    }

    /**
     * Gets the Pose2d and timestamp for use with WPILib pose estimator (addVisionMeasurement) when you are on the BLUE
     * alliance
     *
     * @param limelightName
     * @return
     */
    fun getBotPoseEstimate_wpiBlue_MegaTag2(limelightName: String): PoseEstimate? {
        return getBotPoseEstimate(limelightName, "botpose_orb_wpiblue")
    }

    /**
     * Gets the Pose2d for easy use with Odometry vision pose estimator
     * (addVisionMeasurement)
     *
     * @param limelightName
     * @return
     */
    fun getBotPose2d_wpiRed(limelightName: String?): Pose2d {
        val result = getBotPose_wpiRed(limelightName)
        return toPose2D(result)
    }

    /**
     * Gets the Pose2d and timestamp for use with WPILib pose estimator (addVisionMeasurement) when you are on the RED
     * alliance
     * @param limelightName
     * @return
     */
    fun getBotPoseEstimate_wpiRed(limelightName: String): PoseEstimate? {
        return getBotPoseEstimate(limelightName, "botpose_wpired")
    }

    /**
     * Gets the Pose2d and timestamp for use with WPILib pose estimator (addVisionMeasurement) when you are on the RED
     * alliance
     * @param limelightName
     * @return
     */
    fun getBotPoseEstimate_wpiRed_MegaTag2(limelightName: String): PoseEstimate? {
        return getBotPoseEstimate(limelightName, "botpose_orb_wpired")
    }

    /**
     * Gets the Pose2d for easy use with Odometry vision pose estimator
     * (addVisionMeasurement)
     *
     * @param limelightName
     * @return
     */
    fun getBotPose2d(limelightName: String?): Pose2d {
        val result = getBotPose(limelightName)
        return toPose2D(result)
    }

    fun getTV(limelightName: String?): Boolean {
        return 1.0 == getLimelightNTDouble(limelightName, "tv")
    }

    /////
    /////
    fun setPipelineIndex(limelightName: String?, pipelineIndex: Int) {
        setLimelightNTDouble(limelightName, "pipeline", pipelineIndex.toDouble())
    }


    fun setPriorityTagID(limelightName: String?, ID: Int) {
        setLimelightNTDouble(limelightName, "priorityid", ID.toDouble())
    }

    /**
     * The LEDs will be controlled by Limelight pipeline settings, and not by robot
     * code.
     */
    fun setLEDMode_PipelineControl(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 0.0)
    }

    fun setLEDMode_ForceOff(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 1.0)
    }

    fun setLEDMode_ForceBlink(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 2.0)
    }

    fun setLEDMode_ForceOn(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 3.0)
    }

    fun setStreamMode_Standard(limelightName: String?) {
        setLimelightNTDouble(limelightName, "stream", 0.0)
    }

    fun setStreamMode_PiPMain(limelightName: String?) {
        setLimelightNTDouble(limelightName, "stream", 1.0)
    }

    fun setStreamMode_PiPSecondary(limelightName: String?) {
        setLimelightNTDouble(limelightName, "stream", 2.0)
    }


    /**
     * Sets the crop window. The crop window in the UI must be completely open for
     * dynamic cropping to work.
     */
    fun setCropWindow(limelightName: String?, cropXMin: Double, cropXMax: Double, cropYMin: Double, cropYMax: Double) {
        val entries = DoubleArray(4)
        entries[0] = cropXMin
        entries[1] = cropXMax
        entries[2] = cropYMin
        entries[3] = cropYMax
        setLimelightNTDoubleArray(limelightName, "crop", entries)
    }

    /**
     * Sets 3D offset point for easy 3D targeting.
     */
    fun setFiducial3DOffset(limelightName: String?, offsetX: Double, offsetY: Double, offsetZ: Double) {
        val entries = DoubleArray(3)
        entries[0] = offsetX
        entries[1] = offsetY
        entries[2] = offsetZ
        setLimelightNTDoubleArray(limelightName, "fiducial_offset_set", entries)
    }

    fun SetRobotOrientation(
        limelightName: String, yaw: Double, yawRate: Double,
        pitch: Double, pitchRate: Double,
        roll: Double, rollRate: Double
    ) {
        SetRobotOrientation_INTERNAL(limelightName, yaw, yawRate, pitch, pitchRate, roll, rollRate, true)
    }

    fun SetRobotOrientation_NoFlush(
        limelightName: String, yaw: Double, yawRate: Double,
        pitch: Double, pitchRate: Double,
        roll: Double, rollRate: Double
    ) {
        SetRobotOrientation_INTERNAL(limelightName, yaw, yawRate, pitch, pitchRate, roll, rollRate, false)
    }

    private fun SetRobotOrientation_INTERNAL(
        limelightName: String, yaw: Double, yawRate: Double,
        pitch: Double, pitchRate: Double,
        roll: Double, rollRate: Double, flush: Boolean
    ) {
        val entries = DoubleArray(6)
        entries[0] = yaw
        entries[1] = yawRate
        entries[2] = pitch
        entries[3] = pitchRate
        entries[4] = roll
        entries[5] = rollRate
        setLimelightNTDoubleArray(limelightName, "robot_orientation_set", entries)
        if (flush) {
            Flush()
        }
    }


    fun SetFidcuial3DOffset(
        limelightName: String?, x: Double, y: Double,
        z: Double
    ) {
        val entries = DoubleArray(3)
        entries[0] = x
        entries[1] = y
        entries[2] = z
        setLimelightNTDoubleArray(limelightName, "fiducial_offset_set", entries)
    }

    fun SetFiducialIDFiltersOverride(limelightName: String?, validIDs: IntArray) {
        val validIDsDouble = DoubleArray(validIDs.size)
        for (i in validIDs.indices) {
            validIDsDouble[i] = validIDs[i].toDouble()
        }
        setLimelightNTDoubleArray(limelightName, "fiducial_id_filters_set", validIDsDouble)
    }

    fun SetFiducialDownscalingOverride(limelightName: String?, downscale: Float) {
        var d = 0 // pipeline
        if (downscale.toDouble() == 1.0) {
            d = 1
        }
        if (downscale.toDouble() == 1.5) {
            d = 2
        }
        if (downscale == 2f) {
            d = 3
        }
        if (downscale == 3f) {
            d = 4
        }
        if (downscale == 4f) {
            d = 5
        }
        setLimelightNTDouble(limelightName, "fiducial_downscale_set", d.toDouble())
    }

    fun setCameraPose_RobotSpace(
        limelightName: String?,
        forward: Double,
        side: Double,
        up: Double,
        roll: Double,
        pitch: Double,
        yaw: Double
    ) {
        val entries = DoubleArray(6)
        entries[0] = forward
        entries[1] = side
        entries[2] = up
        entries[3] = roll
        entries[4] = pitch
        entries[5] = yaw
        setLimelightNTDoubleArray(limelightName, "camerapose_robotspace_set", entries)
    }

    /////
    /////
    fun setPythonScriptData(limelightName: String?, outgoingPythonData: DoubleArray?) {
        setLimelightNTDoubleArray(limelightName, "llrobot", outgoingPythonData)
    }

    fun getPythonScriptData(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "llpython")
    }

    /////
    /////
    /**
     * Asynchronously take snapshot.
     */
    fun takeSnapshot(tableName: String, snapshotName: String?): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            SYNCH_TAKESNAPSHOT(
                tableName,
                snapshotName
            )
        }
    }

    private fun SYNCH_TAKESNAPSHOT(tableName: String, snapshotName: String?): Boolean {
        val url = getLimelightURLString(tableName, "capturesnapshot")
        try {
            val connection = url!!.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            if (snapshotName != null && snapshotName !== "") {
                connection.setRequestProperty("snapname", snapshotName)
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                return true
            } else {
                System.err.println("Bad LL Request")
            }
        } catch (e: IOException) {
            System.err.println(e.message)
        }
        return false
    }

    /**
     * Parses Limelight's JSON results dump into a LimelightResults Object
     */
    fun getLatestResults(limelightName: String?): LimelightResults {
        val start = System.nanoTime()
        var results = LimelightResults()
        if (mapper == null) {
            mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        try {
            results = mapper!!.readValue(getJSONDump(limelightName), LimelightResults::class.java)
        } catch (e: JsonProcessingException) {
            results.error = "lljson error: " + e.message
        }

        val end = System.nanoTime()
        val millis = (end - start) * .000001
        results.latency_jsonParse = millis
        if (profileJSON) {
            System.out.printf("lljson: %.2f\r\n", millis)
        }

        return results
    }

    class LimelightTarget_Retro {
        @JsonProperty("t6c_ts")
        private val cameraPose_TargetSpace = DoubleArray(6)

        @JsonProperty("t6r_fs")
        private val robotPose_FieldSpace = DoubleArray(6)

        @JsonProperty("t6r_ts")
        private val robotPose_TargetSpace = DoubleArray(6)

        @JsonProperty("t6t_cs")
        private val targetPose_CameraSpace = DoubleArray(6)

        @JsonProperty("t6t_rs")
        private val targetPose_RobotSpace = DoubleArray(6)

        fun getCameraPose_TargetSpace(): Pose3d {
            return toPose3D(cameraPose_TargetSpace)
        }

        fun getRobotPose_FieldSpace(): Pose3d {
            return toPose3D(robotPose_FieldSpace)
        }

        fun getRobotPose_TargetSpace(): Pose3d {
            return toPose3D(robotPose_TargetSpace)
        }

        fun getTargetPose_CameraSpace(): Pose3d {
            return toPose3D(targetPose_CameraSpace)
        }

        fun getTargetPose_RobotSpace(): Pose3d {
            return toPose3D(targetPose_RobotSpace)
        }

        val cameraPose_TargetSpace2D: Pose2d
            get() = toPose2D(cameraPose_TargetSpace)
        val robotPose_FieldSpace2D: Pose2d
            get() = toPose2D(robotPose_FieldSpace)
        val robotPose_TargetSpace2D: Pose2d
            get() = toPose2D(robotPose_TargetSpace)
        val targetPose_CameraSpace2D: Pose2d
            get() = toPose2D(targetPose_CameraSpace)
        val targetPose_RobotSpace2D: Pose2d
            get() = toPose2D(targetPose_RobotSpace)

        @JsonProperty("ta")
        var ta: Double = 0.0

        @JsonProperty("tx")
        var tx: Double = 0.0

        @JsonProperty("txp")
        var tx_pixels: Double = 0.0

        @JsonProperty("ty")
        var ty: Double = 0.0

        @JsonProperty("typ")
        var ty_pixels: Double = 0.0

        @JsonProperty("ts")
        var ts: Double = 0.0
    }

    class LimelightTarget_Fiducial {
        @JsonProperty("fID")
        var fiducialID: Double = 0.0

        @JsonProperty("fam")
        var fiducialFamily: String? = null

        @JsonProperty("t6c_ts")
        private val cameraPose_TargetSpace = DoubleArray(6)

        @JsonProperty("t6r_fs")
        private val robotPose_FieldSpace = DoubleArray(6)

        @JsonProperty("t6r_ts")
        private val robotPose_TargetSpace = DoubleArray(6)

        @JsonProperty("t6t_cs")
        private val targetPose_CameraSpace = DoubleArray(6)

        @JsonProperty("t6t_rs")
        private val targetPose_RobotSpace = DoubleArray(6)

        fun getCameraPose_TargetSpace(): Pose3d {
            return toPose3D(cameraPose_TargetSpace)
        }

        fun getRobotPose_FieldSpace(): Pose3d {
            return toPose3D(robotPose_FieldSpace)
        }

        fun getRobotPose_TargetSpace(): Pose3d {
            return toPose3D(robotPose_TargetSpace)
        }

        fun getTargetPose_CameraSpace(): Pose3d {
            return toPose3D(targetPose_CameraSpace)
        }

        fun getTargetPose_RobotSpace(): Pose3d {
            return toPose3D(targetPose_RobotSpace)
        }

        val cameraPose_TargetSpace2D: Pose2d
            get() = toPose2D(cameraPose_TargetSpace)
        val robotPose_FieldSpace2D: Pose2d
            get() = toPose2D(robotPose_FieldSpace)
        val robotPose_TargetSpace2D: Pose2d
            get() = toPose2D(robotPose_TargetSpace)
        val targetPose_CameraSpace2D: Pose2d
            get() = toPose2D(targetPose_CameraSpace)
        val targetPose_RobotSpace2D: Pose2d
            get() = toPose2D(targetPose_RobotSpace)

        @JsonProperty("ta")
        var ta: Double = 0.0

        @JsonProperty("tx")
        var tx: Double = 0.0

        @JsonProperty("txp")
        var tx_pixels: Double = 0.0

        @JsonProperty("ty")
        var ty: Double = 0.0

        @JsonProperty("typ")
        var ty_pixels: Double = 0.0

        @JsonProperty("ts")
        var ts: Double = 0.0
    }

    class LimelightTarget_Barcode

    class LimelightTarget_Classifier {
        @JsonProperty("class")
        var className: String? = null

        @JsonProperty("classID")
        var classID: Double = 0.0

        @JsonProperty("conf")
        var confidence: Double = 0.0

        @JsonProperty("zone")
        var zone: Double = 0.0

        @JsonProperty("tx")
        var tx: Double = 0.0

        @JsonProperty("txp")
        var tx_pixels: Double = 0.0

        @JsonProperty("ty")
        var ty: Double = 0.0

        @JsonProperty("typ")
        var ty_pixels: Double = 0.0
    }

    class LimelightTarget_Detector {
        @JsonProperty("class")
        var className: String? = null

        @JsonProperty("classID")
        var classID: Double = 0.0

        @JsonProperty("conf")
        var confidence: Double = 0.0

        @JsonProperty("ta")
        var ta: Double = 0.0

        @JsonProperty("tx")
        var tx: Double = 0.0

        @JsonProperty("txp")
        var tx_pixels: Double = 0.0

        @JsonProperty("ty")
        var ty: Double = 0.0

        @JsonProperty("typ")
        var ty_pixels: Double = 0.0
    }

    class LimelightResults {
        var error: String? = null

        @JsonProperty("pID")
        var pipelineID: Double = 0.0

        @JsonProperty("tl")
        var latency_pipeline: Double = 0.0

        @JsonProperty("cl")
        var latency_capture: Double = 0.0

        var latency_jsonParse: Double = 0.0

        @JsonProperty("ts")
        var timestamp_LIMELIGHT_publish: Double = 0.0

        @JsonProperty("ts_rio")
        var timestamp_RIOFPGA_capture: Double = 0.0

        @JsonProperty("v")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        var valid: Boolean = false

        @JsonProperty("botpose")
        var botpose: DoubleArray = DoubleArray(6)

        @JsonProperty("botpose_wpired")
        var botpose_wpired: DoubleArray = DoubleArray(6)

        @JsonProperty("botpose_wpiblue")
        var botpose_wpiblue: DoubleArray = DoubleArray(6)

        @JsonProperty("botpose_tagcount")
        var botpose_tagcount: Double = 0.0

        @JsonProperty("botpose_span")
        var botpose_span: Double = 0.0

        @JsonProperty("botpose_avgdist")
        var botpose_avgdist: Double = 0.0

        @JsonProperty("botpose_avgarea")
        var botpose_avgarea: Double = 0.0

        @JsonProperty("t6c_rs")
        var camerapose_robotspace: DoubleArray = DoubleArray(6)

        val botPose3d: Pose3d
            get() = toPose3D(botpose)

        val botPose3d_wpiRed: Pose3d
            get() = toPose3D(botpose_wpired)

        val botPose3d_wpiBlue: Pose3d
            get() = toPose3D(botpose_wpiblue)

        val botPose2d: Pose2d
            get() = toPose2D(botpose)

        val botPose2d_wpiRed: Pose2d
            get() = toPose2D(botpose_wpired)

        val botPose2d_wpiBlue: Pose2d
            get() = toPose2D(botpose_wpiblue)

        @JsonProperty("Retro")
        var targets_Retro: Array<LimelightTarget_Retro?> = arrayOfNulls(0)

        @JsonProperty("Fiducial")
        var targets_Fiducials: Array<LimelightTarget_Fiducial?> = arrayOfNulls(0)

        @JsonProperty("Classifier")
        var targets_Classifier: Array<LimelightTarget_Classifier?> = arrayOfNulls(0)

        @JsonProperty("Detector")
        var targets_Detector: Array<LimelightTarget_Detector?> = arrayOfNulls(0)

        @JsonProperty("Barcode")
        var targets_Barcode: Array<LimelightTarget_Barcode?> = arrayOfNulls(0)
    }

    class RawFiducial(
        id: Int,
        txnc: Double,
        tync: Double,
        ta: Double,
        distToCamera: Double,
        distToRobot: Double,
        ambiguity: Double
    ) {
        var id: Int = 0
        var txnc: Double = 0.0
        var tync: Double = 0.0
        var ta: Double = 0.0
        var distToCamera: Double = 0.0
        var distToRobot: Double = 0.0
        var ambiguity: Double = 0.0


        init {
            this.id = id
            this.txnc = txnc
            this.tync = tync
            this.ta = ta
            this.distToCamera = distToCamera
            this.distToRobot = distToRobot
            this.ambiguity = ambiguity
        }
    }

    class RawDetection(
        classId: Int, txnc: Double, tync: Double, ta: Double,
        corner0_X: Double, corner0_Y: Double,
        corner1_X: Double, corner1_Y: Double,
        corner2_X: Double, corner2_Y: Double,
        corner3_X: Double, corner3_Y: Double
    ) {
        var classId: Int = 0
        var txnc: Double = 0.0
        var tync: Double = 0.0
        var ta: Double = 0.0
        var corner0_X: Double = 0.0
        var corner0_Y: Double = 0.0
        var corner1_X: Double = 0.0
        var corner1_Y: Double = 0.0
        var corner2_X: Double = 0.0
        var corner2_Y: Double = 0.0
        var corner3_X: Double = 0.0
        var corner3_Y: Double = 0.0


        init {
            this.classId = classId
            this.txnc = txnc
            this.tync = tync
            this.ta = ta
            this.corner0_X = corner0_X
            this.corner0_Y = corner0_Y
            this.corner1_X = corner1_X
            this.corner1_Y = corner1_Y
            this.corner2_X = corner2_X
            this.corner2_Y = corner2_Y
            this.corner3_X = corner3_X
            this.corner3_Y = corner3_Y
        }
    }

    class PoseEstimate {
        var pose: Pose2d
        var timestampSeconds: Double
        var latency: Double
        var tagCount: Int
        var tagSpan: Double
        var avgTagDist: Double
        var avgTagArea: Double
        var rawFiducials: Array<RawFiducial?>?

        /**
         * Makes a PoseEstimate object with default values
         */
        constructor() {
            this.pose = Pose2d()
            this.timestampSeconds = 0.0
            this.latency = 0.0
            this.tagCount = 0
            this.tagSpan = 0.0
            this.avgTagDist = 0.0
            this.avgTagArea = 0.0
            this.rawFiducials = arrayOf()
        }

        constructor(
            pose: Pose2d, timestampSeconds: Double, latency: Double,
            tagCount: Int, tagSpan: Double, avgTagDist: Double,
            avgTagArea: Double, rawFiducials: Array<RawFiducial?>?
        ) {
            this.pose = pose
            this.timestampSeconds = timestampSeconds
            this.latency = latency
            this.tagCount = tagCount
            this.tagSpan = tagSpan
            this.avgTagDist = avgTagDist
            this.avgTagArea = avgTagArea
            this.rawFiducials = rawFiducials
        }
    }
}