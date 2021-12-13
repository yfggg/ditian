//package com.test.ditian;
//
//import com.test.ditian.service.Account122Service;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.Resource;
//import java.util.concurrent.BrokenBarrierException;
//import java.util.concurrent.CyclicBarrier;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class GiftmallApplicationTests {
//
//    @Resource
//    private Account122Service account122Service;
//
//    private CyclicBarrier cyclicBarrier = new CyclicBarrier(100);
//    private CyclicBarrier cyclicBarrier1 = new CyclicBarrier(100);
//
//    @Test
//    public void contextLoads() {
//        for (int i = 0; i < 100; i++) {
//            new Thread(() -> {
//                try {
//                    cyclicBarrier.await();
//
//                    account122Service.multi(1L);
//                } catch (InterruptedException | BrokenBarrierException e) {
//                    e.printStackTrace();
//                }
//            }
//            ).start();
//            new Thread(() -> {
//                try {
//                    cyclicBarrier1.await();
//
//                    account122Service.multi(2L);
//                } catch (InterruptedException | BrokenBarrierException e) {
//                    e.printStackTrace();
//                }
//            }
//            ).start();
//        }
//
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}