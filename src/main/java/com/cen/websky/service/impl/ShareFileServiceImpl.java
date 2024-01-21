package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.mapper.ShareFileMapper;
import com.cen.websky.pojo.po.ShareFile;
import com.cen.websky.service.ShareFileService;
import org.springframework.stereotype.Service;

@Service
public class ShareFileServiceImpl extends ServiceImpl<ShareFileMapper, ShareFile> implements ShareFileService {
}
