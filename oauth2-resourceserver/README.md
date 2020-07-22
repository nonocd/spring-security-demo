spring boot + spring security5 资源服务器

最近使用spring security搭建oauth服务，发现网上基本上都是基于过时的 spring security oauth2.
spring security5默认已经集成了oauth2.

## 1. 添加项目依赖
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
implementation 'com.nimbusds:oauth2-oidc-sdk:8.19'
```

## 2. application.yml添加token认证配置
```yaml
server:
  port: 8080

spring:
  security:
    oauth2:
      resourceserver:
        opaque:
          introspection-uri: http://localhost:9001/oauth/check
          introspection-client-id: 000000
          introspection-client-secret: 999999
```

## 3. 测试Controller配置
```java
@Controller
public class DemoController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(value = "name", defaultValue = "world") String name) {
        return String.format("Hello %s!", name);
    }
}
```
## 4. SecurityConfig配置
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
    String introspectionUri;
    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
    String clientId;
    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
    String clientSecret;

    @Override
        protected void configure(HttpSecurity http) throws Exception {
    
            http
                    .authorizeRequests(authorizeRequests ->
                            authorizeRequests
                                    .anyRequest()
                                    .authenticated()
                    )
                    .oauth2ResourceServer(oauth2 ->
                            oauth2
                                    .opaqueToken(opaqueToken ->
                                            opaqueToken
                                                    .introspectionUri(introspectionUri)
                                                    .introspectionClientCredentials(clientId, clientSecret)
                                    )
                    );
        }
}
```
spring security 默认使用的NimbusOpaqueTokenIntrospector直接使用token字段作为body体发送请求。这里采用自定义`NimbusOpaqueTokenIntrospector` Bean，
使用BearerAuth方式将token添加到请求头中。

## 5. 定义`NimbusOpaqueTokenIntrospector` Bean
```java
@Bean
OpaqueTokenIntrospector tokenIntrospector() {
    NimbusOpaqueTokenIntrospector introspector = new NimbusOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    introspector.setRequestEntityConverter(defaultRequestEntityConverter(URI.create(introspectionUri)));

    return introspector;
}

private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
    return token -> {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new RequestEntity<>(null, headers, HttpMethod.POST, introspectionUri);
    };
}
```
修改SecurityConfig configure方法
```java
@Override
protected void configure(HttpSecurity http) throws Exception {

    http
            .authorizeRequests(authorizeRequests ->
                    authorizeRequests
                            .anyRequest()
                            .authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                    oauth2
                            .opaqueToken(opaqueToken ->
                                    opaqueToken
                                            .introspector(tokenIntrospector())
                            )
            );
}
```

## 6. 测试
postman调用

**请求方式**

`GET`  `localhost:8080/hello?name=admin`

**请求头 Authorization**

- Bearer Token
- Token: `access_token`
