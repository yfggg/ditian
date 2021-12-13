//package com.test.ditian.config;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.redisson.Redisson;
//import org.redisson.api.RLock;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.concurrent.TimeUnit;
//
///**
// * 分布式锁
// * @author wuweifeng wrote on 2019/5/8.
// */
//@Aspect
//@Component
//@Order(1) //该order必须设置，很关键
//public class RedissonLockAspect {
//
//    private Logger log = LoggerFactory.getLogger(getClass());
//
//    @Resource
//    private Redisson redisson;
//
//    @Around("@annotation(redissonLock)")
//    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
//
//        //方法内的所有参数
////        Object[] params = joinPoint.getArgs();
////        //取得方法名
////        String key = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
////
////        int lockIndex = redissonLock.lockIndex();
////        //-1代表锁整个方法，而非具体锁哪条数据
////        if (lockIndex != -1) {
////            key += params[lockIndex];
////        }
//
//        //多久会自动释放，默认10秒
////        int leaseTime = redissonLock.leaseTime();
//        int leaseTime = 10;
//        int waitTime = 5;
//
//        Object obj = null;
//        RLock rLock = redisson.getLock("key");
//        boolean res = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
//        if (res) {
//            log.info("取到锁");
//            obj = joinPoint.proceed();
//            rLock.unlock();
//            log.info("释放锁");
//        } else {
//            log.info("----------nono----------");
//            throw new RuntimeException("没有获得锁");
//        }
//
//        return obj;
//    }
//}