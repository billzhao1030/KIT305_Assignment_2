package au.edu.utas.xunyiz.kit305.assignment2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityCustomizationBinding

class Customization : AppCompatActivity() {
    private lateinit var ui: ActivityCustomizationBinding

    var time = -1
    var round = -1
    var gameMode = true
    var goal = true

    var buttonRadius = -1
    var numOfButtons = 3
    var hasIndication = true
    var isRandom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        time = intent.getIntExtra("Time", -1)
        round = intent.getIntExtra("Rounds", -1)
        gameMode = intent.getBooleanExtra("ExerciseMode", true)
        goal = intent.getBooleanExtra("Goal", true)

        super.onCreate(savedInstanceState)
        ui = ActivityCustomizationBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d(console_log, time.toString())
        Log.d(console_log, round.toString())
        Log.d(console_log, gameMode.toString())
        Log.d(console_log, goal.toString())

        ui.customizeBack.setOnClickListener {
            finish()
        }

        //        var buttonList = mutableListOf<Map<String, Int>>()
//
//        ui.button.setOnClickListener {
//            var currentTime = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
//            currentTime = SimpleDateFormat("HH:mm:ss").format(Date())
//            Log.d(console_log, currentTime)
//            buttonList.add(mapOf(currentTime to 1))
//            printList(buttonList)
//        }
    }

    fun customization_back(view: View) {
        finish()
    }

    fun startGame(view: View) {
        var game = Intent(this, PrescribedGame::class.java)
        game.putExtra("ExerciseMode", gameMode)
        game.putExtra("Goal", goal)
        game.putExtra("Rounds", round)
        game.putExtra("Time", time)

        game.putExtra("numOfButtons", numOfButtons)
        game.putExtra("isRandom", isRandom)
        game.putExtra("hasIndication", hasIndication)
        game.putExtra("buttonRadius", buttonRadius)

        startActivity(game)
    }
}