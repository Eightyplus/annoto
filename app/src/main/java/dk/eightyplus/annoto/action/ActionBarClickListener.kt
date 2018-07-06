package dk.eightyplus.annoto.action

import android.view.View
import dk.eightyplus.annoto.Callback

import java.lang.ref.SoftReference

/**
 * User: fries
 * Date: 23/03/14
 * Time: 22.06
 */
class ActionBarClickListener(private val deselect: List<View>, private val clickAction: State, callback: Callback) : View.OnClickListener {
    private val callback: SoftReference<Callback> = SoftReference(callback)

    override fun onClick(v: View) {
        this.callback.get()?.let {
            for (view in deselect) {
                view.isSelected = false
            }
            v.isSelected = true
            it.state = clickAction
        }
    }
}
