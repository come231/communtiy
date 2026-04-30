/** 轻量 Markdown → HTML（代码块、链接、图片、加粗/斜体），用于帖子正文 */

export function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function escapeAttr(s: string): string {
  return escapeHtml(s).replace(/'/g, '&#39;')
}

function renderInlineMarkdown(t: string): string {
  const imgs: { alt: string; url: string }[] = []
  let s = t.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_, alt, url) => {
    imgs.push({ alt, url })
    return `\0IMG${imgs.length - 1}\0`
  })
  const links: { label: string; url: string }[] = []
  s = s.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, label, url) => {
    links.push({ label, url })
    return `\0LINK${links.length - 1}\0`
  })
  s = escapeHtml(s)
  s = s.replace(/\*\*(.+?)\*\*/g, '<strong class="text-white font-semibold">$1</strong>')
  s = s.replace(/\n/g, '<br/>')
  imgs.forEach((im, i) => {
    s = s.replace(
      `\0IMG${i}\0`,
      `<img class="md-img rounded-lg border border-white/10 my-3 max-w-full block" src="${escapeAttr(im.url)}" alt="${escapeAttr(im.alt)}" loading="lazy" />`
    )
  })
  links.forEach((ln, i) => {
    s = s.replace(
      `\0LINK${i}\0`,
      `<a class="text-indigo-300 hover:underline break-all" href="${escapeAttr(ln.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(ln.label)}</a>`
    )
  })
  return s
}

export function renderMarkdownToHtml(raw: string): string {
  if (!raw) return ''
  const segments = raw.split(/```/)
  const out: string[] = []
  for (let i = 0; i < segments.length; i++) {
    if (i % 2 === 1) {
      // 代码块：深色「方框」样式，与正文区分（对齐常见技术博客阅读体验）
      out.push(
        '<pre class="md-code-block rounded-lg p-4 overflow-x-auto text-sm my-4 font-mono text-gray-100 border border-white/10 shadow-inner" style="background:#0d1117;"><code>' +
          escapeHtml(segments[i]) +
          '</code></pre>'
      )
    } else {
      out.push(renderInlineMarkdown(segments[i]))
    }
  }
  return out.join('')
}

/** 解析帖子 images 字段（JSON 数组或单 URL 兼容） */
export function parsePostImageList(images?: string | null): string[] {
  if (!images || !String(images).trim()) return []
  const str = String(images).trim()
  try {
    const j = JSON.parse(str)
    if (Array.isArray(j)) {
      return j.filter((x): x is string => typeof x === 'string' && x.length > 0)
    }
  } catch {
    /* 单 URL */
  }
  return [str]
}
