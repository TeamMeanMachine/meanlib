package org.team2471.frc.lib.motion_profiling

import com.google.gson.Gson
import org.team2471.frc.lib.motion_profiling.MotionKey.SlopeMethod
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.min

class MotionCurve {
    @Transient
    private val MAXFRAMEERROR = 0.003
    var headKey: MotionKey? = null

    @Transient
    var tailKey: MotionKey? = null
    var defaultValue: Double = 0.0
    var minValue: Double = 0.0
    var maxValue: Double = 0.0

    @Transient
    private var m_lastValue = 0.0

    @Transient
    private var m_lastDerivative = 0.0

    @Transient
    private var m_lastTime = 0.0

    @Transient
    private var m_bLastTimeValid = false

    @Transient
    var lastAccessedKey: MotionKey? = null
    private val m_preExtrapolation: ExtrapolationMethods
    private val m_postExtrapolation: ExtrapolationMethods
    var markbeginOrEndKeysToZeroSlope: Boolean = true
        private set

    init {
        m_preExtrapolation = ExtrapolationMethods.EXTRAPOLATION_CONSTANT
        m_postExtrapolation = ExtrapolationMethods.EXTRAPOLATION_CONSTANT
    }

    val length: Double
        get() = if (this.tailKey != null) this.tailKey!!.time else 0.0

    fun scaleLength(length: Double) {
        val origLength = this.length
        if (origLength > 0) {
            val scaleFactor = length / origLength

            var nextKey: MotionKey?
            var pKey = this.headKey
            while (pKey != null) {
                nextKey = pKey.nextKey
                val currTime = pKey.time
                pKey.time = scaleFactor * currTime
                pKey = nextKey
            }
        }
    }

    fun removeAllPoints() {
        var nextKey: MotionKey?
        var pKey = this.headKey
        while (pKey != null) {
            nextKey = pKey.nextKey
            pKey.nextKey = null
            pKey.prevKey = null
            pKey = nextKey
        }
        this.headKey = null
        this.tailKey = null
    }

    fun removeKey(key: MotionKey) {
        if (key.prevKey == null) this.headKey = key.nextKey
        else {
            key.prevKey!!.nextKey = key.nextKey
            key.prevKey!!.onPositionChanged()
        }

        if (key.nextKey == null) this.tailKey = key.prevKey
        else {
            key.nextKey!!.prevKey = key.prevKey
            key.nextKey!!.onPositionChanged()
        }
    }

    private fun insertKeyBefore(atKey: MotionKey?, newKey: MotionKey) {
        newKey.motionCurve = this

        if (atKey == this.headKey) {
            this.headKey = newKey
            if (this.tailKey == null) this.tailKey = this.headKey
        }

        this.lastAccessedKey = newKey
        m_bLastTimeValid = false

        if (atKey != null) {
            if (newKey.time > atKey.time) {
                println("Out of order keys detected.")
            }
            newKey.insertBefore(atKey)
        }

        newKey.onPositionChanged()
    }

    private fun insertKeyAfter(atKey: MotionKey?, newKey: MotionKey) {
        newKey.motionCurve = this

        if (atKey == this.tailKey) {
            this.tailKey = newKey
            if (this.headKey == null) this.headKey = this.tailKey
        }

        this.lastAccessedKey = newKey
        m_bLastTimeValid = false

        if (atKey != null) {
            assert(newKey.time >= atKey.time)
            newKey.insertAfter(atKey)
        }

        newKey.onPositionChanged()
    }

    fun addKey(newKey: MotionKey) // adds the key to the end
    {
        insertKeyAfter(this.tailKey, newKey)
    }

    fun onKeyPositionChanged(key: MotionKey?) {
        m_bLastTimeValid = false
    }

