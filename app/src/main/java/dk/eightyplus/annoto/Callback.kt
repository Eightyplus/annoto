package dk.eightyplus.annoto

import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component

/**
 *
 */
interface Callback {

    var state: State

    fun textEditDone()

    fun move(component: Component, dx: Float, dy: Float, scale: Float)

    fun setStrokeWidth(width: Int)

    fun add(undo: Undo)

    fun load(fileName: String)

    fun colorChanged(color: Int)

    fun hexColorForResourceId(color: Int): Int
}
