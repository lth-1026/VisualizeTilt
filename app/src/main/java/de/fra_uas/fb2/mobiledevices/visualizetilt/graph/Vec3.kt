package de.fra_uas.fb2.mobiledevices.visualizetilt.graph

class Vec3(val x: Float, val y: Float, val z: Float) {

    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

    fun dot(other: Vec3) = x * other.x + y * other.y + z * other.z
}