import { useState } from 'react'
import { auth } from '../api'

interface Props {
  onAuth: (username: string) => void
}

/** Login / register screen. Toggles between the two modes. */
export function Login({ onAuth }: Props) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const fn = mode === 'login' ? auth.login : auth.register
      const res = await fn(username.trim(), password)
      onAuth(res.username)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong')
      setBusy(false)
    }
  }

  // One-click demo login — no typing required.
  const demoLogin = async () => {
    setBusy(true)
    setError(null)
    try {
      const res = await auth.login('demo', 'demo1234')
      onAuth(res.username)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Demo login failed')
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <h1>Job Tracker</h1>
        <p className="auth-sub">
          {mode === 'login' ? 'Log in to your board' : 'Create your account'}
        </p>

        <form onSubmit={submit} className="auth-form">
          <label>
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
              autoCapitalize="none"
              placeholder="Miss. Roy"
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="at least 6 characters"
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn-primary" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Sign up'}
          </button>
        </form>

        <p className="auth-toggle">
          {mode === 'login' ? 'New here? ' : 'Already have an account? '}
          <button
            type="button"
            onClick={() => {
              setMode(mode === 'login' ? 'register' : 'login')
              setError(null)
            }}
          >
            {mode === 'login' ? 'Create an account' : 'Log in'}
          </button>
        </p>

        <button type="button" className="auth-demo-btn" onClick={demoLogin} disabled={busy}>
          Just exploring? <strong>Try the demo →</strong>
        </button>
      </div>
    </div>
  )
}
