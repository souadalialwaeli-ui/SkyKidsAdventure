package com.example.abbasgame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.abbasgame.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private val soundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = appContext.getSharedPreferences("sky_kids_adventure_prefs", Context.MODE_PRIVATE)
    @Volatile private var musicVolume = prefs.getFloat("music_volume", 0.28f).coerceIn(0f, 1f)
    @Volatile private var effectsVolume = prefs.getFloat("effects_volume", 0.7f).coerceIn(0f, 1f)
    private val soundLock = Any()
    private var soundPool: SoundPool? = null
    private var soundPoolReleased = false
    private var sampleIds: Map<Effect, Int> = emptyMap()
    private val loadedSamples = mutableSetOf<Int>()
    private val pendingSamples = mutableListOf<Pair<Int, Float>>()
    private var bgPlayer: MediaPlayer? = null
    private var musicAllowed = false

    private enum class Effect(val resourceId: Int, val rate: Float) {
        SUCCESS(R.raw.success, 1f),
        FAILURE(R.raw.failure, 1f),
        WRONG(R.raw.wrong, 1f)
    }

    fun startMenuMusic(enabled: Boolean) = startBackgroundMusic(enabled)
    fun startGameMusic(enabled: Boolean) = startBackgroundMusic(enabled)

    private fun startBackgroundMusic(enabled: Boolean) {
        stopBgMusic()
        if (!enabled) return

        musicAllowed = true
        val player = MediaPlayer()
        try {
            appContext.resources.openRawResourceFd(R.raw.propeller_picnic).use { audioFile ->
                player.setDataSource(audioFile.fileDescriptor, audioFile.startOffset, audioFile.length)
            }
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            player.isLooping = true
            player.setVolume(musicVolume, musicVolume)
            player.setOnPreparedListener {
                if (bgPlayer === it && musicAllowed) it.start()
            }
            player.setOnErrorListener { failedPlayer, _, _ ->
                if (bgPlayer === failedPlayer) bgPlayer = null
                musicAllowed = false
                runCatching { failedPlayer.release() }
                true
            }
            bgPlayer = player
            player.prepareAsync()
        } catch (_: Exception) {
            musicAllowed = false
            if (bgPlayer === player) bgPlayer = null
            runCatching { player.release() }
        }
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("music_volume", musicVolume).apply()
        runCatching { bgPlayer?.setVolume(musicVolume, musicVolume) }
    }

    fun setEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("effects_volume", effectsVolume).apply()
    }

    fun stopBgMusic() {
        musicAllowed = false
        val player = bgPlayer
        bgPlayer = null
        if (player != null) {
            runCatching { if (player.isPlaying) player.stop() }
            runCatching { player.release() }
        }
    }

    fun pauseBgMusic() {
        musicAllowed = false
        runCatching { if (bgPlayer?.isPlaying == true) bgPlayer?.pause() }
    }

    fun resumeBgMusic(enabled: Boolean) {
        if (!enabled) {
            pauseBgMusic()
            return
        }
        musicAllowed = true
        val player = bgPlayer
        if (player == null) {
            startBackgroundMusic(true)
        } else {
            runCatching { if (!player.isPlaying) player.start() }
        }
    }

    fun playClickSound(enabled: Boolean) {
        if (enabled) soundScope.launch { playTone(900.0, 60, 0.55f) }
    }

    fun playStarSound(enabled: Boolean) = playEffect(Effect.SUCCESS, enabled, 1.08f)
    fun playCoinSound(enabled: Boolean) = playEffect(Effect.SUCCESS, enabled, 1.17f)
    fun playRingSound(enabled: Boolean) = playEffect(Effect.SUCCESS, enabled, 0.96f)
    fun playRescueSound(enabled: Boolean) = playEffect(Effect.SUCCESS, enabled, 0.9f)
    fun playVictorySound(enabled: Boolean) = playEffect(Effect.SUCCESS, enabled, 1.04f)
    fun playUnlockSound(enabled: Boolean) = playEffect(Effect.SUCCESS, enabled, 1.2f)
    fun playHitSound(enabled: Boolean) = playEffect(Effect.WRONG, enabled)
    fun playFailureSound(enabled: Boolean) = playEffect(Effect.FAILURE, enabled)

    private fun playEffect(effect: Effect, enabled: Boolean, rate: Float = effect.rate) {
        if (!enabled || effectsVolume <= 0f) return
        synchronized(soundLock) {
            if (soundPoolReleased) return
            val pool = soundPool ?: createSoundPoolLocked()
            val sampleId = sampleIds[effect] ?: return
            if (sampleId in loadedSamples) {
                pool.play(sampleId, effectsVolume, effectsVolume, 1, 0, rate)
            } else if (pendingSamples.size < 8) {
                pendingSamples += sampleId to rate
            }
        }
    }

    private fun createSoundPoolLocked(): SoundPool {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
        soundPool = pool
        pool.setOnLoadCompleteListener { loadedPool, sampleId, status ->
            synchronized(soundLock) {
                if (status != 0 || soundPoolReleased || soundPool !== loadedPool) return@synchronized
                loadedSamples += sampleId
                val ready = pendingSamples.filter { it.first == sampleId }
                pendingSamples.removeAll { it.first == sampleId }
                ready.forEach { (_, rate) ->
                    loadedPool.play(sampleId, effectsVolume, effectsVolume, 1, 0, rate)
                }
            }
        }
        sampleIds = Effect.entries.associateWith { effect ->
            pool.load(appContext, effect.resourceId, 1)
        }
        return pool
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        var track: AudioTrack? = null
        try {
            val sampleRate = 44100
            val sampleCount = durationMs * sampleRate / 1000
            val samples = ShortArray(sampleCount)
            for (i in samples.indices) {
                val angle = i / (sampleRate.toDouble() / frequency) * 2.0 * Math.PI
                val envelope = exp(-3.0 * i / sampleCount)
                samples[i] = (sin(angle) * envelope * 32767 * volume * effectsVolume).toInt().toShort()
            }
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(durationMs.toLong())
            }
        } catch (_: Exception) {
            // Audio feedback must never interrupt a flight.
        } finally {
            track?.let {
                runCatching { if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.stop() }
                runCatching { it.release() }
            }
        }
    }

    fun stopAllSounds() {
        stopBgMusic()
        soundScope.coroutineContext[Job]?.cancel()
        synchronized(soundLock) {
            soundPoolReleased = true
            pendingSamples.clear()
            loadedSamples.clear()
            soundPool?.release()
            soundPool = null
            sampleIds = emptyMap()
        }
    }
}
