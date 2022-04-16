package au.edu.utas.xunyiz.kit305.assignment2

import android.graphics.Color
import android.media.Image
import android.util.Log

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
                "With ${repetition} round(s) in total"
    }

    fun toSummary(): String {
        var type = if (gameType == true) " number in order" else " matching numbers"

        return "Congratulations!\nYou have completed ${type} exercise\n" +
                "From: ${startTime}\nTo: ${endTime}\n" + "With ${repetition} round(s) in total"
    }

    fun toTable(): String {
        var type = if (gameType == true) " number in order," else " matching numbers,"
        var complete = if (completed == true) "completed" else "not completed"
        var str = "["
        for (buttonClick in buttonList!!) {
            if (buttonClick.containsValue(10)|| buttonClick.containsValue(20) || buttonClick.containsValue(30) || buttonClick.containsValue(40) || buttonClick.containsValue(50)) {
                var temp = "${buttonClick.keys} : Button ${buttonClick.values}"
                str += "{${temp.substring(0, temp.length - 2)}]},"
            } else {
                str += "{${buttonClick.keys} : Button ${buttonClick.values}},"
            }
        }
        str += "]"

        var prescribed = if (gameType == true) ", total press of buttons: ${totalClick}, " +
                "correct press of buttons: ${rightClick}, The button list: ${str} " else ""

        return "Exercise:${type} ${complete}, Start at: ${startTime}, End at: ${endTime}," +
                " ${repetition} round(s) in total${prescribed}"
    }
}