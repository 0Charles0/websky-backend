package com.cen.websky.controller;

import com.cen.websky.pojo.vo.Result;
import com.cen.websky.utils.AliOSSUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    private final AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    public Result upload(List<MultipartFile> files, HttpServletRequest request) {
        try {
            // 调用阿里云OSS工具类，将上传上来的文件存入阿里云
            aliOSSUtils.upload(files, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("上传失败");
        }
        return Result.success("上传成功");
    }

    @PostMapping("/addFolder")
    public Result addFolder(String folderName, HttpServletRequest request) {
        try {
            aliOSSUtils.addFolder(folderName, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("新建文件夹失败");
        }
        return Result.success("新建文件夹成功");
    }

    @GetMapping("/fileList")
    public void fileList(String path, HttpServletRequest request) {

    }
}
