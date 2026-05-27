/**
 * 图片压缩：仅 JPG/PNG；长边超过 maxWidth/maxHeight 时缩放；尽量压到 maxSizeMB 以内。
 */

function approxBytesFromDataUrl(dataUrl) {
  const prefix = 'data:image/jpeg;base64,'
  if (!dataUrl || !dataUrl.startsWith(prefix)) return Infinity
  const b64 = dataUrl.slice(prefix.length)
  return Math.round((b64.length * 3) / 4)
}

/**
 * @param {File} file
 * @param {Object} [options]
 * @returns {Promise<string>} data:image/jpeg;base64,...
 */
export function compressImage(file, options = {}) {
  const {
    maxWidth = 1000,
    maxHeight = 1000,
    maxSizeMB = 1,
    quality = 0.85
  } = options

  const maxBytes = maxSizeMB * 1024 * 1024

  return new Promise((resolve, reject) => {
    const ok =
      file.type === 'image/jpeg' ||
      file.type === 'image/jpg' ||
      file.type === 'image/png'
    if (!ok) {
      reject(new Error('仅支持 JPG、PNG 格式，请重新选择图片'))
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        let { width, height } = img
        const ratio = Math.min(maxWidth / width, maxHeight / height, 1)
        if (ratio < 1) {
          width = Math.round(width * ratio)
          height = Math.round(height * ratio)
        }

        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        if (file.type === 'image/png') {
          ctx.fillStyle = '#FFFFFF'
          ctx.fillRect(0, 0, width, height)
        }
        ctx.drawImage(img, 0, 0, width, height)

        let q = quality
        let dataUrl = canvas.toDataURL('image/jpeg', q)
        while (approxBytesFromDataUrl(dataUrl) > maxBytes && q > 0.12) {
          q -= 0.07
          dataUrl = canvas.toDataURL('image/jpeg', q)
        }
        if (approxBytesFromDataUrl(dataUrl) > maxBytes) {
          reject(
            new Error(
              '图片无法在 1MB 以内压缩完成，请换一张更小或分辨率更低的图片'
            )
          )
          return
        }
        resolve(dataUrl)
      }
      img.onerror = () =>
        reject(new Error('图片无法解析，文件可能已损坏，请重新选择'))
      img.src = e.target.result
    }
    reader.onerror = () => reject(new Error('读取本地文件失败，请重试'))
    reader.readAsDataURL(file)
  })
}

export function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
