package dk.eightyplus.annoto.utilities

/**
 * File identifier used when saving/loading primitives
 */
interface FileId {
    companion object {

        const val NEW_LINE = "\n"

        const val TYPE = "type"
        const val SIZE = "size"
        const val LIST = "list"

        const val X = "x"
        const val Y = "y"
        const val X2 = "x2"
        const val Y2 = "y2"
        const val COLOR = "color"
        const val SCALE = "scale"
        const val WIDTH = "width"

        const val TEXT = "text"
        const val FONT_SIZE = "fontSize"
        const val FILE_NAME = "filename"
        const val PATH = "path"
        const val ACTIONS = "actions"
    }
}
