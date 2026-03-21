package cn.gdeiassistant.core.express.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.ExpressException.CorrectRecordException;
import cn.gdeiassistant.common.exception.ExpressException.NoRealNameException;
import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.common.pojo.Entity.ExpressLike;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.express.converter.ExpressConverter;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.express.pojo.dto.ExpressPublishDTO;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExpressService {

    @Autowired
    private ExpressMapper expressMapper;

    @Autowired
    private ExpressConverter expressConverter;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    public List<ExpressVO> queryExpressPage(int start, int size, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<ExpressEntity> list = expressMapper.selectExpress(start, size, user.getUsername());
        return list == null || list.isEmpty() ? new ArrayList<>() : expressConverter.toVOList(list);
    }

    public List<ExpressVO> queryExpressPageByKeyword(String sessionId, int start, int size, String keyword) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<ExpressEntity> list = expressMapper.selectExpressByKeyWord(start, size, user.getUsername(), keyword);
        return list == null || list.isEmpty() ? new ArrayList<>() : expressConverter.toVOList(list);
    }

    public List<ExpressVO> queryMyExpressList(String sessionId, int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<ExpressEntity> list = expressMapper.selectExpressByUsername(start, size, user.getUsername(), user.getUsername());
        return list == null || list.isEmpty() ? new ArrayList<>() : expressConverter.toVOList(list);
    }

    public ExpressVO queryExpressById(int expressId, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = expressMapper.selectExpressById(expressId, user.getUsername());
        if (entity == null) {
            throw new DataNotExistException("表白信息不存在");
        }
        entity.setCanGuess(StringUtils.isNotBlank(entity.getRealname()));
        entity.setRealname(null);
        return expressConverter.toVO(entity);
    }

    public List<ExpressComment> queryExpressComment(int expressId) {
        List<ExpressComment> list = expressMapper.selectExpressComment(expressId);
        return list == null || list.isEmpty() ? new ArrayList<>() : list;
    }

    @Transactional("appTransactionManager")
    public void addExpressComment(int expressId, String sessionId, String comment) throws DataNotExistException {
        if (comment == null || comment.trim().isEmpty() || comment.length() > 50) {
            throw new IllegalArgumentException("评论内容不能为空且不能超过 50 字");
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = expressMapper.selectExpressById(expressId, user.getUsername());
        if (entity == null) {
            throw new DataNotExistException("表白信息不存在");
        }
        ExpressComment expressComment = new ExpressComment();
        expressComment.setExpressId(expressId);
        expressComment.setUsername(user.getUsername());
        expressComment.setComment(comment);
        expressMapper.insertExpressComment(expressComment);
        interactionNotificationService.createInteractionNotification(
                "express",
                "comment",
                entity.getUsername(),
                user.getUsername(),
                String.valueOf(expressId),
                expressComment.getId() == null ? null : String.valueOf(expressComment.getId()),
                "comment",
                "表白墙收到新评论",
                user.getUsername() + " 评论了你的表白：" + comment
        );
    }

    public void addExpress(ExpressPublishDTO dto, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = new ExpressEntity();
        entity.setUsername(user.getUsername());
        entity.setNickname(dto.getNickname());
        entity.setRealname(dto.getRealname());
        entity.setSelfGender(dto.getSelfGender());
        entity.setName(dto.getName());
        entity.setContent(dto.getContent());
        entity.setPersonGender(dto.getPersonGender());
        expressMapper.insertExpress(entity);
    }

    @Transactional("appTransactionManager")
    public void likeExpress(int expressId, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = expressMapper.selectExpressById(expressId, user.getUsername());
        if (entity == null) {
            throw new DataNotExistException("表白信息不存在");
        }
        ExpressLike like = expressMapper.selectExpressLike(expressId, user.getUsername());
        if (like == null) {
            expressMapper.insertExpressLike(expressId, user.getUsername());
            interactionNotificationService.createInteractionNotification(
                    "express",
                    "like",
                    entity.getUsername(),
                    user.getUsername(),
                    String.valueOf(expressId),
                    null,
                    "like",
                    "表白墙收到新点赞",
                    user.getUsername() + " 点赞了你的表白"
            );
        }
    }

    @Transactional("appTransactionManager")
    public boolean guessExpress(int expressId, String sessionId, String name) throws NoRealNameException, DataNotExistException, CorrectRecordException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = expressMapper.selectExpressById(expressId, user.getUsername());
        if (entity == null) {
            throw new DataNotExistException("表白信息不存在");
        }
        int correctTime = expressMapper.selectCorrectExpressGuessRecord(user.getUsername());
        if (correctTime > 0) {
            throw new CorrectRecordException("已经猜中过真实姓名");
        }
        String realname = entity.getRealname();
        if (StringUtils.isBlank(realname)) {
            throw new NoRealNameException("表白者未填写真实姓名");
        }
        boolean correct = realname.equals(name);
        if (realname.equals(name)) {
            expressMapper.insertExpressGuess(expressId, user.getUsername(), 1);
        } else {
            expressMapper.insertExpressGuess(expressId, user.getUsername(), 0);
        }
        interactionNotificationService.createInteractionNotification(
                "express",
                "guess",
                entity.getUsername(),
                user.getUsername(),
                String.valueOf(expressId),
                null,
                "guess",
                correct ? "表白墙有人猜中了" : "表白墙有人参与猜名字",
                correct ? user.getUsername() + " 猜中了你的表白对象" : user.getUsername() + " 参与了你的猜名字"
        );
        return correct;
    }
}
