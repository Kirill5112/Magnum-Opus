package diploma.work.magnum_opus.settings

import android.content.Context
import androidx.core.content.edit

object AppPreferences {
    private const val PREFS_NAME = "root_preferences"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var Context.completedIsHide: Boolean
        get() = getPrefs(this).getBoolean("completed_is_hide", false)
        set(value) = getPrefs(this).edit { putBoolean("completed_is_hide", value) }
    var Context.sortId: Int
        get() = getPrefs(this).getInt("sort_id", 3)
        set(value) = getPrefs(this).edit { putInt("sort_id", value) }
    var Context.sortIsIncreasing: Boolean
        get() = getPrefs(this).getBoolean("sort_direction", true)
        set(value) = getPrefs(this).edit { putBoolean("sort_direction", value) }
}