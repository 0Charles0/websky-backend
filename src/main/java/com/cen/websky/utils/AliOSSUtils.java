package com.cen.websky.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import com.cen.websky.pojo.vo.FileVO;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class AliOSSUtils {
    private final String bucketName;
    private final OSS ossClient;

    public AliOSSUtils(AliOSSProperties aliOSSProperties) {
        bucketName = aliOSSProperties.getBucketName();
        // 创建OSSClient实例。
        ossClient = new OSSClientBuilder().build(aliOSSProperties.getEndpoint(), aliOSSProperties.getAccessKeyId(), aliOSSProperties.getAccessKeySecret());
    }

    /**
     * 上传文件
     *
     * @param files
     * @param userId
     * @throws Exception
     */
    public void upload(List<MultipartFile> files, Long userId) throws Exception {
        for (MultipartFile file : files) {
            // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
            String fileName = userId + "/" + file.getOriginalFilename();

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
            }/* finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }*/
        }
    }

    /**
     * 新增文件夹
     *
     * @param folderName
     * @param userId
     */
    public void addFolder(String folderName, Long userId) {
        try {
            // 在目录名称后添加一个空对象作为标识
            String directoryName = userId + "/" + (folderName.endsWith("/") ? folderName : folderName + "/");
            ossClient.putObject(bucketName, directoryName, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
            System.out.println("目录创建成功");
        } catch (Exception e) {
            System.err.println("目录创建失败，错误信息：" + e.getMessage());
        }/* finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }*/
    }

    /**
     * 查询文件
     *
     * @param path
     * @param userId
     */
    public List<FileVO> fileList(String path, Long userId) {
        List<FileVO> urls = null;
        try {
            // 构造ListObjectsRequest请求。
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);

            // 设置正斜线（/）为文件夹的分隔符。
            listObjectsRequest.setDelimiter("/");

            // 列出path目录下的所有文件和文件夹。
            listObjectsRequest.setPrefix(userId + "/" + (!path.equals("/") ? (path.endsWith("/") ? path : path + "/") : ""));

            ObjectListing listing = ossClient.listObjects(listObjectsRequest);

            urls = new ArrayList<>();

            // 遍历所有文件。
            System.out.println("Objects:");
            // objectSummaries的列表中给出的是path目录下的文件。
            for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                FileVO fileVO = new FileVO();
                fileVO.setSize(objectSummary.getSize());
                String key = objectSummary.getKey();
                fileVO.setFileName(key);
                fileVO.setUrl(generateURL(key));
                fileVO.setUpdateTime(ossClient.getObjectMetadata(bucketName, key).getLastModified());
                urls.add(fileVO);
                System.out.println(objectSummary.getKey());
            }

            // 遍历所有commonPrefix。
            System.out.println("\nCommonPrefixes:");
            // commonPrefixs列表中显示的是path目录下的所有子文件夹。由于path/movie/001.avi和path/movie/007.avi属于path文件夹下的movie目录，因此这两个文件未在列表中。
            for (String commonPrefix : listing.getCommonPrefixes()) {
                FileVO fileVO = new FileVO();
                fileVO.setFileName(commonPrefix);
                fileVO.setUrl(generateURL(commonPrefix));
                Pair<Long, Date> longDatePair = calculateFolderLength(commonPrefix);
                fileVO.setSize(longDatePair.getFirst());
                fileVO.setUpdateTime(longDatePair.getSecond());
                urls.add(fileVO);
                System.out.println(commonPrefix);
            }
        } catch (Exception e) {
            // 输出异常信息
            e.printStackTrace();
        }/* catch (OSSException oe) {
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
        }*//* finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }*/
        return urls;
    }

    /**
     * 生成URL
     *
     * @param path
     * @return
     */
    public URL generateURL(String path) {
        URL signedUrl = null;
        try {
            // 指定生成的签名URL过期时间，单位为毫秒。本示例以设置过期时间为1小时为例。
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);

            // 生成签名URL。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, path, HttpMethod.GET);
            // 设置过期时间。
            request.setExpiration(expiration);

            // 通过HTTP GET请求生成签名URL。
            signedUrl = ossClient.generatePresignedUrl(request);
            // 打印签名URL。
            System.out.println("signed url for getObject: " + signedUrl);
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
        }
        return signedUrl;
    }

    private Pair<Long, Date> calculateFolderLength(String folderKey) {
        long size = 0L;
        Date maxUpdateTime = new Date();
        maxUpdateTime.setTime(0);
        try {
            // 构造ListObjectsRequest请求。
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
            listObjectsRequest.setPrefix(folderKey);

            ObjectListing folderListing = ossClient.listObjects(listObjectsRequest);

            for (OSSObjectSummary objectSummary : folderListing.getObjectSummaries()) {
                // 累加文件大小
                size += objectSummary.getSize();
                // 更新时间
                Date updateTime = ossClient.getObjectMetadata(bucketName, objectSummary.getKey()).getLastModified();
                if (updateTime.compareTo(maxUpdateTime) > 0) {
                    maxUpdateTime = updateTime;
                }
            }
        } catch (OSSException | ClientException e) {
            throw new RuntimeException(e);
        }
        return Pair.of(size, maxUpdateTime);
    }
}
