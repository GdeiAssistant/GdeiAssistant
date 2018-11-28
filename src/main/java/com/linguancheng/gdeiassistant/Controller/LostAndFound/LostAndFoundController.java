package com.linguancheng.gdeiassistant.Controller.LostAndFound;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.NoAccessException;
import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import com.linguancheng.gdeiassistant.Service.LostAndFound.LostAndFoundService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LostAndFoundController {

    @Autowired
    private LostAndFoundService lostAndFoundService;

    private final String[] LOSTANDFOUND_ITEM_TYPE = {"手机", "校园卡", "身份证"
            , "银行卡", "书", "钥匙"
            , "包包", "衣帽", "校园代步"
            , "运动健身", "数码配件", "其他"};

    /**
     * 进入失物主页
     *
     * @return
     */
    @RequestMapping(value = {"/lostandfound", "/lostandfound/lost"}, method = RequestMethod.GET)
    public ModelAndView ResolveLostIndexPage() {
        return new ModelAndView("LostAndFound/lostIndex");
    }

    /**
     * 进入招领主页
     *
     * @return
     */
    @RequestMapping(value = "/lostandfound/found", method = RequestMethod.GET)
    public ModelAndView ResolveFoundIndexPage() {
        return new ModelAndView("LostAndFound/foundIndex");
    }

    /**
     * 进入发布页面
     *
     * @return
     */
    @RequestMapping(value = "/lostandfound/publish", method = RequestMethod.GET)
    public ModelAndView ResolvePublishPage() {
        return new ModelAndView("LostAndFound/publish");
    }

    /**
     * 进入搜索页面
     *
     * @return
     */
    @RequestMapping(value = "/lostandfound/search/index", method = RequestMethod.GET)
    public ModelAndView ResolveSearchPage() {
        return new ModelAndView("LostAndFound/search");
    }

    /**
     * 进入个人页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/lostandfound/personal", method = RequestMethod.GET)
    public ModelAndView ResolvePersonalPage(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundService
                .QueryPersonalLostAndFoundItems(username);
        List<LostAndFoundItem> lostItemList = new ArrayList<>();
        List<LostAndFoundItem> foundItemList = new ArrayList<>();
        List<LostAndFoundItem> didFoundItemList = new ArrayList<>();
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            if (lostAndFoundItem.getState().equals(1)) {
                didFoundItemList.add(lostAndFoundItem);
            } else {
                if (lostAndFoundItem.getLostType().equals(0)) {
                    lostItemList.add(lostAndFoundItem);
                } else {
                    foundItemList.add(lostAndFoundItem);
                }
            }
        }
        modelAndView.setViewName("LostAndFound/personal");
        modelAndView.addObject("LostItemList", lostItemList);
        modelAndView.addObject("FoundItemList", foundItemList);
        modelAndView.addObject("DidFoundItemList", didFoundItemList);
        return modelAndView;
    }

    /**
     * 编辑失物招领信息
     *
     * @param request
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/lostandfound/edit/id/{id}", method = RequestMethod.GET)
    public ModelAndView EditLostAndFoundInfo(HttpServletRequest request, @PathVariable("id") Integer id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundService
                .QueryLostAndFoundInfoByID(id);
        LostAndFoundItem lostAndFoundItem = lostAndFoundInfo.getLostAndFoundItem();
        if (username.equals(lostAndFoundItem.getUsername())) {
            modelAndView.addObject("LostAndFoundItemID", id);
            modelAndView.addObject("LostAndFoundItemName", lostAndFoundItem.getName());
            modelAndView.addObject("LostAndFoundItemDescription", lostAndFoundItem.getDescription());
            modelAndView.addObject("LostAndFoundItemLocation", lostAndFoundItem.getLocation());
            modelAndView.addObject("LostAndFoundItemLostType", lostAndFoundItem.getLostType());
            modelAndView.addObject("LostAndFoundItemItemType", LOSTANDFOUND_ITEM_TYPE[lostAndFoundItem.getItemType()]);
            modelAndView.addObject("LostAndFoundItemItemTypeValue", lostAndFoundItem.getItemType());
            modelAndView.addObject("LostAndFoundItemQQ", lostAndFoundItem.getQq());
            modelAndView.addObject("LostAndFoundItemWechat", lostAndFoundItem.getWechat());
            modelAndView.addObject("LostAndFoundItemPhone", lostAndFoundItem.getPhone());
            modelAndView.setViewName("LostAndFound/edit");
            return modelAndView;
        }
        throw new NoAccessException("没有权限编辑该失物招领信息");
    }

    /**
     * 关键字查询失物招领信息
     *
     * @param lostType
     * @param keywords
     * @return
     */
    @RequestMapping(value = "/lostandfound/search", method = RequestMethod.POST)
    public ModelAndView SearchLostAndFoundInfo(@Validated @Range(min = 0, max = 1) @RequestParam("type") Integer lostType
            , @Validated @NotBlank @Length(min = 1, max = 50) @RequestParam("keywords") String keywords) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        List<LostAndFoundItem> lostAndFoundItemList = null;
        if (lostType == 0) {
            lostAndFoundItemList = lostAndFoundService.QueryLostItemsWithKeyword(keywords, 0);
        } else {
            lostAndFoundItemList = lostAndFoundService.QueryFoundItemsWithKeyword(keywords, 0);
        }
        modelAndView.setViewName("LostAndFound/searchResult");
        modelAndView.addObject("KeyWord", keywords);
        modelAndView.addObject("LostType", lostType);
        modelAndView.addObject("LostAndFoundItemList", lostAndFoundItemList);
        return modelAndView;
    }

    /**
     * 查看ID对应的失物招领信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/lostandfound/detail/id/{id}", method = RequestMethod.GET)
    public ModelAndView GetLostAndFoundItemDetailInfo(@PathVariable("id") Integer id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundService.QueryLostAndFoundInfoByID(id);
        modelAndView.addObject("LostAndFoundInfo", lostAndFoundInfo);
        modelAndView.setViewName("LostAndFound/ItemDetail");
        return modelAndView;
    }

    /**
     * 根据分类查找失物信息
     *
     * @param type
     * @return
     */
    @RequestMapping(value = "/lostandfound/lostinfo/type/{type}", method = RequestMethod.GET)
    public ModelAndView SearchLostInfoByType(@Validated @Range(min = 0, max = 11) @PathVariable("type") Integer type) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundService.QueryLostItemsByType(type, 0);
        modelAndView.setViewName("LostAndFound/TypeResult");
        modelAndView.addObject("LostAndFoundItemList", lostAndFoundItemList);
        modelAndView.addObject("ItemType", type);
        modelAndView.addObject("LostType", 0);
        modelAndView.addObject("KeyWord", LOSTANDFOUND_ITEM_TYPE[type]);
        return modelAndView;
    }

    /**
     * 根据分类查找招领信息
     *
     * @param type
     * @return
     */
    @RequestMapping(value = "/lostandfound/foundinfo/type/{type}", method = RequestMethod.GET)
    public ModelAndView SearchFoundInfoByType(@Validated @Range(min = 0, max = 11) @PathVariable("type") Integer type) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundService
                .QueryFoundItemsByType(type, 0);
        modelAndView.setViewName("LostAndFound/TypeResult");
        modelAndView.addObject("LostAndFoundItemList", lostAndFoundItemList);
        modelAndView.addObject("ItemType", type);
        modelAndView.addObject("LostType", 1);
        modelAndView.addObject("KeyWord", LOSTANDFOUND_ITEM_TYPE[type]);
        return modelAndView;
    }
}
