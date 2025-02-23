package diploma.work.magnum_opus.model

/**
 * Этот класс используется для хранения объекта повторения материла.
 * @param materialId уникальный номер материала.
 * @param number номер повторения
 * @param timestamp время завершения повторения(или время, когда следует повторить материал, если он ещё не был повторён)
 * @param valuation оценка успешности запоминания
 */
data class Repetition(
    val materialId: Long,
    val number: Int,
    var timestamp: Long,
    var valuation: Int
)
