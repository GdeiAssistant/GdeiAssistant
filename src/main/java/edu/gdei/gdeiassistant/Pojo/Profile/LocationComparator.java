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
        if (r1.getName().contains("中国") && r2.getName().contains("中国")) {
            if (r1.getName().substring(4).equals("中国")) {
                return -1;
            }
            if (r1.getName().substring(4).equals("中国香港")) {
                if (!r2.getName().substring(4).equals("中国")) {
                    return -1;
                }
            }
            if (r1.getName().substring(4).equals("中国澳门")) {
                if (r2.getName().substring(4).equals("中国台湾")) {
                    return -1;
                }
            }
            return 0;
        }
        return Collator.getInstance(Locale.CHINESE).compare(r1.getName().substring(4), r2.getName().substring(4));
    }
}
