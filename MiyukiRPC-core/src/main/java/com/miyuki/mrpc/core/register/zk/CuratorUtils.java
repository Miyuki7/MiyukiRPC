package com.miyuki.mrpc.core.register.zk;

import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.miyuki.mrpc.core.common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: Curator(zookeeper client) utils
 */
@Slf4j
public final class CuratorUtils {

    public static final String ZK_REGISTER_ROOT_PATH= "/myRpc";
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static CuratorFramework zkClient;
    private static final Map<String, Set<ProviderNodeInfo>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static final Map<String, List<String>> PERMIT_PATH_MAP = new ConcurrentHashMap<>();
     /*
    * @Description
    *   获取 zookeeper 连接
    * @Date 2022/11
    * @param  * @param
    * @return org.apache.curator.framework.CuratorFramework
    **/
     public static CuratorFramework getZkClient() {
         String zookeeperAddress = SERVER_CONFIG.getRegisterAddr() == null ? DEFAULT_ZOOKEEPER_ADDRESS : SERVER_CONFIG.getRegisterAddr();

         // if zkClient has been started, return directly
         if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
             return zkClient;
         }
         // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
     }

     /*
    * @Description
    *   创建永久结点，用于服务注册
    * @Date 2022/11
    * @param  * @param zkClient
     * @param path
    * @return void
    **/
    public static void createPersistentNode(CuratorFramework zkClient, String path, int mode){
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            if (mode == RpcConstants.SERVICE_NODE){
                REGISTERED_PATH_SET.add(path);
            }

        } catch (Exception e){
            log.error("create persistent node for path [{}] fail", path);
        }

    }

    //更新一个服务的地址列表
    public static void updatePermit(CuratorFramework zkClient,String serviceAddress, String permitPath){
        try {
            List<String> permitList = zkClient.getChildren().forPath(permitPath);
            if (permitList != null){
                PERMIT_PATH_MAP.put(serviceAddress, permitList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //得到一个服务的地址列表 mrpc/goodservice/127.0.0.1:80/permit/.....
    public static List<String> getPermitList(CuratorFramework zkClient, String rpcServiceName, String serviceAddress){
        if (PERMIT_PATH_MAP.containsKey(serviceAddress)){
            return PERMIT_PATH_MAP.get(serviceAddress);
        } else {
            String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
            String permitPath = servicePath + "/" + serviceAddress +  "/permit";
            List<String> permitList = new ArrayList<>();
            try {
                permitList = zkClient.getChildren().forPath(permitPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return permitList;
        }
    }

    /*
     * @Description
     *   清除某个服务的某个注册，例如当某台 server 挂了，我们需要在注册中心清除其提供的服务
     * @Date 2022/11
     * @param  * @param zkClient
     * @param inetSocketAddress
     * @return void
     **/
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())){
                    // 因为我们有监视器 watcher，因此不需要手动更新 SERVICE_ADDRESS_MAP
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e){
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    //获取某个服务子节点的信息, 如果没有就创建，并且添加一个监听器，如果检测到变化就会自动变化
    public static Set<ProviderNodeInfo> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        Set<ProviderNodeInfo> result = new HashSet<>();
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            List<String> stringList = zkClient.getChildren().forPath(servicePath);

            stringList.forEach(serviceAddress -> {
                ProviderNodeInfo providerNode = ProviderNodeInfo.builder()
                        .rpcServiceName(rpcServiceName)
                        .serviceAddr(serviceAddress)
                        .weight(100).build();
                result.add(providerNode);
                String permitPath = servicePath + "/" + serviceAddress +  "/permit";
                updatePermit(zkClient,serviceAddress,permitPath);
            });
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            regiterWatcher(rpcServiceName,zkClient);
        }
        catch (Exception e){
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /*
    * @Description
    *   监听 rpcServiceName 的实现，一旦有新增注册，立刻更新 SERVICE_ADDRESS_MAP
    * @Date 2022/11
    * @param  * @param rpcServiceName eg:com.miyuki.HelloServicetest2version
     * @param zkClient
    * @return void
    **/
    private static void regiterWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        Set<ProviderNodeInfo> providerNodeInfos = SERVICE_ADDRESS_MAP.get(rpcServiceName);
        PathChildrenCacheListener pathChildrenCacheListener = ((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddressList = curatorFramework.getChildren().forPath(servicePath);

            serviceAddressList.forEach(serviceAddress -> {
                ProviderNodeInfo providerNodeInfo = ProviderNodeInfo.builder()
                        .rpcServiceName(rpcServiceName)
                        .serviceAddr(serviceAddress)
                        .weight(100)
                        .build();;
                providerNodeInfos.add(providerNodeInfo);
                String permitPath = servicePath + "/" + serviceAddress +  "/permit";
                System.out.println(permitPath);
                updatePermit(zkClient,serviceAddress,permitPath);
            });
            // 底层是个 map ， 多次 put 也无妨
            SERVICE_ADDRESS_MAP.put(rpcServiceName, providerNodeInfos);
            System.out.println(providerNodeInfos);

        });
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }
}
