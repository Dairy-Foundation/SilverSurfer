package dev.frozenmilk.silversurfer.path

import dev.frozenmilk.silversurfer.util.Pose
import org.joml.Intersectiond
import org.joml.Vector2d
import org.joml.Math
import org.joml.Vector2dc
import org.joml.Vector3d
import org.joml.times
import kotlin.math.min

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
    private var segmentIdx = 0

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

    private fun lookahead(
        currentPose: Pose,
        currentVelocity: Pose,
        path: Path
    ): Vector2d {
        val pos = currentPose.vector
        val lookahead = path.segments[segmentIdx].lookahead(currentVelocity.vector.length())

        run {
            val end = path.segments[segmentIdx].point
            if (pos.distanceSquared(end) < 1e-4) {
                if (segmentIdx < path.segments.size - 1) {
                    segmentIdx++
                }
            }
        }

        if (segmentIdx == path.segments.size - 1) {
            val end = path.segments.last().point
            if (pos.distance(end) <= lookahead) {
                return target.set(end)
            }
        }

        var i = segmentIdx
        val maxI = min(segmentIdx + 1, path.segments.size - 1)

        var prev =
            if (i == 0) path.start
            else path.segments[i - 1].point

        while (i <= maxI) {
            val seg = path.segments[i]

            val hit = Intersectiond.intersectLineCircle(
                prev.x(), prev.y(),
                seg.point.x(), seg.point.y(),
                pos.x(), pos.y(),
                lookahead,
                intersectionOutput
            )

            if (hit) {
                val base = scratch.set(intersectionOutput)
                val dir = seg.point.sub(prev, scratch2)

                if (intersectionOutput.z() > 1e-12) {
                    dir.normalize(intersectionOutput.z(), scratch3)

                    val a = base.add(scratch3, scratch2)
                    val b = base.sub(scratch3, scratch2)

                    return target.set(
                        if (a.distanceSquared(seg.point) < b.distanceSquared(seg.point)) a else b
                    )
                }

                return target.set(base)
            }

            prev = seg.point
            i++
        }

        return target.set(path.segments[segmentIdx].point)
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


