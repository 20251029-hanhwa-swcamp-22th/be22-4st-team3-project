import api from './index.js'

export const dashboardApi = {
  getDashboard() {
    return api.get('/dashboard')
  },
}

