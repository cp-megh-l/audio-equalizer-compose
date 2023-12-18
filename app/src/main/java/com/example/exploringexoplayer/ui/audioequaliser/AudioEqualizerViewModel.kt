package com.example.exploringexoplayer.ui.audioequaliser

import android.media.audiofx.Equalizer
import androidx.lifecycle.ViewModel
import com.example.exploringexoplayer.data.model.AudioEffects
import com.example.exploringexoplayer.data.preferences.EqualizerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

const val PRESET_CUSTOM = 0
const val PRESET_FLAT = 1
const val PRESET_ACOUSTIC = 2
const val PRESET_DANCE_LOUNGE = 3
const val PRESET_HIP_HOP = 4
const val PRESET_JAZZ_BLUES = 5
const val PRESET_POP = 6
const val PRESET_ROCK = 7
const val PRESET_PODCAST = 8

val FLAT = arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0)
val ACOUSTIC = arrayListOf(0.44, 0.12, 0.12, 0.34, 0.2)
val DANCE = arrayListOf(0.52, 0.08, 0.28, 0.48, 0.06)
val HIP_HOPE = arrayListOf(0.44, 0.06, -0.14, 0.1, 0.38)
val JAZZ = arrayListOf(0.32, 0.0, 0.22, 0.1, 0.2)
val POP = arrayListOf(-0.14, 0.28, 0.38, 0.22, -0.2)
val ROCK = arrayListOf(0.38, 0.2, -0.04, 0.02, 0.34)
val PODCAST = arrayListOf(-0.12, 0.26, 0.36, 0.16, -0.2)

@HiltViewModel
class AudioEqualizerViewModel @Inject constructor(
    private val equalizerPreferences: EqualizerPreferences
) : ViewModel() {

    val audioEffects = MutableStateFlow<AudioEffects?>(null)
    private var equalizer: Equalizer? = null
    val enableEqualizer = MutableStateFlow(false)
    private var audioSessionId = 0

    init {
        enableEqualizer.value = equalizerPreferences.isEqualizerEnabled
        audioEffects.tryEmit(equalizerPreferences.audioEffects)

        if (audioEffects.value == null) {
            audioEffects.tryEmit(AudioEffects(PRESET_FLAT, FLAT))
        }
    }

    fun onStart(sessionId: Int) {
        audioSessionId = sessionId
        equalizer?.enabled = enableEqualizer.value
        equalizer = Equalizer(Int.MAX_VALUE, audioSessionId)
        equalizerPreferences.lowestBandLevel = equalizer?.bandLevelRange?.get(0)?.toInt() ?: 0
        audioEffects.value?.gainValues?.forEachIndexed { index, value ->
            val bandLevel = (value * 1000).toInt().toShort()
            equalizer?.setBandLevel(index.toShort(), bandLevel)
        }
    }

    fun onSelectPreset(presetPosition: Int) {
        if (audioEffects.value == null) return

        val gain = if (presetPosition == PRESET_CUSTOM) {
            ArrayList(audioEffects.value!!.gainValues)
        } else {
            ArrayList(getPresetGainValue(presetPosition))
        }

        audioEffects.tryEmit(AudioEffects(presetPosition, gain))
        equalizerPreferences.audioEffects = audioEffects.value

        equalizer?.apply {
            gain.forEachIndexed { index, value ->
                val bandLevel = (value * 1000).toInt().toShort()
                setBandLevel(index.toShort(), bandLevel)
            }
        }
    }

    fun onBandLevelChanged(changedBand: Int, newGainValue: Int) {
        val lowest = equalizerPreferences.lowestBandLevel
        val bandLevel = newGainValue.plus(lowest)
        equalizer?.setBandLevel(changedBand.toShort(), bandLevel.toShort())
        val list = ArrayList(audioEffects.value!!.gainValues)
        list[changedBand] = (newGainValue.toDouble() / 1000)
        audioEffects.tryEmit(
            AudioEffects(
                PRESET_CUSTOM,
                list
            )
        )
        equalizerPreferences.audioEffects = audioEffects.value
    }

    fun toggleEqualizer() {
        enableEqualizer.tryEmit(!enableEqualizer.value)
        equalizer?.enabled = enableEqualizer.value
        equalizerPreferences.isEqualizerEnabled = enableEqualizer.value
        if (!enableEqualizer.value) {
            audioEffects.tryEmit(AudioEffects(PRESET_FLAT, FLAT))
            equalizerPreferences.audioEffects = audioEffects.value
        }
    }

    private fun getPresetGainValue(index: Int): List<Double> {
        return when (index) {
            PRESET_FLAT -> FLAT
            PRESET_ACOUSTIC -> ACOUSTIC
            PRESET_DANCE_LOUNGE -> DANCE
            PRESET_HIP_HOP -> HIP_HOPE
            PRESET_JAZZ_BLUES -> JAZZ
            PRESET_POP -> POP
            PRESET_ROCK -> ROCK
            PRESET_PODCAST -> PODCAST
            else -> FLAT
        }
    }
}