    private fun findClosestKey(time: Double): MotionKey? {
        var pKey: MotionKey?
        if (this.lastAccessedKey != null) pKey = this.lastAccessedKey
        else pKey = this.headKey

        if (pKey == null) return null

        if (pKey.time != time) {
            if (pKey.time < time) {
                var pNextKey = pKey.nextKey
                while (pNextKey != null) {
                    if (pNextKey.time > time) break
                    pKey = pNextKey
                    pNextKey = pKey.nextKey
                }
            } else {
                var pPrevKey = pKey.prevKey
                while (pPrevKey != null) {
                    pKey = pPrevKey
                    if (pKey.time <= time) break
                    pPrevKey = pKey.prevKey
                }
            }
        }

        this.lastAccessedKey = pKey
        m_bLastTimeValid = false

        return pKey
    }

    fun getKey(time: Double): MotionKey? {
        var pKey: MotionKey?
        if (this.lastAccessedKey != null) pKey = this.lastAccessedKey
        else pKey = this.headKey

        if (pKey == null) return null

        if (pKey.time != time) {
            if (pKey.time < time) {
                pKey = pKey.nextKey
                while (pKey != null && pKey.time != time) {
                    pKey = pKey.nextKey
                }
            } else {
                pKey = pKey.prevKey
                while (pKey != null && pKey.time != time) {
                    pKey = pKey.prevKey
                }
            }
        }

        if (pKey != null && this.lastAccessedKey != pKey) {
            this.lastAccessedKey = pKey
            m_bLastTimeValid = false
        }

        return pKey
    }

    private fun createMotionKey(time: Double): MotionKey {
        val pKey = findClosestKey(time)
        val pNewKey: MotionKey?

        if (pKey != null && pKey.time == time) pNewKey = pKey
        else {
            pNewKey = MotionKey()
            pNewKey.motionCurve = this
            pNewKey.time = time

            if (pKey == null) addKey(pNewKey)
            else if (pKey.time <= time) insertKeyAfter(pKey, pNewKey)
            else if (pKey.time > time) insertKeyBefore(pKey, pNewKey)
        }

        // for motion profiling, we want the first and last keys to be 0 slope, but all others to be normally smooth
        if (pNewKey == this.headKey && pNewKey.markbeginOrEndKeysToZeroSlope) {
            pNewKey.prevSlopeMethod = SlopeMethod.SLOPE_FLAT
            pNewKey.nextSlopeMethod = SlopeMethod.SLOPE_FLAT
            if (pNewKey.nextKey != null && pNewKey.nextKey != this.tailKey) {  // the former head is not also the tail
                pNewKey.nextKey!!.nextSlopeMethod = SlopeMethod.SLOPE_SMOOTH
                pNewKey.nextKey!!.prevSlopeMethod = SlopeMethod.SLOPE_SMOOTH
            }
        } else if (pNewKey == this.tailKey && pNewKey.markbeginOrEndKeysToZeroSlope) {
            pNewKey.prevSlopeMethod = SlopeMethod.SLOPE_FLAT
            pNewKey.nextSlopeMethod = SlopeMethod.SLOPE_FLAT
            if (pNewKey.prevKey != null && pNewKey.prevKey != this.headKey) {  // the former tail is not also the head
                pNewKey.prevKey!!.nextSlopeMethod = SlopeMethod.SLOPE_SMOOTH
                pNewKey.prevKey!!.prevSlopeMethod = SlopeMethod.SLOPE_SMOOTH
            }
        } else {
            pNewKey.nextSlopeMethod = SlopeMethod.SLOPE_SMOOTH
            pNewKey.prevSlopeMethod = SlopeMethod.SLOPE_SMOOTH
        }

        this.lastAccessedKey = pNewKey
        m_bLastTimeValid = false

        return pNewKey
    }

    fun storeValue(time: Double, value: Double): MotionKey {
        val motionKey = createMotionKey(time)
        motionKey.value = value
        return motionKey
    }

