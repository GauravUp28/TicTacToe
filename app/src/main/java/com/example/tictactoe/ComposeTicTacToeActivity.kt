package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ComposeTicTacToeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeApp()
        }
    }
}

// App Navigation
@Composable
fun TicTacToeApp() {
    var currentScreen by remember { mutableStateOf("Menu") }
    var selectedDifficulty by remember { mutableStateOf("Hard") }

    when (currentScreen) {
        "Menu" -> MenuScreen(
            onStartGame = { difficulty ->
                selectedDifficulty = difficulty
                currentScreen = "Game"
            },
            onExit = { /* Handle exit logic if needed */ }
        )
        "Game" -> TicTacToeScreen(selectedDifficulty, onBackToMenu = { currentScreen = "Menu" })
    }
}

// 🎨 Gradient Background
@Composable
fun gradientBackground(): Brush {
    return Brush.verticalGradient(
        colors = listOf(Color(0xFF1E3B11), Color(0xFF2A5298)) // Deep Blue gradient
    )
}

// 🎮 Main Menu Screen
@Composable
fun MenuScreen(onStartGame: (String) -> Unit, onExit: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var selectedDifficulty by remember { mutableStateOf("Hard") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tic-Tac-Toe", fontSize = 32.sp, color = textColor)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Difficulty:", fontSize = 20.sp, color = textColor)
        Row {
            listOf("Easy", "Medium", "Hard").forEach { level ->
                Button(
                    onClick = { selectedDifficulty = level },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDifficulty == level) Color.Green else Color.Gray
                    ),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(level, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onStartGame(selectedDifficulty) }) {
            Text("Start Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onExit) {
            Text("Exit")
        }
    }
}

