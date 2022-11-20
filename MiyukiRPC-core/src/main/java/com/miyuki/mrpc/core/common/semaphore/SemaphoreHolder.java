package com.miyuki.mrpc.core.common.semaphore;

import lombok.Data;

import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Data
public class SemaphoreHolder {
    private Semaphore semaphore;

    private int maxNums;

    public SemaphoreHolder(int maxNums){
        this.maxNums = maxNums;
        semaphore = new Semaphore(maxNums);
    }

}
