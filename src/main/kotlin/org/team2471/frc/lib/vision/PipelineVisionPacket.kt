package org.team2471.frc.lib.vision

import org.photonvision.targeting.PhotonTrackedTarget

/** A data class for a pipeline packet.  */
class PipelineVisionPacket(
    /**
     * If the vision packet has valid targets.
     *
     * @return if targets are found.
     */
    val hasTargets: Boolean,
    /**
     * Gets best target.
     *
     * @return the best target.
     */
    val bestTarget: PhotonTrackedTarget?,
    /**
     * Gets targets.
     *
     * @return the targets.
     */
    val targets: MutableList<PhotonTrackedTarget>?,
    /**
     * Gets the capture timestamp in seconds. -1 if the result has no timestamp set.
     *
     * @return the timestamp in seconds.
     */
    val captureTimestamp: Double
)
