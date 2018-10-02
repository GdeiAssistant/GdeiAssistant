package com.linguancheng.gdeiassistant.Controller.CollectionQuery;

import com.linguancheng.gdeiassistant.Pojo.CollectionQuery.*;
import com.linguancheng.gdeiassistant.Pojo.Entity.CollectionDetail;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.CollectionQuery.CollectionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;

@Controller
public class CollectionController {

    @Autowired
    private CollectionQueryService collectionQueryService;

    @RequestMapping(value = "/collection", method = RequestMethod.GET)
    public ModelAndView ResolveCollectionPage() {
        return new ModelAndView("Book/collection");
    }

    @RequestMapping(value = "/rest/collectiondetail", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<CollectionDetail> RestCollectionDetailQuery(@Validated CollectionDetailQuery collectionDetailQuery
            , BindingResult bindingResult) {
        DataJsonResult<CollectionDetail> jsonResult = new DataJsonResult<>();
        if (bindingResult.hasErrors()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合法");
        } else {
            CollectionDetailQueryResult result = collectionQueryService
                    .CollectionDetailQuery(collectionDetailQuery);
            switch (result.getCollectionDetailQueryResultEnum()) {
                case SERVER_ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("移动图书馆系统维护中,请稍候再试");
                    break;

                case TIME_OUT:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("网络连接超时，请重试");
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("该图书暂无馆藏信息");
                    break;

                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(result.getCollectionDetail());
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 查询图书详细信息
     *
     * @param collectionDetailQuery
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/collectiondetail")
    public ModelAndView CollectionDetailQuery(@Validated CollectionDetailQuery collectionDetailQuery, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Book/collectionDetail");
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("QueryErrorMessage", "请求参数不合法");
        } else {
            CollectionDetailQueryResult collectionDetailQueryResult = collectionQueryService
                    .CollectionDetailQuery(collectionDetailQuery);
            switch (collectionDetailQueryResult.getCollectionDetailQueryResultEnum()) {
                case SERVER_ERROR:
                    modelAndView.addObject("QueryErrorMessage", "移动图书馆系统维护中,请稍候再试");
                    break;

                case TIME_OUT:
                    modelAndView.addObject("QueryErrorMessage", "网络连接超时，请重试");
                    break;

                case EMPTY_RESULT:
                    modelAndView.addObject("QueryErrorMessage", "该图书暂无馆藏信息");
                    break;

                case SUCCESS:
                    modelAndView.addObject("CollectionDetail", collectionDetailQueryResult.getCollectionDetail());
                    break;
            }
        }
        return modelAndView;
    }

    /**
     * 查询馆藏信息
     *
     * @param collectionQuery
     * @param bindingResult
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "/collectionquery", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView CollectionQuery(@Validated CollectionQuery collectionQuery, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/collection");
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("QueryErrorMessage", "请求参数不合法");
        } else {
            if (collectionQuery.getPage() == null) {
                collectionQuery.setPage(1);
            }
            CollectionQueryResult collectionQueryResult = collectionQueryService.CollectionQuery(collectionQuery.getPage(), collectionQuery.getBookname());
            switch (collectionQueryResult.getCollectionQueryResultEnum()) {
                case EMPTY_RESULT:
                    //查询没有结果
                    redirectAttributes.addFlashAttribute("QueryErrorMessage", "查询结果为空");
                    break;

                case SERVER_ERROR:
                case TIME_OUT:
                    //服务器异常
                    redirectAttributes.addFlashAttribute("QueryErrorMessage", "移动图书馆系统维护中,暂不可用");
                    redirectAttributes.addFlashAttribute("BookName", collectionQuery.getBookname());
                    break;

                case SUCCESS:
                    //查询成功
                    redirectAttributes.addFlashAttribute("CollectionList", collectionQueryResult.getCollectionList());
                    redirectAttributes.addFlashAttribute("BookName", collectionQuery.getBookname());
                    redirectAttributes.addFlashAttribute("SumPage", collectionQueryResult.getSumPage());
                    break;
            }
        }
        return modelAndView;
    }

    /**
     * 使用rest方式查询馆藏信息,返回JSON数据
     *
     * @param collectionQuery
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/collectionquery", method = RequestMethod.POST)
    @ResponseBody
    public CollectionQueryJsonResult RestfulCollectionQuery(@Validated CollectionQuery collectionQuery, BindingResult bindingResult) {
        CollectionQueryJsonResult collectionQueryJsonResult = new CollectionQueryJsonResult();
        if (bindingResult.hasErrors()) {
            collectionQueryJsonResult.setSuccess(false);
            collectionQueryJsonResult.setErrorMessage("请求参数不合法");
        } else {
            CollectionQueryResult collectionQueryResult = collectionQueryService.CollectionQuery(collectionQuery.getPage(), collectionQuery.getBookname());
            switch (collectionQueryResult.getCollectionQueryResultEnum()) {
                case EMPTY_RESULT:
                    //查询结果为空
                    collectionQueryJsonResult.setSuccess(false);
                    collectionQueryJsonResult.setErrorMessage("查询结果为空");
                    break;

                case SERVER_ERROR:
                case TIME_OUT:
                    //服务器异常
                    collectionQueryJsonResult.setSuccess(false);
                    collectionQueryJsonResult.setErrorMessage("移动图书馆系统维护中,暂不可用");
                    break;

                case SUCCESS:
                    //查询成功
                    collectionQueryJsonResult.setSuccess(true);
                    collectionQueryJsonResult.setCollectionList(collectionQueryResult.getCollectionList());
                    collectionQueryJsonResult.setSumPage(collectionQueryResult.getSumPage());
                    break;
            }
        }
        return collectionQueryJsonResult;
    }

}
