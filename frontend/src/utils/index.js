import { format, formatDistanceToNow } from 'date-fns'

export const formatCurrency = (amount, currency = '₹') => {
  const num = parseFloat(amount) || 0
  return `${currency}${num.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`
}

export const formatDate = (dateStr) => {
  if (!dateStr) return ''
  try { return format(new Date(dateStr), 'dd MMM yyyy') }
  catch { return dateStr }
}

export const formatRelative = (dateStr) => {
  if (!dateStr) return ''
  try { return formatDistanceToNow(new Date(dateStr), { addSuffix: true }) }
  catch { return dateStr }
}

export const getCategoryEmoji = (category) => {
  const map = {
    FOOD: '🍽️', TRAVEL: '✈️', ACCOMMODATION: '🏨',
    SHOPPING: '🛍️', ENTERTAINMENT: '🎉', TRANSPORT: '🚗',
    UTILITIES: '💡', HEALTH: '💊', OTHER: '📋'
  }
  return map[category] || '📋'
}

export const getGroupCategoryEmoji = (category) => {
  const map = {
    TRIP: '✈️', DINING: '🍽️', ROOMMATES: '🏠',
    PARTY: '🎉', SPORTS: '⚽', WORK: '💼', OTHER: '👥'
  }
  return map[category] || '👥'
}

export const getErrorMessage = (err) => {
  return err?.response?.data?.message
    || err?.message
    || 'Something went wrong'
}

export const EXPENSE_CATEGORIES = [
  { value: 'FOOD', label: 'Food & Drinks', emoji: '🍽️' },
  { value: 'TRAVEL', label: 'Travel', emoji: '✈️' },
  { value: 'ACCOMMODATION', label: 'Accommodation', emoji: '🏨' },
  { value: 'SHOPPING', label: 'Shopping', emoji: '🛍️' },
  { value: 'ENTERTAINMENT', label: 'Entertainment', emoji: '🎉' },
  { value: 'TRANSPORT', label: 'Transport', emoji: '🚗' },
  { value: 'UTILITIES', label: 'Utilities', emoji: '💡' },
  { value: 'HEALTH', label: 'Health', emoji: '💊' },
  { value: 'OTHER', label: 'Other', emoji: '📋' },
]

export const GROUP_CATEGORIES = [
  { value: 'TRIP', label: 'Trip', emoji: '✈️' },
  { value: 'DINING', label: 'Dining Out', emoji: '🍽️' },
  { value: 'ROOMMATES', label: 'Roommates', emoji: '🏠' },
  { value: 'PARTY', label: 'Party / Event', emoji: '🎉' },
  { value: 'SPORTS', label: 'Sports', emoji: '⚽' },
  { value: 'WORK', label: 'Work', emoji: '💼' },
  { value: 'OTHER', label: 'Other', emoji: '👥' },
]