package diploma.work.magnum_opus.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import diploma.work.magnum_opus.MainActivity.Companion.ID_SORT_DATE
import diploma.work.magnum_opus.MainActivity.Companion.ID_SORT_NAME
import diploma.work.magnum_opus.MainActivity.Companion.ID_SORT_TIMESTAMP
import diploma.work.magnum_opus.item.ItemOfMaterialAdapter
import diploma.work.magnum_opus.item.ItemOfRepetitionAdapter
import diploma.work.magnum_opus.model.Intervals
import diploma.work.magnum_opus.model.Repetition
import diploma.work.magnum_opus.model.StudyMaterial
import diploma.work.magnum_opus.settings.AppPreferences.completedIsHide
import diploma.work.magnum_opus.settings.AppPreferences.sortId
import diploma.work.magnum_opus.settings.AppPreferences.sortIsIncreasing

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "storage", null, 7) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE intervals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                "desc" TEXT,
                quantity INTEGER DEFAULT 0 CHECK(quantity >= 0)
            )
                """.trimIndent()
        )

        db.execSQL("INSERT INTO intervals (title) VALUES ('Базовый');")

        db.execSQL(
            """
            CREATE TABLE interval (
                id INTEGER,
                number INTEGER NOT NULL CHECK(number > 0),
                delay INTEGER NOT NULL,
                FOREIGN KEY(id) REFERENCES intervals ON DELETE CASCADE,
                PRIMARY KEY (number, id)
            )
                """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE material (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                intervals_id INTEGER,
                title TEXT,
                content TEXT,
                created_at INTEGER,
                is_completed INTEGER DEFAULT 0 CHECK (is_completed IN (0, 1)),
                FOREIGN KEY(intervals_id) REFERENCES intervals(id)
            )
                """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE repetition (
                material_id INTEGER,
                number INTEGER,
                timestamp INTEGER,
                valuation INTEGER,
                PRIMARY KEY (number, material_id),
                FOREIGN KEY(material_id) REFERENCES material(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TRIGGER after_interval_insert
            AFTER INSERT ON interval
            FOR EACH ROW
            BEGIN
                UPDATE intervals
                SET quantity = quantity + 1
                WHERE id = NEW.id;
            END;
                """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TRIGGER after_interval_delete
            AFTER DELETE ON interval
            FOR EACH ROW
            BEGIN
                UPDATE intervals
                SET quantity = quantity - 1
                WHERE id = OLD.id;
            END;
                """.trimIndent()
        )

        db.execSQL("INSERT INTO interval VALUES (1, 1, 1);")
        db.execSQL("INSERT INTO interval VALUES (1, 2, 20);")
        db.execSQL("INSERT INTO interval VALUES (1, 3, 480);")
        db.execSQL("INSERT INTO interval VALUES (1, 4, 1440);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE material")
        db.execSQL("DROP TABLE repetition")
        db.execSQL("DROP TABLE intervals")
        db.execSQL("DROP TABLE interval")
        onCreate(db)
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
        values.put("intervals_id", material.intervalsId)
        val newRowId = db.insert("material", null, values)

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
        val newRowId = db.insert("repetition", null, values)

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
        val rows = db.update("material", values, "id = ?", arrayOf("${material.id}"))

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
            "repetition",
            values,
            "material_id = ? AND number = ?",
            arrayOf("${repetition.materialId}", "${repetition.number}")
        )

        return rows == 1
    }

    fun deleteMaterial(id: Long) {
        val db = this.writableDatabase
        db.delete("material", "id = ?", arrayOf("$id"))
    }

    /**
     * Метод получения материала из БД Sqlite
     *
     * @param id материала, который нужно получить
     * @return  возвращает материл, если он был получен, иначе null
     */
    fun getMaterialById(id: Long): StudyMaterial? {
        val db = readableDatabase
        val query = "SELECT * FROM material WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf("$id"))

        val studyMaterial: StudyMaterial? = if (cursor.moveToFirst()) {
            StudyMaterial(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                intervalsId = cursor.getLong(cursor.getColumnIndexOrThrow("intervals_id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
            )
        } else {
            null
        }
        cursor.close()

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
        FROM repetition 
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

        return repetition
    }

    fun getRepetitionsList(materialId: Long): List<ItemOfRepetitionAdapter> {
        val db = readableDatabase
        val query = """
            SELECT * FROM repetition
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
        return repetitionsList
    }

    fun getItemsOfMaterialAdapterList(context: Context): MutableList<ItemOfMaterialAdapter> {
        val db = readableDatabase
        val skeletQuery = """
            SELECT m.id, m.title, r.timestamp, m.is_completed
            FROM material m JOIN repetition r on m.id = r.material_id
            WHERE r.number = (SELECT max(number) FROM repetition where material_id = r.material_id)
        """.trimIndent()
        val queryWithWhere =
            if (context.completedIsHide) "$skeletQuery AND m.is_completed = '0' ORDER BY" else "$skeletQuery ORDER BY"
        val querySort = when (context.sortId) {
            ID_SORT_DATE -> "$queryWithWhere m.id"
            ID_SORT_NAME -> "$queryWithWhere m.title"
            ID_SORT_TIMESTAMP -> "$queryWithWhere r.timestamp"
            else -> {
                throw IllegalStateException("sortId неверный")
            }
        }
        val query = if (!context.sortIsIncreasing) "$querySort DESC" else querySort
        val items = mutableListOf<ItemOfMaterialAdapter>()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val item = ItemOfMaterialAdapter(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                    isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
                )
                items.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun getIntervalsObject(id: Long): Intervals? {
        val db = readableDatabase
        val query = "SELECT * FROM intervals WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf("$id"))

        val intervals: Intervals? = if (cursor.moveToFirst()) {
            Intervals(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                desc = cursor.getString(cursor.getColumnIndexOrThrow("desc")),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
            )
        } else {
            null
        }
        cursor.close()
        return intervals
    }

    fun getIntervalDelay(id: Long, number: Int): Long? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT delay 
        FROM interval 
        WHERE id = ? AND number = ?
        """,
            arrayOf("$id", "$number")
        )
        val delay = if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow("delay"))
        } else null
        cursor.close()
        return delay
    }

    fun deleteIntervalDelay(id: Long, number: Int) {
        val db = writableDatabase
        db.delete("interval", "id = ? AND number = ?", arrayOf("$id", "$number"))
    }

    fun saveInter(id: Long, number: Int, delay: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("delay", delay)
        if (getIntervalDelay(id, number) == null) {
            values.put("id", id)
            values.put("number", number)
            db.insert("interval", null, values)
        } else
            db.update("interval", values, "id = ? AND number = ?", arrayOf("$id", "$number"))
    }

    fun saveIntervals(title: String, desc: String?): Long? {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("title", title)
        if (desc != null)
            values.put("desc", desc)
        val newRowId = db.insert("intervals", null, values)
        return if (newRowId == -1L) null else newRowId
    }

    fun deleteIntervals(id: Long) {
        val db = this.writableDatabase
        db.delete("intervals", "id = ?", arrayOf("$id"))
    }

    fun getQuantity(id: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT quantity 
        FROM intervals 
        WHERE id = ?
        """,
            arrayOf("$id")
        )
        val quantity = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
        } else null
        cursor.close()

        return quantity!!
    }

    fun getIntervalsList(): MutableList<Intervals> {
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT * FROM intervals", arrayOf())
        val list = mutableListOf<Intervals>()
        if (cursor.moveToFirst()) {
            do {
                val item = Intervals(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    desc = cursor.getString(cursor.getColumnIndexOrThrow("desc")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                )
                list.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return list
    }

    fun getDelayList(id: Long): MutableList<Long> {
        val db = readableDatabase
        val cursor =
            db.rawQuery(
                "SELECT delay FROM interval WHERE id = ? ORDER BY number",
                arrayOf("$id")
            )
        val delays = mutableListOf<Long>()
        if (cursor.moveToFirst()) {
            do {
                val delay = cursor.getLong(cursor.getColumnIndexOrThrow("delay"))
                delays.add(delay)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return delays
    }
}