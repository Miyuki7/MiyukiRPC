package com.miyuki.mrpc.core.loadbalance.balancer;

import com.miyuki.mrpc.core.loadbalance.AbstractLoadBalance;
import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class ConsistentHashLoadBalancer extends AbstractLoadBalance {

    private final Map<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    static class ConsistentHashSelector{
        private final int identityHashCode;

        private final TreeMap<Long, String> virtualInvokers;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode){
            this.identityHashCode = identityHashCode;
            virtualInvokers = new TreeMap<>();

            invokers.forEach((invoker) -> {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            });

        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        /*
         * 一种被广泛使用的密码散列函数，可以产生出一个128位（16字节）的散列值（hash value）
         **/
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        public String select(String rpcServiceKey){
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest,0));
        }

        private String selectForKey(long hash) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hash, true).firstEntry();
            if (entry == null){
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

    }
    @Override
    protected String doSelect(List<ProviderNodeInfo> serviceProviderList, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceProviderList);
        String rpcServiceName = rpcRequest.getRpcServiceName();
        List<String> serviceUrlList = new ArrayList<>();
        serviceProviderList.forEach(provider -> {
            serviceUrlList.add(provider.getServiceAddr());
        });
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        if (selector == null || selector.identityHashCode != identityHashCode){
            selectors.putIfAbsent(rpcServiceName,new ConsistentHashSelector(serviceUrlList,160,identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }
}
