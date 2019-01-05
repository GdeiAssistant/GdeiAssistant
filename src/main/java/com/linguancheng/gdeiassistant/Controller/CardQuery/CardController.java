package com.linguancheng.gdeiassistant.Controller.CardQuery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CardController {

    @RequestMapping(value = "/card", method = RequestMethod.GET)
    public ModelAndView ResolveCardPage() {
        return new ModelAndView("Card/card");
    }

    @RequestMapping(value = "/cardinfo", method = RequestMethod.GET)
    public ModelAndView ResolveCardInfoPage() {
        return new ModelAndView("Card/cardInfo");
    }

    @RequestMapping(value = "/cardlost", method = RequestMethod.GET)
    public ModelAndView ResolveCardLostPage() {
        return new ModelAndView("Card/cardLost");
    }
}
