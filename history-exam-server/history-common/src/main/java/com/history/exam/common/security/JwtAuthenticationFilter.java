package com.history.exam.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.history.exam.common.api.Result;
import com.history.exam.common.api.ResultCode;
import com.history.exam.common.constant.CommonConstants;
import com.history.exam.common.context.CurrentUser;
import com.history.exam.common.context.CurrentUserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * JWT 鉴权过滤器
 * <p>解析 Authorization 头部 Bearer token，写入 SecurityContext 与 ThreadLocal。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * 过滤逻辑：解析 token → 注入用户上下文 → 放行；失败返回 401
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(CommonConstants.AUTHORIZATION_HEADER);
        // 仅处理 Bearer 开头的 token
        if (header == null || !header.startsWith(CommonConstants.AUTHORIZATION_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(CommonConstants.AUTHORIZATION_PREFIX.length());
        CurrentUser user = tokenProvider.parse(token);
        if (user == null) {
            writeUnauthorized(response, ResultCode.UNAUTHORIZED);
            return;
        }
        // 写入 ThreadLocal 供业务层取用
        CurrentUserHolder.set(user);
        // 写入 Spring Security 上下文，权限按用户类型区分
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束清理 ThreadLocal，避免内存泄漏
            CurrentUserHolder.clear();
        }
    }

    /**
     * 输出未授权响应（JSON）
     *
     * @param response   HTTP 响应
     * @param resultCode 业务码
     */
    private void writeUnauthorized(HttpServletResponse response, ResultCode resultCode) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail(resultCode)));
        } catch (IOException e) {
            log.error("写入未授权响应失败", e);
        }
    }
}
