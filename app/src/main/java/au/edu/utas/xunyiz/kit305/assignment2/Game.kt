package au.edu.utas.xunyiz.kit305.assignment2

import android.media.Image

class Game {
    var id: String? = null

    var designed: Boolean? = false
    var mode: Boolean? = null

    var startTime: String? = null
    var endTime: String? = null

    var completed: Boolean? = true
    var repetitions: Int? = 0

    var image: Image? = null

    var buttonList: List<Map<String, Int>>? = null
}