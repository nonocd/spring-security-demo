package com.stvd.oauth2.client.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private UserDetails userDetails = User.withUsername("admin")
            .username("admin")
            .password("$2a$10$y8YrNP/wR2hY1DK499M94.JlJnYf/b81.ASw7kZiCxHeLRO1FyFEu")
            .roles("USER")
            .build();

    private InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager(userDetails);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /*
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
        */

        return manager.loadUserByUsername(username);
    }
}
