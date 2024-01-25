package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.mapper.ShareFileMapper;
import com.cen.websky.pojo.dto.ShareFileDTO;
import com.cen.websky.pojo.po.ShareFile;
import com.cen.websky.pojo.vo.FileVO;
import com.cen.websky.pojo.vo.ShareFileVO;
import com.cen.websky.pojo.vo.ShareListVO;
import com.cen.websky.service.ShareFileService;
import com.cen.websky.utils.AliOSSUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareFileServiceImpl extends ServiceImpl<ShareFileMapper, ShareFile> implements ShareFileService {
    private final AliOSSUtils aliOSSUtils;

    @Override
    public URL share(ShareFileDTO shareFileDTO, Long userId) throws MalformedURLException {
        String folder = aliOSSUtils.share(shareFileDTO, userId);
        // 新增分享记录
        ShareFile shareFile = new ShareFile();
        shareFile.setTitle(shareFileDTO.getTitle());
        shareFile.setPath(folder);
        shareFile.setUserId(userId);
        shareFile.setOpen(shareFileDTO.getOpen());
        save(shareFile);
        return new URL("http://localhost:8080/#/share/" + shareFile.getId());
    }

    @Override
    public ShareFileVO fileList(Long id) {
        ShareFile shareFile = getById(id);
        ShareFileVO shareFileVO = new ShareFileVO();
        shareFileVO.setTitle(shareFile.getTitle());
        List<FileVO> fileVOS = aliOSSUtils.fileList(shareFile.getPath());
        fileVOS.remove(0);
        shareFileVO.setFiles(fileVOS);
        return shareFileVO;
    }

    @Override
    public ShareFileVO fileList(String path) {
        ShareFileVO shareFileVO = new ShareFileVO();
        List<FileVO> fileVOS = aliOSSUtils.fileList("share/" + path);
        fileVOS.remove(0);
        shareFileVO.setFiles(fileVOS);
        return shareFileVO;
    }

    @Override
    public List<ShareFile> getByUserId(Long userId) {
        return lambdaQuery().eq(ShareFile::getUserId, userId).list();
    }

    @Override
    public List<ShareFile> getOpen() {
        return lambdaQuery().eq(ShareFile::getOpen, true).list();
    }

    @Override
    public List<ShareListVO> shareList(Long userId) throws MalformedURLException {
        List<ShareFile> shareFiles = getByUserId(userId);
        return getShareListVOs(shareFiles);
    }

    @Override
    public List<ShareListVO> shareSearch(String shareSearchName) throws MalformedURLException {
        List<ShareFile> shareFiles = lambdaQuery().eq(ShareFile::getOpen, true)
                .like(ShareFile::getTitle, shareSearchName).list();
        return getShareListVOs(shareFiles);
    }

    @Override
    public void delete(Long shareFileId, Long userId) {
        remove(Wrappers.<ShareFile>lambdaQuery()
                .eq(ShareFile::getId, shareFileId)
                .eq(ShareFile::getUserId, userId));
    }

    private List<ShareListVO> getShareListVOs(List<ShareFile> shareFiles) throws MalformedURLException {
        List<ShareListVO> shareListVOs = new ArrayList<>();
        for (ShareFile shareFile : shareFiles) {
            ShareListVO shareListVO = new ShareListVO();
            shareListVO.setShareFileId(shareFile.getId().toString());
            shareListVO.setTitle(shareFile.getTitle());
            shareListVO.setUrl(new URL("http://localhost:8080/#/share/" + shareFile.getId()));
            shareListVOs.add(shareListVO);
        }
        return shareListVOs;
    }
}
