package com.cen.websky.controller;

import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public Result upload(List<MultipartFile> files, HttpServletRequest request) {
        return fileService.upload(files, request);
    }
}
