package com.cen.websky.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.UserService;
import com.cen.websky.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

//自定义拦截器
@Slf4j
@Component  //当前拦截器对象由Spring创建和管理
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    private final UserService userService;

    //前置方式
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("preHandle .... ");

        // 添加跨域配置
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");

        //1.获取请求url
        //2.判断请求url中是否包含login，如果包含，说明是登录操作，放行

        //3.获取请求头中的令牌（token）
        String token = request.getHeader("token");
        log.info("从请求头中获取的令牌：{}", token);

        //4.判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(token)) {
            log.info("Token不存在");

            //创建响应结果对象
            Result responseResult = Result.error(401, "未登录");
            //把Result对象转换为JSON格式字符串 (fastjson是阿里巴巴提供的用于实现对象和json的转换工具类)
            String json = JSONObject.toJSONString(responseResult);
            //设置响应头（告知浏览器：响应的数据类型为json、响应的数据编码表为utf-8）
            response.setContentType("application/json;charset=utf-8");
            //响应
            response.getWriter().write(json);

            return false;//不放行
        }

        //5.解析token，如果解析失败，返回错误结果（未登录）
        try {
            Claims claims = JwtUtils.parseJWT(token);
            request.setAttribute("userInfo", claims);
            if (!userService.verifyTokenTime(claims.get("id", Long.class), LocalDateTime.parse(claims.get("createTime", String.class)))) {
                sendErrorResponse(response);
                return false;
            }
        } catch (Exception e) {
            sendErrorResponse(response);
            return false;
        }
        // 6.放行
        return true;
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        log.info("令牌解析失败!");
        // 创建响应结果对象
        Result responseResult = Result.error(401, "未登录");
        // 将Result对象转换为JSON格式字符串
        String json = JSONObject.toJSONString(responseResult);
        // 设置响应头
        response.setContentType("application/json;charset=utf-8");
        // 响应
        response.getWriter().write(json);
    }
}