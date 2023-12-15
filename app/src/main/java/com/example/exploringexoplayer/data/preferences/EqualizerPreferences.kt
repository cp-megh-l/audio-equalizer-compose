package com.example.exploringexoplayer.data.preferences

import android.content.SharedPreferences
import com.example.exploringexoplayer.ui.audioequaliser.AudioEffects
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

const val AUDIO_EFFECT_PREFERENCES = "audio_effect_preferences"

private const val AUDIO_EFFECT_IS_EQUALIZER_ENABLED = "is_equalizer_enabled"
private const val AUDIO_EFFECT_EQUALIZER_SETTING = "equalizer_audio_effect"
private const val AUDIO_EFFECT_LOWEST_BAND_LEVEL = "equalizer_lowest_band_level"

@Singleton
class EqualizerPreferences
@Inject constructor(
    @param:Named(AUDIO_EFFECT_PREFERENCES) private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {

    var isEqualizerEnabled: Boolean
        get() = sharedPreferences.getBoolean(AUDIO_EFFECT_IS_EQUALIZER_ENABLED, false)
        set(isEnable) = sharedPreferences.edit()
            .putBoolean(AUDIO_EFFECT_IS_EQUALIZER_ENABLED, isEnable).apply()

    var audioEffects: AudioEffects?
        get() {
            val json = sharedPreferences.getString(AUDIO_EFFECT_EQUALIZER_SETTING, null)
            if (json != null) {
                try {
                    return gson.fromJson(json, AudioEffects::class.java)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            return null
        }
        set(audioEffects) {
            var json: String? = null
            if (audioEffects != null) {
                json = gson.toJson(audioEffects)
            }
            sharedPreferences.edit().putString(AUDIO_EFFECT_EQUALIZER_SETTING, json).apply()
        }

    var lowestBandLevel: Int
        get() = sharedPreferences.getInt(AUDIO_EFFECT_LOWEST_BAND_LEVEL, 0)
        set(value) = sharedPreferences.edit().putInt(AUDIO_EFFECT_LOWEST_BAND_LEVEL, value).apply()
}