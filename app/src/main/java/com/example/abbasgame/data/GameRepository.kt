package com.example.abbasgame.data

import android.content.Context
import android.content.SharedPreferences
import com.example.abbasgame.model.*

class GameRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sky_kids_adventure_prefs", Context.MODE_PRIVATE)

    // ── Stars ──────────────────────────────────────────────────────────────────
    fun getStars(): Int = prefs.getInt("total_stars", 0)   // start at 0, earn through play
    fun addStars(count: Int) {
        prefs.edit().putInt("total_stars", (getStars() + count).coerceAtLeast(0)).apply()
    }

    // ── Coins ──────────────────────────────────────────────────────────────────
    fun getCoins(): Int = prefs.getInt("total_coins", 0)
    fun addCoins(count: Int) {
        prefs.edit().putInt("total_coins", (getCoins() + count).coerceAtLeast(0)).apply()
    }

    // ── Airplane ───────────────────────────────────────────────────────────────
    fun getSelectedAirplaneId(): String = prefs.getString("selected_airplane", "plane_red") ?: "plane_red"
    fun setSelectedAirplaneId(id: String) { prefs.edit().putString("selected_airplane", id).apply() }

    // ── Settings ───────────────────────────────────────────────────────────────
    fun isMusicEnabled(): Boolean = prefs.getBoolean("music_enabled", true)
    fun setMusicEnabled(e: Boolean) { prefs.edit().putBoolean("music_enabled", e).apply() }

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(e: Boolean) { prefs.edit().putBoolean("sound_enabled", e).apply() }

    fun getMusicVolume(): Float = prefs.getFloat("music_volume", 0.28f).coerceIn(0f, 1f)
    fun setMusicVolume(volume: Float) {
        prefs.edit().putFloat("music_volume", volume.coerceIn(0f, 1f)).apply()
    }

    fun getEffectsVolume(): Float = prefs.getFloat("effects_volume", 0.7f).coerceIn(0f, 1f)
    fun setEffectsVolume(volume: Float) {
        prefs.edit().putFloat("effects_volume", volume.coerceIn(0f, 1f)).apply()
    }

    fun isVoiceEnabled(): Boolean = prefs.getBoolean("voice_enabled", true)
    fun setVoiceEnabled(e: Boolean) { prefs.edit().putBoolean("voice_enabled", e).apply() }

    fun getDifficulty(): String = prefs.getString("game_difficulty", "سهل") ?: "سهل"
    fun setDifficulty(d: String) { prefs.edit().putString("game_difficulty", d).apply() }

    // ── Levels ─────────────────────────────────────────────────────────────────
    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId <= 1) return true
        return prefs.getBoolean("level_unlocked_$levelId", false)
    }
    fun unlockLevel(levelId: Int) {
        if (levelId > 20) return
        prefs.edit().putBoolean("level_unlocked_$levelId", true).apply()
    }
    fun getLevelStars(levelId: Int): Int = prefs.getInt("level_stars_$levelId", 0)
    fun setLevelStars(levelId: Int, stars: Int) {
        val cur = getLevelStars(levelId)
        if (stars > cur) prefs.edit().putInt("level_stars_$levelId", stars).apply()
    }

    // ── Airplanes ──────────────────────────────────────────────────────────────
    fun isAirplaneUnlocked(id: String): Boolean {
        if (id == "plane_red") return true
        return prefs.getBoolean("airplane_unlocked_$id", false)
    }
    fun unlockAirplane(id: String) { prefs.edit().putBoolean("airplane_unlocked_$id", true).apply() }

    fun getAirplanes(): List<Airplane> = listOf(
        Airplane("plane_red",    "الطائرة الحمراء",   0,   0xFFE53935, 0xFFEF5350, 1.0f, "plane_red",    true),
        Airplane("plane_blue",   "الطائرة الزرقاء",  50,  0xFF1E88E5, 0xFF42A5F5, 1.1f, "plane_red",    isAirplaneUnlocked("plane_blue")),
        Airplane("plane_yellow", "الطائرة الصفراء",  80,  0xFFFFB300, 0xFFFFCA28, 1.2f, "plane_red",    isAirplaneUnlocked("plane_yellow")),
        Airplane("plane_pink",   "الطائرة الوردية",  100, 0xFFEC407A, 0xFFF48FB1, 1.25f,"plane_red",    isAirplaneUnlocked("plane_pink")),
        Airplane("plane_rainbow","طائرة قوس قزح",    150, 0xFFAB47BC, 0xFFBA68C8, 1.3f, "plane_red",    isAirplaneUnlocked("plane_rainbow")),
        Airplane("plane_space",  "طائرة الفضاء",     200, 0xFF26A69A, 0xFF4DB6AC, 1.4f, "plane_red",    isAirplaneUnlocked("plane_space")),
        Airplane("plane_dragon", "تنين الأحلام",     300, 0xFF66BB6A, 0xFF81C784, 1.5f, "plane_red",    isAirplaneUnlocked("plane_dragon"))
    )

    // ── Worlds ─────────────────────────────────────────────────────────────────
    fun getWorlds(): List<World> = listOf(
        World(1, "عالم السماء الزرقاء",     "سماء صافية وجميلة للمبتدئين",        0xFF29B6F6, 0xFF81D4FA, 0xFFFFFFFF, true),
        World(2, "عالم الغيوم الناعمة",     "اطِر فوق الغيوم البيضاء الكثيفة",   0xFF90CAF9, 0xFFE3F2FD, 0xFFECEFF1, isLevelUnlocked(5)),
        World(3, "عالم قوس قزح",            "ألوان مبهجة ومسارات سحرية",          0xFFCE93D8, 0xFFF3E5F5, 0xFFFCE4EC, isLevelUnlocked(9)),
        World(4, "عالم الجزر الطائرة",      "استكشف الجزر المعلقة والشلالات",    0xFF80CBC4, 0xFFE0F2F1, 0xFFB2DFDB, isLevelUnlocked(12)),
        World(5, "عالم النجوم الساطعة",     "نجوم براقة وكواكب لطيفة",            0xFFFFF176, 0xFFFFFDE7, 0xFFFFF9C4, isLevelUnlocked(15)),
        World(6, "عالم المغامرات الليلية",  "سماء الليل الساحرة مع النجوم",       0xFF5C6BC0, 0xFF1A237E, 0xFF7986CB, isLevelUnlocked(18))
    )

    // ── Levels ─────────────────────────────────────────────────────────────────
    fun getLevels(): List<Level> {
        val modes = listOf(GameMode.FLYING_ADVENTURE, GameMode.STAR_COLLECTION, GameMode.RING_CHALLENGE, GameMode.ANIMAL_RESCUE)
        return (1..20).map { i ->
            val worldId = when (i) {
                in 1..4 -> 1
                in 5..8 -> 2
                in 9..11 -> 3
                in 12..14 -> 4
                in 15..17 -> 5
                else -> 6
            }
            val mode = modes[(i - 1) % modes.size]
            Level(
                id = i,
                worldId = worldId,
                levelNumber = i,
                title = "مرحلة $i",
                gameMode = mode,
                targetScore = 5 + (i * 3),
                isUnlocked = isLevelUnlocked(i),
                starsEarned = getLevelStars(i)
            )
        }
    }

    // ── Achievements ───────────────────────────────────────────────────────────
    fun getAchievements(): List<Achievement> = listOf(
        Achievement("first_flight",    "أول رحلة",          "أكمل مرحلتك الأولى بنجاح!",          50,  prefs.getBoolean("ach_first_flight", false)),
        Achievement("star_collector",  "جامع النجوم",        "اجمع 50 نجمة في إجمالي المراحل",      100, prefs.getBoolean("ach_star_collector", false)),
        Achievement("sky_master",      "ملك السماء",         "افتح جميع الطائرات",                  200, prefs.getBoolean("ach_sky_master", false)),
        Achievement("ring_expert",     "خبير الحلقات",       "اجتز 20 حلقة سحرية",                 150, prefs.getBoolean("ach_ring_expert", false)),
        Achievement("rescue_hero",     "بطل الإنقاذ",        "أنقذ 10 حيوانات",                    120, prefs.getBoolean("ach_rescue_hero", false)),
        Achievement("world_explorer",  "مستكشف العوالم",     "افتح جميع العوالم",                   250, prefs.getBoolean("ach_world_explorer", false))
    )

    fun unlockAchievement(id: String) {
        prefs.edit().putBoolean("ach_$id", true).apply()
    }

    // ── Reward tracking (prevent repeat collection on same level win) ──────────
    fun hasCollectedLevelReward(levelId: Int): Boolean =
        prefs.getBoolean("reward_collected_$levelId", false)

    fun markLevelRewardCollected(levelId: Int) {
        prefs.edit().putBoolean("reward_collected_$levelId", true).apply()
    }

    // Check and unlock achievements based on current stats
    fun checkAchievements() {
        if (getStars() >= 50) unlockAchievement("star_collector")
        val airplanes = getAirplanes()
        if (airplanes.all { it.isUnlocked }) unlockAchievement("sky_master")
        val worlds = getWorlds()
        if (worlds.all { it.isUnlocked }) unlockAchievement("world_explorer")
    }
}
