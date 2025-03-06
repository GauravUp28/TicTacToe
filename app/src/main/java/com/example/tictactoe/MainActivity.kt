package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the Compose-based Tic-Tac-Toe activity
        startActivity(Intent(this, ComposeTicTacToeActivity::class.java))
        finish() // Close MainActivity to prevent returning to it
    }
}

