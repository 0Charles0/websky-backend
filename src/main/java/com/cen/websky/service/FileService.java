package com.cen.websky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.websky.pojo.po.File;
import com.cen.websky.pojo.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File> {
    Result upload(List<MultipartFile> files, HttpServletRequest request);
}
