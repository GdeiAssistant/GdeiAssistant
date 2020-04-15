package edu.gdei.gdeiassistant.Pojo.Profile;

import edu.gdei.gdeiassistant.Pojo.Entity.Region;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class LocationComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Region r1 = (Region) o1;
        Region r2 = (Region) o2;
        return Collator.getInstance(Locale.CHINESE).compare(r1.getName(), r2.getName());
    }
}