    fun storeValueSlopeAndMagnitude(time: Double, value: Double, slope: Double, magnitude: Double): MotionKey {
        val motionKey = createMotionKey(time)
        motionKey.value = value
        val angleAndMagnitude = DoublePair(atan(slope), magnitude)
        motionKey.nextAngleAndMagnitude = angleAndMagnitude
        motionKey.prevAngleAndMagnitude = angleAndMagnitude
        motionKey.setMarkBeginOrEndKeysToZeroSlope(false)
        return motionKey
    }

    fun getValue(time: Double): Double {
        if (this.headKey == null) return this.defaultValue

        // post-extrapolation
        if (time > this.tailKey!!.time) {
            when (m_postExtrapolation) {
                ExtrapolationMethods.EXTRAPOLATION_CONSTANT -> return this.tailKey!!.value
                ExtrapolationMethods.EXTRAPOLATION_LINEAR -> {
                    val v2Slope = this.tailKey!!.prevTangent
                    return this.tailKey!!.value + (v2Slope.y / v2Slope.x) * (time - this.tailKey!!.time)
                }

                ExtrapolationMethods.EXTRAPOLATION_CYCLE -> {
                    val tStartdouble = this.headKey!!.time
                    val tLength = this.tailKey!!.time - tStartdouble
                    if (tLength != 0.0) {
                        val tdoubleSinceStart = time - tStartdouble
                        val tdoubleInto = tdoubleSinceStart % tLength
                        val tNewdouble = tStartdouble - tdoubleInto
                        return getValue(tNewdouble)
                    } else return getValue(tStartdouble)
                }

                ExtrapolationMethods.EXTRAPOLATION_CYCLE_RELATIVE -> {
                    val tStartdouble = this.headKey!!.time
                    val tLength = this.tailKey!!.time - tStartdouble
                    if (tLength != 0.0) {
                        val tdoubleSinceStart = time - tStartdouble
                        val tdoubleInto = tdoubleSinceStart % tLength
                        val tNewdouble = tStartdouble + tdoubleInto

                        val nCount = (tdoubleSinceStart / tLength).toInt()
                        val fHeight = this.tailKey!!.value - this.headKey!!.value
                        return fHeight * nCount + getValue(tNewdouble)
                    } else return getValue(tStartdouble)
                }

                ExtrapolationMethods.EXTRAPOLATION_OSCILLATE -> {
                    val tStartdouble = this.headKey!!.time
                    val tLength = this.tailKey!!.time - tStartdouble
                    if (tLength != 0.0) {
                        val tdoubleSinceStart = time - tStartdouble
                        val tdoubleInto = tdoubleSinceStart % tLength
                        val tNewdouble = tStartdouble + tdoubleInto

                        val nCount = (tdoubleSinceStart / tLength).toInt()
                        if (nCount % 2 != 0) return getValue(tLength - tNewdouble)
                        else return getValue(tNewdouble)
                    } else return getValue(tStartdouble)
                }
            }
        }

        // pre-extrapolation
        if (time < this.headKey!!.time) {
            when (m_preExtrapolation) {
                ExtrapolationMethods.EXTRAPOLATION_CONSTANT -> return this.headKey!!.value
                ExtrapolationMethods.EXTRAPOLATION_LINEAR -> {
                    val v2Slope = this.headKey!!.nextTangent
                    return this.headKey!!.value + (v2Slope.y / v2Slope.x) * (time - this.headKey!!.time)
                }

                ExtrapolationMethods.EXTRAPOLATION_CYCLE -> {
                    val tStartdouble = this.headKey!!.time
                    val tEnddouble = this.tailKey!!.time
                    val tLength = tEnddouble - tStartdouble
                    if (tLength != 0.0) {
                        val tdoubleSinceEnd = tEnddouble - time
                        val tdoubleInto = tdoubleSinceEnd % tLength
                        val tNewdouble = tEnddouble - tdoubleInto
                        return getValue(tNewdouble)
                    } else return getValue(tStartdouble)
                }

                ExtrapolationMethods.EXTRAPOLATION_CYCLE_RELATIVE -> {
                    val tStartdouble = this.headKey!!.time
                    val tEnddouble = this.tailKey!!.time
                    val tLength = tEnddouble - tStartdouble
                    if (tLength != 0.0) {
                        val tdoubleSinceEnd = tEnddouble - time
                        val tdoubleInto = tdoubleSinceEnd % tLength
                        val tNewdouble = tEnddouble - tdoubleInto

                        val nCount = (tdoubleSinceEnd / tLength).toInt()
                        val fHeight = this.headKey!!.value - this.tailKey!!.value
                        return fHeight * nCount + getValue(tNewdouble)
                    } else return getValue(tStartdouble)
                }

                ExtrapolationMethods.EXTRAPOLATION_OSCILLATE -> {
                    val tStartdouble = this.headKey!!.time
                    val tEnddouble = this.tailKey!!.time
                    val tLength = tEnddouble - tStartdouble
                    if (tLength != 0.0) {
                        val tdoubleSinceEnd = tEnddouble - time
                        val tdoubleInto = tdoubleSinceEnd % tLength
                        val tNewdouble = tEnddouble - tdoubleInto

                        val nCount = (tdoubleSinceEnd / tLength).toInt()
                        if (nCount % 2 != 0) return getValue(tLength - tNewdouble)
                        else return getValue(tNewdouble)
                    } else return getValue(tStartdouble)
                }
            }
        }

        if (this.lastAccessedKey != null) {
            if (m_bLastTimeValid && time == m_lastTime) return m_lastValue // if same as last time
        } else  // if last key is not valid start from the beginning
        {
            this.lastAccessedKey = this.headKey
        }

        if (this.lastAccessedKey!!.time <= time) {
            var key = this.lastAccessedKey
            while (key != null) {
                val nextKey = key.nextKey
                if (key.time == time) {
                    this.lastAccessedKey = key
                    m_lastValue = key.value
                    break
                } else if (nextKey!!.time == time) {
                    this.lastAccessedKey = nextKey
                    m_lastValue = nextKey.value
                    break
                } else if (nextKey.time > time) {
                    this.lastAccessedKey = key
                    m_lastValue = InterpolateValue(time, key)
                    break
                }
                key = key.nextKey
            }
        } else {
            var key = this.lastAccessedKey!!.prevKey
            while (key != null) {
                val nextKey = key.nextKey
                if (key.time == time) {
                    this.lastAccessedKey = key
                    m_lastValue = key.value
                    break
                } else if (nextKey!!.time == time) {
                    this.lastAccessedKey = nextKey
                    m_lastValue = nextKey.value
                    break
                } else if (key.time < time) {
                    this.lastAccessedKey = key
                    m_lastValue = InterpolateValue(time, key)
                    break
                }
                key = key.prevKey
            }
        }

        m_lastTime = time
        m_bLastTimeValid = true
        return m_lastValue
    }

