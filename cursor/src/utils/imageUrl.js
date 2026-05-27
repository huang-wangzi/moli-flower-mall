/**
 * 将上传接口返回的路径规范为可在浏览器中访问的 URL（补全 /uploads 前缀）。
 * 同时支持 Base64 图片直接返回。
 */
export function normalizeUploadUrl(url) {
  // 处理 null、undefined，空字符串
  if (url == null || url === '') return ''

  const u = String(url).trim()

  // 1. Base64 图片直接返回（已完整的 data URI）
  if (u.startsWith('data:')) return u

  // 2. 已经是完整的 HTTP URL
  if (u.startsWith('http://') || u.startsWith('https://')) return u

  // 3. 已经是 /uploads 开头的绝对路径
  if (u.startsWith('/uploads/')) return u

  // 4. 已经是 /api 开头的路径
  if (u.startsWith('/api/')) return u

  // 5. 其他 / 开头直接返回
  if (u.startsWith('/')) return u

  // 6. 相对路径加上 /uploads/ 前缀
  return '/uploads/' + u.replace(/^\/+/, '')
}
