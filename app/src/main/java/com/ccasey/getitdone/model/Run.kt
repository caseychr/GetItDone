package com.ccasey.getitdone.model

data class Run(
    var distance: Float,
    var duration: Long,
    var mileSplit: Float,
    var dateTime: Long,
    var routeLineX: List<Float>,
    var routeLineY: List<Float>) {
    override fun toString(): String {
        return "Run(distance=$distance, duration=$duration, mileSplit=$mileSplit, dateTime=$dateTime, routeLineX=$routeLineX, routeLineY=$routeLineY)"
    }
}