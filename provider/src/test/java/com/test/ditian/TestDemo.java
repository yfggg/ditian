package com.test.ditian;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.Queue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDemo {

    @Test
    public void contextLoads() {
        Queue<String> stringQueue = new LinkedList<String>();
        stringQueue.offer("你好");
        stringQueue.offer("你好1");
        System.out.println(stringQueue.poll());
    }
}
