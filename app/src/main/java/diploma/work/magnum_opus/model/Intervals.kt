package diploma.work.magnum_opus.model

data class Intervals(
    val id: Long,
    val title: String,
    val desc: String?,
    val quantity: Int
) {
    override fun toString() = title
}
