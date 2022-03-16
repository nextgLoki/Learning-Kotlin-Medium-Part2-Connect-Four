package connectfour

class Player(val name: String, val disc: BoardCell, var points: Int)

enum class BoardCell(val item: Char) {
    DISC_PLAYER1('o'),
    DISC_PLAYER2('*'),
    EMPTY(' ');

    override fun toString() = item.toString()
}

const val WIN_CELLS_COUNT = 4

class Game(
    playerName1: String, playerName2: String,
    borderSize: Pair<Int, Int>,
    private val gamesCount: Int,
) {
    private val rowsCount = borderSize.first
    private val columnsCount = borderSize.second
    private val board = MutableList(columnsCount) {
        MutableList(rowsCount) { BoardCell.EMPTY }
    }
    private var lastChangedCell = -1 to -1
    private var isGameOver = false
    private var isForceEnd = false
    private var currentGameNumber = 1

    private val players = listOf(
        Player(playerName1, BoardCell.DISC_PLAYER1, 0),
        Player(playerName2, BoardCell.DISC_PLAYER2, 0),
    )
    private var currentPlayerIndex = 0
    private val currentPlayer
        get() = players[currentPlayerIndex]

    private fun printGameState() =
        StringBuilder("${players[0].name} VS ${players[1].name}")
            .appendLine("\n$rowsCount X $columnsCount board")
            .append(if (gamesCount == 1) "Single game" else "Total $gamesCount games")
            .let(::println)

    private fun drawBoard() {
        List(columnsCount) { it + 1 }
            .joinToString(separator = " ", prefix = " ")
            .let(::println)

        repeat(rowsCount) {
            board.forEach { columns -> print("║${columns[rowsCount - 1 - it].item}") }
            println("║")
        }

        println("╚" + "═╩".repeat(columnsCount - 1) + "═╝")
    }

    private fun selectNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    private fun addDiscOnBoard(column: Int): Boolean {
        val columnIndex = column - 1
        val columnDiscs = board[columnIndex]
        val firstEmptyCell = columnDiscs.indexOfFirst { it == BoardCell.EMPTY }
        if (firstEmptyCell >= 0) {
            columnDiscs[firstEmptyCell] = currentPlayer.disc
            lastChangedCell = columnIndex to firstEmptyCell
            return true
        }
        return false
    }

    private fun processPlayerTurn() {
        currentPlayer.apply {
            while (true) {
                println("$name's turn:")
                val input = readLine()!!
                if (input == "end") {
                    isForceEnd = true
                    return
                }
                when (val column = input.toIntOrNull()) {
                    null -> println("Incorrect column number")
                    !in 1..columnsCount -> println("The column number is out of range (1 - $columnsCount)")
                    else -> {
                        if (addDiscOnBoard(column)) {
                            drawBoard()
                            return
                        } else {
                            println("Column $column is full")
                        }
                    }
                }
            }
        }
    }

    private fun getDiscsInRow(way: Pair<Int, Int>): Int {
        val playerCellType = currentPlayer.disc.item
        var count = 0
        var pos = lastChangedCell
        while (pos.first in 0 until columnsCount && pos.second in 0 until rowsCount) {
            if (board[pos.first][pos.second].item == playerCellType) {
                count++
            } else {
                break
            }
            pos = pos.first + way.first to pos.second + way.second
        }
        return count
    }

    private fun isPlayerWin(): Boolean {
        listOf(
            Pair(0 to 1, 0 to -1), // vertical win line
            Pair(1 to 0, -1 to 0), // horizontal win line
            Pair(1 to 1, -1 to -1), // diagonal win line
            Pair(1 to -1, -1 to 1), // another diagonal win line
        ).forEach {
            if (getDiscsInRow(it.first) + getDiscsInRow(it.second) - 1 >= WIN_CELLS_COUNT) {
                return true
            }
        }
        return false
    }

    private fun processGameOver() {
        if (isPlayerWin()) {
            with(currentPlayer) {
                println("Player $name won")
                points += 2
            }
            isGameOver = true
        } else if (board.flatten().all { it != BoardCell.EMPTY }) {
            println("It is a draw")
            players.forEach { it.points += 1 }
            isGameOver = true
        }
    }

    private fun getPlayerScoresLine(index: Int) = "${players[index].name}: ${players[index].points}"

    private fun resetBoard() = board.forEach { it.fill(BoardCell.EMPTY) }

    fun play() {
        printGameState()
        do {
            if (gamesCount > 1) {
                println("Game #$currentGameNumber")
            }
            drawBoard()
            do {
                processPlayerTurn()
                processGameOver()
                selectNextPlayer()
            } while (!isGameOver && !isForceEnd)

            if (isForceEnd) {
                break
            }

            if (gamesCount > 1) {
                println("Score\n${getPlayerScoresLine(0)} ${getPlayerScoresLine(1)}")
                resetBoard()
                isGameOver = false
                isForceEnd = false
            }
        } while (currentGameNumber++ < gamesCount)
        println("Game over!")
    }
}

fun main() {
    println("Connect Four")
    Game(
        requestPlayerName("First"),
        requestPlayerName("Second"),
        requestBoardSize(),
        requestGameMode(),
    ).play()
}

fun requestPlayerName(prefix: String) = println("$prefix player's name:").let { readLine()!! }

fun requestGameMode(): Int {
    do {
        println("""
            Do you want to play single or multiple games?
            For a single game, input 1 or press Enter
            Input a number of games:
            """.trimIndent())
        when (val input = readLine()!!) {
            "", "1" -> return 1
            else -> {
                val num = input.toIntOrNull()
                if (num != null && num > 0) {
                    return num
                }
            }
        }
        println("Invalid input")
    } while (true)
}

fun requestBoardSize(): Pair<Int, Int> {
    while (true) {
        println("Set the board dimensions (Rows x Columns)\nPress Enter for default (6 x 7)")
        val input = readLine()!!
        if (input.isEmpty()) {
            return 6 to 7
        }

        val entries = Regex("""\s*(\d+)\s*[xX]\s*(\d+)\s*""").matchEntire(input)
        if (entries == null) {
            println("Invalid input")
        } else {
            val size = entries.destructured.let { (rows, columns) ->
                rows.toInt() to columns.toInt()
            }

            if (size.first !in 5..9) {
                println("Board rows should be from 5 to 9")
            } else if (size.second !in 5..9) {
                println("Board columns should be from 5 to 9")
            } else {
                return size
            }
        }
    }
}