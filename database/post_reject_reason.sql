-- community_content.post：人工/审核打回原因（作者在个人中心可见）
ALTER TABLE `post`
  ADD COLUMN `reject_reason` varchar(1000) DEFAULT NULL COMMENT '审核打回原因/建议' AFTER `status`;
