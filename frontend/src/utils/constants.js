// 分类图标映射
export const categoryIcons = {
  // 支出分类
  '餐饮': 'fa-utensils',
  '交通': 'fa-car',
  '购物': 'fa-shopping-bag',
  '娱乐': 'fa-gamepad',
  '居家': 'fa-home',
  '医疗': 'fa-hospital',
  '教育': 'fa-graduation-cap',
  '通讯': 'fa-phone',
  '人情': 'fa-gift',
  '其他': 'fa-ellipsis-h',

  // 收入分类
  '工资': 'fa-money-bill',
  '奖金': 'fa-trophy',
  '投资': 'fa-chart-line',
  '兼职': 'fa-briefcase',
  '红包': 'fa-envelope',

  // 默认
  'default': 'fa-tag'
}

// 获取分类图标
export function getCategoryIcon(name) {
  return categoryIcons[name] || categoryIcons['default']
}

// 支付方式选项
export const paymentMethods = [
  { value: '微信', label: '微信', icon: 'fab fa-weixin', color: '#07C160' },
  { value: '支付宝', label: '支付宝', icon: 'fab fa-alipay', color: '#1677FF' },
  { value: '银行卡', label: '银行卡', icon: 'fa-credit-card', color: '#F59E0B' },
  { value: '现金', label: '现金', icon: 'fa-money-bill-wave', color: '#10B981' },
  { value: '其他', label: '其他', icon: 'fa-ellipsis-h', color: '#6B7280' }
]

// 账本图标选项
export const bookIcons = [
  'fa-book',
  'fa-wallet',
  'fa-piggy-bank',
  'fa-home',
  'fa-plane',
  'fa-car',
  'fa-heart',
  'fa-star',
  'fa-shopping-bag',
  'fa-graduation-cap',
  'fa-briefcase',
  'fa-gift'
]

// 标签颜色选项
export const tagColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#5f3dc4',
  '#ff922b',
  '#20c997',
  '#e64980',
  '#7950f2'
]

// 预设支出分类
export const defaultExpenseCategories = [
  { name: '餐饮', icon: 'fa-utensils' },
  { name: '交通', icon: 'fa-car' },
  { name: '购物', icon: 'fa-shopping-bag' },
  { name: '娱乐', icon: 'fa-gamepad' },
  { name: '居家', icon: 'fa-home' },
  { name: '医疗', icon: 'fa-hospital' },
  { name: '教育', icon: 'fa-graduation-cap' },
  { name: '通讯', icon: 'fa-phone' },
  { name: '人情', icon: 'fa-gift' },
  { name: '其他', icon: 'fa-ellipsis-h' }
]

// 预设收入分类
export const defaultIncomeCategories = [
  { name: '工资', icon: 'fa-money-bill' },
  { name: '奖金', icon: 'fa-trophy' },
  { name: '投资', icon: 'fa-chart-line' },
  { name: '兼职', icon: 'fa-briefcase' },
  { name: '红包', icon: 'fa-envelope' },
  { name: '其他', icon: 'fa-ellipsis-h' }
]
