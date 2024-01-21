package com.cen.websky.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import com.cen.websky.pojo.po.ShareFile;
import com.cen.websky.pojo.vo.FileVO;
import com.cen.websky.service.ShareFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class AliOSSUtils {
    private final String bucketName;
    private final OSS ossClient;
    private final String picture;
    private final String document;
    private final String video;
    private final String audio;
    private final String other;
    private final ShareFileService shareFileService;

    public AliOSSUtils(AliOSSProperties aliOSSProperties, CategoryProperties categoryProperties, ShareFileService shareFileService) {
        bucketName = aliOSSProperties.getBucketName();
        // 创建OSSClient实例。
        ossClient = new OSSClientBuilder().build(aliOSSProperties.getEndpoint(), aliOSSProperties.getAccessKeyId(), aliOSSProperties.getAccessKeySecret());

        picture = categoryProperties.getPicture();
        document = categoryProperties.getDocument();
        video = categoryProperties.getVideo();
        audio = categoryProperties.getAudio();
        other = picture + "," + document + "," + video + "," + audio;

        this.shareFileService = shareFileService;
    }

    /**
     * 上传文件
     *
     * @param files
     * @param userId
     * @throws Exception
     */
    public void upload(boolean isImage, List<MultipartFile> files, Long userId) throws Exception {
        for (MultipartFile file : files) {
            // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
            String fileName = userId + "/" + file.getOriginalFilename();
            if (isImage) {
                fileName = "image" + "/" + fileName;
            }
            try {
                InputStream inputStream = file.getInputStream();
                // 创建PutObjectRequest对象。
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
                // 创建PutObject请求。
                PutObjectResult result = ossClient.putObject(putObjectRequest);
                if (isImage) {
                    ossClient.setObjectAcl(bucketName, fileName, CannedAccessControlList.PublicRead);
                    // 通过拷贝重命名头像名
                    ossClient.copyObject(bucketName, fileName, bucketName, fileName.substring(0, fileName.lastIndexOf("/") + 1) + "websky头像.jpg");
                    // 删除旧文件
                    ossClient.deleteObject(bucketName, fileName);
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
    }

    public void upload(List<MultipartFile> files, Long userId) throws Exception {
        upload(false, files, userId);
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
     * 文件查询
     *
     * @param path
     * @param fuzzyName
     * @param userId
     * @return
     */
    public List<FileVO> fileList(String path, String fuzzyName, Long userId) {
        List<FileVO> files = null;
        try {
            files = new ArrayList<>();
            // 构造ListObjectsRequest请求。
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
            // 模糊词
            Pattern p = null;
            Matcher m = null;
            if (fuzzyName != null) {
                String pattern = ".*" + fuzzyName + ".*";
                p = Pattern.compile(pattern);
            } else {
                // 设置正斜线（/）为文件夹的分隔符。
                listObjectsRequest.setDelimiter("/");
            }
            // 列出path目录下的所有文件和文件夹。
            listObjectsRequest.setPrefix((userId == null ? "" : userId + "/") + (path.equals("/") ? "" : (path.endsWith("/") ? path : path + "/")));
            ObjectListing listing = ossClient.listObjects(listObjectsRequest);
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
                String key = objectSummary.getKey();
                if (key.substring(key.indexOf('/') + 1).equals(path)) {
                    continue;
                }
                // 模糊匹配
                if (fuzzyName != null) {
                    String[] keySplit = key.split("/");
                    m = p.matcher(keySplit[keySplit.length - 1]);
                }
                if (fuzzyName == null || m.matches()) {
                    FileVO fileVO = new FileVO();
                    fileVO.setSize(objectSummary.getSize());
                    fileVO.setFileName(key.substring(key.indexOf('/') + 1));
                    fileVO.setUrl(generateURL(key));
                    fileVO.setUpdateTime(ossClient.getObjectMetadata(bucketName, key).getLastModified());
                    fileVO.setCategory(determineCategory(key));
                    files.add(fileVO);
                }
            }
            // 遍历所有commonPrefix。
            System.out.println("\nCommonPrefixes:");
            // commonPrefixs列表中显示的是path目录下的所有子文件夹。由于path/movie/001.avi和path/movie/007.avi属于path文件夹下的movie目录，因此这两个文件未在列表中。
            for (String commonPrefix : listing.getCommonPrefixes()) {
                // 模糊匹配
                if (fuzzyName != null) {
                    String[] keySplit = commonPrefix.split("/");
                    m = p.matcher(keySplit[keySplit.length - 1]);
                }
                if (fuzzyName == null || m.matches()) {
                    FileVO fileVO = new FileVO();
                    fileVO.setFileName(commonPrefix.substring(commonPrefix.indexOf('/') + 1));
                    fileVO.setUrl(generateURL(commonPrefix));
                    Pair<Long, Date> longDatePair = calculateFolderLength(commonPrefix);
                    fileVO.setSize(longDatePair.getFirst());
                    fileVO.setUpdateTime(longDatePair.getSecond());
                    files.add(fileVO);
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

    /**
     * 重载文件查询
     *
     * @param path
     * @param userId
     */
    public List<FileVO> fileList(String path, Long userId) {
        return fileList(path, null, userId);
    }

    /**
     * 重载文件查询
     *
     * @param userId
     * @return
     */
    public List<FileVO> fileList(Long userId) {
        return fileList("/", null, userId);
    }

    /**
     * 重载文件查询
     *
     * @param path
     */
    public List<FileVO> fileList(String path) {
        return fileList(path, null, null);
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
            // 指定生成的签名URL过期时间，单位为毫秒，1小时。
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

    /**
     * 分类查询
     *
     * @param category
     * @param userId
     * @return
     */
    public List<FileVO> classify(String category, Long userId) {
        List<FileVO> files = null;
        try {
            files = new ArrayList<>();
            ObjectListing objectListing = ossClient.listObjects(bucketName, userId + "/");
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                String objectName = objectSummary.getKey();
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
                // 判断文件名是否以集合中任意一个扩展名结尾,其它情况则相反,且排除文件夹
                if ((!isOther && extensions.stream().anyMatch(objectName::endsWith)) ||
                        (isOther && extensions.stream().noneMatch(objectName::endsWith) && !objectName.endsWith("/"))) {
                    FileVO fileVO = new FileVO();
                    fileVO.setSize(objectSummary.getSize());
                    fileVO.setFileName(objectName.substring(objectName.indexOf('/') + 1));
                    fileVO.setUrl(generateURL(objectName));
                    fileVO.setUpdateTime(ossClient.getObjectMetadata(bucketName, objectName).getLastModified());
                    fileVO.setCategory(category);
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

    /**
     * 批量下载 oss 文件,并打成 zip 包返回到 response 中
     *
     * @param fileNames
     * @param response
     */
    public void downLoad(String[] fileNames, HttpServletResponse response, Long userId) {
        String zipFileName = "AllDownloaded";
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + zipFileName + ".zip");
        BufferedInputStream bis = null;
        try {
            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
            for (String fileName : fileNames) {
                fileName = userId + "/" + fileName;
                if (fileName.endsWith("/")) {
                    // 如果是文件夹，则递归处理文件夹中的文件
                    addFilesToZip(fileName, fileName.substring(0, fileName.lastIndexOf('/', fileName.length() - 2) + 1), zos);
                    continue;
                }
                // 生成签名URL
                URL signedUrl = generateURL(fileName);
                // 使用签名URL发送请求。
                OSSObject ossObject = ossClient.getObject(signedUrl, new HashMap<>());

                if (ossObject != null) {
                    InputStream inputStream = ossObject.getObjectContent();
                    byte[] buffs = new byte[1024 * 10];

                    String zipFile = fileName.substring(fileName.lastIndexOf("/") + 1);
                    ZipEntry zipEntry = new ZipEntry(zipFile);
                    zos.putNextEntry(zipEntry);
                    bis = new BufferedInputStream(inputStream, 1024 * 10);

                    int read;
                    while ((read = bis.read(buffs, 0, 1024 * 10)) != -1) {
                        zos.write(buffs, 0, read);
                    }
                    ossObject.close();
                }
            }
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭流
            try {
                if (null != bis) {
                    bis.close();
                }
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归添加文件夹中的文件到压缩包中
     *
     * @param folderName
     * @param parentFolder
     * @param zos
     */
    private void addFilesToZip(String folderName, String parentFolder, ZipOutputStream zos) {
        // 构造ListObjectsRequest请求。
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
        // 设置正斜线（/）为文件夹的分隔符。
        listObjectsRequest.setDelimiter("/");
        // 列出folderName目录下的所有文件和文件夹。
        listObjectsRequest.setPrefix(folderName);
        ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);

        List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();

        for (OSSObjectSummary objectSummary : objectSummaries) {
            // 由于查询空目录会返回空目录本身，判断如果objectSummary为空目录本身，则跳过
            if (objectSummary.getKey().equals(folderName)) {
                continue;
            }

            String fileName = objectSummary.getKey();
            String entryName = fileName.substring(parentFolder.length());

            // 如果是文件，则将文件添加到压缩包中
            try (InputStream inputStream = ossClient.getObject(bucketName, fileName).getObjectContent()) {
                byte[] buffer = new byte[1024 * 10];
                zos.putNextEntry(new ZipEntry(entryName));
                int read;
                while ((read = inputStream.read(buffer, 0, 1024 * 10)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 递归处理文件夹中的文件
        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            addFilesToZip(commonPrefix, parentFolder, zos);
        }
    }

    public String determineCategory(String fileName) {
        Map<String, Set<String>> extensionsMap = new HashMap<>();
        extensionsMap.put("图片", new HashSet<>(Set.of(picture.split(",\\s*"))));
        extensionsMap.put("文档", new HashSet<>(Set.of(document.split(",\\s*"))));
        extensionsMap.put("视频", new HashSet<>(Set.of(video.split(",\\s*"))));
        extensionsMap.put("音频", new HashSet<>(Set.of(audio.split(",\\s*"))));
        for (Map.Entry<String, Set<String>> entry : extensionsMap.entrySet()) {
            if (entry.getValue().stream().anyMatch(fileName::endsWith)) {
                return entry.getKey();
            }
        }
        return "其它";
    }

    public URL share(String title, String[] files, Long userId) throws MalformedURLException {
        String destinationKey = "share/" + UUID.randomUUID() + "/";
        try {
            for (String file : files) {
                String sourceKey = userId + "/" + file;
                ObjectMetadata objectMetadata = ossClient.getObjectMetadata(bucketName, sourceKey);
                // 获取被拷贝文件的大小。
                long contentLength = objectMetadata.getContentLength();
                // 设置分片大小为10 MB。单位为字节。
                long partSize = 1024 * 1024 * 10;
                // 计算分片总数。
                int partCount = (int) (contentLength / partSize);
                if (contentLength % partSize != 0) {
                    partCount++;
                }
                System.out.println("total part count:" + partCount);
                // 初始化拷贝任务。可以通过InitiateMultipartUploadRequest指定目标文件元信息。
                InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName, destinationKey);
                // 拷贝源文件ContentType和UserMetadata，分片拷贝默认不拷贝源文件的ContentType和UserMetadata。
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(objectMetadata.getContentType());
                metadata.setUserMetadata(objectMetadata.getUserMetadata());
                initiateMultipartUploadRequest.setObjectMetadata(metadata);
                InitiateMultipartUploadResult initiateMultipartUploadResult = ossClient.initiateMultipartUpload(initiateMultipartUploadRequest);
                String uploadId = initiateMultipartUploadResult.getUploadId();
                // 分片拷贝。
                List<PartETag> partETags = new ArrayList<PartETag>();
                for (int i = 0; i < partCount; i++) {
                    // 计算每个分片的大小。
                    long skipBytes = partSize * i;
                    long size = partSize < contentLength - skipBytes ? partSize : contentLength - skipBytes;
                    // 创建UploadPartCopyRequest。可以通过UploadPartCopyRequest指定限定条件。
                    UploadPartCopyRequest uploadPartCopyRequest =
                            new UploadPartCopyRequest(bucketName, sourceKey, bucketName, destinationKey);
                    uploadPartCopyRequest.setUploadId(uploadId);
                    uploadPartCopyRequest.setPartSize(size);
                    uploadPartCopyRequest.setBeginIndex(skipBytes);
                    uploadPartCopyRequest.setPartNumber(i + 1);
                    /*//Map headers = new HashMap();
                    // 指定拷贝的源地址。
                    // headers.put(OSSHeaders.COPY_OBJECT_SOURCE, "/examplebucket/desexampleobject.txt");
                    // 指定源Object的拷贝范围。例如设置bytes=0~1023，表示拷贝1~1024字节的内容。
                    // headers.put(OSSHeaders.COPY_SOURCE_RANGE, "bytes=0~1023");
                    // 如果源Object的ETag值和您提供的ETag相等，则执行拷贝操作，并返回200 OK。
                    // headers.put(OSSHeaders.COPY_OBJECT_SOURCE_IF_MATCH, "5B3C1A2E053D763E1B002CC607C5****");
                    // 如果源Object的ETag值和您提供的ETag不相等，则执行拷贝操作，并返回200 OK。
                    // headers.put(OSSHeaders.COPY_OBJECT_SOURCE_IF_NONE_MATCH, "5B3C1A2E053D763E1B002CC607C5****");
                    // 如果指定的时间等于或者晚于文件实际修改时间，则正常拷贝文件，并返回200 OK。
                    // headers.put(OSSHeaders.COPY_OBJECT_SOURCE_IF_UNMODIFIED_SINCE, "2021-12-09T07:01:56.000Z");
                    // 如果源Object在用户指定的时间以后被修改过，则执行拷贝操作。
                    // headers.put(OSSHeaders.COPY_OBJECT_SOURCE_IF_MODIFIED_SINCE, "2021-12-09T07:01:56.000Z");
                    // uploadPartCopyRequest.setHeaders(headers);*/
                    UploadPartCopyResult uploadPartCopyResult = ossClient.uploadPartCopy(uploadPartCopyRequest);
                    // 将返回的分片ETag保存到partETags中。
                    partETags.add(uploadPartCopyResult.getPartETag());
                }
                // 提交分片拷贝任务。
                CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(
                        bucketName, destinationKey, uploadId, partETags);
                ossClient.completeMultipartUpload(completeMultipartUploadRequest);
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
        // 新增分享记录
        ShareFile shareFile = new ShareFile();
        shareFile.setTitle(title);
        shareFile.setPath(destinationKey);
        shareFile.setId(userId);
        shareFileService.save(shareFile);

        return new URL("http://localhost:8080/#/share/" + destinationKey);
    }
}
