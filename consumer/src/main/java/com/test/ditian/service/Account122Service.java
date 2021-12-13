package com.test.ditian.service;

//import com.test.ditian.config.RedissonLock;
import com.test.ditian.dao.Account122Repository;
import com.test.ditian.entity.Account122;
//import jdk.nashorn.internal.runtime.logging.DebugLogger;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Account122Service {

    @Autowired
    private Account122Repository account122Repository;
    @Resource
    private Redisson redisson;
    @Autowired
    Environment environment;

    public void kill(String name) throws InterruptedException {
        int leaseTime = 10;
        int waitTime = 5;
        RLock rLock = redisson.getLock("kill");
        boolean res = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

        if (res) {
            log.info("取到锁");

            List<Account122> accountList = account122Repository.findAllByName(name);
            if (CollectionUtils.isEmpty(accountList)) {
                Account122 account = new Account122();
                account.setName(name);
                account122Repository.save(account);
                log.info("save");
            } else {
                log.info("exist");
            }

            rLock.unlock();
            log.info("释放锁");
        }
    }


//    @Transactional(rollbackFor = Exception.class)
    //@RedissonLock
//    public void multi(Long num, HttpServletRequest request) throws InterruptedException {
//
//        Optional<Account122> account = account122Repository.findById(1L);
//        account.ifPresent(a -> {
//            if(a.getAmount() > 0) {
//                log.info("-------------amount"+a.getAmount()+" "+"port"+request.getRemotePort());
//                a.setAmount(a.getAmount() - num);
//                account122Repository.save(a);
//            }
//        });
//
////        int leaseTime = 10;
////        int waitTime = 5;
////        RLock rLock = redisson.getLock("key");
////        boolean res = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
////        if (res) {
////            log.info("取到锁");
////            Optional<Account122> account = account122Repository.findById(1L);
////            account.ifPresent(a -> {
////                log.info("-------------当前金额{}", a.getAmount());
////                a.setAmount(a.getAmount() - num);
////                account122Repository.save(a);
////            });
////            rLock.unlock();
////            log.info("释放锁");
////        } else {
////            log.info("----------nono----------");
////            throw new RuntimeException("没有获得锁");
////        }
//
//    }

}



