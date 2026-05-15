import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/client'
import { getErrorMessage } from '../utils'
import toast from 'react-hot-toast'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: '', name: '', newPassword: '', confirmPassword: ''
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)

  const validate = () => {
    const e = {}
    if (!form.email) e.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Invalid email'
    if (!form.name) e.name = 'Full name is required'
    if (!form.newPassword || form.newPassword.length < 6)
      e.newPassword = 'Password must be at least 6 characters'
    if (form.newPassword !== form.confirmPassword)
      e.confirmPassword = 'Passwords do not match'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      await api.post('/users/forgot-password', {
        email: form.email.toLowerCase(),
        name: form.name.trim(),
        newPassword: form.newPassword,
      })
      setSuccess(true)
      toast.success('Password reset successfully!')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className="auth-page">
        <div className="auth-left">
          <h1 className="auth-hero-title">
            Password<br />
            <em>reset!</em>
          </h1>
          <p className="auth-hero-text">
            Your password has been reset successfully.
            You can now login with your new password.
          </p>
        </div>
        <div className="auth-right">
          <div className="auth-form-container" style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '4rem', marginBottom: 20 }}>🎉</div>
            <h2 className="auth-form-title">Password Reset!</h2>
            <p className="auth-form-subtitle">
              Your password has been changed successfully.
            </p>
            <button
              className="btn btn-primary btn-lg"
              style={{ width: '100%', marginTop: 16 }}
              onClick={() => navigate('/login')}
            >
              Go to Login
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-page">
      <div className="auth-left">
        <h1 className="auth-hero-title">
          Reset your<br />
          <em>password.</em>
        </h1>
        <p className="auth-hero-text">
          Enter your registered email and full name
          to verify your identity and reset your password.
        </p>
        <div className="auth-features">
          {[
            'No email required',
            'Instant password reset',
            'Verify with your name',
            'Secure and simple',
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
          <h2 className="auth-form-title">Forgot Password</h2>
          <p className="auth-form-subtitle">
            Verify your identity to reset your password
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
              <label className="form-label">Full Name</label>
              <input className="form-input" type="text"
                placeholder="Enter your registered full name"
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
              {errors.name && <div className="form-error">{errors.name}</div>}
              <div style={{ fontSize: '0.75rem', color: 'var(--ink-muted)', marginTop: 4 }}>
                Must match the name you registered with
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">New Password</label>
              <input className="form-input" type="password"
                placeholder="At least 6 characters"
                value={form.newPassword}
                onChange={e => setForm(f => ({ ...f, newPassword: e.target.value }))} />
              {errors.newPassword && <div className="form-error">{errors.newPassword}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Confirm New Password</label>
              <input className="form-input" type="password"
                placeholder="Repeat new password"
                value={form.confirmPassword}
                onChange={e => setForm(f => ({ ...f, confirmPassword: e.target.value }))} />
              {errors.confirmPassword &&
                <div className="form-error">{errors.confirmPassword}</div>}
            </div>

            <button type="submit" className="btn btn-primary btn-lg"
              style={{ width: '100%', marginTop: 8 }} disabled={loading}>
              {loading ? 'Resetting…' : 'Reset Password'}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: 24,
            color: 'var(--ink-muted)', fontSize: '0.875rem' }}>
            Remember your password?{' '}
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