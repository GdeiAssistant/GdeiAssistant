package cn.gdeiassistant.core.demo;

import cn.gdeiassistant.common.pojo.document.GradeDocument;
import cn.gdeiassistant.common.pojo.document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.entity.Card;
import cn.gdeiassistant.common.pojo.entity.CardInfo;
import cn.gdeiassistant.common.pojo.entity.Grade;
import cn.gdeiassistant.common.pojo.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public final class DemoCampusData {

    private DemoCampusData() { }

    public static GradeDocument grades() {
        GradeDocument document = new GradeDocument();
        document.setGradeList(List.of(List.of(
                grade("1", "DEMO-101", "高等数学", "4", "92", "4.0"),
                grade("2", "DEMO-102", "数据结构", "3", "89", "3.7")
        )));
        document.setFirstTermGPAList(List.of(4.0));
        document.setFirstTermIGPList(List.of(16.0));
        document.setSecondTermGPAList(List.of(3.7));
        document.setSecondTermIGPList(List.of(11.1));
        return document;
    }

    private static Grade grade(String term, String id, String name, String credit, String score, String gpa) {
        Grade grade = new Grade();
        grade.setGradeYear("2025-2026");
        grade.setGradeTerm(term);
        grade.setGradeId(id);
        grade.setGradeName(name);
        grade.setGradeCredit(credit);
        grade.setGradeScore(score);
        grade.setGradeGpa(gpa);
        grade.setGradeType("必修");
        return grade;
    }

    public static ScheduleDocument schedule() {
        ScheduleDocument document = new ScheduleDocument();
        document.setScheduleList(List.of(
                course("DEMO-101", "高等数学", "1-2", "综合楼 B101", 0, "#4CAF50"),
                course("DEMO-102", "数据结构", "3-4", "教学楼 A201", 16, "#2196F3")
        ));
        return document;
    }

    private static Schedule course(String id, String name, String lesson, String location, int position, String color) {
        Schedule course = new Schedule();
        course.setId(id);
        course.setScheduleName(name);
        course.setScheduleType("必修");
        course.setScheduleLesson(lesson);
        course.setScheduleLocation(location);
        course.setScheduleLength(2);
        course.setScheduleWeek("1-20");
        course.setMinScheduleWeek(1);
        course.setMaxScheduleWeek(20);
        course.setPosition(position);
        course.setRow(position / 7);
        course.setColumn(position % 7);
        course.setColorCode(color);
        return course;
    }

    public static CardInfo cardInfo() {
        CardInfo card = new CardInfo();
        card.setName("演示学生");
        card.setNumber("00000001");
        card.setCardNumber("00000001");
        card.setCardBalance("84.50");
        card.setCardInterimBalance("0.00");
        card.setCardLostState("正常");
        card.setCardFreezeState("正常");
        return card;
    }

    public static List<Card> cardTransactions(LocalDate date) {
        return List.of(
                transaction(date + " 12:00:00", "第一食堂", "-10.00", "84.50"),
                transaction(date + " 08:30:00", "早餐档口", "-5.50", "94.50")
        );
    }

    private static Card transaction(String time, String merchant, String price, String balance) {
        Card card = new Card();
        card.setTradeTime(time);
        card.setMerchantName(merchant);
        card.setTradeName("一卡通消费");
        card.setTradePrice(price);
        card.setAccountBalance(balance);
        return card;
    }
}
