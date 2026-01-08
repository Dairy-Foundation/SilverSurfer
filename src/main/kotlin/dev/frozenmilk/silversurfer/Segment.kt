package dev.frozenmilk.silversurfer

import org.joml.Vector2dc

data class Segment(
    val point: Vector2dc,
    val lookahead: (velocity: Double) -> Double,
)
