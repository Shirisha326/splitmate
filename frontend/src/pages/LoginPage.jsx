import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../utils'
import toast from 'react-hot-toast'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const validate = () => {
    const e = {}
    if (!form.email) e.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Invalid email'
    if (!form.password) e.password = 'Password is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (ev) => {
    ev.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      await login(form.email, form.password)
      toast.success('Welcome back!')
      navigate('/dashboard')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-left">
        <h1 className="auth-hero-title">
          Split bills,<br />
          <em>not friendships.</em>
        </h1>
        <p className="auth-hero-text">
          SplitMate makes group expense tracking effortless.
          Add expenses, track balances, and settle up with
          minimum transactions.
        </p>
        <div className="auth-features">
          {[
            'Smart debt minimization algorithm',
            'Equal & custom expense splits',
            'Real-time balance tracking',
            'Settle up in one tap',
          ].map(f => (
            <div className="auth-feature" key={f}>
              <span className="auth-feature-dot" />
              {f}
            </div>
          ))}
        </div>
      </div>

      <div className="auth-right">
        <div className="auth-form-container">
          <h2 className="auth-form-title">Welcome back</h2>
          <p className="auth-form-subtitle">
            Sign in to your account to continue
          </p>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email address</label>
              <input className="form-input" type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                autoFocus />
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password"
                placeholder="Your password"
                value={form.password}
                onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
              {errors.password && <div className="form-error">{errors.password}</div>}
            </div>
            <div style={{ textAlign: 'right', marginTop: 4 }}>
            <Link to="/forgot-password"
              style={{ fontSize: '0.78rem', color: 'var(--terracotta)' }}>
              Forgot password?
             </Link>
            </div>

            <button type="submit" className="btn btn-primary btn-lg"
              style={{ width: '100%', marginTop: 8 }} disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: 24,
            color: 'var(--ink-muted)', fontSize: '0.875rem' }}>
            Don't have an account?{' '}
            <Link to="/register"
              style={{ color: 'var(--terracotta)', fontWeight: 500 }}>
              Create one
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}