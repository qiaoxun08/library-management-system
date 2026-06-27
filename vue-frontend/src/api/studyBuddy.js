import axios from './axios'

// 获取我的学习伙伴 profile
export function getMyBuddyProfile() {
  return axios.get('/study-buddy/mine')
}

// 保存/更新我的学习伙伴 profile
export function saveBuddyProfile(data) {
  return axios.post('/study-buddy', data)
}

// 获取匹配的学习伙伴
export function getMatchedBuddies() {
  return axios.get('/study-buddy/match')
}

// 删除我的学习伙伴 profile
export function deleteMyBuddyProfile() {
  return axios.delete('/study-buddy/mine')
}
