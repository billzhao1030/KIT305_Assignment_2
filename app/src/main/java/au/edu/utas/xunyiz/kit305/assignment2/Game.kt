package au.edu.utas.xunyiz.kit305.assignment2

import android.media.Image

class Game {
    var id: String? = null

    var gameType: Boolean? = false
    var gameMode: Boolean? = null

    var startTime: String? = null
    var endTime: String? = null

    var completed: Boolean? = true
    var repetition: Int? = 0

    var buttonList: MutableList<Map<String, Int>>? = null

    override fun toString(): String {
        var game = ""

        var type = if (gameType == true) " number in order, " else " matching numbers, "
        var mode = if (gameMode == true) " goal mode, " else " free mode, "
        var status = if (completed == true) " completed, " else " not completed, "

        return type + mode + status + " id: ${id}, start at: ${startTime}, end at: ${endTime}," +
                " ${repetition} round in total, the button list: ${buttonList} "
    }
}