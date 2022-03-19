package au.edu.utas.xunyiz.kit305.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityDesignedGameBinding

class DesignedGame : AppCompatActivity() {
    private lateinit var ui: ActivityDesignedGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityDesignedGameBinding.inflate(layoutInflater)
        setContentView(ui.root)
    }
}