package cn.gdeiassistant.Tools.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class WeekUtils {

    private static int startYear;

    private static int startMonth;

    private static int startDate;

    private static int year;

    private static int term;

    @Value("#{propertiesReader['schedule.start.date']}")
    public void setStartDate(int startDate) {
        WeekUtils.startDate = startDate;
    }

    @Value("#{propertiesReader['schedule.start.month']}")
    public void setStartMonth(int startMonth) {
        WeekUtils.startMonth = startMonth;
    }

    @Value("#{propertiesReader['schedule.start.year']}")
    public void setStartYear(int startYear) {
        WeekUtils.startYear = startYear;
    }

    @Value("#{propertiesReader['schedule.year']}")
    public void setYear(int year) {
        WeekUtils.year = year;
    }

    @Value("#{propertiesReader['schedule.term']}")
    public void setTerm(int term) {
        WeekUtils.term = term;
    }

    /**
     * 获取当前周数
     *
     * @return
     */
    public static int GetCurrentWeek() {
        //当前日期
        LocalDate current = LocalDate.now();
        //开学日期
        LocalDate start = LocalDate.of(startYear, startMonth, startDate);
        //计算当前周数
        int result = (int) (start.until(current, ChronoUnit.WEEKS) + 1);
        if (result < 1) {
            return 1;
        }
        if (result > 20) {
            return 20;
        }
        return result;
    }

    /**
     * 获取当前学年
     *
     * @return
     */
    public static int GetCurrentYear() {
        return year;
    }

    /**
     * 获取当前学期
     *
     * @return
     */
    public static int GetCurrentTerm() {
        return term;
    }
}
