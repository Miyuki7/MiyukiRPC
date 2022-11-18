package com.miyuki.mrpc.core.common.config;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Getter
public class ServerConfig {
    private Integer serverPort;

    private String registerAddr;

    private String applicationName;

}
