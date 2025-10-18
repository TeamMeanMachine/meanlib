package org.team2471.frc.lib.motion_profiling

// CUBIC COEFFICIENTS f(t) = a*t^3 + b*t^2 + c*t + d
class CubicCoefficients1D internal constructor(private val d: Double, p4: Double, private val c: Double, r4: Double) {
    // construct from two values and two tangents (slope)
    //   a     2 -2  1  1   p1
    //   b =  -3  3 -2 -1 * p4
    //   c     0  0  1  0   r1
    //   d     1  0  0  0   r4
    private val a: Double = 2 * d + -2 * p4 + c + r4
    private val b: Double = -3 * d + 3 * p4 + -2 * c + -r4
    var fDValue: Double = 0.0
        private set
    private var fdb = 0.0
    private var fdc = 0.0
    private var fdd = 0.0 // BUMP FD COEFFICIENTS
    var fdSteps: Int = 0 // BUMP FD memory
        private set
    var fdPrevValue: Double = 0.0
        private set

    fun evaluate(t: Double): Double {
        return t * (t * (a * t + b) + c) + d
    }

    fun derivative(t: Double): Double {
        return t * (3 * a * t + 2 * b) + c
    }

    fun secondDerivative(t: Double): Double {
        return 3 * a * t + 2 * b
    }

    fun initFD(steps: Int): Double {
        fdSteps = steps
        //   fda     0          0          0     1       a
        //   fdb  =  delta**3   delta**2   delta 0   *   b
        //   fdc     6*delta**3 2*delta**2 0     0       c
        //   fdd     6*delta**3 0          0     0       d
        val fd12 = 1.0 / steps
        val fd11 = fd12 * fd12
        val fd10 = fd12 * fd11
        val fd20 = 6.0f * fd10
        val fd21 = 2.0f * fd11
        this.fDValue = d
        fdb = a * fd10 + b * fd11 + c * fd12
        fdc = a * fd20 + b * fd21
        fdd = a * fd20
        return this.fDValue
    }

    fun bumpFD(): Double {
        fdPrevValue = this.fDValue
        this.fDValue += fdb
        fdb += fdc
        fdc += fdd
        return this.fDValue
    }

    fun bumpFDFaster(): Double {
        this.fDValue += fdb
        fdb += fdc
        fdc += fdd
        return this.fDValue
    }
}
