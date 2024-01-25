package com.cen.websky.controller;

import com.cen.websky.pojo.dto.ShareFileDTO;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.pojo.vo.ShareFileVO;
import com.cen.websky.pojo.vo.ShareListVO;
import com.cen.websky.service.ShareFileService;
import com.cen.websky.utils.AliOSSUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shareFile")
public class ShareFileController {
    private final AliOSSUtils aliOSSUtils;
    private final ShareFileService shareFileService;

    @PostMapping("/share")
    public Result share(@RequestBody ShareFileDTO shareFileDTO, HttpServletRequest request) {
        URL url;
        try {
            url = shareFileService.share(shareFileDTO, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("共享失败");
        }
        return Result.success(url);
    }

    @GetMapping("/fileList")
    public Result fileList(String shareFileId, String path) {
        ShareFileVO shareFileVO;
        try {
            if (path != null) {
                shareFileVO = shareFileService.fileList(path);
            } else {
                shareFileVO = shareFileService.fileList(Long.parseLong(shareFileId));
            }
        } catch (Exception e) {
            return Result.error("查询文件失败");
        }
        return Result.success(shareFileVO);
    }

    @GetMapping("/queryShare")
    public Result shareList(HttpServletRequest request) {
        List<ShareListVO> shareListVO;
        try {
            shareListVO = shareFileService.shareList(((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("查询文件失败");
        }
        return Result.success(shareListVO);
    }

    @GetMapping("/shareSearch")
    public Result shareSearch(String shareSearchName) {
        List<ShareListVO> shareListVO;
        try {
            shareListVO = shareFileService.shareSearch(shareSearchName);
        } catch (Exception e) {
            return Result.error("搜索文件失败");
        }
        return Result.success(shareListVO);
    }

    @GetMapping("/download")
    public void download(String[] fileNames, HttpServletResponse response, HttpServletRequest request) {
        aliOSSUtils.downLoad(fileNames, response);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam String shareFileId, HttpServletRequest request) {
        shareFileService.delete(Long.parseLong(shareFileId), ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
    }
}
