package com.dawnwin.stick.model;

import com.dawnwin.stick.utils.JwtHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request =(HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        String token = request.getHeader("Authorization"); //获取请求传来的token
        if(token.startsWith("Bearer ")) {
            Claims claims = JwtHelper.verifyJwt(token.substring(7)); //验证token
            request.setAttribute("Authorization", claims);
            if (claims == null) {
                response.getWriter().write("token is invalid");
            }else {
                String mobile = claims.getSubject();
                request.setAttribute("mobile", mobile);
                chain.doFilter(request,response);
            }
        }else{
            response.getWriter().write("token is invalid");
        }

    }

    @Override
    public void destroy() {

    }
}
