package diploma.work.magnum_opus.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import diploma.work.magnum_opus.item.ItemOfRepetitionAdapter
import diploma.work.magnum_opus.model.Repetition
import diploma.work.magnum_opus.model.StudyMaterial

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "storage", null, 4) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE materials (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                content TEXT,
                created_at INTEGER,
                is_completed INTEGER DEFAULT 0 CHECK (is_completed IN (0, 1))
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE repetitions (
                material_id INTEGER,
                number INTEGER,
                timestamp INTEGER,
                valuation INTEGER,
                PRIMARY KEY (number, material_id),
                FOREIGN KEY(material_id) REFERENCES materials(id) ON DELETE CASCADE
            )
        """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db!!.execSQL("ALTER TABLE materials ADD COLUMN is_completed INTEGER DEFAULT 0 CHECK (is_completed IN (0, 1))")
        }
    }

    /**
     * Метод сохранения материала в Sqlite БД
     *
     * @param material объект материал
     * @return возвращает id сохранённого материала, или null если произошла ошибка
     */
    fun saveMaterial(material: StudyMaterial): Long? {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("title", material.title)
        values.put("content", material.content)
        values.put("created_at", material.createdAt)
        val newRowId = db.insert("materials", null, values)
        db.close()
        return if (newRowId == -1L) null else newRowId
    }

    /**
     * Метод сохранения повторения в Sqlite БД
     *
     * @param repetition объект повторение
     * @return возвращает id сохранённого повторения, или null если произошла ошибка
     */
    fun saveRepetition(repetition: Repetition): Long? {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("material_id", repetition.materialId)
        values.put("number", repetition.number)
        values.put("timestamp", repetition.timestamp)
        values.put("valuation", repetition.valuation)
        val newRowId = db.insert("repetitions", null, values)
        db.close()
        return if (newRowId == -1L) null else newRowId
    }

    /**
     * Метод обновления материала
     *
     * @param material объект материал
     * @return возвращает true, если был обновлён, иначе false
     */
    @Throws(IllegalStateException::class)
    fun updateMaterial(material: StudyMaterial): Boolean {
        val db = this.writableDatabase
        if (material.id == null) {
            throw IllegalStateException("id for updateMaterial must not be null")
        }
        val values = ContentValues()
        values.put("title", material.title)
        values.put("content", material.content)
        val isCompleted = if (material.isCompleted) 1 else 0
        values.put("is_completed", isCompleted)
        val rows = db.update("materials", values, "id = ?", arrayOf("${material.id}"))
        db.close()
        return rows == 1
    }

    /**
     * Метод обновления повторения
     *
     * @param repetition объект повторение
     * @return возвращает true, если был обновлён, иначе false
     */
    fun updateRepetition(repetition: Repetition): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("timestamp", repetition.timestamp)
        values.put("valuation", repetition.valuation)
        val rows = db.update(
            "repetitions",
            values,
            "material_id = ? AND number = ?",
            arrayOf("${repetition.materialId}", "${repetition.number}")
        )
        db.close()
        return rows == 1
    }

    fun deleteMaterial(id: Long) {
        val db = this.writableDatabase
        db.delete("materials", "id = ?", arrayOf("$id"))
        db.close()
    }

    /**
     * Метод получения материала из БД Sqlite
     *
     * @param id материала, который нужно получить
     * @return  возвращает материл, если он был получен, иначе null
     */
    fun getMaterialById(id: Long): StudyMaterial? {
        val db = readableDatabase
        val query = "SELECT * FROM materials WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf("$id"))

        val studyMaterial: StudyMaterial? = if (cursor.moveToFirst()) {
            StudyMaterial(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
            )
        } else {
            null
        }
        cursor.close()
        db.close()
        return studyMaterial
    }

    /**
     * Метод получения последнего повторения из БД Sqlite
     *
     * @param materialId  id материала, повторение которого нужно получить
     * @return  возвращает повторение, если оно было получено, иначе null
     */
    fun getLastRepetition(materialId: Long): Repetition? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT * 
        FROM repetitions 
        WHERE material_id = ? 
        ORDER BY number DESC 
        LIMIT 1
        """,
            arrayOf("$materialId")
        )
        val repetition = if (cursor.moveToFirst()) {
            Repetition(
                materialId = cursor.getLong(cursor.getColumnIndexOrThrow("material_id")),
                number = cursor.getInt(cursor.getColumnIndexOrThrow("number")),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                valuation = cursor.getInt(cursor.getColumnIndexOrThrow("valuation"))
            )
        } else null
        cursor.close()
        db.close()
        return repetition
    }

    /**
     * Метод получения списка всех материалов из БД Sqlite
     *
     * @return  возвращает список материлов
     */
    fun getAllMaterials(): MutableList<StudyMaterial> {
        val db = readableDatabase
        val query = "SELECT * FROM materials ORDER BY id"
        val cursor = db.rawQuery(query, null)
        val materialsList = mutableListOf<StudyMaterial>()
        if (cursor.moveToFirst()) {
            do {
                val studyMaterial = StudyMaterial(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                    isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
                )
                materialsList.add(studyMaterial)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return materialsList
    }

    /**
     * Метод получения списка всех последних повторений из БД Sqlite.
     * Список отсортирован по material_id
     * @return  возвращает список повторений
     */
    fun getAllLastRepetition(): MutableList<Repetition> {
        val db = readableDatabase
        val query = """
            SELECT r1.material_id, r1.number, r1.timestamp, r1.valuation
            FROM repetitions r1
            JOIN (
                SELECT material_id, MAX(number) AS max_number
                FROM repetitions
                GROUP BY material_id
            ) r2 ON r1.material_id = r2.material_id AND r1.number = r2.max_number;
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        val repetitionList = mutableListOf<Repetition>()
        if (cursor.moveToFirst()) {
            do {
                val repetition = Repetition(
                    materialId = cursor.getLong(cursor.getColumnIndexOrThrow("material_id")),
                    number = cursor.getInt(cursor.getColumnIndexOrThrow("number")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                    valuation = cursor.getInt(cursor.getColumnIndexOrThrow("valuation"))
                )
                repetitionList.add(repetition)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return repetitionList
    }

    fun getRepetitionsList(materialId: Long): List<ItemOfRepetitionAdapter> {
        val db = readableDatabase
        val query = """
            SELECT * FROM repetitions
            WHERE material_id = ?
            GROUP BY number
            """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf("$materialId"))
        val repetitionsList = mutableListOf<ItemOfRepetitionAdapter>()
        if (cursor.moveToFirst()) {
            do {
                val valuation = cursor.getInt(cursor.getColumnIndexOrThrow("valuation"))
                if (valuation >= 0) {
                    val item = ItemOfRepetitionAdapter(
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        valuation = valuation
                    )
                    repetitionsList.add(item)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return repetitionsList
    }
}