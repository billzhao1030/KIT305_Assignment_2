package au.edu.utas.xunyiz.kit305.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityCustomizationBinding

class Customization : AppCompatActivity() {
    private lateinit var ui: ActivityCustomizationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityCustomizationBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.textView2.text = intent.getBooleanExtra("GameMode", true).toString()
        ui.textView3.text = intent.getIntExtra("Rounds", 5).toString()

        ui.button.setOnClickListener {
            finish()
        }
    }
}