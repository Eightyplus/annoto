/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.eightyplus.annoto.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle

class ColorPickerDialog(context: Context,
                        private val mListener: OnColorChangedListener,
                        private val mInitialColor: Int) : Dialog(context) {


    interface OnColorChangedListener {
        fun colorChanged(color: Int)
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        val l = object : OnColorChangedListener {
            override fun colorChanged(color: Int) {
                mListener.colorChanged(color)
                dismiss()
            }
        }

        setContentView(ColorPickerView(context, l, mInitialColor))
        setTitle("Pick a Color")
    }
}
