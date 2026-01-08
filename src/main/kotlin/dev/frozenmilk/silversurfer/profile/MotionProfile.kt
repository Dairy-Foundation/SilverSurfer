package dev.frozenmilk.silversurfer.profile

abstract class MotionProfile(constraints: MotionConstraints) {
    protected lateinit var segments: List<MotionSegment>

    protected abstract fun generate(
        startPosition: Double,
        startVelocity: Double,
        goalPosition: Double
    )

    val duration: Double
        get() = segments.sumOf { it.duration }

    fun sample(t: Double): MotionState {
        require(::segments.isInitialized) { "Motion profile isn't initialized, did you generate it?" }

        var time = t.coerceIn(0.0, duration)

        for (segment in segments) {
            if (time <= segment.duration) {
                return segment.sample(time)
            }
            time -= segment.duration
        }

        return segments.last().sample(segments.last().duration)
    }

    protected data class MotionSegment(
        val initialPosition: Double,
        val initialVelocity: Double,
        val acceleration: Double,
        val duration: Double
    ) {
        fun sample(t: Double): MotionState {
            val clampedT = t.coerceIn(0.0, duration)
            val velocity = initialVelocity + acceleration * clampedT
            val position =
                initialPosition +
                        initialVelocity * clampedT +
                        0.5 * acceleration * clampedT * clampedT

            return MotionState(
                position = position,
                velocity = velocity,
                acceleration = acceleration
            )
        }
    }
}