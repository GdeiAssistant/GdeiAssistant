package edu.gdei.gdeiassistant.Controller.BookQuery.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BookQueryController {

    @RequestMapping(value = "/book", method = RequestMethod.GET)
    public ModelAndView ResolveBookPage() {
        return new ModelAndView("Book/book");
    }
}
