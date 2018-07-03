package dk.eightyplus.annoto.action

import android.graphics.RectF
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.Text
import dk.eightyplus.annoto.view.ComponentList

/**
 * Undo (Redo) class contains the reverse action of an user interaction, so these can be reversed.
 */
class Undo(private val component: Component, private var text: String?, private var undoAction: State?) {

    private var x: Float = 0f
    private var y: Float = 0f
    private var scale: Float = 0f

    /**
     * Undo for components being moved
     * @param component component being moved
     * @param state action to be undo
     */
    constructor(component: Component, state: State) : this(component, 0f, 0f, 1f, state) {}

    /**
     * Undo for components being moved
     * @param component component being moved
     * @param x previous x-coordinate
     * @param y previous y-coordinate
     * @param state action to be undo
     */
    constructor(component: Component, x: Float, y: Float, scale: Float, state: State) : this(component, null, state) {
        this.x = x
        this.y = y
        this.scale = scale
        this.undoAction = state
    }

    /**
     * Undo action taken on component
     * @param components list of components
     * @return true if undo was successful
     */
    fun undo(components: ComponentList): Boolean {
        when (undoAction) {
            State.Delete -> {
                components.add(component)
                return true
            }
            State.Move -> return move()
            State.Text -> return changeText()
            State.Add, State.DrawPath -> {
                components.remove(component)
                return true
            }
            else -> {
                components.add(component)
                return true
            }
        }
    }

    /**
     * Redo action taken on component
     * @param components list of components
     * @return true if redo was successful
     */
    fun redo(components: ComponentList): Boolean {
        when (undoAction) {
            State.Delete -> {
                components.remove(component)
                return true
            }
            State.Move -> return move()
            State.Text -> return changeText()
            State.Add, State.DrawPath -> {
                components.add(component)
                return true
            }
            else -> {
                components.remove(component)
                return true
            }
        }
    }

    private fun move(): Boolean {
        val bounds = component.bounds
        val x = bounds.left
        val y = bounds.top
        val scale = component.scale
        component.scale = this.scale
        component.move(this.x - x, this.y - y)
        this.x = x
        this.y = y
        this.scale = scale
        return true
    }


    private fun changeText(): Boolean {
        val txt = text

        if (component is Text && txt != null) {
            val tmp = component.text
            component.text = txt
            text = tmp
            return true
        }
        return false
    }

    fun cleanup() {
        if (undoAction == State.Delete) {
            component.delete()
        }
    }
}
