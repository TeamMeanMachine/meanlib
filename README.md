<img width="200px" align="right" src="Team_2471_Logo_Minimal.png">

# Meanlib

Meanlib is a Kotlin FRC Robot utility library made by Team 2471 Mean Machine.



## Features

- **`SwerveDriveSubsystem` class. inherits from CTRE SwerveDrivetrain, includes more features and commands:**
    - Implements Choreo and Autopilot
    - Settable path PID controllers and Autopilot constants
    - Module and gyro disconnection Alerts
    - Commands:
        - `driveToPoint` `driveToAutopilotPoint`
        - `driveAlongChoreoPath`
        - `driveToLine` `joystickDriveAlongLine`


- **WpiLib units library extensions:**
  - Number → Unit: `1.0.inches`, `2.5.degrees` 
  - Unit → Number: `Distance.asFeet`, `Angle.asDegrees` "as" keyword
  - Supports all the same operations and functions as WpiLib units:


- **`PoseLocalizer`:**
    - Interacts with offboarded [ParticleFilter](https://github.com/TeamMeanMachine/ParticleFilter), a program that uses an SIR Particle Filter to combine camera measurements and odometry data
    - Sends vision measurements to ParticleFilter and latency-adjusts the poses it gets back.
    - Custom `PhotonVisionCamera` and `LimelightCamera` classes support simulation/replay
    - Calculates three different poses:
        - `pose`/`fusedPose` Pose2d from ParticleFilter (Global Positioning)
        - `singleTagPose` Simple trig position solver for the closest tag (Local Positioning)
        - `odometryPose` Only the swerve odometry
    - ParticleFilter and PoseLocalizer were originally made by Team 604 and modified by us.


- **`BatteryLogger`:** 
  - logs power usage (in amp-hours and watt-hours) for each subsystem
```kotlin
BatteryLogger.recordCurrent("Shooter Roller", shooterMotor.supplyCurrent.value * 2.0)
```
- **`LoopLogger`:**
    - Logs `sinceReset` (time since `LoopLogger.reset()` was called) and,
    - `period` for loop periods

```kotlin
// First thing in robot periodic:
LoopLogger.reset()
// In subsystem periodic:
LoopLogger.record("Subsystem periodic")
val isConnected = camera.isConnected
LoopLogger.record("After camera check")
```
- **Assortment of Kotlin/WpiLib extension functions:**
    - Math: `Double.deadband()`, `Double.round()`, `Translation2d.normalize()`, etc.
    - Conversions: `Translation2d().toPose2d(heading)`, `ChassisSpeeds().fieldToRobotCentric(heading)`, etc.


- **CTRE device configuration utility functions:** (TalonFX, TalonFXS, CANcoder, Pigeon2)
```kotlin
val hoodMotor = TalonFX(0); val hoodMotorFollower = TalonFX(1)

hoodMotor.applyConfiguration {
    currentLimits(25.0, 30.0, 1.0)
    inverted(true)
    brakeMode()

    s(0.2, StaticFeedforwardSignValue.UseClosedLoopSign)
    p(200.0)
    d(0.0)

    motionMagic(0.75, 5.0)
    remoteCANCoder(hoodEncoder.deviceID, hoodSensorToMechanismRatio)
}
hoodMotor.addFollower(hoodMotorFollower, MotorAlignmentValue.Opposed)

hoodEncoder.setCANCoderAngle(0.0.degrees)
```
- **`LoggedTalonFX` and `LoggedTalonFXS`:**
    - Inherits from the normal TalonFX
    - Includes WpiLib motor sim + motor sim configuration
    - Runs asynchronously via coroutine when calling `brakeMode())` and `coastMode()`, so they are no longer blocking.
    - Automatically logs motor data and supports replay (wip)


- **WpiLib Commands v2 extension functions/syntax shortcuts:**
    - `runOnceCommand`, `sequenceCommand`, etc. instead of `Command.runOnce`, `Command.sequenceCommand`


- **Custom Autonomous manager `Autonomi` class:**
    - Automatically finds all Choreo paths, preloads them, and puts them in a keyed map
```kotlin
override val autoChooser: LoggedDashboardChooser<AutoCommand?> =
    LoggedDashboardChooser<AutoCommand?>("Auto Chooser").apply {
            addOption("8 Foot Straight", AutoCommand(eightFootStraight()) { robotStartPose2d })
        }

private fun eightFootStraight(): Command {
    return Drive.driveAlongChoreoPath(paths["eightFoot"]!!)
}
```

- **[Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines):**
    - This is used in a lot of utilities, and it is also exposed for use in robot code.
```kotlin
// Launch a new thread
GlobalScope.launch {
    // do async task
    periodic(0.02) { // loop at 50hz (0.02 seconds)
        // do periodic task
        this.stop() // exit loop
    }
}
```
- **Other:**
    - `measureTimeFPGA {}` measures code segment execution time.
    - `PDVelocityController` Custom compounding PD controller for velocity control.
    - `NT4NonFMSPublisher` An AdvantageKit `LogDataReceiver` that only publishes to NetworkTables when an FMS isn't connected: `Logger.addDataReceiver(NT4NonFMSPublisher())`

## Installation
Start by creating a standard FRC robot project.

Add meanlib as a submodule inside your robot project:
```shell
git submodule add https://github.com/TeamMeanMachine/meanlib/
```

Checkout the current year's branch:
```shell
git checkout frc[CURRENT_YEAR] # ex: "git checkout frc2026"
```

Add Meanlib to your build.gradle dependencies:
```groovy
dependencies {
    implementation(project(":meanlib"))
    ..
}
```

Add Meanlib to the jar task in your build.gradle:
```groovy
jar {
    dependsOn(':meanlib:jar') // <- add me

    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    from sourceSets.main.allSource
    manifest GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
```

Optional: Use meanlib vendordeps (paste anywhere in build.gradle):
```groovy
// Tell the global WPILib provider to also load vendors from meanlib,
wpi.vendor.loadFrom(project(":meanlib"))
```


