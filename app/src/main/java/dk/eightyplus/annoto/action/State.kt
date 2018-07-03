package dk.eightyplus.annoto.action

/**
 *
 */
enum class State {
    Add,
    Delete,
    DrawPath,
    Move,
    Text;

    companion object {
        fun state(state: String): State {
            return State.valueOf(state)
        }
    }
}

