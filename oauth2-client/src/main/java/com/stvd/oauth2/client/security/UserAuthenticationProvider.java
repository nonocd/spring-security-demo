package com.stvd.oauth2.client.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

/**
 * 用户自定义身份认证
 */
public class UserAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserDetailsService authUserDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 认证处理，返回一个Authentication的实现类则代表认证成功，返回null则代表认证失败
     *
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("username用户名不可以为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new BadCredentialsException("密码不可以为空");
        }
        //获取用户信息
        UserDetails user = authUserDetailsService.loadUserByUsername(username);
        //比较前端传入的密码明文和数据库中加密的密码是否相等
        if (!passwordEncoder.matches(password, user.getPassword())) {
            //发布密码不正确事件
            BadCredentialsException ex = new BadCredentialsException("password密码不正确");
            publisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(authentication, ex));
            throw ex;
        }
        //获取用户权限信息
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        return new UsernamePasswordAuthenticationToken(user, password, authorities);
    }

    /**
     * 如果该AuthenticationProvider支持传入的Authentication对象，则返回true
     *
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
