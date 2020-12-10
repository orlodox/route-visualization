package com.example.routevisual

class Maneuver(val type: ManeuverType, val numberOfMeters: Int = 0) {

    override fun toString(): String {
        return "${type.toString()} ${if (numberOfMeters > 0) numberOfMeters.toString() else ""}\n"
    }
}