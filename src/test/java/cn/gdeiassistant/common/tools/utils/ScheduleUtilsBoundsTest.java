package cn.gdeiassistant.common.tools.utils;

import cn.gdeiassistant.common.exception.customscheduleexception.GenerateScheduleException;
import cn.gdeiassistant.common.pojo.entity.CustomSchedule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleUtilsBoundsTest {
    private CustomSchedule schedule(int position, int length) {
        CustomSchedule value = new CustomSchedule();
        value.setPosition(position);
        value.setScheduleLength(length);
        value.setScheduleName("Synthetic course");
        value.setScheduleLocation("Synthetic room");
        value.setMinScheduleWeek(1);
        value.setMaxScheduleWeek(20);
        return value;
    }

    @Test void rejectsOverflowAndCoursesPastTheLastLesson() {
        assertThrows(GenerateScheduleException.class, () -> ScheduleUtils.generateCustomSchedule(schedule(0, Integer.MAX_VALUE)));
        assertThrows(GenerateScheduleException.class, () -> ScheduleUtils.generateCustomSchedule(schedule(Integer.MAX_VALUE, 1)));
        assertThrows(GenerateScheduleException.class, () -> ScheduleUtils.generateCustomSchedule(schedule(63, 2)));
    }

    @Test void rendersConsecutiveLessonsWithoutDuplicatingTheFirst() throws Exception {
        assertEquals("周一第1,2,3节", ScheduleUtils.generateCustomSchedule(schedule(0, 3)).getScheduleLesson());
        assertEquals("周日第10节", ScheduleUtils.generateCustomSchedule(schedule(69, 1)).getScheduleLesson());
    }
}
