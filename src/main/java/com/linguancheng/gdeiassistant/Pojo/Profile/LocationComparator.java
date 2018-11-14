package com.linguancheng.gdeiassistant.Pojo.Profile;

import com.linguancheng.gdeiassistant.Pojo.Entity.Region;

import java.util.Comparator;

public class LocationComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Region r1 = (Region) o1;
        Region r2 = (Region) o2;
        return r1.getName().substring(4, r1.getName().length())
                .compareTo(r2.getName().substring(4, r2.getName().length()));
    }
}
