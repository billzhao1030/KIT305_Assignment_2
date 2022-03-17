package au.edu.utas.xunyiz.kit305.assignment2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityPrescribedGameBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.PauseButtonBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PrescribedGame : AppCompatActivity(), View.OnClickListener {
    private lateinit var ui: ActivityPrescribedGameBinding
    private lateinit var pause: PauseButtonBinding

    private lateinit var pauseBuilder: AlertDialog.Builder
    private lateinit var pausePopup: AlertDialog

    var time = -1
    var round = -1
    var gameMode = true
    var goal = true

    var buttonRaduis = 3
    var hasIndication = true
    var isRandom = true
    var numOfButtons = 3

    var completed = false
    var repetition = 0

    var id = ""
    var btnNow = 1

    var id_list = Array<Int>(numOfButtons, {i -> i})

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPrescribedGameBinding.inflate(layoutInflater)
        pause = PauseButtonBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // set the pause popup
        pauseBuilder = AlertDialog.Builder(this)
        pauseBuilder.setView(pause.root)
        pausePopup = pauseBuilder.create()
        pausePopup.setCanceledOnTouchOutside(false)

        gamePreset()

        if (!gameMode) {
            pause.goToMenu.text = "Finish Exercise"
        }

        // get database connection
        var db = Firebase.firestore
        var games = db.collection("games")

        //get the id and set
//        id = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
//        var currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())
//        var dataMap = hashMapOf(
//            "completed" to completed,
//            "startTime" to currentTime,
//            "endTime" to currentTime,
//            "gameMode" to gameMode,
//            "gameType" to true,
//            "repetition" to repetition,
//            "buttonList" to mutableListOf<Map<String, Int>>()
//        )
//        games.document(id)
//            .set(dataMap)
//            .addOnSuccessListener { Log.d(database_log, "prescribe new game") }
//            .addOnFailureListener { Log.d(database_log, "prescribe new game fail")}

        startPrescribedGame()

    }

    private fun gamePreset() {
        time = intent.getIntExtra("Time", -1)
        round = intent.getIntExtra("Rounds", -1)
        gameMode = intent.getBooleanExtra("ExerciseMode", true)
        goal = intent.getBooleanExtra("Goal", true)

        buttonRaduis = (intent.getIntExtra("buttonRadius", 3) * 20 + 40).toInt()
        hasIndication = intent.getBooleanExtra("hasIndication", true)
        isRandom = intent.getBooleanExtra("isRandom", true)
        numOfButtons = intent.getIntExtra("numOfButtons", 3)

        id_list = Array<Int>(numOfButtons, {i -> i})
    }

    @SuppressLint("ResourceType")
    private fun startPrescribedGame() {
        id_list = Array<Int>(numOfButtons, {i -> i})

        for (button in 1..numOfButtons) {
            var btn: Button = Button(this)
            btn.id = button
            id_list[button - 1] = btn.id

            btn.text = "btn ${button}"
            btn.setBackgroundResource(R.drawable.round_button)
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28F)


            val metrics = Resources.getSystem().displayMetrics
            var scale:Int = (buttonRaduis * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
            btn.layoutParams = ViewGroup.LayoutParams(scale, scale)

            ui.prescribed.addView(btn)

            btn.setOnClickListener(this)
        }
        reposition(id_list)

        ui.button.setOnClickListener {
            reposition(id_list)
        }
    }

    private fun reposition(id_list: Array<Int>) {
        var x_point = setLocationX()
        var y_point = setLocationY()

        for (button in 1..numOfButtons) {
            var setting = ConstraintSet()
            var mainWindow = ui.prescribed
            setting.clone(mainWindow)

            var marginStart: Int = getDP(x_point[button - 1])
            var marginBottom: Int = getDP(y_point[button - 1])

            setting.connect(id_list[button-1], ConstraintSet.START, ui.root.id, ConstraintSet.START, marginStart)
            setting.connect(id_list[button-1], ConstraintSet.BOTTOM, ui.root.id, ConstraintSet.BOTTOM, marginBottom)

            setting.applyTo(mainWindow)

            var btn: Button = findViewById<Button>(id_list[button-1])
            btn.setText(button.toString())
        }
    }

    private fun setLocationX(): Array<Int> {
        var listX = Array<Int>(numOfButtons, {i -> i * 1})
        val totalX = SCREEN_X
        var randomX: Int

        var numList = mutableListOf<Int>()

        for (i in 1..numOfButtons) {
            var random = (0..4).random()
            while (numList.contains(random)) {
                random = (0..4).random()
            }
            numList.add(random)
            randomX = random * 150 + 8
            listX[i - 1] = randomX
        }

        return listX
    }

    private fun setLocationY(): Array<Int> {
        var listY = Array<Int>(numOfButtons, {i -> i * 1})
        val totalY = SCREEN_Y
        var randomY: Int

        var numList = mutableListOf<Int>()

        for (i in 1..numOfButtons) {
            var random = (0..4).random()
            while (numList.contains(random)) {
                random = (0..4).random()
            }
            numList.add(random)
            randomY = random * 150 + 32
            listY[i - 1] = randomY
        }

        return listY
    }


    fun showPausePopup(view: View) {
        pausePopup.show()
    }

    fun closePausePopup(view: View) {
        pausePopup.cancel()
    }

    fun goToMenu(view: View) {
        // need to deal with the complete or not
        var menu = Intent(this, MainActivity::class.java)
        startActivity(menu)
    }

    fun getDP(dp: Int) : Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun complete_game() {
        var finishGame = Intent(this, GameFinish::class.java)
        startActivity(finishGame)
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        if (v != ui.pauseButton) {
            if (v != null) {
                if (v.id == id_list[btnNow-1]) {
                    btnNow++

                    (v as Button).setText("\u2713")
                }
            }
        }
    }
}


