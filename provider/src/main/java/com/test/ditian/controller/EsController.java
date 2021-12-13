package com.test.ditian.controller;

import cn.hutool.json.JSONUtil;
import com.test.ditian.entity.BookDocument;
import com.test.ditian.entity.BookRequest;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class EsController {

    @Autowired
    private JestClient jestClient;

//    @GetMapping("/createIndex")
//    public void createIndex() throws IOException {
//        JestResult result = jestClient.execute(new IndicesExists.Builder("username").build());
//        if(!result.isSucceeded()) {
//            JestResult jestResult = jestClient.execute(new CreateIndex.Builder("username").build());
//            this.check(jestResult);
//        } else {
//            System.out.println("索引已经存在！");
//        }
//    }
//
//    @GetMapping("/deleteIndex")
//    public void deleteIndex() throws IOException {
//        JestResult result = jestClient.execute(new IndicesExists.Builder("username").build());
//        if(result.isSucceeded()) {
//            JestResult jestResult = jestClient.execute(new DeleteIndex.Builder("username").build());
//            this.check(jestResult);
//        } else {
//            System.out.println("索引已经删除！");
//        }
//    }

//    @PostMapping("/searchIndex")
//    public void searchIndex() throws IOException {
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(new MultiMatchQueryBuilder(bookRequest.getKeyword(), "bookName","bookAuthor","desc"));
//
//        SearchResult result = jestClient.execute(new Search.Builder(json).addIndex("employees").build());
//        if(result.isSucceeded()) {
//            System.out.println("Success!");
//        } else {
//            System.out.println("Error: " + result.getErrorMessage());
//        }
//    }

//    private void check(JestResult result) {
//        if(result.isSucceeded()) {
//            System.out.println("Success!");
//        } else {
//            System.out.println("Error: " + result.getErrorMessage());
//        }
//    }

    @PostMapping("saveOrUpdateDocument")
    public String saveOrUpdateDocument(@RequestBody BookRequest bookRequest) throws Exception{
        Index.Builder builder = new Index.Builder(bookRequest.getBody());
        Index index = builder.index(bookRequest.getIndexName()).type(bookRequest.getTypeName()).build();
        JestResult result = jestClient.execute(index);
        return result.getJsonString();
    }

    @PostMapping("deleteDocumentById")
    public String deleteDocumentById(@RequestBody BookRequest bookRequest) throws Exception{
        Delete index = new Delete.Builder(bookRequest.getId()).index(bookRequest.getIndexName()).type(bookRequest.getTypeName()).build();
        JestResult result = jestClient.execute(index);
        return result.getJsonString();
    }

    @PostMapping("search")
    public String search(@RequestBody BookRequest bookRequest) throws Exception{
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 从第几页开始
        searchSourceBuilder.from(0);
        // 每页显示多少条
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(new MultiMatchQueryBuilder(bookRequest.getKeyword(), "bookName","bookAuthor","desc"));
//        log.info(searchSourceBuilder.toString());
        SearchResult result = jestClient.execute(new Search.Builder(searchSourceBuilder.toString())
                .addIndex(bookRequest.getIndexName())
                .addType(bookRequest.getTypeName())
                .build());
//        return result.getJsonString();
        // 查询后的结果集导入 BookDocument
//        BookDocument bookDocument = JSONUtil.toBean(result.getSourceAsString(), BookDocument.class);
//        List<BookDocument> bookDocument = result.getSourceAsObjectList( BookDocument.class);
//        List<String> b = result.getSourceAsStringList();
        List<SearchResult.Hit<BookDocument, Void>> concepts = result.getHits(BookDocument.class);
        List<BookDocument> bookDocument = concepts.stream().map(b -> b.source).collect(Collectors.toList());
        return null;
    }

}
