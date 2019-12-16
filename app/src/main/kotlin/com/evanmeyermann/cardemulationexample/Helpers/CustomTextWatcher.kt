package com.evanmeyermann.cardemulationexample.helpers

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

//Custom Text Watcher
class CustomTextWatcher(e: EditText, m: String) : TextWatcher {

    private var _editText: EditText? = e
    private var _errorMessage: String = m

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if(_editText?.text!!.isEmpty())
            _editText?.error = _errorMessage
    }

    override fun afterTextChanged(s: Editable) {}
}