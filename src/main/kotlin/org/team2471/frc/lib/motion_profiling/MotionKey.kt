package org.team2471.frc.lib.motion_profiling

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class MotionKey {
    @Transient
    private val CLAMPTOLERANCE = 0.005
    private var m_timeAndValue: BooleanPair = BooleanPair(0.0, 0.0)
    private var m_prevAngleAndMagnitude: BooleanPair = BooleanPair(0.0, 1.0)
    private var m_nextAngleAndMagnitude: BooleanPair = BooleanPair(0.0, 1.0)
    private var m_prevTangent: BooleanPair = BooleanPair(0.0, 0.0)
    private var m_nextTangent: BooleanPair = BooleanPair(0.0, 0.0)
    var prevSlopeMethod: SlopeMethod = SlopeMethod.SLOPE_SMOOTH
    var nextSlopeMethod: SlopeMethod = SlopeMethod.SLOPE_SMOOTH
    private var m_markBeginOrEndKeysToZeroSlope: Boolean = true

    var nextKey: MotionKey? = null

    @Transient
    var motionCurve: MotionCurve? = null

    @Transient
    var prevKey: MotionKey? = null

    @Transient
    private var m_bTangentsDirty: Boolean = true

    @Transient
    private var m_bCoefficientsDirty: Boolean = true

    @Transient
    private var m_xCoeff: CubicCoefficients1D? = null

    @Transient
    private var m_yCoeff: CubicCoefficients1D? = null

    init {
        m_timeAndValue.set(0.0, 0.0)
        m_prevAngleAndMagnitude.set(0.0, 1.0)
        m_nextAngleAndMagnitude.set(0.0, 1.0)
    }

    fun onPositionChanged() {
        this.motionCurve!!.onKeyPositionChanged(this) // tell the curve too

        setTangentsDirty(true)
        setCoefficientsDirty(true)

        if (this.prevKey != null) {
            this.prevKey!!.setTangentsDirty(true)
            this.prevKey!!.setCoefficientsDirty(true)
            if (this.prevKey!!.prevKey != null && this.prevKey!!.prevKey!!.nextSlopeMethod == SlopeMethod.SLOPE_PLATEAU) {  // Need to go two away if it is Plateau because they use Prev and Next Tangents
                this.prevKey!!.prevKey!!.setTangentsDirty(true)
                this.prevKey!!.prevKey!!.setCoefficientsDirty(true)
            }
        }

        if (this.nextKey != null) {
            this.nextKey!!.setTangentsDirty(true)
            if (this.nextKey!!.nextKey != null && this.nextKey!!.nextKey!!.prevSlopeMethod == SlopeMethod.SLOPE_PLATEAU) {  // Need to go two away if it is Plateau because they use Prev and Next Tangents
                this.nextKey!!.nextKey!!.setTangentsDirty(true)
                this.nextKey!!.setCoefficientsDirty(true)
            }
        }
    }

    var time: Double
        get() = m_timeAndValue.x
        set(time) {
            m_timeAndValue.x = time
            onPositionChanged()
        }

    var value: Double
        get() = m_timeAndValue.y
        set(value) {
            m_timeAndValue.y = value
            onPositionChanged()
            if (value > this.motionCurve!!.maxValue) { //set curve min and max
                this.motionCurve!!.maxValue = value
            } else if (value < this.motionCurve!!.minValue) {
                this.motionCurve!!.minValue = value
            }
        }

    fun areTangentsDirty(): Boolean {
        return m_bTangentsDirty
    }

    fun setTangentsDirty(bTangentsDirty: Boolean) {
        m_bTangentsDirty = bTangentsDirty
    }

    fun areCoefficientsDirty(): Boolean {
        return m_bCoefficientsDirty
    }

    fun setCoefficientsDirty(bCoefficientsDirty: Boolean) {
        m_bCoefficientsDirty = bCoefficientsDirty
    }

    var timeAndValue: BooleanPair
        get() = m_timeAndValue
        set(m_timeAndValue) {
            this.m_timeAndValue = m_timeAndValue
            onPositionChanged()
        }

    var prevAngleAndMagnitude: BooleanPair
        get() = m_prevAngleAndMagnitude
        set(m_prevAngleAndMagnitude) {
            m_markBeginOrEndKeysToZeroSlope = false
            this.m_prevAngleAndMagnitude = m_prevAngleAndMagnitude
            this.prevSlopeMethod = SlopeMethod.SLOPE_MANUAL
            setTangentsDirty(true)
            onPositionChanged()
        }

    var nextAngleAndMagnitude: BooleanPair
        get() = m_nextAngleAndMagnitude
        set(m_nextAngleAndMagnitude) {
            m_markBeginOrEndKeysToZeroSlope = false
            this.m_nextAngleAndMagnitude = m_nextAngleAndMagnitude
            this.nextSlopeMethod = SlopeMethod.SLOPE_MANUAL
            setTangentsDirty(true)
            onPositionChanged()
        }

    var prevTangent: BooleanPair
        get() {
            if (areTangentsDirty()) calculateTangents()

            return m_prevTangent
        }
        set(m_PrevTangent) {
            this.m_prevTangent = m_PrevTangent
        }

    var nextTangent: BooleanPair
        get() {
            if (areTangentsDirty()) calculateTangents()

            return m_nextTangent
        }
        set(m_NextTangent) {
            this.m_nextTangent = m_NextTangent
        }

    var prevMagnitude: Double
        get() = m_prevAngleAndMagnitude.y
        set(magnitude) {
            m_prevAngleAndMagnitude.y = magnitude
            this.prevSlopeMethod = SlopeMethod.SLOPE_MANUAL
        }

    var nextMagnitude: Double
        get() = m_nextAngleAndMagnitude.y
        set(magnitude) {
            m_nextAngleAndMagnitude.y = magnitude
            this.nextSlopeMethod = SlopeMethod.SLOPE_MANUAL
        }

    fun insertBefore(newKey: MotionKey) {
        this.prevKey = newKey.prevKey
        if (newKey.prevKey != null) newKey.prevKey!!.nextKey = this
        newKey.prevKey = this
        this.nextKey = newKey
    }

    fun insertAfter(newKey: MotionKey) {
        this.nextKey = newKey.nextKey
        if (newKey.nextKey != null) newKey.nextKey!!.prevKey = this
        newKey.nextKey = this
        this.prevKey = newKey
    }

    private fun calculateTangents() {
        setTangentsDirty(false)

        var bCalcSmoothPrev = false
        var bCalcSmoothNext = false

        when (this.prevSlopeMethod) {
            SlopeMethod.SLOPE_MANUAL -> {
                m_prevTangent.set(cos(this.prevAngleAndMagnitude.x), sin(this.prevAngleAndMagnitude.x))
                if (this.prevKey != null)  // TODO: result is unused
                    m_prevTangent.times(this.timeAndValue.x - prevKey!!.timeAndValue.x)
            }

            SlopeMethod.SLOPE_LINEAR -> if (this.prevKey != null) m_prevTangent =
                this.timeAndValue.minus(prevKey!!.timeAndValue)

            SlopeMethod.SLOPE_FLAT -> if (this.prevKey != null) m_prevTangent.set(
                (this.timeAndValue.x - prevKey!!.timeAndValue.x) * 0.5,
                0.0
            )

            SlopeMethod.SLOPE_SMOOTH -> bCalcSmoothPrev = true
            SlopeMethod.SLOPE_CLAMPED -> {
                val fClampTolerence = (this.motionCurve!!.maxValue - this.motionCurve!!.minValue) * CLAMPTOLERANCE
                if (this.prevKey != null && abs(prevKey!!.value - this.value) <= fClampTolerence)  // make Flat
                    m_prevTangent.set(this.time - prevKey!!.time, 0.0)
                else if (this.nextKey != null && abs(nextKey!!.value - this.value) <= fClampTolerence)  // Make Flat
                {
                    if (this.prevKey != null) m_prevTangent.set(this.time - prevKey!!.time, 0.0)
                    else m_prevTangent.set(0.0, 0.0)
                } else bCalcSmoothPrev = true
            }

            SlopeMethod.SLOPE_PLATEAU -> if (this.prevKey == null || this.nextKey == null) {
                if (this.prevKey != null) m_prevTangent.set(this.time - prevKey!!.time, 0.0) // Make Flat
                else m_prevTangent.set(0.0, 0.0)
            } else  // we have a prev and a next, lets see if both the prev's out tangent and the next's in tangent are both either greater or less than our value, if so lets make out tangent flat
            {
                val fPrevTangentValue: Double
                if (prevKey!!.nextSlopeMethod == SlopeMethod.SLOPE_PLATEAU) fPrevTangentValue =
                    prevKey!!.value // This way we don't get an infinite recursion
                else {
                    val vPrevPos = prevKey!!.timeAndValue
                    val vPrevTangent = prevKey!!.nextTangent.times(1.0 / 3.0).plus(vPrevPos)
                    fPrevTangentValue = vPrevTangent.y
                }

                val fNextTangentValue: Double
                if (nextKey!!.prevSlopeMethod == SlopeMethod.SLOPE_PLATEAU) fNextTangentValue =
                    nextKey!!.value // This way we don't get an infinite recursion
                else {
                    val vNextPos = nextKey!!.timeAndValue
                    val vNextTangent = vNextPos.minus(nextKey!!.prevTangent.times(1.0 / 3.0))
                    fNextTangentValue = vNextTangent.y
                }

                val fValue = this.value
                if (fPrevTangentValue > fValue && fNextTangentValue > fValue) m_prevTangent.set(
                    this.time - prevKey!!.time,
                    0.0
                ) // Make Flat
                else if (fPrevTangentValue < fValue && fNextTangentValue < fValue) m_prevTangent.set(
                    this.time - prevKey!!.time,
                    0.0
                ) // Make Flat
                else bCalcSmoothPrev = true
            }

            SlopeMethod.SLOPE_STEPPED, SlopeMethod.SLOPE_STEPPED_NEXT -> assert(false) // Not a valid method for PREV Interp Method, it is only valid for NEXT key direction
            else -> {}
        }

        when (this.nextSlopeMethod) {
            SlopeMethod.SLOPE_MANUAL -> {
                m_nextTangent.set(cos(this.nextAngleAndMagnitude.x), sin(this.nextAngleAndMagnitude.x))
                if (this.nextKey != null) m_nextTangent.times(nextKey!!.timeAndValue.x - this.timeAndValue.x)
            }

            SlopeMethod.SLOPE_LINEAR -> if (this.nextKey != null) m_nextTangent =
                nextKey!!.timeAndValue.minus(this.timeAndValue)

            SlopeMethod.SLOPE_FLAT -> if (this.nextKey != null) m_nextTangent.set(
                nextKey!!.timeAndValue.x - this.timeAndValue.x,
                0.0
            )

            SlopeMethod.SLOPE_SMOOTH -> bCalcSmoothNext = true
            SlopeMethod.SLOPE_CLAMPED -> {
                val fClampTolerence = (this.motionCurve!!.maxValue - this.motionCurve!!.minValue) * CLAMPTOLERANCE
                if (this.prevKey != null && abs(prevKey!!.value - this.value) <= fClampTolerence)  // make Flat
                {
                    if (this.nextKey != null) m_nextTangent.set(nextKey!!.time - this.time, 0.0)
                    else m_nextTangent.set(0.0, 0.0)
                } else if (this.nextKey != null && abs(nextKey!!.value - this.value) <= fClampTolerence)  // Make Flat
                    m_nextTangent.set(nextKey!!.time - this.time, 0.0)
                else bCalcSmoothNext = true
            }

            SlopeMethod.SLOPE_PLATEAU -> if (this.prevKey == null || this.nextKey == null) {
                if (this.nextKey != null) m_nextTangent.set(nextKey!!.time - this.time, 0.0) // Make it flat
                else m_nextTangent.set(0.0, 0.0)
            } else  // we have a prev and a next, lets see if both the prev's out tangent and the next's in tangent are both either greater or less than our value, if so lets make out tangent flat
            {
                val fPrevTangentValue: Double
                if (prevKey!!.nextSlopeMethod == SlopeMethod.SLOPE_PLATEAU) fPrevTangentValue =
                    prevKey!!.value // This way we don't get an infinite recursion
                else {
                    val vPrevPos = BooleanPair(prevKey!!.time, prevKey!!.value)
                    val vPrevTangent = prevKey!!.nextTangent.times(1.0 / 3.0).plus(vPrevPos)
                    fPrevTangentValue = vPrevTangent.y
                }

                val fNextTangentValue: Double
                if (nextKey!!.prevSlopeMethod == SlopeMethod.SLOPE_PLATEAU) fNextTangentValue =
                    nextKey!!.value // This way we don't get an infinite recursion
                else {
                    val vNextPos = BooleanPair(nextKey!!.time, nextKey!!.value)
                    val vNextTangent = vNextPos.minus(nextKey!!.prevTangent.times(1.0 / 3.0))
                    fNextTangentValue = vNextTangent.y
                }

                val fValue = this.value
                if (fPrevTangentValue > fValue && fNextTangentValue > fValue) m_nextTangent.set(
                    nextKey!!.time - this.time,
                    0.0
                ) // Make it flat
                else if (fPrevTangentValue < fValue && fNextTangentValue < fValue) m_nextTangent.set(
                    nextKey!!.time - this.time,
                    0.0
                ) // Make it flat
                else bCalcSmoothNext = true
            }

            SlopeMethod.SLOPE_STEPPED, SlopeMethod.SLOPE_STEPPED_NEXT -> {}
            else -> {}
        }

        if (bCalcSmoothPrev || bCalcSmoothNext) {
            if (this.prevKey != null && this.nextKey != null) {
                var delta = nextKey!!.timeAndValue.minus(prevKey!!.timeAndValue)
                val weight = abs(delta.x)
                if (weight == 0.0)  // if keys are on top of one another (no tangents)
                {
                    if (bCalcSmoothPrev) m_prevTangent.set(0.0, 0.0)
                    if (bCalcSmoothNext) m_nextTangent.set(0.0, 0.0)
                } else {
                    delta = delta.div(weight)

                    if (bCalcSmoothPrev) {
                        val prevWeight = this.timeAndValue.x - prevKey!!.timeAndValue.x
                        m_prevTangent = delta.times(prevWeight)
                    }
                    if (bCalcSmoothNext) {
                        val nextWeight = nextKey!!.timeAndValue.x - this.timeAndValue.x
                        m_nextTangent = delta.times(nextWeight)
                    }
                }
            } else {
                if (this.nextKey != null) {
                    if (bCalcSmoothPrev) m_prevTangent = nextKey!!.timeAndValue.minus(this.timeAndValue)

                    if (bCalcSmoothNext) m_nextTangent = nextKey!!.timeAndValue.minus(this.timeAndValue)
                }

                if (this.prevKey != null) {
                    if (bCalcSmoothPrev) m_prevTangent = this.timeAndValue.minus(prevKey!!.timeAndValue)

                    if (bCalcSmoothNext) m_nextTangent = this.timeAndValue.minus(prevKey!!.timeAndValue)
                }
            }
        }

        m_prevTangent =
            m_prevTangent.times(this.prevAngleAndMagnitude.y) // / 3.0 it seems like this is more of a UI only thing, and shouldn't really be done in this case.  But maybe I'm wrong.  Subtract the points, then take a third to get a good default tangent.  Does that still appear too long in the UI?  So we divide by 3 again.
        m_nextTangent = m_nextTangent.times(this.nextAngleAndMagnitude.y) // / 3.0
    }

    val xCoefficients: CubicCoefficients1D?
        get() {
            if (areCoefficientsDirty()) {
                calculateCoefficients()
            }
            return m_xCoeff
        }

    val yCoefficients: CubicCoefficients1D?
        get() {
            if (areCoefficientsDirty()) {
                calculateCoefficients()
            } else if (m_yCoeff == null) {
                println("m_yCoeff = null")
                calculateCoefficients()
            }
            return m_yCoeff
        }

    private fun calculateCoefficients() {
        setCoefficientsDirty(false)

        val pointax = this.time
        val pointbx = nextKey!!.time
        val xspan = pointbx - pointax

        val pointay = this.value
        val pointby = nextKey!!.value
        val pointcy = this.nextTangent.y
        val pointdy = nextKey!!.prevTangent.y

        m_yCoeff = CubicCoefficients1D(pointay, pointby, pointcy, pointdy)

        // if the weights are default, then the x cubic is linear and there is no need to evaluate it
        if (this.nextMagnitude == 1.0 && nextKey!!.prevMagnitude == 1.0) return

        // Spline - non default tangents means that we need a second parametric cubic for x as a function of t
        var pointcx = this.nextTangent.x
        var pointdx = nextKey!!.prevTangent.x

        val xspan3 = xspan * 3

        // if c going beyond b limit
        if (pointcx > xspan3) {
            val ratio = xspan3 / pointcx
            pointcx = xspan3
        }

        // if d going beyond a limit
        if (pointdx > xspan3) {
            val ratio = xspan3 / pointdx
            pointdx = xspan3
        }

        m_xCoeff = CubicCoefficients1D(pointax, pointbx, pointcx, pointdx)
    }

    val markbeginOrEndKeysToZeroSlope: Boolean
        get() = m_markBeginOrEndKeysToZeroSlope && this.motionCurve!!.markbeginOrEndKeysToZeroSlope

    fun setMarkBeginOrEndKeysToZeroSlope(m_setBeginOrEndKeysToZeroSlope: Boolean) {
        this.m_markBeginOrEndKeysToZeroSlope = m_setBeginOrEndKeysToZeroSlope
    }

    enum class SlopeMethod {
        SLOPE_MANUAL, SLOPE_LINEAR, SLOPE_FLAT, SLOPE_SMOOTH, SLOPE_CLAMPED, SLOPE_PLATEAU,
        SLOPE_STEPPED, SLOPE_STEPPED_NEXT
    }

    var magnitude: Double
        get() {
            if (this.prevKey != null) return this.prevMagnitude
            return this.nextMagnitude
        }
        set(magnitude) {
            m_prevAngleAndMagnitude.y = magnitude
            m_nextAngleAndMagnitude.y = magnitude
            this.prevSlopeMethod = SlopeMethod.SLOPE_MANUAL
            this.nextSlopeMethod = this.prevSlopeMethod
            onPositionChanged()
        }

    var angle: Double
        get() {
            if (this.prevKey != null) return this.prevAngle
            return this.nextAngle
        }
        set(angle) {
            m_prevAngleAndMagnitude.x = angle
            m_nextAngleAndMagnitude.x = angle
            this.prevSlopeMethod = SlopeMethod.SLOPE_MANUAL
            this.nextSlopeMethod = this.prevSlopeMethod
            onPositionChanged()
        }

    var prevAngle: Double
        get() = m_prevAngleAndMagnitude.x
        set(angle) {
            m_prevAngleAndMagnitude.x = angle
            this.prevSlopeMethod = SlopeMethod.SLOPE_MANUAL
        }
    var nextAngle: Double
        get() = m_nextAngleAndMagnitude.x
        set(angle) {
            m_nextAngleAndMagnitude.x = angle
            this.nextSlopeMethod = SlopeMethod.SLOPE_MANUAL
        }
}
