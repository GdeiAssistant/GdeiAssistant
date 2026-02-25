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
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void addExpressComment(int expressId, String sessionId, String comment) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = expressMapper.selectExpressById(expressId, user.getUsername());
        if (entity == null) {
            throw new DataNotExistException("表白信息不存在");
        }
        expressMapper.insertExpressComment(expressId, user.getUsername(), comment);
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

    public void likeExpress(int expressId, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        ExpressEntity entity = expressMapper.selectExpressById(expressId, user.getUsername());
        if (entity == null) {
            throw new DataNotExistException("表白信息不存在");
        }
        ExpressLike like = expressMapper.selectExpressLike(expressId, user.getUsername());
        if (like == null) {
            expressMapper.insertExpressLike(expressId, user.getUsername());
        }
    }

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
        if (realname.equals(name)) {
            expressMapper.insertExpressGuess(expressId, user.getUsername(), 1);
            return true;
        }
        expressMapper.insertExpressGuess(expressId, user.getUsername(), 0);
        return false;
    }
}
