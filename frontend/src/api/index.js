import api from './client'

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
}

export const usersAPI = {
  getMe: () => api.get('/users/me'),
  searchByEmail: (email) => api.get('/users/search', { params: { email } }),
}

export const groupsAPI = {
  create: (data) => api.post('/groups', data),
  getAll: () => api.get('/groups'),
  getById: (id) => api.get(`/groups/${id}`),
  update: (id, data) => api.patch(`/groups/${id}`, data),
  getSummary: (id) => api.get(`/groups/${id}/summary`),
  addMember: (id, data) => api.post(`/groups/${id}/members`, data),
  removeMember: (groupId, userId) =>
    api.delete(`/groups/${groupId}/members/${userId}`),
}

export const expensesAPI = {
  create: (groupId, data) => api.post(`/groups/${groupId}/expenses`, data),
  getAll: (groupId) => api.get(`/groups/${groupId}/expenses`),
  getById: (groupId, expenseId) =>
    api.get(`/groups/${groupId}/expenses/${expenseId}`),
  update: (groupId, expenseId, data) =>
    api.patch(`/groups/${groupId}/expenses/${expenseId}`, data),
  delete: (groupId, expenseId) =>
    api.delete(`/groups/${groupId}/expenses/${expenseId}`),
}

export const balancesAPI = {
  getBalances: (groupId) => api.get(`/groups/${groupId}/balances`),
  getDebts: (groupId) => api.get(`/groups/${groupId}/debts`),
}

export const settlementsAPI = {
  create: (groupId, data) =>
    api.post(`/groups/${groupId}/settlements`, data),
  getAll: (groupId) => api.get(`/groups/${groupId}/settlements`),
  delete: (groupId, settlementId) =>
    api.delete(`/groups/${groupId}/settlements/${settlementId}`),
}