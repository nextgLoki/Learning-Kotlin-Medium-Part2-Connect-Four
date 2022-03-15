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
    private var isGameOver = false
    private var isForceEnd = false
    private var currentGameNumber = 0

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
            .append(if (gamesCount == 1) "Single game" else "Total $gamesCount games").let(::println)

    private fun drawBoard() {
        val title = List(columnsCount) {
            it + 1
        }.joinToString(separator = " ", prefix = " ")
        println(title)

        repeat(rowsCount) {
            board.forEach { columns -> print("║${columns[rowsCount - 1 - it].item}") }
            println("║")
        }

        println("╚" + "═╩".repeat(columnsCount - 1) + "═╝")
    }

    private fun selectNextPlayer() {
        if (currentPlayerIndex + 1 >= players.size) {
            currentPlayerIndex = 0
        } else {
            currentPlayerIndex++
        }
    }

    private fun addDiscOnBoard(column: Int): Boolean {
        val columnDiscs = board[column - 1]
        val firstEmptyCell = columnDiscs.indexOfFirst { it == BoardCell.EMPTY }
        if (firstEmptyCell >= 0) {
            columnDiscs[firstEmptyCell] = currentPlayer.disc
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

    private fun isVerticalWin() = board.any {
        it.joinToString("").contains(currentPlayer.disc.toString().repeat(WIN_CELLS_COUNT))
    }

    private fun isHorizontalWin(): Boolean {
        var countInRow: Int
        val playerCellType = currentPlayer.disc.item
        repeat(rowsCount) { rowIndex ->
            countInRow = 0
            repeat(columnsCount) { columnIndex ->
                if (board[columnIndex][rowIndex].item == playerCellType) {
                    countInRow++
                    if (countInRow == WIN_CELLS_COUNT) {
                        return true
                    }
                } else {
                    countInRow = 0
                }
            }
        }
        return false
    }

    private fun isDiagonallyWin(): Boolean {
        val winLines = mutableMapOf<Pair<Int, Int>, MutableList<Pair<Int, Int>>>()

        // top border
        for (i in 0..columnsCount - WIN_CELLS_COUNT) {
            val lines = mutableListOf<Pair<Int, Int>>()
            winLines[i to 0] = lines
            for (columnIndex in i until columnsCount) {
                for (rowIndex in 0 until rowsCount) {
                    if (columnIndex - rowIndex == i)
                        lines.add(columnIndex to rowIndex)
                }
            }
        }

        // right border
        val rightBorderIndex = columnsCount - 1
        for (j in 0..rowsCount - WIN_CELLS_COUNT) {
            val lines = mutableListOf<Pair<Int, Int>>()
            winLines[rightBorderIndex to j] = lines
            for (columnIndex in rightBorderIndex downTo 0) {
                for (rowIndex in j until rowsCount) {
                    if (columnIndex + rowIndex == rightBorderIndex)
                        lines.add(columnIndex to rowIndex)
                }
            }
        }

        // bottom border
        val bottomBorderIndex = columnsCount - 1
        for (i in WIN_CELLS_COUNT - 1 until columnsCount) {
            val lines = mutableListOf<Pair<Int, Int>>()
            winLines[i to bottomBorderIndex] = lines
            for (columnIndex in i downTo 0) {
                for (rowIndex in rowsCount - 1 downTo 0) {
                    if (columnIndex - rowIndex == i - bottomBorderIndex)
                        lines.add(columnIndex to rowIndex)
                }
            }
        }

        // left border
        for (j in WIN_CELLS_COUNT - 1 until rowsCount) {
            val lines = mutableListOf<Pair<Int, Int>>()
            winLines[0 to j] = lines
            for (columnIndex in 0 until columnsCount) {
                for (rowIndex in j downTo 0) {
                    if (rowIndex + columnIndex == j)
                        lines.add(columnIndex to rowIndex)
                }
            }
        }

        val winningCells = currentPlayer.disc.toString().repeat(WIN_CELLS_COUNT)
        return winLines.any {
            it.value.joinToString("") { indexes ->
                board[indexes.first][indexes.second].item.toString()
            }.contains(winningCells)
        }
    }

    private fun processGameOver() {
        if (isHorizontalWin() || isVerticalWin() || isDiagonallyWin()) {
            with(currentPlayer) {
                println("Player $name won")
                points += 2
            }
            isGameOver = true
        } else if (board.flatten().all { it != BoardCell.EMPTY }) {
            println("It is a draw")
            players.forEach {
                it.points += 1
            }
            isGameOver = true
        }
    }

    private fun getPlayerScoresLine(index: Int) = "${players[index].name}: ${players[index].points}"

    private fun resetBoard() = board.forEach {
        it.fill(BoardCell.EMPTY)
    }

    fun play() {
        printGameState()
        do {
            currentGameNumber++
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
        } while (currentGameNumber < gamesCount)
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

        val entries = Regex("\\s*(\\d+)\\s*[x|X]\\s*(\\d+)\\s*").matchEntire(input)
        if (entries == null) {
            println("Invalid input")
        } else {
            val size = entries.destructured.let { (rows, columns) ->
                {
                    rows.toInt() to columns.toInt()
                }
            }.invoke()

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