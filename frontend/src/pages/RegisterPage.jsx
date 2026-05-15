import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../utils'
import toast from 'react-hot-toast'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    name: '', email: '', password: '', confirm: ''
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const validate = () => {
    const e = {}
    if (!form.name || form.name.trim().length < 2)
      e.name = 'Name must be at least 2 characters'
    if (!form.email) e.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Invalid email'
    if (!form.password || form.password.length < 6)
      e.password = 'Password must be at least 6 characters'
    if (form.password !== form.confirm)
      e.confirm = 'Passwords do not match'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (ev) => {
    ev.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      await register(form.name.trim(), form.email, form.password)
      toast.success('Account created! Welcome to SplitMate 🎉')
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
          Start splitting<br />
          <em>smarter.</em>
        </h1>
        <p className="auth-hero-text">
          Create a free account and start tracking group
          expenses in minutes.
        </p>
        <div className="auth-features">
          {[
            'Free forever — no hidden fees',
            'Invite friends by email',
            'Multiple groups & trips',
            'Equal, exact, or % splits',
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
          <h2 className="auth-form-title">Create account</h2>
          <p className="auth-form-subtitle">
            Join SplitMate and stop doing the math
          </p>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Your name</label>
              <input className="form-input" placeholder="Priya Sharma"
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                autoFocus />
              {errors.name && <div className="form-error">{errors.name}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Email address</label>
              <input className="form-input" type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password"
                placeholder="At least 6 characters"
                value={form.password}
                onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
              {errors.password && <div className="form-error">{errors.password}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Confirm password</label>
              <input className="form-input" type="password"
                placeholder="Repeat your password"
                value={form.confirm}
                onChange={e => setForm(f => ({ ...f, confirm: e.target.value }))} />
              {errors.confirm && <div className="form-error">{errors.confirm}</div>}
            </div>

            <button type="submit" className="btn btn-primary btn-lg"
              style={{ width: '100%', marginTop: 8 }} disabled={loading}>
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: 24,
            color: 'var(--ink-muted)', fontSize: '0.875rem' }}>
            Already have an account?{' '}
            <Link to="/login"
              style={{ color: 'var(--terracotta)', fontWeight: 500 }}>
              Sign in
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}