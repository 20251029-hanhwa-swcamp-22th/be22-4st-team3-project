import api from './index.js'

export const accountApi = {
  getList() {
    return api.get('/accounts')
  },
  getDetail(id) {
    return api.get(`/accounts/${id}`)
  },
  getSummary() {
    return api.get('/accounts/summary')
  },
  create(data) {
    return api.post('/accounts', data)
  },
  update(id, data) {
    return api.put(`/accounts/${id}`, data)
  },
  remove(id) {
    return api.delete(`/accounts/${id}`)
  },
}
