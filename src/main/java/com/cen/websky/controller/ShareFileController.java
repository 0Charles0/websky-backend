package com.cen.websky.controller;

import com.cen.websky.pojo.dto.ShareFileDTO;
import com.cen.websky.pojo.vo.FileVO;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.utils.AliOSSUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shareFile")
public class ShareFileController {
    private final AliOSSUtils aliOSSUtils;

    @PostMapping("/share")
    public Result share(@RequestBody ShareFileDTO shareFileDTO, HttpServletRequest request) {
        URL url;
        try {
            url = aliOSSUtils.share(shareFileDTO, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
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
