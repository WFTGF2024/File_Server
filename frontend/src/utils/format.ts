/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的字符串，如 "1.5 MB"
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const value = bytes / Math.pow(k, i)
  return `${value.toFixed(i === 0 ? 0 : 2)} ${units[i]}`
}

/**
 * 格式化日期时间
 * @param dateStr 日期字符串
 * @returns 格式化后的字符串，如 "2025-01-01 12:00"
 */
export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化百分比
 * @param value 0-100 的数值
 * @returns 格式化后的字符串
 */
export function formatPercent(value: number): string {
  return `${Math.min(value, 100).toFixed(1)}%`
}

/**
 * 根据文件扩展名获取文件图标类型
 * @param ext 文件扩展名
 * @returns Element Plus 图标名称
 */
export function getFileIcon(ext: string): string {
  const iconMap: Record<string, string> = {
    pdf: 'Document',
    doc: 'Document',
    docx: 'Document',
    xls: 'Grid',
    xlsx: 'Grid',
    ppt: 'DataLine',
    pptx: 'DataLine',
    jpg: 'Picture',
    jpeg: 'Picture',
    png: 'Picture',
    gif: 'Picture',
    svg: 'Picture',
    webp: 'Picture',
    mp4: 'VideoCamera',
    avi: 'VideoCamera',
    mkv: 'VideoCamera',
    mp3: 'Headset',
    wav: 'Headset',
    flac: 'Headset',
    zip: 'Files',
    rar: 'Files',
    '7z': 'Files',
    tar: 'Files',
    gz: 'Files',
    txt: 'Tickets',
    md: 'Tickets',
    json: 'Tickets',
    xml: 'Tickets',
    csv: 'Tickets',
    js: 'Tickets',
    ts: 'Tickets',
    py: 'Tickets',
    java: 'Tickets',
    html: 'Tickets',
    css: 'Tickets'
  }
  return iconMap[ext.toLowerCase()] || 'Document'
}

/**
 * 根据文件扩展名判断是否可预览
 */
export function isPreviewable(ext: string): boolean {
  const previewableExts = [
    'jpg', 'jpeg', 'png', 'gif', 'svg', 'webp', 'bmp',
    'pdf', 'txt', 'md', 'json', 'xml', 'csv',
    'mp4', 'mp3', 'wav'
  ]
  return previewableExts.includes(ext.toLowerCase())
}
