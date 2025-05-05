package diploma.work.magnum_opus.model


/**
 * Этот класс используется для хранения материала запоминания.
 * @param id уникальный номер материала.
 * @param title заголовок материала
 * @param content текст материала
 * @param createdAt время создания материала
 */
data class StudyMaterial(
    val id: Long? = null,
    val intervalsId: Int,
    val title: String,
    val content: String,
    val createdAt: Long,
    var isCompleted:Boolean = false
)
