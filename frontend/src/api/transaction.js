import api from './index.js'

export const transactionApi = {
  getList(params) {
    return api.get('/transactions', { params })
  },
  create(data) {
    return api.post('/transactions', data)
  },
  update(id, data) {
    return api.put(`/transactions/${id}`, data)
  },
  remove(id) {
    return api.delete(`/transactions/${id}`)
  },

    getSummary(year, month){
      return api.get(`/transactions/summary/${year}/${month}`)
    },
    getDaily(year, month) {
      return api.get(`/transactions/daily/${year}/${month}`)
    },
    exportCsv(params) {
      return api.get('/transactions/export/csv', { params, responseType: 'blob' })
    },
    exportXlsx(params) {
      return api.get('/transactions/export/xlsx', { params, responseType: 'blob' })
    },
}
