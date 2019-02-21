<img width="200px" align="right" src="https://team2471.org/wp-content/uploads/2017/08/tmm-logo_new-300x300.png">

# meanlib

Meanlib is a Kotlin coroutine-driven library for use on FRC robots.

## Installation

Add meanlib to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    compile "com.github.TeamMeanMachine:meanlib:SNAPSHOT-frc2019"
    // other dependencies ...
}
```

## Basic Usage

A very basic RobotProgram can be seen below:

```kotlin
object Robot : RobotProgram {
    override suspend fun enable() = println("Enabling robot!")

    override suspend fun disable() = println("Disabling robot!")

    override suspend fun teleop() = println("Beginning teleoperated mode!")

    override suspend fun autonomous() = println("Beginning autonomous mode!")
}

fun main() {
    initializeWpilib()

    runRobotProgram(Robot)
}
```

This program will print "Enabling robot!" followed by "Beginning teleoperated mode!" as soon as it's
enabled in teleop from the Driver Station.

Being driven by Kotlin coroutines, long-running operations can easily be run in parallel with one
another.

```kotlin
object Robot : RobotProgram {
    override suspend fun teleop() {
        GlobalScope.launch(MeanlibDispatcher) {
            parallel({
                // Wait 5 seconds
                delay(5.0)
                println("Hello world!")
            }, {
                for (i in 0..10) {
                    println("Hello $i!")
                    delay(1.0)
                }
            })
        }
    }
}
```

_TODO: More examples coming soon._
