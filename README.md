# MeanLib
*A java library for use in FRC*

Team 2471 won the innovation in control award at the world championships last year in the Hopper/Newton division. This was a huge honor, 
so naturally, we decided to step up our software game over the off-season. Linked below is the fruits of our programming team’s 
labor over the last several months.

Included in the library:

* A class for interpreting the output from Magnepot absolute encoders.
  *  This includes the ability to set individual offsets for any given encoder.
* Ability to put permanent variables to SmartDashboard that save themselves to eeprom when disabled.
* A Class for generating cubic interpolated motion profiles for single or multi joint mechanisms.
  * This is designed to be used with PID control on the RoboRio, or on talon SRX. 
  * Setpoint to time spline Curves are generated in the constructor of the command, and 
  exact values are looked up from the robot time each cycle.
A class for generating cubic skid-steer trajectories for autonomous robot navigation. 
This is designed to be used with PID control on the RoboRio, or on talon SRX. 
Key points are entered to define an X-Y path for the robot to follow, 
then an ease versus time curve is generated the same way that arm profiles are made.

## Animations

The following code generates an animation for a two joint arm:
```java
public class ReadyToSpitCommand extends PlayAnimationCommand {
  private final MotionProfileAnimation animation;
  private final MotionProfileCurve shoulderCurve;
  private final MotionProfileCurve elbowCurve;

  public ReadyToSpitCommand() {
    requires(arm);

    animation = new MotionProfileAnimation();
    shoulderCurve = new MotionProfileCurve( arm.shoulderController, animation );
    elbowCurve = new MotionProfileCurve( arm.elbowController, animation );

    shoulderCurve.storeValue( 0.0, 51.0 );
    shoulderCurve.storeValue( 0.5, 44.0 );
    shoulderCurve.storeValue( 0.75, 26.0 );

    elbowCurve.storeValue( 0.0, -110.0 );
    elbowCurve.storeValue( 0.5, -70.0 );
    elbowCurve.storeValue( 0.75, -55.0 );

    setAnimation(animation);
  }

  @Override
  protected void initialize() {
    super.initialize();
    if (arm.shoulderEncoder.pidGet() > 40) {
      setSpeed(1.0);
    } else {
      setSpeed(-0.75);
    }
  }
}
```

Initialize was overridden so that the command could be ran backwards as well.  This can be done by specifying a negative speed.
It can be passed in through the constructor.  In this case it was handled automatically by detecting the current arm position 
via the shoulderEncoder.

### Paths

Creating a trajectory to follow can be as simple as follows:
```java
path.addPointAndTangent(0.0, 0.0, 0.0, 2.0);
path.addPoint(5.0, 5.0);
path.addPointAndTangent(10.0, 0.0, 0.0, -2.0);
path.addPoint(10.0, -7);

path.addEasePoint(0.0, 0.0);
path.addEasePoint(8.0, 1.0);
```

All of the units are in feet.  The tangent when specified represents the direction and distance from the control point that the
handle should be.  Longer handles create softer arcs, like the handles in a drawing package.
You can find an example of a complete path following command [here](https://github.com/TeamMeanMachine/2016BunnyBot/blob/master/Delta/src/main/java/org/team2471/bunnybot/autonomouscommands/DriveArroundCanLeft.java).

This will create the following path:

![Motion path](https://ipfs.pics/ipfs/QmSsspmJQjQhkDp1anz23vPTnLxtseWU1smFELEEvyei73)

The dots are drawn for example only and the spacing will actually be closer together since they are sampled at 50 hz.  The colors of the dots represent robot speed, where green is 10 feet per second and red is zero.
The following is the ease curve, which defines the percentage along the path at a given time:

![Motion path](https://ipfs.pics/ipfs/QmZaRBZRQtWHXmHMLASjbh8A5KTL2p3Eh8cYVZ9YhkxPdD)

By default, the first and last points of the animation curves set the slope to be zero (flat).  This is usually desired so that
the mechanism will smoothly start up and stop.  In the path picture above, you can see that the inside tire moves more slowly.
You can see that the robot starts off slow, speeds up, and ends slowing down to a stop.

This code was used on one off-season robot. It was one of our entries for the BunnyBot competition this fall.
Its name is Thunder McBoomstix. Here is a video of it in action:
https://youtu.be/ez-iOZKoi9Q

Please feel free to look through the repo, post any questions or comments, and use it on your 2017 robots!
More complex examples of its use can be found [here](https://github.com/TeamMeanMachine/2016BunnyBot/tree/master/Delta) 
(Delta was Thunder McBoomstix’s code name before its formal name was chosen).

## Building

A jar file can be built by navigating to the project root with a command line and 
running `gradlew.bat assemble` on windows, or `./gradlew assemble` on Linux and mac.

The assembled jar will appear in the `build/libs` folder.
Official instructions for installing third-party libraries can be found [here](https://wpilib.screenstepslive.com/s/4485/m/13503/l/682619-3rd-party-libraries).

## Contact us

If you are having any issues building or using meanlib, you found a bug, or you have a suggestion, you are absolutely welcome to 
open an issue! We will get back to you quickly.
