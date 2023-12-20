package com.cen.websky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AliOSSUtils {
    private final AliOSSProperties aliOSSProperties;

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    private void initialize() {
        endpoint = aliOSSProperties.getEndpoint();
        accessKeyId = aliOSSProperties.getAccessKeyId();
        accessKeySecret = aliOSSProperties.getAccessKeySecret();
        bucketName = aliOSSProperties.getBucketName();
    }

    public void upload(List<MultipartFile> files, Long userId) throws Exception {
        initialize();

        for (MultipartFile file : files) {
            // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
            String fileName = userId + "/" + file.getOriginalFilename();

            // 创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            try {
                InputStream inputStream = file.getInputStream();
                // 创建PutObjectRequest对象。
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
                // 创建PutObject请求。
                PutObjectResult result = ossClient.putObject(putObjectRequest);
            } catch (OSSException oe) {
                System.out.println("Caught an OSSException, which means your request made it to OSS, "
                        + "but was rejected with an error response for some reason.");
                System.out.println("Error Message:" + oe.getErrorMessage());
                System.out.println("Error Code:" + oe.getErrorCode());
                System.out.println("Request ID:" + oe.getRequestId());
                System.out.println("Host ID:" + oe.getHostId());
            } catch (ClientException ce) {
                System.out.println("Caught an ClientException, which means the client encountered "
                        + "a serious internal problem while trying to communicate with OSS, "
                        + "such as not being able to access the network.");
                System.out.println("Error Message:" + ce.getMessage());
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        }
    }

    public void addFolder(String folderName, Long userId) {
        initialize();

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 在目录名称后添加一个空对象作为标识
            String directoryName = userId + "/" + (folderName.endsWith("/") ? folderName : folderName + "/");
            ossClient.putObject(bucketName, directoryName, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
            System.out.println("目录创建成功");
        } catch (Exception e) {
            System.err.println("目录创建失败，错误信息：" + e.getMessage());
        } finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }
    }
}
