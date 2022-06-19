package com.andb.apps.weather.util

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.execute
import com.chibatching.kotpref.pref.AbstractPref
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegate list shared preferences property.
 * @param default default list value
 * @param key custom preferences key
 * @param commitByDefault commit this property instead of apply
 */
fun <T : Any> KotprefModel.listPref(
    default: List<T> = listOf(),
    key: String? = null,
    adapter: JsonAdapter<T>,
    commitByDefault: Boolean = commitAllPropertiesByDefault
): ReadWriteProperty<KotprefModel, List<T>> =
    ListPref(default, key, adapter, commitByDefault)

class ListPref<T : Any>(
    private val default: List<T>,
    override val key: String?,
    private val adapter: JsonAdapter<T>,
    private val commitByDefault: Boolean
) : AbstractPref<List<T>>() {

    val moshi = Moshi.Builder().build()
    private val stringAdapter = moshi.adapter<List<String>>(List::class.java)

    override fun getFromPreference(property: KProperty<*>, preference: SharedPreferences): List<T> {
        val asString = preference.getString(key ?: property.name, null) ?: return default
        return stringToList(asString)
    }

    @SuppressLint("CommitPrefEdits")
    override fun setToPreference(
        property: KProperty<*>,
        value: List<T>,
        preference: SharedPreferences
    ) {
        val toString = listToString(value)
        preference.edit().putString(key ?: property.name, toString).execute(commitByDefault)
    }

    override fun setToEditor(
        property: KProperty<*>,
        value: List<T>,
        editor: SharedPreferences.Editor
    ) {
        val toString = listToString(value)
        editor.putString(key ?: property.name, toString)
    }

    private fun listToString(list: List<T>): String {
        return stringAdapter.toJson(list.map { adapter.toJson(it) })
    }

    private fun stringToList(data: String?): List<T> {
        if (data == null) {
            return emptyList()
        }
        val strings: List<String> =
            stringAdapter.fromJson(data) as List<String>? ?: return emptyList()
        return strings.mapNotNull { adapter.fromJson(it) }
    }
}
