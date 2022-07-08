package cn.gdeiassistant.Service.Socialising.Express;

import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Exception.ExpressException.CorrectRecordException;
import cn.gdeiassistant.Exception.ExpressException.NoRealNameException;
import cn.gdeiassistant.Pojo.Entity.Express;
import cn.gdeiassistant.Pojo.Entity.ExpressComment;
import cn.gdeiassistant.Pojo.Entity.ExpressLike;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Express.ExpressMapper;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExpressService {

    @Autowired
    private ExpressMapper expressMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    /**
     * 分页查询表白信息
     *
     * @param start
     * @param size
     * @param sessionId
     * @return
     */
    public List<Express> QueryExpressPage(int start, int size, String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<Express> expressList = expressMapper.selectExpress(start, size
                , user.getUsername());
        if (expressList != null && !expressList.isEmpty()) {
            for (Express express : expressList) {
                express.setUsername(express.getUsername());
            }
            return expressList;
        }
        return new ArrayList<>();
    }

    /**
     * 分页关键词查询表白信息
     *
     * @param sessionId
     * @param start
     * @param size
     * @param keyword
     * @return
     */
    public List<Express> QueryExpressPageByKeyword(String sessionId, int start, int size, String keyword) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<Express> expressList = expressMapper.selectExpressByKeyWord(start, size, user.getUsername(), keyword);
        if (expressList != null && !expressList.isEmpty()) {
            for (Express express : expressList) {
                express.setUsername(express.getUsername());
            }
            return expressList;
        }
        return new ArrayList<>();
    }

    /**
     * 查询表白评论
     *
     * @param expressId
     * @return
     */
    public List<ExpressComment> QueryExpressComment(int expressId) {
        List<ExpressComment> expressCommentList = expressMapper.selectExpressComment(expressId);
        if (expressCommentList != null && !expressCommentList.isEmpty()) {
            for (ExpressComment expressComment : expressCommentList) {
                expressComment.setUsername(expressComment.getUsername());
            }
            return expressCommentList;
        }
        return new ArrayList<>();
    }

    /**
     * 插入表白评论
     *
     * @param expressId
     * @param sessionId
     * @param comment
     * @throws DataNotExistException
     */
    public void AddExpressComment(int expressId, String sessionId, String comment) throws DataNotExistException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Express express = expressMapper.selectExpressById(expressId, user.getUsername());
        if (express != null) {
            expressMapper.insertExpressComment(expressId, user.getUsername(), comment);
            return;
        }
        throw new DataNotExistException("表白信息不存在");
    }

    /**
     * 添加表白信息
     *
     * @param express
     * @param sessionId
     */
    public void AddExpress(Express express, String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Express data = new Express();
        data.setNickname(express.getNickname());
        if (StringUtils.isNotBlank(express.getRealname())) {
            data.setRealname(express.getRealname());
        }
        data.setSelfGender(express.getSelfGender());
        data.setName(express.getName());
        data.setPersonGender(express.getPersonGender());
        data.setContent(express.getContent());
        data.setUsername(user.getUsername());
        expressMapper.insertExpress(data);
    }

    /**
     * 点赞表白信息
     *
     * @param expressId
     * @param sessionId
     */
    public void LikeExpress(int expressId, String sessionId) throws DataNotExistException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Express express = expressMapper.selectExpressById(expressId, user.getUsername());
        if (express != null) {
            ExpressLike expressLike = expressMapper.selectExpressLike(expressId, user.getUsername());
            if (expressLike == null) {
                expressMapper.insertExpressLike(expressId, user.getUsername());
            }
        }
        throw new DataNotExistException("表白信息不存在");
    }

    /**
     * 猜一下表白人
     *
     * @param expressId
     * @param sessionId
     * @param name
     */
    public boolean GuessExpress(int expressId, String sessionId, String name) throws NoRealNameException, DataNotExistException, CorrectRecordException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        //检查所猜名字与表白者填写的是否相符
        Express express = expressMapper.selectExpressById(expressId, user.getUsername());
        if (express != null) {
            //检测当前用户有无猜中过表白者的记录
            int correctTime = expressMapper.selectCorrectExpressGuessRecord(user.getUsername());
            if (correctTime > 0) {
                //猜中过，不能继续提交猜一下记录
                throw new CorrectRecordException("已经猜中过真实姓名");
            }
            String realname = express.getRealname();
            if (StringUtils.isBlank(realname)) {
                throw new NoRealNameException("表白者未填写真实姓名");
            }
            if (realname.equals(name)) {
                //猜中了真实姓名
                expressMapper.insertExpressGuess(expressId, user.getUsername(), 1);
                return true;
            }
            //没有猜中真实姓名
            expressMapper.insertExpressGuess(expressId, user.getUsername(), 0);
            return false;
        }
        throw new DataNotExistException("表白信息不存在");
    }
}
