/**
 * 封装 localStorage 读写，自动加密/解密敏感字段
 */

import { encrypt, decrypt } from './crypto'

const USER_KEY = 'user'

/**
 * 保存用户信息（加密存储）
 */
export async function setUser(user) {
  try {
    const encrypted = await encrypt(JSON.stringify(user), user.userId)
    localStorage.setItem(USER_KEY, encrypted)
    // 同时存储 userId 以便解密时获取密钥
    localStorage.setItem('uid', String(user.userId))
  } catch (e) {
    console.error('加密存储失败:', e)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  }
}

/**
 * 读取用户信息（自动解密）
 */
export async function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null

  const uid = localStorage.getItem('uid')
  if (!uid) {
    // 无 uid，尝试直接解析（兼容未加密数据）
    try { return JSON.parse(raw) } catch { return null }
  }

  try {
    const decrypted = await decrypt(raw, uid)
    return JSON.parse(decrypted)
  } catch (e) {
    // 解密失败（密钥变更/数据篡改），清除数据
    console.warn('解密失败，数据可能被篡改，清除登录状态')
    clearUser()
    return null
  }
}

/**
 * 清除用户信息
 */
export function clearUser() {
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem('uid')
}
