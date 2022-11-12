package cn.gdeiassistant.Controller.Feedback.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FeedbackController {

    /**
     * 进入帮助反馈主页
     *
     * @return
     */
    @RequestMapping(value = "/feedback", method = RequestMethod.GET)
    public ModelAndView ResolveFeedbackPage() {
        return new ModelAndView("Feedback/index");
    }

    /**
     * 进入帮助反馈常见问题页
     *
     * @return
     */
    @RequestMapping(value = "/feedback/faq", method = RequestMethod.GET)
    public ModelAndView ResolveFeedbackFaqPage() {
        return new ModelAndView("Feedback/faq");
    }

    /**
     * 进入功能意见反馈页
     *
     * @return
     */
    @RequestMapping(value = "/feedback/function", method = RequestMethod.GET)
    public ModelAndView ResolveFeedbackFunctionPage() {
        return new ModelAndView("Feedback/function");
    }

    /**
     * 进入故障工单提交页
     *
     * @return
     */
    @RequestMapping(value = "/feedback/ticket", method = RequestMethod.GET)
    public ModelAndView ResolveFeedbackTicketPage() {
        return new ModelAndView("Feedback/ticket");
    }
}
