package diploma.work.magnum_opus

import org.junit.Test

import org.junit.Assert.*
import java.util.Calendar
import java.util.Locale

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun timerTime_isCorrect() {
        assertEquals("00:05", getTimerTime(300_000))//5 минут
        assertEquals("00:06", getTimerTime(300_001))//5 минут и 1 мс
        assertEquals("01:00", getTimerTime(3_600_000))//1 час
        assertEquals("03:00", getTimerTime(3_600_000 * 3))//3 часа
        assertEquals("03:15", getTimerTime(3_600_000 * 3 + 900_000))//3 часа и 15 минут
        assertEquals("01:01", getTimerTime(3_600_001))//1 час и 1 мс
        //проверено 3 мая
        assertEquals("4-го мая", getTimerTime(86_400_000))//через 24 часа(следующий день)
        assertEquals("10-го мая", getTimerTime(86_400_000 * 7))//через 7 дней
    }

    private fun getTimerTime(remainingTimeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val currentCalendar = Calendar.getInstance().apply {
            this@apply.timeInMillis = now
        }
        val timerCalendar = Calendar.getInstance().apply {
            this@apply.timeInMillis = now + remainingTimeInMillis
        }
        return if (currentCalendar.get(Calendar.YEAR) == timerCalendar.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.DAY_OF_YEAR) == timerCalendar.get(Calendar.DAY_OF_YEAR)
        ) {
            val hours = (remainingTimeInMillis / ((1000 * 60 * 59))) % 24
            val minutes = (((remainingTimeInMillis - 1) / (1000 * 60)) + 1) % 60
            String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        } else {
            val day = timerCalendar.get(Calendar.DAY_OF_MONTH)
            val month = getMonthNameInGenitive(timerCalendar.get(Calendar.MONTH))
            "${day}-го $month"
        }
    }

    private fun getMonthNameInGenitive(month: Int): String {
        val monthsGenitive = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        return monthsGenitive[month]
    }
}