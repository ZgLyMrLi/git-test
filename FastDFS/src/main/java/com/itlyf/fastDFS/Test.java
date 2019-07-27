package com.itlyf.fastDFS;

import org.csource.fastdfs.*;

public class Test {
    public static void main(String[] args) throws Exception {
        //1.加载配置文件
        ClientGlobal.init("D:\\A_blackHorse_java_vedio\\shopping\\dubbox\\FastDFS\\src\\main\\resources\\fdfs_client.conf");
        //2.构建一个管理者客户端
        TrackerClient client = new TrackerClient();
        //3.连接管理者服务端
        TrackerServer trackerServer = client.getConnection();
        //4.声明存储服务端
        StorageServer storageServer = null;
        //5.获取存储服务器的客户端对象
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        //6.上传文件
        String[] strings = storageClient.upload_file("D:\\A_blackHorse_java_vedio\\java02_SE -ToStudents\\数据库和前端\\品优购\\静态原型\\网站前台\\img\\banner4.jpg", "jpg", null);
        for (String string : strings) {
            System.out.println(string);
        }
    }
}
