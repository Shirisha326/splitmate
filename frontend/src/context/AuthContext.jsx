import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authAPI, usersAPI } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('splitmate_user')
      return saved ? JSON.parse(saved) : null
    } catch { return null }
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('splitmate_token')
    if (token) {
      usersAPI.getMe()
        .then(res => {
          setUser(res.data.data)
          localStorage.setItem('splitmate_user', JSON.stringify(res.data.data))
        })
        .catch(() => {
          localStorage.removeItem('splitmate_token')
          localStorage.removeItem('splitmate_user')
          setUser(null)
        })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [])

  const login = useCallback(async (email, password) => {
    const res = await authAPI.login({ email, password })
    const { token, user } = res.data.data
    localStorage.setItem('splitmate_token', token)
    localStorage.setItem('splitmate_user', JSON.stringify(user))
    setUser(user)
    return user
  }, [])

  const register = useCallback(async (name, email, password) => {
    const res = await authAPI.register({ name, email, password })
    const { token, user } = res.data.data
    localStorage.setItem('splitmate_token', token)
    localStorage.setItem('splitmate_user', JSON.stringify(user))
    setUser(user)
    return user
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('splitmate_token')
    localStorage.removeItem('splitmate_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}