@Composable
fun TicTacToeScreen(difficulty: String, onBackToMenu: () -> Unit) {
    // Game state variables
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var winningCells by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var scoreX by remember { mutableIntStateOf(0) }
    var scoreO by remember { mutableIntStateOf(0) }

    // Dark mode support
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val borderColor = if (isDarkTheme) Color.LightGray else Color.Black

    // Animated text for smooth scaling effect
    @Composable
    fun AnimatedText(text: String) {
        val scale = remember { Animatable(0f) }

        LaunchedEffect(text) {
            if (text.isNotEmpty()) {
                scale.snapTo(0f)
                scale.animateTo(1f, animationSpec = tween(300))
            }
        }

        Text(
            text = text,
            fontSize = 32.sp,
            modifier = Modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Difficulty: $difficulty", fontSize = 18.sp, color = textColor)

        Spacer(modifier = Modifier.height(16.dp))

        // Score display
        Text(text = "Score - X: $scoreX | O: $scoreO", fontSize = 20.sp, color = textColor)

        Spacer(modifier = Modifier.height(16.dp))

        // Game status
        Text(
            text = winner?.let { "$it Wins!" } ?: "Player: $currentPlayer",
            fontSize = 24.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Function to check if there is a winner
        fun checkWinner(board: List<List<String>>): String? {
            val winPatterns = listOf(
                listOf(0 to 0, 0 to 1, 0 to 2),
                listOf(1 to 0, 1 to 1, 1 to 2),
                listOf(2 to 0, 2 to 1, 2 to 2),
                listOf(0 to 0, 1 to 0, 2 to 0),
                listOf(0 to 1, 1 to 1, 2 to 1),
                listOf(0 to 2, 1 to 2, 2 to 2),
                listOf(0 to 0, 1 to 1, 2 to 2),
                listOf(0 to 2, 1 to 1, 2 to 0)
            )

            for (pattern in winPatterns) {
                val (r1, c1) = pattern[0]
                val (r2, c2) = pattern[1]
                val (r3, c3) = pattern[2]

                if (board[r1][c1].isNotEmpty() &&
                    board[r1][c1] == board[r2][c2] &&
                    board[r1][c1] == board[r3][c3]
                ) {
                    return board[r1][c1]
                }
            }
            return null
        }

        // Easy Mode (Random Moves)
        fun getRandomMove(): Pair<Int, Int>? {
            val emptyCells = board.flatMapIndexed { rowIndex, row ->
                row.mapIndexedNotNull { colIndex, cell ->
                    if (cell.isEmpty()) Pair(rowIndex, colIndex) else null
                }
            }
            return if (emptyCells.isNotEmpty()) emptyCells.random() else null
        }

        // Medium Mode (Blocks Wins, Otherwise Random)
        fun getMediumMove(): Pair<Int, Int>? {
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col].isEmpty()) {
                        val newBoard = board.map { it.toMutableList() }
                        newBoard[row][col] = "O"
                        if (checkWinner(newBoard) == "O") return Pair(row, col) // AI Wins
                    }
                }
            }

            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col].isEmpty()) {
                        val newBoard = board.map { it.toMutableList() }
                        newBoard[row][col] = "X"
                        if (checkWinner(newBoard) == "X") return Pair(row, col) // Block player
                    }
                }
            }

            return getRandomMove()
        }

        // Minimax Algorithm for Unbeatable AI
        fun minimax(board: List<List<String>>, isMaximizing: Boolean): Int {
            val winner = checkWinner(board)
            if (winner == "X") return -1
            if (winner == "O") return 1
            if (board.all { row -> row.all { it.isNotEmpty() } }) return 0

            val scores = mutableListOf<Int>()
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col].isEmpty()) {
                        val newBoard = board.map { it.toMutableList() }
                        newBoard[row][col] = if (isMaximizing) "O" else "X"
                        scores.add(minimax(newBoard, !isMaximizing))
                    }
                }
            }
            return if (isMaximizing) scores.maxOrNull() ?: 0 else scores.minOrNull() ?: 0
        }

        // Hard Mode (Minimax AI)
        fun getBestMove(board: List<List<String>>): Pair<Int, Int>? {
            var bestMove: Pair<Int, Int>? = null
            var bestScore = Int.MIN_VALUE

            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col].isEmpty()) {
                        val newBoard = board.map { it.toMutableList() }
                        newBoard[row][col] = "O"
                        val score = minimax(newBoard, isMaximizing = false)

                        if (score > bestScore) {
                            bestScore = score
                            bestMove = Pair(row, col)
                        }
                    }
                }
            }
            return bestMove
        }

        // AI Move Handling
        fun makeAIMove(difficulty: String) {
            if (winner == null) {
                val bestMove = when (difficulty) {
                    "Easy" -> getRandomMove()
                    "Medium" -> getMediumMove()
                    else -> getBestMove(board) // Hard Mode - Minimax AI
                }

                bestMove?.let { (row, col) ->
                    board = board.toMutableList().also {
                        it[row] = it[row].toMutableList().also { rowList ->
                            rowList[col] = "O"
                        }
                    }
                    winner = checkWinner(board)
                    if (winner != null) {
                        if (winner == "X") scoreX++
                        if (winner == "O") scoreO++
                    } else {
                        currentPlayer = "X"
                    }
                }
            }
        }

        // Tic-Tac-Toe board
        for (row in 0..2) {
            Row {
                for (col in 0..2) {
                    val isWinningCell = winningCells.contains(row to col)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(
                                2.dp,
                                if (isWinningCell) Color.Green else borderColor,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (board[row][col].isEmpty() && winner == null && currentPlayer == "X") {
                                    board = board.toMutableList().also {
                                        it[row] = it[row].toMutableList().also { rowList ->
                                            rowList[col] = "X"
                                        }
                                    }
                                    winner = checkWinner(board)
                                    if (winner != null) {
                                        if (winner == "X") scoreX++
                                        if (winner == "O") scoreO++
                                    } else {
                                        currentPlayer = "O"
                                        makeAIMove(difficulty)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedText(text = board[row][col])
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Restart button
        Button(onClick = {
            board = List(3) { MutableList(3) { "" } }
            currentPlayer = "X"
            winner = null
            winningCells = emptyList()
        }) {
            Text("Restart Game", color = textColor)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(onClick = onBackToMenu) {
            Text("Back to Menu")
        }
    }
}
