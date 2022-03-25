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

    var totalClick: Int = 0
    var rightClick: Int = 0

    override fun toString(): String {
        var type = if (gameType == true) " number in order, " else " matching numbers, "
        var mode = if (gameMode == true) " goal mode, " else " free mode, "
        var status = if (completed == true) " completed, " else " not completed, "

        return type + mode + status + " id: ${id}, start at: ${startTime}, end at: ${endTime}," +
                " ${repetition} round in total, the button list: ${buttonList} "
    }

    fun toSummaryRound(): String {
        var type = if (gameType == true) " number in order" else " matching numbers"

        return "Congratulations!\nYou have completed ${type} exercise\n" +
        "From: ${startTime}\nTo: ${endTime}"
    }

    fun toSummaryTime(): String {
        var type = if (gameType == true) " number in order" else " matching numbers"

        return "Congratulations!\nYou have completed ${type} exercise\n" +
                "With ${repetition} round in total"
    }

    fun toTable(): String {
        var type = if (gameType == true) " number in order, " else " matching numbers, "

        var prescribed = if (gameType == true) ", total press of buttons: ${totalClick}, " +
                "correct press of buttons: ${rightClick}, The button list: ${buttonList} " else ""

        return "Exercise:${type} Start at: ${startTime}, End at: ${endTime}," +
                " ${repetition} round(s) in total${prescribed}"
    }
}