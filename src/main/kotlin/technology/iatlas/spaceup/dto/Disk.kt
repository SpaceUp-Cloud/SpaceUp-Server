package technology.iatlas.spaceup.dto

data class Disk(
    val space: String,
    val spacePercentage: Float,
    val quota: String,
    val availableQuota: Float
)
