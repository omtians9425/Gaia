package com.yuyakaido.android.gaia

import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.yuyakaido.android.gaia.core.java.Session
import com.yuyakaido.android.gaia.di.SessionComponent
import com.yuyakaido.android.gaia.ext.newSessionComponent

data class RunningSession(
    private var components: Map<Long, SessionComponent> = mutableMapOf()
) {

    companion object {
        private const val SESSIONS = "sessions"
    }

    fun hasSession(): Boolean {
        return components.isNotEmpty()
    }

    fun sessions(gaia: Gaia): List<Session> {
        val state = gaia.appStore.state()
        return components.keys
            .map { id -> state.sessions.first { it.id == id } }
    }

    fun add(session: Session, component: SessionComponent) {
        components = components.plus(session.id to component)
    }

    fun remove(session: Session) {
        components = components.minus(session.id)
    }

    fun get(session: Session): SessionComponent {
        return components.getValue(session.id)
    }

    fun save(gaia: Gaia) {
        val gson = Gson()
        val arrayArray = JsonArray()
        val state = gaia.appStore.state()
        components.keys.forEach { id ->
            val session = state.sessions.first { it.id == id }
            arrayArray.add(gson.toJsonTree(session))
        }

        val preference = PreferenceManager.getDefaultSharedPreferences(gaia)
        val editor = preference.edit()
        editor.putString(SESSIONS, arrayArray.toString())
        editor.apply()
    }

    fun restore(gaia: Gaia): List<Session> {
        val gson = Gson()
        val preference = PreferenceManager.getDefaultSharedPreferences(gaia)
        val jsonString = preference.getString(SESSIONS, JsonArray().toString())
        val jsonArray = JsonParser().parse(jsonString).asJsonArray

        val sessions = jsonArray
            .map { gson.fromJson(it.toString(), Session::class.java) }

        components = sessions
            .associate { session -> session.id to gaia.component.newSessionComponent(session) }

        return sessions
    }

}