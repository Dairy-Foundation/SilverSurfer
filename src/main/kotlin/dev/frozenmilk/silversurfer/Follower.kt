package dev.frozenmilk.silversurfer

import org.joml.Intersectiond
import org.joml.Vector2d
import org.joml.Math
import org.joml.Vector2dc
import org.joml.Vector3d
import org.joml.times

class Follower(
    val localizer: Localizer,
    val fbController: (error: Double) -> Double,
    val lrController: (error: Double) -> Double,
) {
    private val intersectionOutput = Vector3d()
    private val scratch = Vector2d()
    private val scratch2 = Vector2d()
    private val scratch3 = Vector2d()
    private val target = Vector2d()

    // path is a sequence of points
    fun translationalControl(path: Path, dest: Vector2d): Vector2d {
        val currentPose = localizer.pose
        val currentVelocity = localizer.velocity
        // pure pursuit lookahead, to find target
        val target = lookahead(currentPose, currentVelocity, path)
        val error = target.sub(currentPose.vector, scratch)
        error.rotate(-currentPose.heading)
        // TODO: replace with motion profile
        // run pid on target
        return dest.set(fbController(error.x), lrController(error.y))
    }

    private fun lookahead(currentPose: Pose, currentVelocity: Pose, path: Path): Vector2d {
        var i = 0
        var closestSegment = 0
        path.start.sub(currentPose.vector, target)
        var closestPointDistance = target.length()
        var prev = path.start
        while (i < path.segments.size) {
            val segment = path.segments[i]
            val segmentV = segment.point.sub(prev, scratch)
            val dotted = segmentV.lengthSquared()
            val sigma = if (dotted > 1e-20) currentPose.vector.dot(segmentV) / dotted
            else 0.0

            val segmentClosestPoint = if (sigma < 0) prev
            else if (sigma > 1) segment.point
            else prev.add(segmentV.times(sigma), scratch)

            val dist = segmentClosestPoint.sub(currentPose.vector, scratch).length()

            if (dist < closestPointDistance) {
                closestSegment = i
                target.set(segmentClosestPoint)
                closestPointDistance = dist
            }

            prev = segment.point
            i++
        }

        prev = if (closestSegment == 0) path.start
        else path.segments[closestSegment - 1].point

        val lookahead = path.segments[closestSegment].lookahead(currentVelocity.vector.length())
        i = closestSegment
        val target = target

        while (i < path.segments.size) {
            val segment = path.segments[i]
            val intersects = Intersectiond.intersectLineCircle(
                prev.x(),
                prev.y(),
                segment.point.x(),
                segment.point.y(),
                currentPose.vector.x(),
                currentPose.vector.y(),
                lookahead,
                intersectionOutput,
            )
            if (intersects) {
                val point = scratch.set(intersectionOutput)
                val line = segment.point.sub(prev, scratch2)
                target.set(
                    // two points
                    if (intersectionOutput.z() > 1e-20) {
                        val z = line.normalize(intersectionOutput.z(), scratch3)
                        val a = point.add(z, scratch2).sub(segment.point)
                        val b = point.sub(z, scratch2).sub(segment.point)
                        if (a.length() > b.length()) b
                        else a
                    } else point
                )
            }
            i++
        }

        return target
    }
}

fun Vector2d.rotate(angle: Double) = rotate(angle, this)
fun Vector2dc.rotate(angle: Double, dest: Vector2d) = run {
    val sin = Math.sin(angle)
    val cos = Math.cosFromSin(sin, angle)
    val x = this.x() * cos + this.y() * sin
    val y = -this.x() * sin + this.y() * cos
    dest.x = x
    dest.y = y
    dest
}


