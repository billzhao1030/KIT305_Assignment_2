package au.edu.utas.xunyiz.kit305.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityPrescribedGameBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PrescribedGame : AppCompatActivity() {
    private lateinit var ui: ActivityPrescribedGameBinding

    var time = -1
    var round = -1
    var gameMode = true
    var goal = true

    var buttonRaduis = 10
    var hasIndication = true
    var isRandom = true
    var numOfButtons = 3

    var completed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPrescribedGameBinding.inflate(layoutInflater)
        setContentView(ui.root)

        time = intent.getIntExtra("Time", -1)
        round = intent.getIntExtra("Rounds", -1)
        gameMode = intent.getBooleanExtra("ExerciseMode", true)
        goal = intent.getBooleanExtra("Goal", true)

        buttonRaduis = intent.getIntExtra("buttonRadius", 10)
        hasIndication = intent.getBooleanExtra("hasIndication", true)
        isRandom = intent.getBooleanExtra("isRandom", true)
        numOfButtons = intent.getIntExtra("numOfButtons", 3)

        var db = Firebase.firestore
        var games = db.collection("games")

        startPrescribedGame()
    }

    private fun startPrescribedGame() {
        for (i in 1..numOfButtons) {

        }

        var button = Button(this)
        val game = ui.root as ConstraintLayout

        button.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        button.text = "new"

        game.addView(button)
    }
}