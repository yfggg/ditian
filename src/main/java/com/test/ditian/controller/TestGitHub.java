package com.test.ditian.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestGitHub {

    public void com() {
        List<String> gov122 =new ArrayList<>();
        List<String> erp =new ArrayList<>();
        List<String> gov122_diff =new ArrayList<>();// gov122有
        List<String> erp_diff =new ArrayList<>();// erp有
        List<String> all =new ArrayList<>(); // 都有
        // 添加车架号
        gov122.add("1");
        gov122.add("2");
        gov122.add("3");
        erp.add("2");
        // 添加数据
//        gov122.add("2");
//        erp.add("1");
//        erp.add("2");
//        erp.add("3");

        Map<String, Integer> map =new HashMap<>(gov122.size() + erp.size());
        for (String carLicensePlate : gov122) {
            map.put(carLicensePlate, 1);
        }
        for (String carLicensePlate : erp) {
            Integer count = map.get(carLicensePlate);
            if (count !=null) {
                map.put(carLicensePlate, 3);
                continue;
            }else {
                map.put(carLicensePlate, 2);
            }
        }
        // 记录
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            switch (entry.getValue()) {
                case 1:
                    gov122_diff.add(entry.getKey());
                    break;
                case 2:
                    erp_diff.add(entry.getKey());
                    break;
                case 3:
                    all.add(entry.getKey());
                    break;
                default:
            }
        }
    }
}
