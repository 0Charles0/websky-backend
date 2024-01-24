package com.cen.websky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.websky.pojo.dto.ShareFileDTO;
import com.cen.websky.pojo.po.ShareFile;
import com.cen.websky.pojo.vo.ShareFileVO;
import com.cen.websky.pojo.vo.ShareListVO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public interface ShareFileService extends IService<ShareFile> {
    URL share(ShareFileDTO shareFileDTO, Long userId) throws MalformedURLException;
    ShareFileVO fileList(Long id);
    ShareFileVO fileList(String path);

    List<ShareFile> getByUserId(Long userId);

    List<ShareFile> getOpen();

    List<ShareListVO> shareList(Long userId) throws MalformedURLException;

    List<ShareListVO> shareSearch(String shareSearchName) throws MalformedURLException;
}
