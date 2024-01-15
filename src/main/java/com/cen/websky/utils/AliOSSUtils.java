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
import java.util.*;

@Component
public class AliOSSUtils {
    private final String bucketName;
    private final OSS ossClient;
    private final CategoryProperties categoryProperties;

    public AliOSSUtils(AliOSSProperties aliOSSProperties, CategoryProperties categoryProperties) {
        bucketName = aliOSSProperties.getBucketName();
        // 创建OSSClient实例。
        ossClient = new OSSClientBuilder().build(aliOSSProperties.getEndpoint(), aliOSSProperties.getAccessKeyId(), aliOSSProperties.getAccessKeySecret());

        this.categoryProperties = categoryProperties;
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
    public void addFolder(String folderName, String path, Long userId) {
        try {
            // 在目录名称后添加一个空对象作为标识
            String directoryName = userId + "/" + path + (folderName.endsWith("/") ? folderName : folderName + "/");
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
        List<FileVO> files = null;
        try {
            // 构造ListObjectsRequest请求。
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);

            // 设置正斜线（/）为文件夹的分隔符。
            listObjectsRequest.setDelimiter("/");

            // 列出path目录下的所有文件和文件夹。
            listObjectsRequest.setPrefix(userId + "/" + (path.equals("/") ? "" : (path.endsWith("/") ? path : path + "/")));

            ObjectListing listing = ossClient.listObjects(listObjectsRequest);

            files = new ArrayList<>();
            // 初始化返回的files列表的第一个值为上级路径
            FileVO superiorPath = new FileVO();
            int index = path.lastIndexOf('/', path.length() - 2);
            if (index != -1) {
                superiorPath.setFileName(path.substring(0, index + 1));
            } else {
                superiorPath.setFileName("/");
            }
            files.add(superiorPath);
            // 遍历所有文件。
            System.out.println("Objects:");
            // objectSummaries的列表中给出的是path目录下的文件。
            for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                // 由于查询空目录会返回空目录本身，判断如果objectSummary为空目录本身，则跳过，不返回空目录本身给前端
                String key1 = objectSummary.getKey();
                if (key1.substring(key1.indexOf('/') + 1).equals(path)) {
                    continue;
                }

                FileVO fileVO = new FileVO();
                fileVO.setSize(objectSummary.getSize());
                String key = objectSummary.getKey();
                fileVO.setFileName(key.substring(key.indexOf('/') + 1));
                fileVO.setUrl(generateURL(key));
                fileVO.setUpdateTime(ossClient.getObjectMetadata(bucketName, key).getLastModified());
                files.add(fileVO);
                System.out.println(objectSummary.getKey());
            }
            // 遍历所有commonPrefix。
            System.out.println("\nCommonPrefixes:");
            // commonPrefixs列表中显示的是path目录下的所有子文件夹。由于path/movie/001.avi和path/movie/007.avi属于path文件夹下的movie目录，因此这两个文件未在列表中。
            for (String commonPrefix : listing.getCommonPrefixes()) {
                FileVO fileVO = new FileVO();
                fileVO.setFileName(commonPrefix.substring(commonPrefix.indexOf('/') + 1));
                fileVO.setUrl(generateURL(commonPrefix));
                Pair<Long, Date> longDatePair = calculateFolderLength(commonPrefix);
                fileVO.setSize(longDatePair.getFirst());
                fileVO.setUpdateTime(longDatePair.getSecond());
                files.add(fileVO);
                System.out.println(commonPrefix);
            }
        } catch (Exception e) {
            // 输出异常信息
            e.printStackTrace();
        }/*finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }*/
        return files;
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

    /**
     * 文件大小、时间
     *
     * @param folderKey
     * @return
     */
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

    /**
     * 批量删除
     *
     * @param objectNames
     * @param userId
     */
    public void delete(String[] objectNames, Long userId) {
        try {
            for (String objectName : objectNames) {
                ObjectListing objectListing = ossClient.listObjects(bucketName, userId + "/" + objectName);
                for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    // 删除文件或目录。如果要删除目录，目录必须为空。
                    ossClient.deleteObject(bucketName, objectSummary.getKey());
                }
            }
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

    public List<FileVO> classify(String category, Long userId) {
        List<FileVO> files = null;
        try {
            files = new ArrayList<>();
            ObjectListing objectListing = ossClient.listObjects(bucketName, userId + "/");
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                String objectName = objectSummary.getKey();
                String picture = categoryProperties.getPicture();
                String document = categoryProperties.getDocument();
                String video = categoryProperties.getVideo();
                String audio = categoryProperties.getAudio();
                String other = picture + "," + document + "," + video + "," + audio;
                Set<String> extensions = null;
                boolean isOther = false;
                switch (category) {
                    case "图片":
                        extensions = new HashSet<>(Set.of(picture.split(",\\s*")));
                        break;
                    case "文档":
                        extensions = new HashSet<>(Set.of(document.split(",\\s*")));
                        break;
                    case "视频":
                        extensions = new HashSet<>(Set.of(video.split(",\\s*")));
                        break;
                    case "音频":
                        extensions = new HashSet<>(Set.of(audio.split(",\\s*")));
                        break;
                    default:
                        extensions = new HashSet<>(Set.of(other.split(",\\s*")));
                        isOther = true;
                }
                // 判断文件名是否以集合中任意一个扩展名结尾,其它情况则相反
                if ((!isOther && extensions.stream().anyMatch(objectName::endsWith)) ||
                        (isOther && extensions.stream().noneMatch(objectName::endsWith))) {
                    FileVO fileVO = new FileVO();
                    fileVO.setSize(objectSummary.getSize());
                    fileVO.setFileName(objectName.substring(objectName.indexOf('/') + 1));
                    fileVO.setUrl(generateURL(objectName));
                    fileVO.setUpdateTime(ossClient.getObjectMetadata(bucketName, objectName).getLastModified());
                    files.add(fileVO);
                    System.out.println(objectName + " 的后缀在集合中");
                }
            }
        } catch (Exception e) {
            // 输出异常信息
            e.printStackTrace();
        }/*finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }*/
        return files;
    }
}
