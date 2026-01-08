package dev.frozenmilk.silversurfer.profile

class TrapezoidalProfile(
    private val constraints: MotionConstraints
) : MotionProfile(constraints) {
    override fun generate(
        startPosition: Double,
        startVelocity: Double,
        goalPosition: Double
    ) {}
}
