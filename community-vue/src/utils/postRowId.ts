/**
 * 与后端 PostVO 一致：列表接口返回主键为 postId，部分前端会映射为 id。
 */
export function postRowId(row: { postId?: number; id?: number } | null | undefined): number {
  if (!row) return 0
  const v = row.postId ?? row.id
  if (v == null) return 0
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : 0
}
