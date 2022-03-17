package au.edu.utas.xunyiz.kit305.assignment2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityCustomizationBinding

class Customization : AppCompatActivity() {
    private lateinit var ui: ActivityCustomizationBinding

    var time = -1
    var round = -1
    var gameMode = true
    var goal = true

    var buttonRadius = 50
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

        ui.sizeOfButton.setOnSeekBarChangeListener((object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                when {
                    progress == 2 -> ui.size.text = "Small"
                    progress == 3 -> ui.size.text = "Normal"
                    progress == 4 -> ui.size.text = "Big"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })  )
    }

    fun customizationBack(view: View) {
        finish()
    }

    fun startGame(view: View) {
        val radioButtonID: Int = ui.numOfButtonGroup.checkedRadioButtonId
        val selectedButton: View = ui.numOfButtonGroup.findViewById(radioButtonID)
        numOfButtons = ui.numOfButtonGroup.indexOfChild(selectedButton) + 2
        Log.d(console_log, numOfButtons.toString())
        isRandom = ui.randomOrderCustomize.isChecked
        Log.d(console_log, isRandom.toString())
        hasIndication = ui.indicationCustomize.isChecked
        Log.d(console_log, hasIndication.toString())
        buttonRadius = ui.sizeOfButton.progress
        Log.d(console_log, buttonRadius.toString())


        val game = Intent(this, PrescribedGame::class.java)
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