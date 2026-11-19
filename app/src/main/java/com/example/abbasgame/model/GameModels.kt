package com.example.abbasgame.model

enum class GameMode(val title: String, val description: String, val emoji: String) {
    FLYING_ADVENTURE("مغامرة الطيران", "اطِر بين الغيوم واجمع النجوم وتجنب العوائق!", "🌤️"),
    STAR_COLLECTION("جمع النجوم", "اجمع أكبر عدد ممكن من النجوم اللامعة في السماء!", "⭐"),
    RING_CHALLENGE("تحدي الحلقات", "مُرَّ عبر الحلقات السحرية الملونة بدقة ومهارة!", "🔮"),
    ANIMAL_RESCUE("إنقاذ الحيوانات", "ساعد الحيوانات الكرتونية اللطيفة بالوصول إلى الأمان!", "🐾")
}

data class Airplane(
    val id: String,
    val name: String,
    val price: Int,          // cost in stars
    val colorPrimary: Long,
    val colorSecondary: Long,
    val speedMultiplier: Float,
    val drawableRes: String, // drawable name (no extension)
    var isUnlocked: Boolean
)

data class World(
    val id: Int,
    val name: String,
    val description: String,
    val skyTopColor: Long,
    val skyBottomColor: Long,
    val cloudColor: Long,
    var isUnlocked: Boolean
)

data class Level(
    val id: Int,
    val worldId: Int,
    val levelNumber: Int,
    val title: String,
    val gameMode: GameMode,
    val targetScore: Int,
    var isUnlocked: Boolean,
    var starsEarned: Int = 0
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    var isUnlocked: Boolean
)

// Game object in gameplay
data class GameObject(
    var x: Float,
    var y: Float,
    var rotation: Float = 0f,
    var scale: Float = 1f,
    var velocityX: Float = 0f,
    var velocityY: Float = 5f,
    var type: ObjectType,
    var active: Boolean = true,
    var collectAnimation: Float = 0f // 0=normal, >0 = collecting (shrink)
)

enum class ObjectType { STAR, COIN, RING, ANIMAL, OBSTACLE_BIRD, OBSTACLE_CLOUD }

// Particle for visual effects
data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,       // 1.0 → 0.0
    var maxLife: Float,
    var size: Float,
    var color: Long
)

enum class GameState { PLAYING, PAUSED, WON, LOST }
