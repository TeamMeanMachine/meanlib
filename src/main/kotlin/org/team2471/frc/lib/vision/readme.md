# Vision Guide

*(Last Updated July 21, 2024, by Thatcher Moore)*

Welcome, fellow programmer! This is a guide I (Thatcher Moore) have made to help bring anyone up to speed on our use of vision and keep track of things that need to be done to maintain this complex system. (Before continuing, I would recommend making sure the Markdown plugin is installed and enabled. If the title is big, then it's already installed)

## Overview

In modern years, vision has been crucial for FRC at high levels. Practically every action can use vision, from detecting and picking up game pieces to using AprilTags to guide the robot during Autos. However, vision can be a double-edged sword. It can be too unreliable and/or imprecise for the action, or the benefit gained for the time invested is marginal. Before using vision for a task, I would recommend you pause and think... *Does this **need** vision?* *Will this task work just fine without cameras?*

There are two primary software/hardware packages widely used for vision in FRC. Limelight, which comes as a complete package (Camera, Coprocessor, Software) seems to be the more popular option at the time of writing, mainly due to its simplicity and ease of use. PhotonVision, on the other hand, can be used on any hardware (even a limelight!) and has been used by Team 2471 in the past for AprilTag and object detection. 

The trouble however, is vision is a quickly evolving space. Updates are being pushed to software and created as hardware by the year, and any part of this guide can become way out of date any second. If vision is what you wish to do, you need to stay in the loop. Keep checking ChiefDelphi and stay up to date with updates, so you can stay on top of things and keep this system ever evolving.

## PhotonVision

### Summary

PhotonVision's biggest strength and weakness is its versatility. It can be used with powerful coprocessors like the Orange Pi 5 and wide-angle lenses. However, trying to cater to these different hardware choices can lead to instability and unreliability, like having trouble identifying multiple connected cameras.

### Choosing Hardware

In my opinion, the Orange Pi 5 is the go-to coprocessor for PhotonVision. It is very performant, officially supported, and has a GPU you can leverage for object detection. As a note, the ribbon cable is a different type to the Raspberry PI, so any ribbon cable camera advertised for the Raspberry Pi will ***not*** work with the Orange Pi. To power it, batteries have been fairly reliable ***(MAKE SURE THEY CAN PROVIDE AMPLE AMPERAGE!!!! CHECK THE LABEL ON THE BATTERY!!!)*** We have continually tried wiring up a solution that doesn't require a battery, to mixed success. In an ideal world, I would go with a wired solution, but it takes time away from wiring the robot and has been a difficult choice for us in the past.

In terms of cameras there are two main choices I have heard of; OV9281 & OV2311. Both cameras are Global-Shutter, which means the whole image is captured at once, meaning no image tearing at high speed. No distortion is also a helpful characteristic, as while software can account for it, having no distortion in the image is more accurate. Black and White is also a good quality to look for, as why spend the extra money on a color camera when it is immediately turned to black and white for AprilTag detection. Between these two choices, OV2311 has a higher resolution, which can help with AprilTag detection. OV9281s will work just fine too. Both cameras have swappable lenses, which can be utilized to increase the FOV. (Make sure the lens is non-distortion!)

For limelight, there is very little choice. The classic Limelight 3 a general-purpose camera that can detect both AprilTags and retro reflective tape. A newer version, the Limelight 3G is an AprilTag specific version of the LL3. Limelights can also be paired with a Google Coral for object detection, I would look at their documentation for more information.


## Common Vocabulary

There are many words used with vision that you might not haver herad before or have meanings that may be unclear. These words will be listed here in the following format:

- Name (Short name): Definition


- Position (Pos): A point in space, usually the robot's location, defined with X and Y coordinates.
- Pose (Pose): The position of the robot, along with the robot's heading, defined with X and Y coordinates, as well as a rotation.