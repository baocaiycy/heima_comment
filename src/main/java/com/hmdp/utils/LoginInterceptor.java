package com.hmdp.utils;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //1.获取请求头中的用户
//        String token = String.valueOf(request.getHeader("authorization"));
//        if(token==null){
//            //未登录不允许访问资源
//            response.setStatus(401);
//            return false;
//        }
//        //3. 判断用户是否存在
//        UserDTO userDTO = authToken(token);
//        //4.用户不存在 或token过期
//        if(userDTO==null){
//            response.setStatus(401);
//            return false;
//        }
//        //5. 存在 保存用户信息到ThreadLocal
//        UserHolder.saveUser(userDTO);
//        //6.刷新该用户token存活时间
//        refreshToken(token);
        if(UserHolder.getUser()==null){
            //用户未登录 拦截
            response.setStatus(401);
            return false;
        }
        //用户登录 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserHolder.removeUser();
    }

//    /**
//     * describe 通过token在redis寻找用户信息
//     * @param token
//     * @return UserDTO
//     */
//    public UserDTO authToken(String token){
//        //通过token在redis寻找用户信息
//        String userJson = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_USER_KEY + token);
//        if(userJson==null){
//            return null;
//        }
//        //转成user对象
//        UserDTO userDTO = JSONUtil.toBean(userJson, UserDTO.class);
//        return userDTO;
//    }
//
//    /**
//     * describe 刷新用户登录token
//     * @param token
//     */
//    public void refreshToken(String token){
//        //1.从redis取出
//        String userJson = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_USER_KEY + token);
//        //2.重新插入redis
//        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_USER_KEY+token,userJson,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
//    }
}
