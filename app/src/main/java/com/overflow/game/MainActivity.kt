package com.overflow.game

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class WordClue(val word: String, val clue: String)

class MainActivity : AppCompatActivity() {

    private lateinit var gridArray: Array<CharArray>
    private var numRows = 0
    private var numCols = 0

    private lateinit var wordClues: List<WordClue>

    private var selectedPosition: Pair<Int, Int>? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gridLayout: GridLayout
    private val gridString = ".....ل.x..س..بx..و..نxندرالاx..ي..نx.قارع.."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getPreferences(MODE_PRIVATE)

        gridLayout = findViewById(R.id.gridLayout)

        val rows = mutableListOf<CharArray>()
        var currentRow = mutableListOf<Char>()

        for (char in gridString) {
            if (char == '\\' || char == 'x') {
                rows.add(currentRow.toCharArray())
                currentRow = mutableListOf()
            } else {
                currentRow.add(char)
            }
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow.toCharArray())
        }

        numRows = rows.size
        numCols = rows.maxOf { it.size }

        gridArray = Array(numRows) { row ->
            CharArray(numCols) { col ->
                rows.getOrNull(row)?.getOrNull(col) ?: '.'
            }
        }

        wordClues = listOf(
            WordClue("سوريا", "عاصمتها دمشق"),
            WordClue("عراق", "بلاد الرافديت"),
            WordClue("قارع", "wtf"),
            WordClue("مصر", "ام الدنبا"),
            WordClue("الاردن", "جنوب سوريا"),
            WordClue("ندرالا", "aan"),
            WordClue("لبنان", "الارز"),
            WordClue("ana", "ana"),
            // Add more word-clue pairs as needed
        )

        initializeGridLayout()

        val showWordsButton: Button = findViewById(R.id.showWordsButton)
        showWordsButton.setOnClickListener {
            showWords()
        }

        val savedUserAnswer = sharedPreferences.getString("userAnswer", "")
        updateUIWithSavedState(savedUserAnswer)
    }

    private fun initializeGridLayout() {
        gridLayout.removeAllViews()

        val parentWidth = resources.displayMetrics.widthPixels
        val cellWidth = parentWidth / numCols

        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                val editText = createEditText(i, j, cellWidth)
                gridLayout.addView(editText)

                val wordClue = findWordClueByPosition(i, j)
                editText.tag = wordClue

                editText.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_UP -> {
                            selectedPosition = Pair(i, j)
                            showClueToast()
                            // Handle touch release if needed
                        }
                    }
                    false // Return false to allow normal touch processing, e.g., keyboard input
                }
            }
        }
    }

    private fun createEditText(i: Int, j: Int, cellWidth: Int): EditText {
        val editText = EditText(this)
        editText.textSize = 18f
        editText.gravity = Gravity.CENTER
        editText.setPadding(20, 20, 20, 20)
        editText.setTextColor(resources.getColor(android.R.color.black))

        val params = GridLayout.LayoutParams()
        params.rowSpec = GridLayout.spec(i)
        params.columnSpec = GridLayout.spec(j)
        params.width = cellWidth
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        editText.text = null

        if (gridArray[i][j] == '.') {
            editText.setBackgroundColor(Color.TRANSPARENT)
            editText.isEnabled = false
        } else {
            editText.setBackgroundResource(R.drawable.edittext_border)
        }

        editText.layoutParams = params
        return editText
    }



    private fun createButton(i: Int, j: Int, cellWidth: Int): EditText {
        val editText = EditText(this)
        editText.textSize = 18f
        editText.gravity = Gravity.CENTER
        editText.setPadding(20, 20, 20, 20)
        editText.setTextColor(resources.getColor(android.R.color.black))

        val params = GridLayout.LayoutParams()
        params.rowSpec = GridLayout.spec(i)
        params.columnSpec = GridLayout.spec(j)
        params.width = cellWidth
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        editText.text = null

        if (gridArray[i][j] == '.') {
            // Set the background color to transparent for empty cells
            editText.setBackgroundColor(Color.TRANSPARENT)
            editText.isEnabled = false
        } else {
            editText.setBackgroundResource(R.drawable.edittext_border)
        }

        editText.layoutParams = params
        return editText
    }

    private fun findWordClueByPosition(row: Int, col: Int): WordClue? {
        val horizontalWord = StringBuilder()
        for (j in col downTo 0) {
            val char = gridArray[row][j]
            if (char == '.') {
                break
            }
            horizontalWord.insert(0, char)
        }
        for (j in col + 1 until numCols) {
            val char = gridArray[row][j]
            if (char == '.') {
                break
            }
            horizontalWord.append(char)
        }

        val verticalWord = StringBuilder()
        for (i in row downTo 0) {
            val char = gridArray[i][col]
            if (char == '.') {
                break
            }
            verticalWord.insert(0, char)
        }
        for (i in row + 1 until numRows) {
            val char = gridArray[i][col]
            if (char == '.') {
                break
            }
            verticalWord.append(char)
        }

        val matchingWordClues = wordClues.filter { it.word == horizontalWord.toString() || it.word == verticalWord.toString() }

        return matchingWordClues.minByOrNull {
            when {
                it.word == horizontalWord.toString() && col in 0..it.word.length -> 0
                it.word == verticalWord.toString() && row in 0..it.word.length -> 0
                else -> 1
            }
        }
    }

    private fun showClueToast() {
        val verticalHintTextView: TextView = findViewById(R.id.verticalWordsHintTextView)
        val horizontalHintTextView: TextView = findViewById(R.id.horizontalWordsHintTextView)

        selectedPosition?.let { (row, col) ->
            val horizontalWord = findHorizontalWord(row, col)
            val verticalWord = findVerticalWord(row, col)

            val horizontalWordClue = wordClues.find { it.word == horizontalWord }
            val verticalWordClue = wordClues.find { it.word == verticalWord }

            val horizontalHint = horizontalWordClue?.let { "Horizontal Word Hint: ${it.clue}" } ?: ""
            val verticalHint = verticalWordClue?.let { "Vertical Word Hint: ${it.clue}" } ?: ""

            horizontalHintTextView.text = horizontalHint
            verticalHintTextView.text = verticalHint
        }
    }

    private fun findHorizontalWord(row: Int, col: Int): String {
        val horizontalWord = StringBuilder()
        for (j in (col downTo 0)) {
            val char = gridArray[row][j]
            if (char == '.') {
                break
            }
            horizontalWord.insert(0, char)
        }
        for (j in (col + 1 until numCols)) {
            val char = gridArray[row][j]
            if (char == '.') {
                break
            }
            horizontalWord.append(char)
        }
        return horizontalWord.toString().reversed()
    }

    private fun findVerticalWord(row: Int, col: Int): String {
        val verticalWord = StringBuilder()
        for (i in (row downTo 0)) {
            val char = gridArray[i][col]
            if (char == '.') {
                break
            }
            verticalWord.insert(0, char)
        }
        for (i in (row + 1 until numRows)) {
            val char = gridArray[i][col]
            if (char == '.') {
                break
            }
            verticalWord.append(char)
        }
        return verticalWord.toString()
    }




    private fun showWords() {
        val verticalWords = extractVerticalWords()
        val horizontalWords = extractHorizontalWords()

        val verticalWordsTextView: TextView = findViewById(R.id.verticalWordsTextView)
        val horizontalWordsTextView: TextView = findViewById(R.id.horizontalWordsTextView)

        val verticalWordsList = buildWordsList(verticalWords)
        val horizontalWordsList = buildWordsList(horizontalWords)

        verticalWordsTextView.text = verticalWordsList
        horizontalWordsTextView.text = horizontalWordsList
    }




    fun showCellHint(view: View) {
        if (selectedPosition != null) {
            val (i, j) = selectedPosition!!
            val gridLayout: GridLayout = findViewById(R.id.gridLayout)
            val index = i * numCols + j
            val editText = gridLayout.getChildAt(index) as? EditText

            // Set the text of the selected cell to the corresponding letter in gridArray
            editText?.text = Editable.Factory.getInstance().newEditable(gridArray[i][j].toString())

            // Clear the selected position after showing the hint
            selectedPosition = null
        }
    }


    fun showHint(view: View) {
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                val index = i * numCols + j
                val editText = gridLayout.getChildAt(index) as? EditText

                if (gridArray[i][j] != '.') {
                    editText?.text = Editable.Factory.getInstance().newEditable(gridArray[i][j].toString())
                }
            }
        }
    }

    private fun buildWordsList(words: List<String>): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        for ((index, word) in words.withIndex()) {
            val coloredWord = "$word\n"
            builder.append(coloredWord)
            builder.setSpan(
                ForegroundColorSpan(Color.RED),
                builder.length - coloredWord.length,
                builder.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return builder
    }

    private fun extractVerticalWords(): List<String> {
        val verticalWords = mutableListOf<String>()

        for (j in 0 until numCols) {
            var word = ""
            for (i in 0 until numRows) {
                val char = gridArray[i][j]
                if (char != '.') {
                    word += char
                } else if (word.isNotEmpty()) {
                    if (word.length > 2) {
                        verticalWords.add("${verticalWords.size + 1}. $word")
                    }
                    word = ""
                }
            }

            if (word.isNotEmpty() && word.length > 2) {
                verticalWords.add("${verticalWords.size + 1}. $word")
            }
        }

        return verticalWords
    }

    private fun extractHorizontalWords(): List<String> {
        val horizontalWords = mutableListOf<String>()

        for (i in 0 until numRows) {
            var word = ""
            for (j in 0 until numCols) {
                val char = gridArray[i][j]
                if (char != '.') {
                    word += char
                } else if (word.isNotEmpty()) {
                    if (word.length > 2) {
                        horizontalWords.add("${horizontalWords.size + 1}. ${word.reversed()}")
                    }

                    word = ""
                }
            }

            if (word.isNotEmpty() && word.length > 2) {
                horizontalWords.add("${horizontalWords.size + 1}. ${word.reversed()}")
            }
        }

        return horizontalWords
    }

    fun resetGame(view: View) {
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                val index = i * numCols + j
                val editText = gridLayout.getChildAt(index) as? EditText

                editText?.text?.clear()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val editor = sharedPreferences.edit()
        editor.putString("userAnswer", getUserAnswer())
        editor.apply()
    }

    private fun getUserAnswer(): String {
        var userAnswer = ""
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                val index = i * numCols + j
                val editText = gridLayout.getChildAt(index) as? EditText
                userAnswer += if (gridArray[i][j] == '.') "" else editText?.text.toString()
            }
            userAnswer += ""
        }

        if (userAnswer.endsWith("x")) {
            userAnswer = userAnswer.removeSuffix("x")
        }

        return userAnswer
    }

    fun checkAnswer(view: View) {
        val userAnswer = getUserAnswer()
        val originalGrid = getOriginalGrid()

        // Check if the user grid matches the original grid
        val isGridMatching = userAnswer == originalGrid

        // Display a Toast with user grid and the original grid
        val toastMessage = "User Grid: $userAnswer\nOriginal Grid: $originalGrid"
        val toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG)
        toast.show()

        if (isGridMatching) {
            // Handle the case where the user grid matches the original grid
            // Display an appropriate message, maybe show a success dialog, etc.
            val successMessage = "Congratulations! You've solved the puzzle!"
            val successToast = Toast.makeText(this, successMessage, Toast.LENGTH_LONG)
            successToast.show()
        } else {
            // Handle the case where the user grid does not match the original grid
            // Display a message, show a hint, etc.
            val incorrectMessage = "Incorrect. Try again."
            val incorrectToast = Toast.makeText(this, incorrectMessage, Toast.LENGTH_SHORT)
            incorrectToast.show()
        }
    }

    private fun getOriginalGrid(): String {
        return gridString.replace("x", "").replace(".", "")
    }

    private fun getCorrectAnswer(): String {
        val correctAnswer = gridString
        return correctAnswer.replace("x", "").replace(".", "")
    }



    private fun updateUIWithSavedState(savedUserAnswer: String?) {
        if (!savedUserAnswer.isNullOrBlank()) {
            val gridLayout: GridLayout = findViewById(R.id.gridLayout)

            for (i in 0 until numRows) {
                for (j in 0 until numCols) {
                    val index = i * numCols + j
                    val button = gridLayout.getChildAt(index) as? Button
                    button?.text = savedUserAnswer.getOrNull(i * numCols + j)?.toString() ?: ""
                }
            }
        }
    }

}


