import type { Job, JobInput, JobStatus, StatusCount } from './types'

// Web/Docker: VITE_API_BASE is empty, so URLs are relative ("/api/...") and
//   get proxied to the gateway by Vite (dev) or nginx (docker).
// iOS (Capacitor): the webview loads from capacitor://localhost with no proxy,
//   so the build sets VITE_API_BASE=http://localhost:8080 (see `npm run build:ios`)
//   to call the gateway with an absolute URL.
const API_ROOT = import.meta.env.VITE_API_BASE ?? ''
const BASE = `${API_ROOT}/api/jobs`

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.text()
    throw new Error(`Request failed (${res.status}): ${body}`)
  }
  // DELETE returns 204 No Content.
  return (res.status === 204 ? undefined : await res.json()) as T
}

export const api = {
  list(status?: JobStatus): Promise<Job[]> {
    const query = status ? `?status=${status}` : ''
    return fetch(`${BASE}${query}`).then((r) => handle<Job[]>(r))
  },

  stats(): Promise<StatusCount[]> {
    return fetch(`${BASE}/stats`).then((r) => handle<StatusCount[]>(r))
  },

  create(input: JobInput): Promise<Job> {
    return fetch(BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    }).then((r) => handle<Job>(r))
  },

  remove(id: number): Promise<void> {
    return fetch(`${BASE}/${id}`, { method: 'DELETE' }).then((r) => handle<void>(r))
  },
}
