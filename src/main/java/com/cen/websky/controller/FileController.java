package com.cen.websky.controller;

import com.cen.websky.pojo.vo.FileVO;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.utils.AliOSSUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    public Result addFolder(String folderName, String path, HttpServletRequest request) {
        try {
            aliOSSUtils.addFolder(folderName, path, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("新建文件夹失败");
        }
        return Result.success("新建文件夹成功");
    }

    @GetMapping("/fileList")
    public Result fileList(String path, HttpServletRequest request) {
        List<FileVO> fileList;
        try {
            fileList = aliOSSUtils.fileList(path, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("查询文件失败");
        }
        return Result.success(fileList);
    }

    @GetMapping("/category")
    public Result classify(String category, HttpServletRequest request) {
        List<FileVO> fileList;
        try {
            fileList = aliOSSUtils.classify(category, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("查询该类别文件失败");
        }
        return Result.success(fileList);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestBody String[] objectNames, HttpServletRequest request) {
        try {
            // 调用阿里云OSS工具类，将上传上来的文件存入阿里云
            aliOSSUtils.delete(objectNames, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @GetMapping("/download")
    public void download(String[] fileNames, HttpServletResponse response, HttpServletRequest request) {
        aliOSSUtils.downLoad(fileNames, response, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
    }
}
