package dev.frozenmilk.silversurfer

import org.joml.Vector2dc

interface Path {
    val start: Vector2dc
    val segments: List<Segment>
}