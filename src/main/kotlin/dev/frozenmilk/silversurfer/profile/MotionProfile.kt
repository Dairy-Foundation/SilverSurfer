package dev.frozenmilk.silversurfer.profile

abstract class MotionProfile(constraints: MotionConstraints) {
    lateinit var segments: List<MotionSegment>
}