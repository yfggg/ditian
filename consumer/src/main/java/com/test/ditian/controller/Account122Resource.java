package com.test.ditian.controller;

import com.test.ditian.entity.Account122;
import com.test.ditian.service.Account122Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@RestController
//@RestApiLog
//@Generated
@RequestMapping("/api")
@Api(tags = "接口")
public class Account122Resource {

    @Autowired
    private Account122Service account122Service;

//    @ApiOperation("并发")
//    @GetMapping("/kill")
//    public void concurrent(HttpServletRequest request) throws InterruptedException {
//        account122Service.multi(1L, request);
//    }
//
//    @ApiOperation(value = "登录")
//    @PostMapping("/login")
//    public String toLogin(String username,String password){
//        //获取当前用户（Shiro 的一个抽象概念，包含了用户信息。）
//        Subject subject = SecurityUtils.getSubject();
//        //用来封装用户登录信息，使用用户的登录信息来创建令牌 Token。
//        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username,password);
//        try {
//            //把登录业务逻辑交给shiro的subject，subject就是对接的借口人。进入到UserRealm中的认证。（断点认证）
//            subject.login(usernamePasswordToken);
//            return "login_Success";
//            //捕获 UserRealm中可能抛出的异常
//        } catch (UnknownAccountException e) {
//            return "Username err";
//        } catch (IncorrectCredentialsException e){
//            return "Password err";
//        }
//    }

    @ApiOperation("用户注册并验证名字是否重复")
    @GetMapping("/kill")
    public void kill(String name) throws InterruptedException {
        account122Service.kill(name);
    }

}
