package au.edu.utas.xunyiz.kit305.assignment2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
    var roundCompleted = 0
    var timeLeft: Long = 0

    var id = ""
    var btnNow = 1

    var id_list = Array<Int>(numOfButtons, {i -> i})

    var buttonList: ArrayList<Map<String, Int>> = arrayListOf()

    lateinit var timer: CountDownTimer

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

        // get database connection
        var db = Firebase.firestore
        var games = db.collection("games")

        //get the id and set
        id = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        var currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
        var dataMap = hashMapOf(
            "completed" to completed,
            "startTime" to currentTime,
            "endTime" to currentTime,
            "gameMode" to gameMode,
            "gameType" to true,
            "repetition" to 0,
            "buttonList" to buttonList
        )
        games.document(id)
            .set(dataMap)
            .addOnSuccessListener { Log.d(database_log, "prescribe new game") }
            .addOnFailureListener { Log.d(database_log, "prescribe new game fail")}

        if (gameMode) {
            if (round != -1) {
                ui.progressText.text = "1 of ${round} round"
            } else {
                startTimer()
            }
        } else {
            ui.progressText.text = "Round 1"
        }

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
        timeLeft = (time * 1000).toLong()
    }

    @SuppressLint("ResourceType")
    private fun startPrescribedGame() {
        for (button in 1..numOfButtons) {
            var btn: Button = Button(this)
            btn.id = button
            id_list[button - 1] = btn.id

            btn.text = "btn ${button}"
            btn.setBackgroundResource(R.drawable.round_button)
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, ((buttonRaduis-40)/20+7)*4f)


            val metrics = Resources.getSystem().displayMetrics
            var scale:Int = (buttonRaduis * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
            btn.layoutParams = ViewGroup.LayoutParams(scale, scale)

            ui.prescribed.addView(btn)

            btn.setOnClickListener(this)
        }

        if (btnNow != numOfButtons) {
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
        highlight(id_list[0])
    }

    private fun reposition(id_list: Array<Int>) {
        var x_point = setLocationX()
        var y_point = setLocationY()

        for (button in 1..numOfButtons) {
            if (isRandom) {
                var setting = ConstraintSet()
                var mainWindow = ui.prescribed
                setting.clone(mainWindow)

                var marginStart: Int = getDP(x_point[button - 1])
                var marginBottom: Int = getDP(y_point[button - 1])

                setting.connect(id_list[button-1], ConstraintSet.START, ui.root.id, ConstraintSet.START, marginStart)
                setting.connect(id_list[button-1], ConstraintSet.BOTTOM, ui.root.id, ConstraintSet.BOTTOM, marginBottom)

                setting.applyTo(mainWindow)
            }

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
            var random = (0..4).random() // shuffle
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

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeft, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                complete_game()
            }
        }.start()
    }
    private fun pauseTimer() {
        timer.cancel()
    }
    private fun updateTimerText() {
        var minutes = ((timeLeft / 1000) / 60).toInt()
        var seconds = ((timeLeft / 1000) % 60).toInt()

        ui.progressText.text = "${minutes} min ${seconds}s left "
    }


    fun showPausePopup(view: View) {
        if (gameMode) {
            if (round == -1) {
                pauseTimer()
            }
        }
        pausePopup.show()
    }

    fun closePausePopup(view: View) {
        pausePopup.cancel()
        if (gameMode) {
            if (round == -1) {
                startTimer()
            }
        }
    }

    fun getDP(dp: Int) : Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun complete_game() {
        if (gameMode) {
            completed = true
        }

        var db = Firebase.firestore
        var games = db.collection("games")

        var currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())

        games.document(id)
            .update("endTime", currentTime)
            .addOnSuccessListener { Log.d(database_log, "endtime update") }
            .addOnFailureListener { Log.d(database_log, "endtime not update")}
        games.document(id)
            .update("completed", completed)
            .addOnSuccessListener { Log.d(database_log, "endtime update") }
            .addOnFailureListener { Log.d(database_log, "endtime not update")}

        if (gameMode || completed) {
            var finishGame = Intent(this, GameFinish::class.java)

            finishGame.putExtra("ID", id)
            finishGame.putExtra("completed", completed)

            startActivity(finishGame)
        } else {
            var menu = Intent(this, MainActivity::class.java)
            startActivity(menu)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        if (v != ui.pauseButton) {
            if (v != null) {
                var time = SimpleDateFormat("HH:mm:ss").format(Date())
                if (v.id == id_list[btnNow - 1]) {
                    var buttonRecord = mapOf<String, Int>(
                        time to btnNow
                    )

                    buttonList.add(buttonRecord)
                    uploadButtonList()
                    (v as Button).setText("\u2713")

                    btnNow++
                    if (btnNow == numOfButtons + 1) {
                        btnNow = 1
                        roundCompleted++

                        if (!gameMode) {
                            completed = true
                            pause.goToMenu.text = "Finish Exercise"
                        }

                        uploadRound()
                        if (gameMode) {
                            if (this.round != -1) {
                                if (roundCompleted == round) {
                                    complete_game()
                                } else {
                                    reposition(id_list)
                                    ui.progressText.text = "${roundCompleted + 1} of ${round} round"
                                }
                            } else{
                                reposition(id_list)
                            }
                        } else {
                            ui.progressText.text = "Round ${roundCompleted}"
                            reposition(id_list)
                        }
                    }
                    highlight(btnNow)
                } else {
                    var buttonRecord = mapOf<String, Int>(
                        time to v.id * 10
                    )

                    buttonList.add(buttonRecord)
                    uploadButtonList()
                }
            }
        }
    }

    private fun uploadRound() {
        var db = Firebase.firestore
        var games = db.collection("games")

        games.document(id)
            .update("repetition", roundCompleted)
    }

    private fun uploadButtonList() {
        var db = Firebase.firestore
        var games = db.collection("games")

        games.document(id)
            .update("buttonList", buttonList)
            .addOnSuccessListener { Log.d(database_log, "buttonList update") }
            .addOnFailureListener { Log.d(database_log, "buttonList not update")}
    }

    private fun highlight(button: Int) {
        if (hasIndication) {
            var btn: Button = findViewById<Button>(id_list[button - 1])
            btn.setBackgroundResource(R.drawable.round_button_highlight)
            if (button != 1) {
                btn = findViewById<Button>(id_list[button - 2])
                btn.setBackgroundResource(R.drawable.round_button)
            } else {
                btn = findViewById<Button>(id_list[id_list.size - 1])
                btn.setBackgroundResource(R.drawable.round_button)
            }
        }
    }

    fun goToMenu(view: View) {
        if (gameMode) {
            var menu = Intent(this, MainActivity::class.java)
            startActivity(menu)
        } else {
            complete_game()
        }
    }
}


