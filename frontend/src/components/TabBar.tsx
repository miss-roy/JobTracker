import { STATUSES, STATUS_META, type JobStatus, type StatusCount } from '../types'

interface Props {
  active: JobStatus
  stats: StatusCount[]
  onSelect: (status: JobStatus) => void
}

/** The clickable status tabs. Each shows its count as a small badge. */
export function TabBar({ active, stats, onSelect }: Props) {
  const countFor = (status: JobStatus) =>
    stats.find((s) => s.status === status)?.count ?? 0

  return (
    <nav className="tabs" role="tablist">
      {STATUSES.map((status) => {
        const meta = STATUS_META[status]
        const isActive = status === active
        return (
          <button
            key={status}
            role="tab"
            aria-selected={isActive}
            className={`tab ${isActive ? 'tab--active' : ''}`}
            style={isActive ? { borderColor: meta.color, color: meta.color } : undefined}
            onClick={() => onSelect(status)}
          >
            {meta.label}
            <span className="tab-badge" style={{ background: meta.color }}>
              {countFor(status)}
            </span>
          </button>
        )
      })}
    </nav>
  )
}
