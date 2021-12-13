package com.test.ditian.controller;

import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.test.ditian.entity.Account122;
import com.test.ditian.entity.Account122DTO;
import com.test.ditian.entity.Customer;
import com.test.ditian.entity.Order;
import com.test.ditian.prolificacy.CommonTool;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TestController {

//    @Autowired
//    CommonTool commonTool;

//    @GetMapping("/test")
//    public String test(HttpServletResponse response) {
//        List<Account122> list = null;
//        Account122 a = new Account122();
//        Account122 b = new Account122();
//        a.setName("yf");
//        a.setAmount(11L);
//        b.setName("wyp");
//        b.setAmount(11L);
////        list.add(a);
////        list.add(b);
////        commonTool.exportExcel("表名","文件名",list,Account122.class, response);
////        ExcelImportResult<Account122> result = commonTool.inputExcel("excel/excel.xls", Account122.class);
////        String a = commonTool.ZonedDateTimeToString(ZonedDateTime.now(), "yyyy-MM-dd HH:mm:ss");
////        ZonedDateTime a = commonTool.StringToZonedDateTime("2021-08-01 00:00:00");
////        ZonedDateTime b = a.plusDays(3L);
////        List<Account122DTO> list2 = commonTool.copyList(list, Account122DTO.class);
////        Map<String, Object> testMap = new HashMap<>();
////        testMap.put("key1", "value1");
////        testMap.put("key2", "value2");
////        List<Object> list2 = new ArrayList();
////        list2.add(a);
////        list2.add(b);
////        Account122 c = new Account122();
//
////        List<Account122> list1 = commonTool.toList(commonTool.toJson(list),Account122.class);
////        Map<String, Object> testMap2 = commonTool.toMap(commonTool.toJson(testMap));
////        List<Object> list1 = commonTool.toList(commonTool.toJson(list));
//
////        Optional.ofNullable(a)
////                .map(x->{
////                    x.setAmount(123L);
////                    return x;
////                }).orElseGet(()->{
////                    System.out.println("222");
////                    return null;
////                });
//
//        // 写出对应的模板
//        Optional<String> optional = Optional.ofNullable(b)
//                .filter(x -> "wyp".equals(x.getName()) || 12L==x.getAmount())
//                .map(x -> x.getName());
//
//        if(optional.isPresent()) {
//            return optional.get();
//        }
//        if(StringUtils.isNotEmpty("url")) {
//            Optional.ofNullable(a).map(x->{x.setName("url");return x;});
//        }
//
//
//        // 写出对应的模板
////        Optional.ofNullable(list).map(
////                l -> {l.stream().forEach(System.out::println);
////                return null;}
////        ).orElseGet(() -> {System.out.println("xxx");return null;});
//
//
//        return null;
//    }

//    @PostMapping("/test2")
//    public String test2(MultipartFile[] mFiles) {
//       return commonTool.fileUpload("D:\\data\\", mFiles);
//    }

    @PostMapping("/test1")
    public void response(@RequestBody String s) {//@RequestBody



//        ArrayList<Integer> numbersList = new ArrayList<>(Arrays.asList(1, 1, 2, 3, 3, 3, 4, 5, 6, 6, 6, 7, 8));
//        numbersList.stream().distinct().forEach(System.out::println);

//        List<String> list = Arrays.asList("a", "b", "c", "d", "a", "a", "d", "d");
//        Account122 account122 = new Account122();
//        account122.setId(1L);
//        account122.setName("y");
//        Account122 account12 = new Account122();
//        account12.setId(1L);
//        account12.setName("f");
//        Account122 account1 = new Account122();
//        account1.setId(2L);
//        account1.setName("f");
//
//        List<Account122> list = new ArrayList<>();
//        list.add(account122);
//        list.add(account12);
//        list.add(account1);

//        list.stream().distinct().forEach(System.out::println);

//        list.stream()
////                .filter(CommonTool.getDuplicateElements(Account122::getId))
//                .filter(CommonTool.getDuplicateElements(Account122::getName))
//                .forEach(System.out::println);
//                .forEach(list::add);
//        list.forEach(System.out::println);

//        List<Account122> duplicate = CommonTool.getDuplicateElements(list);
//        System.out.println(duplicate);

//        Customer sheridan = new Customer("Sheridan");
//        Customer ivanova = new Customer("Ivanova");
//        Customer garibaldi = new Customer("Garibaldi");
//
//        sheridan.addOrder(new Order(1))
//                .addOrder(new Order(2))
//                .addOrder(new Order(3));
//        ivanova.addOrder(new Order(4))
//                .addOrder(new Order(5));
//
//        List<Customer> customers = Arrays.asList(sheridan, ivanova, garibaldi);
////        customers.stream().map(Customer::getName).forEach(System.out::println);
//        customers.stream().map(Customer::getOrders).forEach(System.out::println);
//        customers.stream().map(customer -> customer.getOrders().stream()).forEach(System.out::println);
//        customers.stream().flatMap(customer -> customer.getOrders().stream()).forEach(System.out::println);
    }

//    @GetMapping("/test")
//    public String request() throws Exception {
////        ResponseBody responseBody =  commonTool.httpGet("http://127.0.0.1:8080/xiazai1.txt").body();
////        return responseBody.string();
//        commonTool.httpPost();
//        return null;
//    }

    @Autowired
    MessageSource messageSource;

    @GetMapping("/hello")
    public String hello() {
        return messageSource.getMessage("user.name", null, LocaleContextHolder.getLocale());
    }


}
