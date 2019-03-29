package edu.gdei.gdeiassistant.Controller.CollectionQuery;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionDetailQuery;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionQuery;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionQueryResult;
import edu.gdei.gdeiassistant.Pojo.Entity.CollectionDetail;
import edu.gdei.gdeiassistant.Service.CollectionQuery.CollectionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CollectionController {

    @Autowired
    private CollectionQueryService collectionQueryService;

    @RequestMapping(value = "/collection", method = RequestMethod.GET)
    public ModelAndView ResolveCollectionPage() {
        return new ModelAndView("Book/collection");
    }

    /**
     * 查询图书详细信息
     *
     * @param collectionDetailQuery
     * @return
     */
    @RequestMapping(value = "/collectiondetail")
    public ModelAndView CollectionDetailQuery(@Validated CollectionDetailQuery collectionDetailQuery) throws ServerErrorException, NetWorkTimeoutException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Book/collectionDetail");
        CollectionDetail collectionDetail = collectionQueryService.CollectionDetailQuery(collectionDetailQuery);
        modelAndView.addObject("CollectionDetail", collectionDetail);
        return modelAndView;
    }

    /**
     * 查询馆藏信息
     *
     * @param collectionQuery
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "/collectionquery", method = RequestMethod.POST)
    public ModelAndView CollectionQuery(@Validated CollectionQuery collectionQuery, RedirectAttributes redirectAttributes) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/collection");
        if (collectionQuery.getPage() == null) {
            collectionQuery.setPage(1);
        }
        CollectionQueryResult collectionQueryResult = collectionQueryService.CollectionQuery(collectionQuery.getPage(), collectionQuery.getBookname());
        if (collectionQueryResult == null || collectionQueryResult.getCollectionList().isEmpty()) {
            redirectAttributes.addFlashAttribute("QueryErrorMessage", "查询结果为空");
        } else {
            redirectAttributes.addFlashAttribute("CollectionList", collectionQueryResult.getCollectionList());
            redirectAttributes.addFlashAttribute("BookName", collectionQuery.getBookname());
            redirectAttributes.addFlashAttribute("SumPage", collectionQueryResult.getSumPage());
        }
        return modelAndView;
    }
}
