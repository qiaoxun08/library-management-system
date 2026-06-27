import { createStore } from 'vuex'

export default createStore({
  state: {
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || '',
    id: localStorage.getItem('id') || '',
    language: localStorage.getItem('language') || 'zh-CN'
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_ROLE(state, role) {
      state.role = role
      localStorage.setItem('role', role)
    },
    SET_USERNAME(state, username) {
      state.username = username
      localStorage.setItem('username', username)
    },
    SET_REAL_NAME(state, realName) {
      state.realName = realName
      localStorage.setItem('realName', realName)
    },
    SET_ID(state, id) {
      state.id = id
      localStorage.setItem('id', id)
    },
    SET_LANGUAGE(state, language) {
      state.language = language
      localStorage.setItem('language', language)
    },
    CLEAR_USER(state) {
      state.token = ''
      state.role = ''
      state.username = ''
      state.realName = ''
      state.id = ''
      state.language = 'zh-CN'
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('id')
    }
  },
  actions: {
    login({ commit }, { token, role, username, realName, id, language }) {
      commit('SET_TOKEN', token)
      commit('SET_ROLE', role)
      commit('SET_USERNAME', username)
      commit('SET_REAL_NAME', realName)
      commit('SET_ID', id)
      if (language) {
        commit('SET_LANGUAGE', language === 'en_US' ? 'en-US' : 'zh-CN')
      }
    },
    logout({ commit }) {
      commit('CLEAR_USER')
    }
  },
  getters: {
    isAuthenticated: state => !!state.token,
    userRole: state => state.role,
    username: state => state.username,
    realName: state => state.realName,
    id: state => state.id,
    language: state => state.language
  }
})
