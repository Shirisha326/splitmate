import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/client'
import { getErrorMessage } from '../utils'
import toast from 'react-hot-toast'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})

  // Step 1 — Send OTP to email
  const handleSendOtp = async (e) => {
    e.preventDefault()
    if (!email) return setErrors({ email: 'Email is required' })
    if (!/\S+@\S+\.\S+/.test(email))
      return setErrors({ email: 'Invalid email' })

    setLoading(true)
    try {
      await api.post('/users/forgot-password/send-otp', {
        email: email.toLowerCase()
      })
      toast.success('OTP sent to your email!')
      setStep(2)
      setErrors({})
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  // Step 2 — Verify OTP and reset password
  const handleResetPassword = async (e) => {
    e.preventDefault()
    const e2 = {}
    if (!otp) e2.otp = 'Enter the OTP sent to your email'
    if (!newPassword || newPassword.length < 6)
      e2.newPassword = 'Password must be at least 6 characters'
    if (newPassword !== confirmPassword)
      e2.confirmPassword = 'Passwords do not match'
    if (Object.keys(e2).length > 0) return setErrors(e2)

    setLoading(true)
    try {
      await api.post('/users/forgot-password/reset', {
        email: email.toLowerCase(),
        otp,
        newPassword,
      })
      toast.success('Password reset successfully!')
      setOtp('')
      navigate('/login')
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
          Reset your<br />
          <em>password.</em>
        </h1>
        <p className="auth-hero-text">
          {step === 1
            ? 'Enter your email and we will send you a 6-digit code to reset your password.'
            : 'Enter the 6-digit code sent to your email and set your new password.'
          }
        </p>
        <div className="auth-features">
          {[
            '6-digit OTP sent to email',
            'Secure password reset',
            'Takes less than a minute',
            'No hassle verification',
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

          {/* Step 1 — Enter Email */}
          {step === 1 && (
            <>
              <h2 className="auth-form-title">Forgot Password</h2>
              <p className="auth-form-subtitle">
                Enter your email to receive a reset code
              </p>

              <form onSubmit={handleSendOtp}>
                <div className="form-group">
                  <label className="form-label">Email address</label>
                  <input className="form-input" type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    autoFocus />
                  {errors.email && <div className="form-error">{errors.email}</div>}
                </div>

                <button type="submit" className="btn btn-primary btn-lg"
                  style={{ width: '100%', marginTop: 8 }} disabled={loading}>
                  {loading ? 'Sending OTP…' : 'Send Reset Code 📧'}
                </button>
              </form>
            </>
          )}

          {/* Step 2 — Enter OTP and new password */}
          {step === 2 && (
            <>
              <h2 className="auth-form-title">Enter Reset Code</h2>
              <p className="auth-form-subtitle">
                We sent a 6-digit code to <strong>{email}</strong>
              </p>

              <form onSubmit={handleResetPassword}>
                <div className="form-group">
                  <label className="form-label">6-Digit Code</label>
                  <div style={{ display: 'flex', gap: 10, justifyContent: 'center', margin: '10px 0' }}>
                    {[0,1,2,3,4,5].map(i => (
                      <input
                        key={i}
                        id={`otp-${i}`}
                        type="password"
                        maxLength={1}
                        value={otp[i] || ''}
                        onChange={e => {
                          const val = e.target.value.replace(/[^0-9]/g, '')
                          const otpArr = otp.split('')
                          otpArr[i] = val
                          setOtp(otpArr.join(''))
                          if (val && i < 5) {
                            document.getElementById(`otp-${i+1}`)?.focus()
                          }
                        }}
                        onKeyDown={e => {
                          if (e.key === 'Backspace' && !otp[i] && i > 0) {
                            document.getElementById(`otp-${i-1}`)?.focus()
                          }
                        }}
                        onPaste={e => {
                          e.preventDefault()
                          const pasted = e.clipboardData.getData('text')
                            .replace(/[^0-9]/g, '').slice(0, 6)
                          setOtp(pasted)
                          const nextIndex = Math.min(pasted.length, 5)
                          document.getElementById(`otp-${nextIndex}`)?.focus()
                        }}
                        style={{
                          width: 48, height: 56,
                          textAlign: 'center',
                          fontSize: '1.4rem',
                          fontWeight: 700,
                          border: '2px solid',
                          borderColor: otp[i] ? 'var(--terracotta)' : 'var(--cream-dark)',
                          borderRadius: 'var(--radius-sm)',
                          outline: 'none',
                          fontFamily: 'var(--font-display)',
                          background: otp[i] ? 'rgba(201,98,63,0.06)' : 'var(--white)',
                          color: 'var(--ink)',
                          transition: 'var(--transition)',
                        }}
                      />
                    ))}
                  </div>
                  {errors.otp && <div className="form-error">{errors.otp}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label">New Password</label>
                  <input className="form-input" type="password"
                    placeholder="At least 6 characters"
                    value={newPassword}
                    onChange={e => setNewPassword(e.target.value)} />
                  {errors.newPassword &&
                    <div className="form-error">{errors.newPassword}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label">Confirm New Password</label>
                  <input className="form-input" type="password"
                    placeholder="Repeat new password"
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)} />
                  {errors.confirmPassword &&
                    <div className="form-error">{errors.confirmPassword}</div>}
                </div>

                <button type="submit" className="btn btn-primary btn-lg"
                  style={{ width: '100%', marginTop: 8 }} disabled={loading}>
                  {loading ? 'Resetting…' : 'Reset Password'}
                </button>

                <button type="button"
                  className="btn btn-ghost"
                  style={{ width: '100%', marginTop: 8 }}
                  onClick={() => { setStep(1); setOtp('') }}>
                  ← Change email
                </button>
              </form>
            </>
          )}

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