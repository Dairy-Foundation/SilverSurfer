package dev.frozenmilk.silversurfer.profile

import kotlin.math.sqrt

/**
 * Credits: CTRL ALT FTC by Ben Caunt
 * @see <a href="https://www.ctrlaltftc.com/advanced/motion-profiling">Motion Profiling</a>
 */
class TrapezoidalProfile(
    private val constraints: MotionConstraints
) : MotionProfile(constraints) {
    override fun generate(
        startPosition: Double,
        startVelocity: Double,
        goalPosition: Double
    ) {
        val distance = goalPosition - startPosition
        val accel = constraints.maxAcceleration
        val maxVel = constraints.maxVelocity

        var accelTime = maxVel / accel
        val halfwayDistance = distance / 2.0
        var accelDistance = 0.5 * accel * accelTime * accelTime

        if (accelDistance > halfwayDistance) {
            accelTime = sqrt(halfwayDistance / (0.5 * accel))
            accelDistance = 0.5 * accel * accelTime * accelTime
        }

        val peakVelocity = accel * accelTime
        val cruiseDistance = distance - 2 * accelDistance
        val cruiseTime = if (peakVelocity > 0) cruiseDistance / peakVelocity else 0.0

        segments = mutableListOf()

        (segments as MutableList).add(
            MotionSegment(
                initialPosition = startPosition,
                initialVelocity = startVelocity,
                acceleration = accel,
                duration = accelTime
            )
        )

        if (cruiseTime > 0.0) {
            (segments as MutableList).add(
                MotionSegment(
                    initialPosition = startPosition + accelDistance,
                    initialVelocity = peakVelocity,
                    acceleration = 0.0,
                    duration = cruiseTime
                )
            )
        }

        (segments as MutableList).add(
            MotionSegment(
                initialPosition = startPosition + accelDistance + cruiseDistance,
                initialVelocity = peakVelocity,
                acceleration = -accel,
                duration = accelTime
            )
        )
    }
}