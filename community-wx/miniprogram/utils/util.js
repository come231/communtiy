// 工具函数

// 格式化时间
function formatTime(date) {
  var d = typeof date === 'string' ? new Date(date) : date
  var now = new Date()
  var diff = now.getTime() - d.getTime()
  
  var minute = 60 * 1000
  var hour = 60 * minute
  var day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < 7 * day) {
    return Math.floor(diff / day) + '天前'
  } else {
    var year = d.getFullYear()
    var month = String(d.getMonth() + 1).padStart(2, '0')
    var dayNum = String(d.getDate()).padStart(2, '0')
    if (year === now.getFullYear()) {
      return month + '-' + dayNum
    }
    return year + '-' + month + '-' + dayNum
  }
}

// 格式化数字 (超过10000显示为1万)
function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return String(num)
}

// HTML转文本
function htmlToText(html) {
  return html.replace(/<[^>]+>/g, '').trim()
}

// 截取文本
function truncate(text, maxLength) {
  if (text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

// 验证手机号
function isPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 验证邮箱
function isEmail(email) {
  return /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(email)
}

// 防抖函数
function debounce(fn, delay) {
  if (delay === undefined) delay = 300
  var timer = null
  return function() {
    var args = arguments
    var _this = this
    clearTimeout(timer)
    timer = setTimeout(function() {
      fn.apply(_this, args)
    }, delay)
  }
}

// 节流函数
function throttle(fn, delay) {
  if (delay === undefined) delay = 300
  var last = 0
  return function() {
    var args = arguments
    var _this = this
    var now = Date.now()
    if (now - last > delay) {
      last = now
      fn.apply(_this, args)
    }
  }
}

// 获取相对路径中的参数
function getQueryString(url, key) {
  var params = url.split('?')[1]
  if (!params) return null
  var paramList = params.split('&')
  for (var i = 0; i < paramList.length; i++) {
    var param = paramList[i]
    var kv = param.split('=')
    if (kv[0] === key) return kv[1]
  }
  return null
}

// 生成唯一ID
function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

// 深拷贝
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (Array.isArray(obj)) {
    return obj.map(function(item) { return deepClone(item) })
  }
  var cloned = {}
  for (var key in obj) {
    if (obj.hasOwnProperty(key)) {
      cloned[key] = deepClone(obj[key])
    }
  }
  return cloned
}

module.exports = {
  formatTime: formatTime,
  formatNumber: formatNumber,
  htmlToText: htmlToText,
  truncate: truncate,
  isPhone: isPhone,
  isEmail: isEmail,
  debounce: debounce,
  throttle: throttle,
  getQueryString: getQueryString,
  generateId: generateId,
  deepClone: deepClone,
}