    private fun InterpolateValue(time: Double, pKey: MotionKey): Double {
        val pNextKey = pKey.nextKey

        val nextSlopeMethod = pKey.nextSlopeMethod
        val prevSlopeMethod = pNextKey!!.prevSlopeMethod

        if (nextSlopeMethod == SlopeMethod.SLOPE_STEPPED) return pKey.value
        else if (nextSlopeMethod == SlopeMethod.SLOPE_STEPPED_NEXT) return pNextKey.value
        else if (nextSlopeMethod == SlopeMethod.SLOPE_LINEAR && prevSlopeMethod == SlopeMethod.SLOPE_LINEAR) {
            return (pKey.value + (time - pKey.time)
                    / (pNextKey.time - pKey.time)
                    * (pNextKey.value - pKey.value))
        } else {
            val evalx = time
            val pointax = pKey.time
            val pointbx = pNextKey.time
            val xspan = pointbx - pointax
            var guesst = (evalx - pointax) / xspan

            // if the weights are default, then the x cubic is linear and there is no need to evaluate it
            if (pKey.nextMagnitude == 1.0 && pNextKey.prevMagnitude == 1.0) return pKey.yCoefficients!!.evaluate(
                guesst
            )

            // Spline - non default tangents means that we need a second parametric cubic for x as a function of t
            var diffx = evalx - pKey.xCoefficients!!.evaluate(guesst)
            var error = abs(diffx)
            var maxerror = MAXFRAMEERROR / 30.0f

            if (error > maxerror) {
                var positiveError = Double.MAX_VALUE
                var negativeError = -Double.MAX_VALUE

                if (diffx > 0) positiveError = diffx
                else negativeError = diffx

                while (error > maxerror) {
                    guesst = guesst + diffx / pKey.xCoefficients!!.derivative(guesst)
                    diffx = evalx - pKey.xCoefficients!!.evaluate(guesst)
                    error = abs(diffx)

                    if ((diffx > 0 && diffx > positiveError) || (diffx < 0 && diffx < negativeError)) {  // NOT CONVERGING, PROBABLY BOGUS CHANNEL DATA, WALK USING BUMP FD
                        assert(false)
                        maxerror = (1.0f / 100.0f).toDouble() // DON'T BE AS ACCURATE BECAUSE THIS IS MUCH SLOWER
                        var steps = (xspan / maxerror).toInt()
                        steps = min(steps, 1000)
                        val deltat = (1.0f / steps).toDouble()
                        pKey.xCoefficients!!.initFD(steps)
                        var i: Int
                        diffx = error
                        i = 0
                        guesst = 0.0
                        while (diffx > maxerror && i < steps) {
                            diffx = abs(evalx - pKey.xCoefficients!!.bumpFD())
                            guesst += deltat
                            i++
                        }
                        break
                    }

                    if (diffx > 0) positiveError = diffx
                    else negativeError = diffx
                }
            }

            return pKey.yCoefficients!!.evaluate(guesst)
        }
    }

