package dev.frozenmilk.silversurfer.path

import dev.frozenmilk.silversurfer.util.Pose

interface Localizer {
    val pose: Pose
    val velocity: Pose
}