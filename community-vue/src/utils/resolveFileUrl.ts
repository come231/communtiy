/**
 * 将数据库/接口里存的图片地址转为浏览器可请求的地址，便于在 Vite 开发（走 /api 代理）时正常显示。
 *
 * 常见问题：
 * - 存了 `/file/download/...` 漏写 `/api`，在 localhost:3000 会变成请求 Vite 自身 → 404
 * - 仅当无法识别时才原样返回（如完整 https 直链）
 */
export function resolveFileUrl(url: string | null | undefined): string {
  if (url == null) return ''
  const s = String(url).trim()
  if (s === '') return ''
  if (s.startsWith('blob:') || s.startsWith('data:')) {
    return s
  }
  // 仅存 fileId（无 https、无 /）时走网关，由后端 302 到预签名
  if (/^\d{10,20}$/.test(s)) {
    return `/api/file/download/${s}`
  }

  if (s.startsWith('//')) {
    if (typeof window !== 'undefined') {
      return `${window.location.protocol}${s}`
    }
    return `https:${s}`
  }
  if (s.startsWith('http://') || s.startsWith('https://')) {
    return s
  }
  if (s.startsWith('/api/')) {
    return s
  }
  if (s.startsWith('/file/')) {
    return `/api${s}`
  }
  if (s.startsWith('file/')) {
    return `/api/${s}`
  }
  return s
}

/** 用户头像：支持存 fileId、/file/...、完整 URL；无则走占位 */
export function avatarDisplayUrl(
  raw: string | null | undefined,
  fallback: string
): string {
  const u = resolveFileUrl(raw)
  if (u) return u
  return fallback
}
