import type { Job, JobInput, JobStatus, StatusCount } from './types'

// Web/Docker: VITE_API_BASE is empty, so URLs are relative ("/api/...") and
//   get proxied to the gateway by Vite (dev) or nginx (docker).
// iOS (Capacitor): the webview loads from capacitor://localhost with no proxy,
//   so the build sets VITE_API_BASE=http://localhost:8080 (see `npm run build:ios`)
//   to call the gateway with an absolute URL.
const API_ROOT = import.meta.env.VITE_API_BASE ?? ''
const API = `${API_ROOT}/api`

// --- session (JWT stored in localStorage) ---
const TOKEN_KEY = 'jt_token'
const USER_KEY = 'jt_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}
export function getUser(): string | null {
  return localStorage.getItem(USER_KEY)
}
function saveSession(token: string, username: string) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, username)
}
export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

// Lets App react when a token expires (log the user back out).
let onUnauthorized: () => void = () => {}
export function setUnauthorizedHandler(fn: () => void) {
  onUnauthorized = fn
}

function authHeaders(): Record<string, string> {
  const t = getToken()
  return t ? { Authorization: `Bearer ${t}` } : {}
}

async function parseError(res: Response): Promise<string> {
  try {
    const body = await res.json()
    if (body?.detail) return body.detail
  } catch {
    /* not JSON */
  }
  return `Request failed (${res.status})`
}

// For /api/auth/** — a 401 here means wrong credentials, not an expired session.
async function handleAuth<T>(res: Response): Promise<T> {
  if (!res.ok) throw new Error(await parseError(res))
  return res.json() as Promise<T>
}

// For /api/jobs/** — a 401 means the token expired: clear it and bounce to login.
async function handleJob<T>(res: Response): Promise<T> {
  if (res.status === 401) {
    clearSession()
    onUnauthorized()
    throw new Error('Session expired — please log in again.')
  }
  if (!res.ok) throw new Error(await parseError(res))
  return (res.status === 204 ? undefined : await res.json()) as T
}

export interface AuthResult {
  token: string
  username: string
}

export const auth = {
  async register(username: string, password: string): Promise<AuthResult> {
    const res = await fetch(`${API}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    const data = await handleAuth<AuthResult>(res)
    saveSession(data.token, data.username)
    return data
  },
  async login(username: string, password: string): Promise<AuthResult> {
    const res = await fetch(`${API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    const data = await handleAuth<AuthResult>(res)
    saveSession(data.token, data.username)
    return data
  },
  logout() {
    clearSession()
  },
}

const JOBS = `${API}/jobs`

export const api = {
  list(status?: JobStatus): Promise<Job[]> {
    const query = status ? `?status=${status}` : ''
    return fetch(`${JOBS}${query}`, { headers: authHeaders() }).then((r) => handleJob<Job[]>(r))
  },

  stats(): Promise<StatusCount[]> {
    return fetch(`${JOBS}/stats`, { headers: authHeaders() }).then((r) => handleJob<StatusCount[]>(r))
  },

  create(input: JobInput): Promise<Job> {
    return fetch(JOBS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify(input),
    }).then((r) => handleJob<Job>(r))
  },

  remove(id: number): Promise<void> {
    return fetch(`${JOBS}/${id}`, { method: 'DELETE', headers: authHeaders() }).then((r) =>
      handleJob<void>(r),
    )
  },
}
