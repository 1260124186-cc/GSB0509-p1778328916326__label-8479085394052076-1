import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

// 格式化金额
export function formatAmount(amount, showSign = false) {
  const num = parseFloat(amount) || 0
  const formatted = num.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
  if (showSign && num > 0) {
    return `+${formatted}`
  }
  return formatted
}

// 格式化日期
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 格式化日期时间
export function formatDateTime(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 相对时间
export function fromNow(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

// 获取日期描述
export function getDateLabel(date) {
  const d = dayjs(date)
  const today = dayjs().startOf('day')
  const yesterday = today.subtract(1, 'day')

  if (d.isSame(today, 'day')) {
    return '今天'
  } else if (d.isSame(yesterday, 'day')) {
    return '昨天'
  } else if (d.isSame(today, 'year')) {
    return d.format('MM月DD日')
  } else {
    return d.format('YYYY年MM月DD日')
  }
}

// 获取当前年月
export function getCurrentYearMonth() {
  return dayjs().format('YYYY-MM')
}

// 获取月份范围
export function getMonthRange(yearMonth) {
  const d = dayjs(yearMonth + '-01')
  return {
    startDate: d.startOf('month').format('YYYY-MM-DD'),
    endDate: d.endOf('month').format('YYYY-MM-DD')
  }
}

// 获取周范围
export function getWeekRange() {
  const today = dayjs()
  return {
    startDate: today.startOf('week').format('YYYY-MM-DD'),
    endDate: today.endOf('week').format('YYYY-MM-DD')
  }
}

// 获取年范围
export function getYearRange(year) {
  const d = dayjs(year + '-01-01')
  return {
    startDate: d.startOf('year').format('YYYY-MM-DD'),
    endDate: d.endOf('year').format('YYYY-MM-DD')
  }
}
