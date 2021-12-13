package com.test.ditian.prolificacy;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//@Component
@Slf4j
public class CommonTool {

    /**
     * excel上传，开启验证传入的参数 需要在pom中加入hibernate-validator，validation-api依赖
     *
     * @param fileName excel.xls
     * @param contentClass
     * @return
     */
    public static <T> ExcelImportResult<T> inputExcel(String fileName, Class<T> contentClass) {
        ImportParams importParams = new ImportParams();
        // 设置标题的行数，有标题时一定要有
        importParams.setTitleRows(1);
        //设置表头的行数
        importParams.setHeadRows(1);
        // 验证传入的参数
        importParams.setNeedVerfiy(true);
        // 在线程中运行的代码可以通过该类加载器来加载类与资源
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        log.info("文件保存路径:{}", url);
        if(url == null) {
            return null;
        }
        return ExcelImportUtil.importExcelMore(new File(url.getFile()), contentClass, importParams);
    }

    /**
     * excel客户端下载，需要在contentClass中需要加上@Excel
     *
     * @param titleName
     * @param fileName
     * @param contents
     * @param contentClass
     * @param response
     * @throws IOException
     */
    public static <T> void exportExcel(String titleName, String fileName, List<T> contents, Class<T> contentClass, HttpServletResponse response) {
        OutputStream out = null;
        try {
            ExportParams exportParams = new ExportParams(titleName,"v1.0", ExcelType.HSSF);
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, contentClass, contents);
            // 设置浏览器用分段(part)请求
            response.setContentType("multipart/form-data");
            // 设置消息头，告诉浏览器，我要下载
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
            out = response.getOutputStream();
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 多文件上传
     *
     * @param uploadPath
     * @param mFiles
     */
    public static String fileUpload(String uploadPath, MultipartFile[] mFiles) {
        if(null == mFiles) {
            return null;
        }
        for(MultipartFile mFile : mFiles) {
            File file = new File(uploadPath + mFile.getOriginalFilename());
            if(file.getParentFile().exists()) { file.getParentFile().mkdirs(); }
            try {
                mFile.transferTo(file);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return "success";
    }

    /**
     * 同步 get request
     *
     * @param url "http://127.0.0.1:8080/xiazai1.txt"
     * @return
     */
    public static Response httpGet(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 同步 post request
     *
     * @param url "http://127.0.0.1:8080/test1"
     * @param json = "{\"id\":1,\"name\":\"John\"}"
     * @return
     */
    public static Response httpPost(String url, String json) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), json))
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 日期转字符串
     *
     * @param date
     * @param format "yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static String zonedDateTimeToString(ZonedDateTime date, String format) {
        DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(format);
        return simpleDateFormat.format(date);
    }

    /**
     * 字符串转日期
     *
     * @param date 2021-10-01 00:00:00
     * @return
     */
    public static ZonedDateTime stringToZonedDateTime(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        return ZonedDateTime.parse(date, dateTimeFormatter);
    }

    /**
     * list复制
     *
     * @param sources
     * @param eClass
     * @return
     */
    public static <T,E> List<E> copyList(List<T> sources,  Class<E> eClass) {
        if(CollectionUtils.isEmpty(sources)) { return null; }
        return sources.stream().map(
                source -> {
                    try {
                        E target = eClass.newInstance();
                        BeanUtils.copyProperties(source, target);
                        return target;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
        ).collect(Collectors.toList());
    }

    /**public JSON toJson(Object t) {
        return JSON.parseObject(JSON.toJSONString(t));
    }*/

    /**
     * list转json
     *
     * @param list
     * @return
     */
    public static JSON toJson(List<Object> list) {
        return new JSONArray(list);
    }

    /**
     * map转json
     *
     * @param map
     * @return
     */
    public static JSON toJson(Map<String, Object> map) {
        return new JSONObject(map);
    }

    /**
     * json转list
     *
     * @param json 注意需要[]
     * @param tClass
     * @return
     */
    public static <T> List<T> toList(Object json, Class<T> tClass) {
        return JSON.parseArray(json.toString(), tClass);
    }

    /**
     * json转map
     *
     * @param json
     * @return
     */
    public static Map<String, Object> toMap(Object json) {
        return JSON.parseObject(json.toString());
    }

    /**
     * list集合根据字段去重（配合steam中的filter使用）
     *
     * @param keyExtractor 对象中的字段 例如 Class::getId
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> getDuplicateElements(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * list去重
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> getDuplicateElements(List<T> list) {
        return HashMultiset.create(list).entrySet().stream()
                .filter(w -> w.getCount() > 1)
                .map(Multiset.Entry::getElement)
                .collect(Collectors.toList());
    }

}
