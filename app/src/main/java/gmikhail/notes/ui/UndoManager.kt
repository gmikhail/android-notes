package gmikhail.notes.ui

import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged

private const val MAX_UNDO = 1024

class UndoManager(private val editText: AppCompatEditText,
                  private val textChangedListener: TextChangedListener
) {
    private val history: MutableList<UndoHistoryRecord> = mutableListOf()
    private var index = 0
    private var inProgress = false

    init {
        editText.doOnTextChanged { text, _, _, _ ->
            if(!inProgress) {
                // For some reason when this method called first time selectionEnd is always 0,
                // even if EditText has text with cursor on the end
                val cursorPosition =
                    if(history.isEmpty()) text.toString().length
                    else editText.selectionEnd
                history.add(UndoHistoryRecord(text.toString(), cursorPosition))
                if (history.lastIndex > MAX_UNDO) {
                    history.removeAt(0)
                }
                index = history.lastIndex
            }
            textChangedListener.onTextChangedListener(canUndo(), canRedo())
        }
    }

    fun undo(){
        if(canUndo()){
            inProgress = true
            index--
            val state = history[index]
            editText.setText(state.text)
            editText.setSelection(state.cursorPosition)
            inProgress = false
        }
    }

    fun redo(){
        if(canRedo()) {
            inProgress = true
            index++
            val state = history[index]
            editText.setText(state.text)
            editText.setSelection(state.cursorPosition)
            inProgress = false
        }
    }

    private fun canUndo(): Boolean{
        return history.isNotEmpty() && index - 1 in history.indices
    }

    private fun canRedo(): Boolean{
        return history.isNotEmpty() && index + 1 in history.indices
    }
}

private data class UndoHistoryRecord(val text: String, val cursorPosition: Int)

class TextChangedListener(val textChangeListener: (canUndo: Boolean, canRedo: Boolean) -> Unit) {
    fun onTextChangedListener(canUndo: Boolean, canRedo: Boolean) =
        textChangeListener(canUndo, canRedo)
}