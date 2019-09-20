package edu.gdei.gdeiassistant.Pojo.Profile;

import edu.gdei.gdeiassistant.Pojo.Entity.Attribution;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class AttributionComparator implements Comparator<Attribution> {

    @Override
    public int compare(Attribution o1, Attribution o2) {
        return Collator.getInstance(Locale.CHINESE).compare(o1.getName().substring(0, 1), o2.getName().substring(0, 1));
    }
}
