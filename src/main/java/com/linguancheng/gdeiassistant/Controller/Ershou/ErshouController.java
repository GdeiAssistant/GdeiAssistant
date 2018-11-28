package com.linguancheng.gdeiassistant.Controller.Ershou;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.NoAccessException;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.NotAvailableStateException;
import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouItem;
import com.linguancheng.gdeiassistant.Service.Ershou.ErshouService;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ErshouController {

    @Autowired
    private ErshouService ershouService;

    private final String[] ERSHOU_ITEM_TYPE = {"校园代步", "手机", "电脑"
            , "数码配件", "数码", "电器"
            , "运动健身", "衣物伞帽", "图书教材"
            , "租赁", "生活娱乐", "其他"};

    /**
     * 进入二手交易首页
     *
     * @return
     */
    @RequestMapping(value = "/ershou", method = RequestMethod.GET)
    public ModelAndView ResolveErshouIndexPage() {
        return new ModelAndView("Ershou/ershouIndex");
    }

    /**
     * 进入二手交易信息发布界面
     *
     * @return
     */
    @RequestMapping(value = "/ershou/publish", method = RequestMethod.GET)
    public ModelAndView ResolveErshouPublishPage() {
        return new ModelAndView("Ershou/ershouPublish");
    }

    /**
     * 进入二手交易信息编辑界面
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = {"/ershou/edit/id/{id}"}, method = RequestMethod.GET)
    public ModelAndView ResolveErshouEditPage(HttpServletRequest request, @PathVariable("id") int id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        ErshouInfo ershouInfo = ershouService.QueryErshouInfoByID(id);
        if (username.equals(ershouInfo.getErshouItem().getUsername())) {
            if (ershouInfo.getErshouItem().getState().equals(1) || ershouInfo.getErshouItem().getState().equals(0)) {
                modelAndView.addObject("ErshouItemID", id);
                modelAndView.addObject("ErshouItemName", ershouInfo.getErshouItem().getName());
                modelAndView.addObject("ErshouItemDescription", ershouInfo.getErshouItem().getDescription());
                modelAndView.addObject("ErshouItemPrice", ershouInfo.getErshouItem().getPrice());
                modelAndView.addObject("ErshouItemLocation", ershouInfo.getErshouItem().getLocation());
                modelAndView.addObject("ErshouItemType", ERSHOU_ITEM_TYPE[ershouInfo.getErshouItem().getType()]);
                modelAndView.addObject("ErshouItemTypeValue", ershouInfo.getErshouItem().getType());
                modelAndView.addObject("ErshouItemQQ", ershouInfo.getErshouItem().getQq());
                modelAndView.addObject("ErshouItemPhone", ershouInfo.getErshouItem().getPhone());
                modelAndView.setViewName("Ershou/ershouEdit");
                return modelAndView;
            }
            throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
        }
        throw new NoAccessException("没有权限编辑该二手交易信息");
    }

    /**
     * 进入二手交易个人界面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/ershou/personal"}, method = RequestMethod.GET)
    public ModelAndView ResolveErshouPersonalPage(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Ershou/ershouPersonal");
        String username = (String) request.getSession().getAttribute("username");
        List<ErshouItem> ershouItemList = ershouService.QueryPersonalErShouItems(username);
        List<ErshouItem> availableErshouItemList = new ArrayList<>();
        List<ErshouItem> soldedErshouItemList = new ArrayList<>();
        List<ErshouItem> notAvailableErshouItemList = new ArrayList<>();
        for (ErshouItem ershouItem : ershouItemList) {
            switch (ershouItem.getState()) {
                case 0:
                    //下架的二手交易商品
                    notAvailableErshouItemList.add(ershouItem);
                    break;

                case 1:
                    //待出售的二手交易商品
                    availableErshouItemList.add(ershouItem);
                    break;

                case 2:
                    //已出售的的二手交易商品
                    soldedErshouItemList.add(ershouItem);
                    break;
            }
        }
        modelAndView.addObject("NotAvailableErshouItemList", notAvailableErshouItemList);
        modelAndView.addObject("AvailableErshouItemList", availableErshouItemList);
        modelAndView.addObject("SoldedErshouItemList", soldedErshouItemList);
        return modelAndView;
    }

    /**
     * 查询指定关键字二手交易信息
     *
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/ershou/search/keyword/{keyword}", method = RequestMethod.GET)
    public ModelAndView SearchErshouItemWithKeyword(@Validated @Range(min = 1, max = 25) @PathVariable("keyword") String keyword) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("KeyWord", keyword);
        List<ErshouItem> ershouItemList = ershouService.QueryErshouItemsWithKeyword(keyword, 0);
        modelAndView.addObject("ErshouItemList", ershouItemList);
        modelAndView.setViewName("Ershou/ershouSearch");
        return modelAndView;
    }

    /**
     * 查询特殊类型的二手交易信息
     *
     * @param type
     * @return
     */
    @RequestMapping(value = {"/ershou/type/{type}"}, method = RequestMethod.GET)
    public ModelAndView SearchErshouItemByType(@Validated @Range(min = 0, max = 11) @PathVariable("type") int type) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("TypeNumber", type);
        modelAndView.addObject("TypeName", ERSHOU_ITEM_TYPE[type]);
        List<ErshouItem> ershouItemList = ershouService.QueryErshouItemByType(type, 0);
        modelAndView.addObject("ErshouItemList", ershouItemList);
        modelAndView.setViewName("Ershou/ershouType");
        return modelAndView;
    }

    /**
     * 查询指定ID的二手交易商品的详细信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"ershou/detail/id/{id}"})
    public ModelAndView GetErshouItemDetail(@PathVariable("id") int id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        ErshouInfo ershouInfo = ershouService.QueryErshouInfoByID(id);
        if (ershouInfo.getErshouItem().getState().equals(0)) {
            throw new NotAvailableStateException("已下架的二手交易信息不能查看");
        } else if (ershouInfo.getErshouItem().getState().equals(2)) {
            throw new ConfirmedStateException("已出售的二手交易信息不能查看");
        }
        modelAndView.addObject("ErshouInfo", ershouInfo);
        modelAndView.setViewName("Ershou/ershouDetail");
        return modelAndView;
    }
}
