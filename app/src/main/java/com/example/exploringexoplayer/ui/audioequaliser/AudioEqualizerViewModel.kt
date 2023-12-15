package com.example.exploringexoplayer.ui.audioequaliser

import android.media.audiofx.Equalizer
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.exploringexoplayer.data.preferences.EqualizerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

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

        Log.e("XXX", "AudioEffects - ${audioEffects.value}")
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

    fun onStop() {
        equalizer?.let {
            it.release()
            equalizer = null
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

data class AudioEffects(
    var selectedEffectType: Int = 0,
    var gainValues: ArrayList<Double>
)