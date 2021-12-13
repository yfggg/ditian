//package com.test.ditian.config;
//
//import com.test.ditian.entity.User;
//import com.test.ditian.service.UserService;
//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.authc.*;
//import org.apache.shiro.authz.AuthorizationInfo;
//import org.apache.shiro.authz.SimpleAuthorizationInfo;
//import org.apache.shiro.realm.AuthorizingRealm;
//import org.apache.shiro.subject.PrincipalCollection;
//import org.apache.shiro.subject.Subject;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class UserRealm extends AuthorizingRealm {
//
//    @Autowired
//    private UserService userService;
//
//    /**
//     * @Description:   授权（查询数据库进行授权）  角色的权限信息集合，授权时使用。  这里权限代码一般超级多
//     *   也可以根据用户自带的权限标识来进行判断
//     * @date 20.7.14 23:47
//     */
//    @Override
//    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//        System.out.println("执行授权-->doGetAuthorizationInfo");
//        //获取由认证（getPrincipal()）传来的授权标识
//        Subject subject = SecurityUtils.getSubject();
//        User currentUser = (User)subject.getPrincipal();
//
//        //设置角色
//        Set<String> roles = new HashSet<>();
//        roles.add(currentUser.getRole());
//        //设置角色权限
//        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
//        //都有add权限
////        info.addStringPermission("add");
//        //设置update权限
//        info.addStringPermission(currentUser.getPerm());
//        return info;
//    }
//
//    /**
//     * @Description:  认证（查询数据库进行认证） 用户的角色信息集合，认证时使用。
//     * @date 20.7.14 23:47
//     */
//    @Override
//    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
//        System.out.println("执行认证-->doGetAuthenticationInfo");
//        //用户传入的用户名和密码封装在Token中
//        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
//        //根据用户输入到 数据库中查询用户信息
//        User user = userService.queryUserByName(usernamePasswordToken.getUsername());
//        if(user==null){
//            //Controller层会抛出UnknownAccountException异常
//            return null;
//        }
//        //根据用户输入的用户名查到的数据库中用户信息不为null，下面认证密码
//        //密码认证（Shiro自动做认证）(在第一个参数中将权限标识传给授权)
//        return new SimpleAuthenticationInfo(user,user.getPwd(),"");
//    }
//}
