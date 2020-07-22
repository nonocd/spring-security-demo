package com.stvd.oauth2.app.service;

import com.stvd.oauth2.app.model.SysPermission;
import com.stvd.oauth2.app.model.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserService userService;

    @Autowired
    private SysPermissionService permissionService;
    /**
     * 查询数据库用户信息
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userService.getUserByName(username);
        if (null == sysUser) {
            throw new UsernameNotFoundException("username: " + username + " is not exist!");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        // 获取用户权限
        List<SysPermission> permissions = permissionService.getByUserId(sysUser.getId());
        // 设置用户权限
        permissions.forEach(permisson -> {
            authorities.add(new SimpleGrantedAuthority(permisson.getEname()));
        });
        // 返回认证用户
        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
