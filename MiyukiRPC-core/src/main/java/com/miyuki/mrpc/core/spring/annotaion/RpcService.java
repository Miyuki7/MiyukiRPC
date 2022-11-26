package com.miyuki.mrpc.core.spring.annotaion;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {
    int limit() default 0;

    String group() default "default";

    String serviceToken() default "";

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    String[] permitIps() default {};


}
