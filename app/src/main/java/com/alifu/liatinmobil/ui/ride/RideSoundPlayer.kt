package com.alifu.liatinmobil.ui.ride

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.alifu.liatinmobil.R
import com.alifu.liatinmobil.domain.simulation.RideSoundCue

private const val MAX_STREAMS = 3
private const val LOAD_PRIORITY = 1
private const val PLAY_PRIORITY = 1
private const val FULL_VOLUME = 1f
private const val NORMAL_RATE = 1f
private const val NO_LOOP = 0

/**
 * Plays the short voice clips and the horn from the app's own resources, so it works
 * without internet. A new voice clip cuts off the previous one so words never overlap.
 */
class RideSoundPlayer(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val voiceIds: Map<RideSoundCue, Int> =
        RideSoundCue.entries.associateWith { soundPool.load(context, voiceResFor(it), LOAD_PRIORITY) }
    private val hornId: Int = soundPool.load(context, R.raw.sfx_horn, LOAD_PRIORITY)
    private var voiceStreamId = 0

    fun play(cue: RideSoundCue) {
        val soundId = voiceIds[cue] ?: return
        soundPool.stop(voiceStreamId)
        voiceStreamId = soundPool.play(soundId, FULL_VOLUME, FULL_VOLUME, PLAY_PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    fun playHorn() {
        soundPool.play(hornId, FULL_VOLUME, FULL_VOLUME, PLAY_PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    fun stopVoice() {
        soundPool.stop(voiceStreamId)
        voiceStreamId = 0
    }

    fun release() {
        soundPool.release()
    }
}

private fun voiceResFor(cue: RideSoundCue): Int = when (cue) {
    RideSoundCue.TURN_LEFT -> R.raw.voice_turn_left
    RideSoundCue.TURN_RIGHT -> R.raw.voice_turn_right
    RideSoundCue.DRIVER_ARRIVED -> R.raw.voice_driver_arrived
    RideSoundCue.RED_LIGHT -> R.raw.voice_red_light
    RideSoundCue.ARRIVED -> R.raw.voice_arrived
}

@Composable
fun rememberRideSoundPlayer(): RideSoundPlayer {
    val appContext = LocalContext.current.applicationContext
    val player = remember { RideSoundPlayer(appContext) }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}
