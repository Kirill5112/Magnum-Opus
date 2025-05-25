package diploma.work.magnum_opus.model

data class Intervals(
    val id: Long,
    val title: String,
    val desc: String?,
    var quantity: Int
) {
    override fun toString() = title
}
