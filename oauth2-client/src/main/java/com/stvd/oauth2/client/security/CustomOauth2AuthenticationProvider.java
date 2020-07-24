package com.stvd.oauth2.client.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * 用户自定义身份认证
 */
public class CustomOauth2AuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @Autowired
    private OAuth2UserService oAuth2UserService;

    private String clientRegistrationId;

    public CustomOauth2AuthenticationProvider(String clientRegistrationId) {
        this.clientRegistrationId = clientRegistrationId;
    }

    /**
     * oauth2 登陆认证
     * <p>
     * oauth2 用户名密码认证流程，其他认证请参考:
     * 1. DefaultOAuth2AuthorizedClientManager.authorize(OAuth2AuthorizeRequest authorizeRequest) ->
     * 2. DelegatingOAuth2AuthorizedClientProvider.authorize(OAuth2AuthorizationContext context) ->
     * 3. PasswordOAuth2AuthorizedClientProvider.authorize(OAuth2AuthorizationContext context) ->
     * 4. DefaultPasswordTokenResponseClient.getTokenResponse(OAuth2PasswordGrantRequest passwordGrantRequest)
     * </p>
     *
     * @param authentication
     * @return
     */
    public OAuth2AuthorizedClient authorizeClient(Authentication authentication) {
        HttpServletRequest servletRequest = SecurityUtils.getRequest();
        HttpServletResponse servletResponse = SecurityUtils.getResponse();

        OAuth2AuthorizeRequest.Builder builder = OAuth2AuthorizeRequest
                .withClientRegistrationId(this.clientRegistrationId)
                .principal(authentication);
        builder.attributes(attributes -> {
            if (servletRequest != null) {
                attributes.put(HttpServletRequest.class.getName(), servletRequest);
            }
            if (servletResponse != null) {
                attributes.put(HttpServletResponse.class.getName(), servletResponse);
            }
        });
        OAuth2AuthorizeRequest authorizeRequest = builder.build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);
        return authorizedClient;
    }

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
            throw new UsernameNotFoundException("用户名不可以为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new BadCredentialsException("密码不可以为空");
        }

        OAuth2AuthorizedClient oAuth2AuthorizedClient = this.authorizeClient(authentication);
        if (oAuth2AuthorizedClient == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        //获取用户信息
        OAuth2UserRequest oAuth2UserRequest = getOAuth2UserRequest(oAuth2AuthorizedClient);
        OAuth2User user = oAuth2UserService.loadUser(oAuth2UserRequest);
        //获取用户权限信息
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        return new OAuth2AuthenticationToken(user, authorities, clientRegistrationId);
//        return new UsernamePasswordAuthenticationToken(user, password, authorities);
    }

    private OAuth2UserRequest getOAuth2UserRequest(OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        Assert.notNull(oAuth2AuthorizedClient, "oAuth2AuthorizedClient cannot be null");
        ClientRegistration clientRegistration = oAuth2AuthorizedClient.getClientRegistration();
        OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        return oAuth2UserRequest;
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
