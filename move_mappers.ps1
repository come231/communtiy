# PowerShell脚本用于移动Mapper文件
$basePath = "c:\Users\lenovo\Desktop\community\community-admin\src\main\java\com\community\admin\mapper"

# 安全相关的Mapper
$securityMappers = @(
    "OperationLogMapper.java",
    "SensitiveWordMapper.java", 
    "TextReviewMapper.java",
    "RateLimitRecordMapper.java"
)

# 管理员相关的Mapper
$adminMappers = @(
    "SysAdminMapper.java",
    "SysAdminLogMapper.java",
    "SysConfigMapper.java"
)

# 处理安全Mapper
foreach ($mapper in $securityMappers) {
    $sourcePath = Join-Path $basePath $mapper
    if (Test-Path $sourcePath) {
        # 读取文件内容
        $content = Get-Content $sourcePath -Raw
        
        # 修改包名
        $newContent = $content -replace "package com\.community\.admin\.mapper;", "package com.community.admin.mapper.security;"
        
        # 写入新位置
        $destPath = Join-Path (Join-Path $basePath "security") $mapper
        $newContent | Out-File -FilePath $destPath -Encoding UTF8 -Force
        
        Write-Host "移动 $mapper 到 security 目录"
        
        # 删除原文件
        Remove-Item $sourcePath -Force
    } else {
        Write-Host "文件不存在: $sourcePath"
    }
}

# 处理管理员Mapper
foreach ($mapper in $adminMappers) {
    $sourcePath = Join-Path $basePath $mapper
    if (Test-Path $sourcePath) {
        # 读取文件内容
        $content = Get-Content $sourcePath -Raw
        
        # 修改包名
        $newContent = $content -replace "package com\.community\.admin\.mapper;", "package com.community.admin.mapper.admin;"
        
        # 写入新位置
        $destPath = Join-Path (Join-Path $basePath "admin") $mapper
        $newContent | Out-File -FilePath $destPath -Encoding UTF8 -Force
        
        Write-Host "移动 $mapper 到 admin 目录"
        
        # 删除原文件
        Remove-Item $sourcePath -Force
    } else {
        Write-Host "文件不存在: $sourcePath"
    }
}

Write-Host "所有Mapper文件移动完成！"