    fun getDerivative(time: Double): Double {
        if (this.headKey == null || this.headKey == this.tailKey) return 0.0

        if (this.lastAccessedKey!!.time <= time) {
            var key = this.lastAccessedKey
            while (key != null) {
                val nextKey = key.nextKey
                if (nextKey == null) return m_lastDerivative
                if (key.time == time) {
                    val tangent = key.nextTangent
                    m_lastDerivative = tangent.y / tangent.x
                    break
                } else if (nextKey.time == time) {
                    val tangent = nextKey.prevTangent
                    m_lastDerivative = tangent.y / tangent.x
                    break
                } else if (nextKey.time > time) {
                    m_lastDerivative = derivative(time, key)
                    break
                }
                key = key.nextKey
            }
        } else {
            var key = this.lastAccessedKey!!.prevKey
            while (key != null) {
                val nextKey = key.nextKey
                if (nextKey == null) return m_lastDerivative
                if (key.time == time) {
                    val tangent = key.nextTangent
                    m_lastDerivative = tangent.y / tangent.x
                    break
                } else if (nextKey.time == time) {
                    val tangent = nextKey.prevTangent
                    m_lastDerivative = tangent.y / tangent.x
                    break
                } else if (key.time < time) {
                    m_lastDerivative = derivative(time, key)
                    break
                }
                key = key.prevKey
            }
        }

        return m_lastDerivative
    }

