import { STATUS_META, type Job } from '../types'

interface Props {
  jobs: Job[]
  onDelete: (id: number) => void
}

/** Rows of companies for the currently selected tab. */
export function JobList({ jobs, onDelete }: Props) {
  if (jobs.length === 0) {
    return <p className="empty">Nothing here yet.</p>
  }

  return (
    <ul className="job-list">
      {jobs.map((job) => (
        <li key={job.id} className="job-row">
          <span
            className="job-dot"
            style={{ background: STATUS_META[job.status].color }}
          />
          <div className="job-main">
            <strong>{job.company}</strong>
            <small>
              Applied {job.dateApplied}
              {job.hrContact ? ` · ${job.hrContact}` : ''}
            </small>
          </div>
          <button
            className="job-delete"
            aria-label={`Delete ${job.company}`}
            onClick={() => onDelete(job.id)}
          >
            ✕
          </button>
        </li>
      ))}
    </ul>
  )
}
