import com.abidzakly.essentials.utils.functions.IntentUtils
import com.abidzakly.essentials.utils.functions.MediaUtils
import com.abidzakly.essentials.utils.functions.NetworkUtils
import com.abidzakly.essentials.utils.functions.StringUtils
/**
 * Main utility class that provides access to all utility functions.
 * This serves as a single entry point for the shared utilities library.
 */
object SharedUtilities {

    /**
     * Media-related utilities
     */
    val media = MediaUtils

    /**
     * Network-related utilities
     */
    val network = NetworkUtils

    /**
     * Intent-related utilities
     */
    val intent = IntentUtils

    /**
     * String-related utilities
     */
    val string = StringUtils

    /**
     * Initialize the utilities library (if needed for future extensions)
     */
    fun initialize() {
        // Reserved for future initialization logic
    }
}