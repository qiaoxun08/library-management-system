export function formatDate(dateStr) {
  if (!dateStr) return '-'
  try {
    let d
    if (Array.isArray(dateStr)) {
      d = new Date(dateStr[0], dateStr[1] - 1, dateStr[2], dateStr[3], dateStr[4], dateStr[5])
    } else {
      const iso = dateStr.includes('T') && !dateStr.includes('+') && !dateStr.includes('Z')
        ? dateStr + '+08:00'
        : dateStr
      d = new Date(iso)
    }
    if (isNaN(d.getTime())) return '未知'
    return d.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })
  } catch (e) {
    return '未知'
  }
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  try {
    let d
    if (Array.isArray(dateStr)) {
      d = new Date(dateStr[0], dateStr[1] - 1, dateStr[2], dateStr[3], dateStr[4], dateStr[5])
    } else {
      const iso = dateStr.includes('T') && !dateStr.includes('+') && !dateStr.includes('Z')
        ? dateStr + '+08:00'
        : dateStr
      d = new Date(iso)
    }
    if (isNaN(d.getTime())) return '未知'
    return d.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
  } catch (e) {
    return '未知'
  }
}
