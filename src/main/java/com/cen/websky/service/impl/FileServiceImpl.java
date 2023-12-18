package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.mapper.FileMapper;
import com.cen.websky.pojo.po.File;
import com.cen.websky.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    /*private final AliOSSUtils aliOSSUtils;

    @Override
    public Result upload(List<MultipartFile> files, HttpServletRequest request) {
        try {
            // 调用阿里云OSS工具类，将上传上来的文件存入阿里云
            aliOSSUtils.upload(files, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("上传失败");
        }
        return Result.success("上传成功");
    }*/
}
