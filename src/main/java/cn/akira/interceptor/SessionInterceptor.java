package cn.akira.interceptor;

import cn.akira.pojo.User;
import cn.akira.service.UserService;
import cn.akira.util.RsaUtil;
import cn.akira.util.ServletUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 会话拦截器
 */
public class SessionInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = Logger.getLogger(SessionInterceptor.class);

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            if (userService == null) {
                WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
                userService = wac.getBean(UserService.class);
            }
            Object userSession = request.getSession().getAttribute("SESSION_USER");
            if (userSession == null) {
                LOGGER.warn("[" + request.getRequestURI() + "] 需要登录有相应权限的用户才能进行");
                ServletUtil.redirectOutOfIframe(request.getContextPath() + "/login", response);
                return false;
            }
            if (userSession instanceof User) {
                User sessionUser = (User) userSession;
                Integer userId = sessionUser.getUserId();
                if (userId == null) {
                    LOGGER.warn("[" + request.getRequestURI() + "] 请求需要登录有相应权限的用户才能继续");
                    return false;
                }
                User user = new User();
                user.setUserId(userId);
                //这里去数据库查询并核实用户信息
                User user_db = userService.getUserSessionCols(user);
                if (user_db == null) {
                    //todo 不存在此用户
                    request.getSession().removeAttribute("SESSION_USER");
                    response.sendRedirect(request.getContextPath() + "/login");
                    ServletUtil.redirectOutOfIframe(request.getContextPath() + "/login", response);
                    LOGGER.warn("[" + request.getRequestURI() + "] 请求需要登录有相应权限的用户才能继续");
                    return false;
                } else {
                    String decryptedDbPassword = RsaUtil.decrypt(user_db.getRsaPassword());
                    String decryptedLocalPassword = RsaUtil.decrypt(sessionUser.getRsaPassword());
                    if (!decryptedDbPassword.equals(decryptedLocalPassword)) {
                        LOGGER.warn("[" + request.getRequestURI() + "] - 用户id : " + userId + " 密码不匹配，需重新登录");
                        return false;
                    }
                }
                return true;
            } else {
                LOGGER.error("请先登录");
                ServletUtil.redirectOutOfIframe("/login.jsp", response);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}