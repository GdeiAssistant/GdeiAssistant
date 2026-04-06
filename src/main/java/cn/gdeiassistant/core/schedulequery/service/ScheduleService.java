package cn.gdeiassistant.core.schedulequery.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.common.exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.common.pojo.Document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.Entity.CustomSchedule;
import cn.gdeiassistant.common.pojo.Entity.Schedule;
import cn.gdeiassistant.common.pojo.Entity.TeacherSchedule;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userLogin.service.TeacherLoginService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.edu.pojo.EduSessionCredential;
import cn.gdeiassistant.common.tools.Utils.ScheduleUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.common.tools.Utils.WeekUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    private final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private TeacherLoginService teacherLoginService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private EduSystemClient eduSystemClient;

    /**
     * 获取自定义课表信息
     */
    public CustomScheduleDocument getCustomSchedule(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return scheduleDao.queryCustomSchedule(user.getUsername());
    }

    /**
     * 添加自定义课程信息
     *
     * @param sessionId
     * @param customSchedule
     * @throws GenerateScheduleException
     * @throws CountOverLimitException
     */
    public void addCustomSchedule(String sessionId, CustomSchedule customSchedule)
            throws GenerateScheduleException, CountOverLimitException, NotAvailableConditionException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        CustomScheduleDocument doc = getCustomSchedule(sessionId);
        if (doc != null && doc.getScheduleMap() != null) {
            int newRow = customSchedule.getPosition() / 7;
            int newCol = customSchedule.getPosition() % 7;
            int newLen = customSchedule.getScheduleLength() != null ? customSchedule.getScheduleLength() : 1;
            int newRowEnd = newRow + newLen - 1;
            int minWeek = customSchedule.getMinScheduleWeek() != null ? customSchedule.getMinScheduleWeek() : 1;
            int maxWeek = customSchedule.getMaxScheduleWeek() != null ? customSchedule.getMaxScheduleWeek() : 20;
            for (Schedule s : doc.getScheduleMap().values()) {
                if (s.getPosition() == null) continue;
                int exCol = s.getPosition() % 7;
                if (newCol != exCol) continue;
                int exRow = s.getRow() != null ? s.getRow() : (s.getPosition() / 7);
                int exLen = s.getScheduleLength() != null ? s.getScheduleLength() : 1;
                int exRowEnd = exRow + exLen - 1;
                if (newRowEnd < exRow || exRowEnd < newRow) continue;
                Integer exMin = s.getMinScheduleWeek();
                Integer exMax = s.getMaxScheduleWeek();
                if (exMin == null) exMin = 1;
                if (exMax == null) exMax = 20;
                if (minWeek <= exMax && maxWeek >= exMin) {
                    throw new NotAvailableConditionException("该时间段已存在自定义课程");
                }
            }
        }
        scheduleDao.addCustomSchedule(user.getUsername(), customSchedule);
    }

    /**
     * 删除自定义课程信息（按 position 定位，CustomSchedule 无 id 字段）。
     * 校验该 position 必须属于当前 sessionId 对应用户的 custom_schedule，否则视为越权。
     *
     * @param sessionId 当前会话
     * @param position  自定义课程 position
     * @throws cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException 当该 position 非当前用户自定义课程时
     */
    public void deleteCustomSchedule(String sessionId, Integer position) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        boolean removed = scheduleDao.deleteCustomSchedule(user.getUsername(), position);
        if (!removed) {
            throw new DataNotExistException("非法操作");
        }
    }

    /**
     * 清空缓存的课表信息
     *
     * @param sessionId
     */
    public void clearSchedule(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        scheduleDao.removeSchedule(user.getUsername());
    }

    /**
     * 查询课表
     *
     * @param sessionId
     * @param queryWeek
     * @return
     * @throws Exception
     */
    public ScheduleQueryResult querySchedule(String sessionId, Integer queryWeek) throws NetWorkTimeoutException, PasswordIncorrectException, ServerErrorException, TimeStampIncorrectException {
        ScheduleQueryResult ScheduleQueryResult = new ScheduleQueryResult();
        //优先从缓存中加载
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String username = user.getUsername();
        ScheduleDocument scheduleDocument = scheduleDao.querySchedule(username);
        if (scheduleDocument == null) {
            UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
            EduSessionCredential credential = toEduCredential(userCertificate);
            try {
                Document document = eduSystemClient.fetchScheduleDocument(sessionId, credential);
                //获取存放课表信息的表格
                Element table = document.getElementById("Table1");
                                //获取表格中的所有行
                                Elements trs = table.select("tr");
                                //特殊的空课程,用于标记当前单元格被上边的课程信息占用,即上边同列课程的课程时长大于1
                                Schedule specialEmptySchedule = new Schedule();
                                //存放课表信息的数组,包含特殊的空课程对象
                                Schedule[][] schedulesWithSpecialEmptySchedule = new Schedule[70][10];
                                //存放课表信息的列表,过滤特殊的空课程对象后添加入该列表
                                List<Schedule> schedulesWithoutSpecialEmptySchedule = new ArrayList<>();
                                //当前单元格位置position值
                                int currentPosition = 0;
                                //当前行最大位置position值
                                int currentRowMaxPosition = 0;
                                //前两行为行列信息,从第三行开始获取信息
                                for (int row = 2; row < trs.size(); row++) {
                                    //获取该行内所有列
                                    Elements tds = trs.get(row).select("td");
                                    //记录当前访问列数据的游标,若当前为第3行或第7行或第11行,初始列游标值为2,否则初始值为1
                                    //因为特殊行里面的第一列包含上午/下午的信息提示
                                    int currentColumnIndexInThisRow;
                                    if (row == 2 || row == 6 || row == 10) {
                                        currentColumnIndexInThisRow = 2;
                                    } else {
                                        currentColumnIndexInThisRow = 1;
                                    }
                                    //当前行的列最大游标
                                    int maxColumnIndex;
                                    if (tds.size() < 7) {
                                        maxColumnIndex = tds.size() - 1;
                                    } else {
                                        maxColumnIndex = 6;
                                    }
                                    //遍历当前行,获取课表信息
                                    for (currentRowMaxPosition = currentRowMaxPosition + 7; currentPosition < currentRowMaxPosition; currentPosition++) {
                                        if (schedulesWithSpecialEmptySchedule[currentPosition][0] == specialEmptySchedule) {
                                            //当前position指向特殊空Schedule对象,跳过当前单元格
                                        } else {
                                            //判断当前行是否已经遍历完
                                            if (currentPosition % 7 <= maxColumnIndex) {
                                                //判断当前position的课程信息是否为空课表信息
                                                //下面的字符非空格而是一个特殊的Unicode字符
                                                if (tds.get(currentColumnIndexInThisRow).text().equals(" ")) {
                                                    //不存在课表信息
                                                } else {
                                                    //当前td标签属性,代表课程时长
                                                    int rowspan;
                                                    //判断td标签有无rowspan属性,该属性代表课程时长
                                                    if (tds.get(currentColumnIndexInThisRow).hasAttr("rowspan")) {
                                                        //通过rowspan属性得到课程时长
                                                        rowspan = Integer.parseInt(tds.get(currentColumnIndexInThisRow).attr("rowspan"));
                                                        //将当前单元格下方对应的原单元格的课程信息标记为特殊空课程
                                                        switch (rowspan) {
                                                            case 2:
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 7][0] = specialEmptySchedule;
                                                                break;

                                                            case 3:
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 7][0] = specialEmptySchedule;
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 14][0] = specialEmptySchedule;
                                                                break;

                                                            case 4:
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 7][0] = specialEmptySchedule;
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 14][0] = specialEmptySchedule;
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 21][0] = specialEmptySchedule;
                                                                break;
                                                        }
                                                    } else {
                                                        //如果td标签没有rowspan属性,则该课程课程时长为默认的1
                                                        rowspan = 1;
                                                    }
                                                    //将单元格里的课表信息按独立行进行分割单独处理
                                                    String[] string = tds.get(currentColumnIndexInThisRow).text().split("\\$info\\$");
                                                    //记录单元格中的独立课表信息下标
                                                    int n = 0;
                                                    if (string.length == 0) {
                                                        schedulesWithSpecialEmptySchedule[currentPosition][0] = specialEmptySchedule;
                                                    } else {
                                                        for (int j = 0; j < string.length; j++) {
                                                            if (string[j].isEmpty() || string[j].charAt(0) == '<' || string[j].equals(" ") || string[j].charAt(0) == '(') {
                                                                //不是有效的课表头信息,跳过并查询下一个独立行的信息,直到得到有效的课表头信息
                                                            } else {
                                                                //有效的课表头信息,进行信息处理
                                                                String[] time = string[j + 2].split("[{]");
                                                                //课程周数
                                                                String week = null;
                                                                if (time.length < 2) {
                                                                    week = time[0].substring(0, time[0].length() - 1);
                                                                } else {
                                                                    week = time[1].substring(0, time[1].length() - 1);
                                                                }
                                                                //课程名称
                                                                String name = string[j];
                                                                //课程节数
                                                                String lesson = time[0];
                                                                //课程类型
                                                                String type = string[j + 1];
                                                                //任课教师
                                                                String teacher = string[j + 3];
                                                                //上课地点
                                                                String location = "";
                                                                //检查是否已经安排课程
                                                                if (string.length <= j + 4 || string[j + 4] == null) {
                                                                    location = "暂未安排";
                                                                } else {
                                                                    if (string[j + 4].contains("（")) {
                                                                        String[] locations = string[j + 4].split("（");
                                                                        location = locations[0];
                                                                    } else {
                                                                        location = string[j + 4];
                                                                    }
                                                                }
                                                                Schedule schedule = new Schedule();
                                                                schedule.setPosition(currentPosition);
                                                                schedule.setScheduleLength(rowspan);
                                                                schedule.setScheduleName(name);
                                                                schedule.setScheduleType(type);
                                                                schedule.setScheduleLesson(lesson);
                                                                String[] weekString = week.split("-");
                                                                int minWeekNumber;
                                                                int maxWeekNumber;
                                                                if (weekString[1].contains("|")) {
                                                                    String[] s1 = weekString[1].split("\\|");
                                                                    minWeekNumber = Integer.valueOf(weekString[0].substring(1));
                                                                    maxWeekNumber = Integer.valueOf(s1[0].substring(0, s1[0].length() - 1));
                                                                } else {
                                                                    minWeekNumber = Integer.valueOf(weekString[0].substring(1));
                                                                    maxWeekNumber = Integer.valueOf(weekString[1].substring(0, weekString[1].length() - 1));
                                                                }
                                                                schedule.setMinScheduleWeek(minWeekNumber);
                                                                schedule.setMaxScheduleWeek(maxWeekNumber);
                                                                schedule.setScheduleWeek("第" + minWeekNumber + "周至第" + maxWeekNumber + "周");
                                                                schedule.setScheduleTeacher(teacher);
                                                                schedule.setScheduleLocation(location);
                                                                schedule.setRow(row - 2);
                                                                if (row == 2 || row == 6 || row == 10) {
                                                                    schedule.setColumn(currentColumnIndexInThisRow - 2);
                                                                } else {
                                                                    schedule.setColumn(currentColumnIndexInThisRow - 1);
                                                                }
                                                                schedule.setColorCode(ScheduleUtils.getScheduleColor(currentPosition));
                                                                schedulesWithSpecialEmptySchedule[currentPosition][n] = schedule;
                                                                n++;
                                                                j = j + 4;
                                                            }
                                                        }
                                                    }
                                                }
                                                currentColumnIndexInThisRow = currentColumnIndexInThisRow + 1;
                                            }
                                        }
                                    }
                                }
                                for (Schedule[] schedules : schedulesWithSpecialEmptySchedule) {
                                    for (Schedule schedule : schedules) {
                                        if (schedule != null && schedule != specialEmptySchedule) {
                                            schedulesWithoutSpecialEmptySchedule.add(schedule);
                                        }
                                    }
                                }
                                ScheduleQueryResult.setScheduleList(schedulesWithoutSpecialEmptySchedule);
            } catch (ServerErrorException e) {
                logger.error("查询课表异常：", e);
                throw new ServerErrorException("教务系统异常");
            } catch (PasswordIncorrectException ignored) {
                throw new PasswordIncorrectException("用户密码错误");
            } catch (TimeStampIncorrectException e) {
                logger.error("查询课表异常；", e);
                throw new TimeStampIncorrectException("时间戳校验失败");
            } catch (IOException e) {
                logger.error("查询课表异常：", e);
                throw new NetWorkTimeoutException("网络连接超时");
            } catch (Exception e) {
                logger.error("查询课表异常：", e);
                throw new ServerErrorException("教务系统异常");
            }
        } else {
            //从缓存中获取，并做空指针防御
            List<Schedule> cachedList = scheduleDocument.getScheduleList();
            if (cachedList == null) {
                cachedList = new ArrayList<>();
                scheduleDocument.setScheduleList(cachedList);
            }
            ScheduleQueryResult.setScheduleList(cachedList);
        }
        // 获取自定义课表信息（空指针防御）
        // 合并前对自定义课打标 isCustom=true，教务/缓存课程不设置，前端仅认此标记控制删除按钮
        CustomScheduleDocument customScheduleDocument = getCustomSchedule(sessionId);
        if (customScheduleDocument != null && customScheduleDocument.getScheduleMap() != null) {
            List<Schedule> mainList = ScheduleQueryResult.getScheduleList();
            if (mainList != null) {
                for (Schedule schedule : customScheduleDocument.getScheduleMap().values()) {
                    schedule.setIsCustom(true);
                    mainList.add(schedule);
                }
            }
        }
        if (queryWeek == null) {
            //无指定查询周数，则默认返回当前周数课表
            ScheduleQueryResult.setScheduleList(ScheduleUtils.getSpecifiedWeekSchedule
                    (ScheduleQueryResult.getScheduleList(), WeekUtils.getCurrentWeek()));
            ScheduleQueryResult.setWeek(WeekUtils.getCurrentWeek());
        } else if (queryWeek.equals(0)) {
            //若周数指定为0，则返回所有周数的课表
            ScheduleQueryResult.setScheduleList(ScheduleQueryResult.getScheduleList());
            ScheduleQueryResult.setWeek(0);
        } else {
            //返回指定周数的课表
            ScheduleQueryResult.setScheduleList(ScheduleUtils.getSpecifiedWeekSchedule
                    (ScheduleQueryResult.getScheduleList(), queryWeek));
            ScheduleQueryResult.setWeek(queryWeek);
        }
        return ScheduleQueryResult;
    }

    /**
     * 强制更新当前用户的课表缓存（清空 MongoDB 缓存并实时从教务系统拉取一次）
     * 清空 MongoDB 缓存并实时从教务系统拉取一次。
     *
     * @param sessionId
     */
    public void updateScheduleCache(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String username = user.getUsername();
        // 清空缓存的课表信息，下次查询时从教务系统实时获取
        scheduleDao.removeSchedule(username);
        // 主动触发一次实时查询，保证本次操作后前端能看到最新课表（结果通过前端后续 getSchedule 再拉取）
        // queryWeek 传 null，按当前周处理
        querySchedule(sessionId, null);
    }

    /**
     * 查询教师个人课表
     *
     * @param sessionId
     * @param username
     * @param password
     * @param year
     * @param term
     * @param teacherName
     * @return
     */
    @Deprecated
    public List<TeacherSchedule> teacherScheduleQuery(String sessionId
            , String username, String password, String year, String term, String teacherName) throws NetWorkTimeoutException, ServerErrorException, PasswordIncorrectException, RecognitionException {
        Document document;
        try {
            document = eduSystemClient.fetchTeacherScheduleDocument(sessionId, username, teacherName, year, term);
        } catch (ServerErrorException e) {
            if (e.getMessage() != null && e.getMessage().contains("需要重新登录")) {
                teacherLoginService.TeacherLogin(sessionId, username, password);
                try {
                    document = eduSystemClient.fetchTeacherScheduleDocument(sessionId, username, teacherName, year, term);
                } catch (IOException ex) {
                    logger.error("教师个人课表查询异常：", ex);
                    throw new NetWorkTimeoutException("网络连接超时");
                }
            } else {
                throw e;
            }
        } catch (IOException e) {
            logger.error("教师个人课表查询异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        }
        //获取存放课表信息的表格
        Element table = document.getElementById("Table6");
        Elements trs = table.select("tr");
        TeacherSchedule specialEmptySchedule = new TeacherSchedule();
        TeacherSchedule[] schedulesWithSpecialEmptySchedule = new TeacherSchedule[84];
        List<TeacherSchedule> schedulesWithoutSpecialEmptySchedule = new ArrayList<>();
        int currentPosition = 0;
        int currentRowMaxPosition = 0;
        for (int row = 2; row < trs.size(); row++) {
                                //获取该行内所有列
                                Elements tds = trs.get(row).select("td");
                                //记录当前访问列数据的游标,若当前为第3行或第8行或第13行,初始列游标值为2,否则初始值为1
                                //因为特殊行里面的第一列包含上午/下午的信息提示
                                int currentColumnIndexInThisRow;
                                if (row == 2 || row == 7 || row == 11) {
                                    currentColumnIndexInThisRow = 2;
                                } else {
                                    currentColumnIndexInThisRow = 1;
                                }
                                //当前行的列最大游标
                                int maxColumnIndex;
                                if (tds.size() < 7) {
                                    maxColumnIndex = tds.size() - 1;
                                } else {
                                    maxColumnIndex = 6;
                                }
                                //遍历当前行,获取课表信息
                                for (currentRowMaxPosition = currentRowMaxPosition + 7; currentPosition < currentRowMaxPosition; currentPosition++) {
                                    if (schedulesWithSpecialEmptySchedule[currentPosition] == specialEmptySchedule) {
                                        //当前position指向特殊空Schedule对象,跳过当前单元格
                                    } else {
                                        //判断当前行是否已经遍历完
                                        if (currentPosition % 7 <= maxColumnIndex) {
                                            //判断当前position的课程信息是否为空课表信息
                                            //下面的字符非空格而是一个特殊的Unicode字符
                                            if (tds.get(currentColumnIndexInThisRow).text().equals(" ")) {
                                                //不存在课表信息
                                            } else {
                                                //当前td标签属性,代表课程时长
                                                int rowspan;
                                                //判断td标签有无rowspan属性,该属性代表课程时长
                                                if (tds.get(currentColumnIndexInThisRow).hasAttr("rowspan")) {
                                                    //通过rowspan属性得到课程时长
                                                    rowspan = Integer.parseInt(tds.get(currentColumnIndexInThisRow).attr("rowspan"));
                                                    //将当前单元格下方对应的原单元格的课程信息标记为特殊空课程
                                                    switch (rowspan) {
                                                        case 2:
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 7] = specialEmptySchedule;
                                                            break;

                                                        case 3:
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 7] = specialEmptySchedule;
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 14] = specialEmptySchedule;
                                                            break;

                                                        case 4:
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 7] = specialEmptySchedule;
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 14] = specialEmptySchedule;
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 21] = specialEmptySchedule;
                                                            break;
                                                    }
                                                } else {
                                                    //如果td标签没有rowspan属性,则该课程课程时长为默认的1
                                                    rowspan = 1;
                                                }
                                                //将单元格里的课表信息按独立行进行分割单独处理
                                                String[] string = tds.get(currentColumnIndexInThisRow).text().split(" ");
                                                //记录单元格中的独立课表信息下标
                                                for (int j = 0; j < string.length; j++) {
                                                    if (string.length == 0) {
                                                        schedulesWithSpecialEmptySchedule[currentPosition] = specialEmptySchedule;
                                                    } else if (string[j].isEmpty() || string[j].charAt(0) == '<' || string[j].equals(" ") || string[j].charAt(0) == '(') {
                                                        //不是有效的课表头信息,跳过并查询下一个独立行的信息,直到得到有效的课表头信息
                                                    } else {
                                                        //有效的课表头信息,进行信息处理
                                                        String scheduleName = string[j];
                                                        String scheduleType = string[j + 1];
                                                        String scheduleTime = string[j + 2];
                                                        String scheduleWeek = (scheduleTime.split("\\("))[0];
                                                        String scheduleLesson = ((scheduleTime.split("\\("))[1]).split("\\)")[0];
                                                        String scheduleLocation = string[j + 4];
                                                        String scheduleClass = string[j + 5];
                                                        TeacherSchedule teacherSchedule = new TeacherSchedule();
                                                        teacherSchedule.setPosition(currentPosition);
                                                        teacherSchedule.setScheduleLength(rowspan);
                                                        teacherSchedule.setScheduleName(scheduleName);
                                                        teacherSchedule.setScheduleType(scheduleType);
                                                        teacherSchedule.setScheduleLesson(scheduleLesson);
                                                        teacherSchedule.setScheduleWeek(scheduleWeek);
                                                        teacherSchedule.setScheduleClass(scheduleClass);
                                                        teacherSchedule.setScheduleLocation(scheduleLocation);
                                                        teacherSchedule.setRow(row - 2);
                                                        if (row == 2 || row == 7 || row == 11) {
                                                            teacherSchedule.setColumn(currentColumnIndexInThisRow - 2);
                                                        } else {
                                                            teacherSchedule.setColumn(currentColumnIndexInThisRow - 1);
                                                        }
                                                        teacherSchedule.setColorCode(ScheduleUtils.getScheduleColor(currentPosition));
                                                        schedulesWithSpecialEmptySchedule[currentPosition] = teacherSchedule;
                                                        j = j + 5;
                                                    }
                                                }
                                            }
                                            currentColumnIndexInThisRow = currentColumnIndexInThisRow + 1;
                                        }
                                    }
                                }
                            }
        for (TeacherSchedule teacherSchedule : schedulesWithSpecialEmptySchedule) {
            if (teacherSchedule != null && teacherSchedule != specialEmptySchedule) {
                schedulesWithoutSpecialEmptySchedule.add(teacherSchedule);
            }
        }
        return schedulesWithoutSpecialEmptySchedule;
    }

    private static EduSessionCredential toEduCredential(UserCertificateEntity entity) {
        EduSessionCredential cred = new EduSessionCredential();
        cred.setUsername(entity.getUser() != null ? entity.getUser().getUsername() : null);
        cred.setNumber(entity.getNumber());
        cred.setKeycode(entity.getKeycode());
        cred.setTimestamp(entity.getTimestamp());
        return cred;
    }
}
