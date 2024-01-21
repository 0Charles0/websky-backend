package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.mapper.FileMapper;
import com.cen.websky.pojo.dto.FileDTO;
import com.cen.websky.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service

@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, FileDTO> implements FileService {
}
