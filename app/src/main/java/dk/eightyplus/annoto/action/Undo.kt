package dk.eightyplus.annoto.action

import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.Text
import dk.eightyplus.annoto.view.ComponentList

/**
 * Undo (Redo) class contains the reverse action of an user interaction, so these can be reversed.
 */
class Undo(private val component: Component,
           private var undoAction: State,
           private var text: String? = null,
           private var x: Float = 0f,
           private var y: Float = 0f,
           private var scale: Float = 1f) {

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