    private fun derivative(time: Double, pKey: MotionKey): Double {
        val pNextKey = pKey.nextKey

        val nextSlopeMethod = pKey.nextSlopeMethod
        val prevSlopeMethod = pNextKey!!.prevSlopeMethod

        if (nextSlopeMethod == SlopeMethod.SLOPE_STEPPED) return 0.0
        else if (nextSlopeMethod == SlopeMethod.SLOPE_STEPPED_NEXT) return 0.0
        else if (nextSlopeMethod == SlopeMethod.SLOPE_LINEAR && prevSlopeMethod == SlopeMethod.SLOPE_LINEAR) {
            return (pNextKey.value - pKey.value) / (pNextKey.time - pKey.time)
        } else {
            val evalx = time
            val pointax = pKey.time
            val pointbx = pNextKey.time
            val xspan = pointbx - pointax
            var guesst = (evalx - pointax) / xspan

            // if the weights are default, then the x cubic is linear and there is no need to evaluate it
            if (pKey.nextMagnitude == 1.0 && pNextKey.prevMagnitude == 1.0) return pKey.yCoefficients!!.derivative(
                guesst
            )

            // Spline - non default tangents means that we need a second parametric cubic for x as a function of t
            var diffx = evalx - pKey.xCoefficients!!.evaluate(guesst)
            var error = abs(diffx)
            var maxerror = MAXFRAMEERROR / 30.0f

            if (error > maxerror) {
                var positiveError = Double.MAX_VALUE
                var negativeError = -Double.MAX_VALUE

                if (diffx > 0) positiveError = diffx
                else negativeError = diffx

                while (error > maxerror) {
                    guesst = guesst + diffx / pKey.xCoefficients!!.derivative(guesst)
                    diffx = evalx - pKey.xCoefficients!!.evaluate(guesst)
                    error = abs(diffx)

                    if ((diffx > 0 && diffx > positiveError) || (diffx < 0 && diffx < negativeError)) {  // NOT CONVERGING, PROBABLY BOGUS CHANNEL DATA, WALK USING BUMP FD
                        assert(false)
                        maxerror = (1.0f / 100.0f).toDouble() // DON'T BE AS ACCURATE BECAUSE THIS IS MUCH SLOWER
                        var steps = (xspan / maxerror).toInt()
                        steps = min(steps, 1000)
                        val deltat = (1.0f / steps).toDouble()
                        pKey.xCoefficients!!.initFD(steps)
                        var i: Int
                        diffx = error
                        i = 0
                        guesst = 0.0
                        while (diffx > maxerror && i < steps) {
                            diffx = abs(evalx - pKey.xCoefficients!!.bumpFD())
                            guesst += deltat
                            i++
                        }
                        break
                    }

                    if (diffx > 0) positiveError = diffx
                    else negativeError = diffx
                }
            }

            return pKey.yCoefficients!!.derivative(guesst)
        }
    }

    enum class ExtrapolationMethods {
        EXTRAPOLATION_CONSTANT, EXTRAPOLATION_LINEAR, EXTRAPOLATION_CYCLE, EXTRAPOLATION_CYCLE_RELATIVE, EXTRAPOLATION_OSCILLATE
    }

    fun fixUpTailAndPrevPointers() {
        var prevKey: MotionKey? = null
        var key = this.headKey
        while (key != null) {
            key.prevKey = prevKey
            key.motionCurve = this
            prevKey = key
            key = key.nextKey
        }
        this.tailKey = prevKey
    }

    fun setMarkBeginOrEndKeysToZeroSlope(setBeginOrEndKeysToZeroSlope: Boolean) {
        this.markbeginOrEndKeysToZeroSlope = setBeginOrEndKeysToZeroSlope
    }

    companion object {
        fun fromJsonString(json: String?): MotionCurve? {
            val curve = Gson().fromJson<MotionCurve?>(json, MotionCurve::class.java)
            return hydrateCurve(curve)
        }

        fun hydrateCurve(curve: MotionCurve?): MotionCurve? {
            if (curve != null) {
                var k = curve.headKey
                if (k != null) {
                    while (k!!.nextKey != null) {
                        k.nextKey!!.prevKey = k
                        k.motionCurve = curve
                        k = k.nextKey
                    }
                }
                curve.tailKey = k
            }

            return curve
        }
    }
}
