package org.team2471.frc.lib.motion_profiling;

public class CubicCoefficients1D {
    private double a, b, c, d;          // CUBIC COEFFICIENTS f(t) = a*t^3 + b*t^2 + c*t + d
    private double fda, fdb, fdc, fdd;  // BUMP FD COEFFICIENTS
    private int fdSteps;                // BUMP FD memory
    private double fdPrevValue;

    CubicCoefficients1D(double p1, double p4, double r1, double r4) {  // construct from two values and two tangents (slope)
        //   a     2 -2  1  1   p1
        //   b =  -3  3 -2 -1 * p4
        //   c     0  0  1  0   r1
        //   d     1  0  0  0   r4
        a = 2 * p1 + -2 * p4 + r1 + r4;
        b = -3 * p1 + 3 * p4 + -2 * r1 + -r4;
        c = r1;
        d = p1;
    }

    double evaluate(double t) {
        return t * (t * (a * t + b) + c) + d;
    }

    double derivative(double t) {
        return t * (3 * a * t + 2 * b) + c;
    }

    double secondDerivative(double t) {
        return 3 * a * t + 2 * b;
    }

    double initFD(int steps) {
        fdSteps = steps;
        //   fda     0          0          0     1       a
        //   fdb  =  delta**3   delta**2   delta 0   *   b
        //   fdc     6*delta**3 2*delta**2 0     0       c
        //   fdd     6*delta**3 0          0     0       d
        double fd12 = 1.0 / steps;
        double fd11 = fd12 * fd12;
        double fd10 = fd12 * fd11;
        double fd20 = 6.0f * fd10;
        double fd21 = 2.0f * fd11;
        fda = d;
        fdb = a * fd10 + b * fd11 + c * fd12;
        fdc = a * fd20 + b * fd21;
        fdd = a * fd20;
        return fda;
    }

    double bumpFD() {
        fdPrevValue = fda;
        fda += fdb;
        fdb += fdc;
        fdc += fdd;
        return fda;
    }

    double bumpFDFaster() {
        fda += fdb;
        fdb += fdc;
        fdc += fdd;
        return fda;
    }

    double getFDValue() {
        return fda;
    }

    public int getFdSteps() {
        return fdSteps;
    }

    public double getFdPrevValue() {
        return fdPrevValue;
    }
}
