package com.cen.websky.controller;

import com.cen.websky.pojo.vo.Result;
import com.cen.websky.utils.AliOSSUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    private final AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        //调用阿里云OSS工具类，将上传上来的文件存入阿里云
        aliOSSUtils.upload(file);

        return Result.success("上传成功");
    }
}
