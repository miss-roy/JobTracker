import { useCallback, useEffect, useState } from 'react'
import { api } from './api'
import { AddJobModal } from './components/AddJobModal'
import { JobList } from './components/JobList'
import { StatsPie } from './components/StatsPie'
import { TabBar } from './components/TabBar'
import { STATUS_META, type Job, type JobInput, type JobStatus, type StatusCount } from './types'

export default function App() {
  const [active, setActive] = useState<JobStatus>('APPLIED')
  const [jobs, setJobs] = useState<Job[]>([])
  const [stats, setStats] = useState<StatusCount[]>([])
  const [modalOpen, setModalOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Reload both the current tab's rows and the pie-chart counts.
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
    refresh(active)
  }, [active, refresh])

  const handleCreate = async (input: JobInput) => {
    await api.create(input)
    await refresh(active)
  }

  const handleDelete = async (id: number) => {
    await api.remove(id)
    await refresh(active)
  }

  return (
    <div className="app">
      <header className="app-head">
        <h1>Job Tracker</h1>
        <p style={{ color: STATUS_META[active].color }}>{STATUS_META[active].label}</p>
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
