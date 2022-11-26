package com.miyuki.mrpc.core.spring.annotaion;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {

    String[] url() default {};

    String group() default "default";

    String serviceToken() default "";

    int timeOut() default 3000;

    int retry() default 1;

    boolean async() default false;

    /**
     * Service version, default value is empty string
     */
    String version() default "";
}
