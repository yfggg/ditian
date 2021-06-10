package com.test.ditian.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.test.ditian.domain.User;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TestGitHub {

    @GetMapping("/contrast")
    public void contrast() {
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

    @GetMapping("/excel")
    public static void excel() throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for(int n=1;n<4;n++){
            ExportParams exportParams = new ExportParams("用户信息"+n,"用户信息"+n);
            Object entity = User.class;
            List<User> data = new ArrayList<>();
            int i = 0;
            while (i < 10){
                User user = new User();
                user.setName("张三"+i*n);
                data.add(user);
                i++;
            }
            // 构建map
            Map<String,Object> map = new HashMap<>();
            map.put("title",exportParams);
            map.put("entity",entity);
            map.put("data",data);
            list.add(map);
        }

        File savefile = new File(".//temp//excel//");
        if (!savefile.exists()) {
            savefile.mkdirs();
        }
        Workbook workbook = ExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        FileOutputStream fileOutputStream = new FileOutputStream(".//temp//excel//"+"user.xls");
        workbook.write(fileOutputStream);
        fileOutputStream.close();
    }

}
