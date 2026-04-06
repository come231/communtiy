package com.community.interact.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.interact.entity.CollectFolder;
import com.community.interact.mapper.CollectFolderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CollectFolderService {

    private final CollectFolderMapper collectFolderMapper;

    public List<CollectFolder> listFolders(Long userId) {
        return collectFolderMapper.selectList(
                new LambdaQueryWrapper<CollectFolder>()
                        .eq(CollectFolder::getUserId, userId)
                        .orderByDesc(CollectFolder::getIsDefault)
                        .orderByDesc(CollectFolder::getCreateTime));
    }

    public CollectFolder createFolder(Long userId, String name) {
        CollectFolder folder = new CollectFolder();
        folder.setUserId(userId);
        folder.setName(name != null ? name : "默认收藏夹");
        folder.setIsDefault(0);
        collectFolderMapper.insert(folder);
        return folder;
    }

    public void deleteFolder(Long userId, Long folderId) {
        CollectFolder folder = collectFolderMapper.selectById(folderId);
        if (folder != null && folder.getUserId().equals(userId)) {
            if (folder.getIsDefault() == 1) throw new RuntimeException("默认收藏夹不能删除");
            collectFolderMapper.deleteById(folderId);
        }
    }
}
