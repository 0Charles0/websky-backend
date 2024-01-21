package com.cen.websky.controller;

import com.cen.websky.pojo.vo.FileVO;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.utils.AliOSSUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shareFile")
public class ShareFileController {
    private final AliOSSUtils aliOSSUtils;

    @PostMapping("/share")
    public Result share(String title, String[] files, HttpServletRequest request) {
        URL url;
        try {
            url = aliOSSUtils.share(title, files, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("共享失败");
        }
        return Result.success(url);
    }

    @GetMapping("/fileList")
    public Result fileList(String destinationKey) {
        List<FileVO> fileList;
        try {
            fileList = aliOSSUtils.fileList(destinationKey);
        } catch (Exception e) {
            return Result.error("查询文件失败");
        }
        return Result.success(fileList);
    }
}
