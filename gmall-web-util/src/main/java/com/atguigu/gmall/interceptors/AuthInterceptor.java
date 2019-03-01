package com.atguigu.gmall.interceptors;

import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.manage.util.HttpClientUtil;
import com.atguigu.gmall.manage.util.MD5Utils;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author gaochen
 * @create 2019-01-25 11:36
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{

    public boolean preHandler(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 注解判断
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        String token = "";
        String newToken = request.getParameter("newToken");
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if(StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        if(StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        if(methodAnnotation != null) {
            // 校验
            boolean loginCheck = false;
            if(StringUtils.isNotBlank(token)) {
                String nip = request.getHeader("request-forwarded-for");
                if(StringUtils.isBlank(nip)) {
                    nip = request.getRemoteAddr();
                    if(StringUtils.isBlank(nip)) {
                        nip = "127.0.0.1";
                    }
                }
                String success = HttpClientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&requestId=" + nip);
                if(success != null && success.equals("success")) {
                    // 远程调用认证中心的验证业务
                    loginCheck = true;
                    // 将新的token 更新到cookie
                    CookieUtil.setCookie(request,response,"oldToken", token,60*60*24,true);
                    // 添加用户信息到请求的业务中
                    Map userMap = JwtUtil.decode("gmall0906", token, MD5Utils.md5(nip));
                    String userId = (String) userMap.get("userId");
                    request.setAttribute("userId", userId);
                }
            }
            // 校验不通过，并且必须登录
            if(loginCheck == false && methodAnnotation.isNeedLogin() == true) {
                response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl=" + request.getRequestURL());
                return false;
            }
        }
        return true;
    }
}
