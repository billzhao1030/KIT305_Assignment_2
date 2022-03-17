package au.edu.utas.xunyiz.kit305.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {
    private lateinit var ui: ActivityHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(ui.root)


    }
}