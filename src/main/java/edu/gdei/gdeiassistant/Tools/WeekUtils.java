package edu.gdei.gdeiassistant.Tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class WeekUtils {

    private static int startYear;

    private static int startMonth;

    private static int startDate;

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
}
