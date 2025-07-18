package dev.icerock.education.practicetask.data.api

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import dev.icerock.education.practicetask.data.api.KEYS.*

private const val PREF_NAME = "KEY_VALUE_STORAGE"

enum class KEYS {
    AUTH_TOKEN,
    USERNAME,
    IS_AUTHORIZED,
}

@Singleton
class KeyValueStorage @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isAuthorized: Boolean = false
        set(value) {
            field = value
            saveToPreferences(value, IS_AUTHORIZED)
        }
        get() = prefs.getBoolean(IS_AUTHORIZED.name, false)

    var authToken: String? = null
        set(value) {
            if (field != value) {
                field = value
                saveToPreferences(value, AUTH_TOKEN)
            }
        }
        get() = prefs.getString(AUTH_TOKEN.name, "")

    var userName: String? = null
        set(value) {
            if (field != value) {
                field = value
                saveToPreferences(value, USERNAME)
            }
        }
        get() = prefs.getString(USERNAME.name, "")

    private fun <T> saveToPreferences(value: T?, key: KEYS) {
        val editor: SharedPreferences.Editor = prefs.edit()
        when (value) {
            is Boolean -> editor.putBoolean(key.name, value)
            is String -> editor.putString(key.name, value)
        }
        editor.apply()
    }
}