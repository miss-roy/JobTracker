import { useCallback, useEffect, useState } from 'react'
import { api, auth, getToken, getUser, setUnauthorizedHandler } from './api'
import { AddJobModal } from './components/AddJobModal'
import { JobList } from './components/JobList'
import { Login } from './components/Login'
import { StatsPie } from './components/StatsPie'
import { TabBar } from './components/TabBar'
import { STATUS_META, type Job, type JobInput, type JobStatus, type StatusCount } from './types'

export default function App() {
  const [user, setUser] = useState<string | null>(getToken() ? getUser() : null)
  const [active, setActive] = useState<JobStatus>('APPLIED')
  const [jobs, setJobs] = useState<Job[]>([])
  const [stats, setStats] = useState<StatusCount[]>([])
  const [modalOpen, setModalOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // If a token expires mid-session, the API layer calls this to log us out.
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null))
  }, [])

  const refresh = useCallback(async (status: JobStatus) => {
    try {
      const [list, counts] = await Promise.all([api.list(status), api.stats()])
      setJobs(list)
      setStats(counts)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load')
    }
  }, [])

  useEffect(() => {
    if (user) refresh(active)
  }, [user, active, refresh])

  if (!user) {
    return <Login onAuth={setUser} />
  }

  const handleCreate = async (input: JobInput) => {
    await api.create(input)
    await refresh(active)
  }

  const handleDelete = async (id: number) => {
    await api.remove(id)
    await refresh(active)
  }

  const logout = () => {
    auth.logout()
    setUser(null)
  }

  return (
    <div className="app">
      <header className="app-head">
        <div className="app-head-row">
          <h1>Job Tracker</h1>
          <button className="logout-btn" onClick={logout}>Log out</button>
        </div>
        <p style={{ color: STATUS_META[active].color }}>
          {STATUS_META[active].label} <span className="whoami">· {user}</span>
        </p>
      </header>

      {error && <p className="form-error banner">{error}</p>}

      <section className="card">
        <StatsPie stats={stats} />
      </section>

      <TabBar active={active} stats={stats} onSelect={setActive} />

      <section className="card list-card">
        <div className="list-head">
          <h2>{STATUS_META[active].label} companies</h2>
          <button
            className="fab"
            aria-label="Add application"
            onClick={() => setModalOpen(true)}
          >
            +
          </button>
        </div>
        <JobList jobs={jobs} onDelete={handleDelete} />
      </section>

      {modalOpen && (
        <AddJobModal
          status={active}
          onClose={() => setModalOpen(false)}
          onSubmit={handleCreate}
        />
      )}
    </div>
  )
}
