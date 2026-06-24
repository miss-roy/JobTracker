import { useEffect, useRef, useState } from 'react'
import { STATUSES, STATUS_META, type JobStatus, type StatusCount } from '../types'

interface Props {
  active: JobStatus
  stats: StatusCount[]
  onSelect: (status: JobStatus) => void
}

/** The clickable status tabs. Each shows its count as a small badge.
 *  The row scrolls horizontally; a fade on the right/left edge signals when
 *  there are more tabs to scroll to (and hides once that edge is reached). */
export function TabBar({ active, stats, onSelect }: Props) {
  const scrollerRef = useRef<HTMLElement>(null)
  const [atStart, setAtStart] = useState(true)
  const [atEnd, setAtEnd] = useState(true)

  const updateEdges = () => {
    const el = scrollerRef.current
    if (!el) return
    setAtStart(el.scrollLeft <= 0)
    setAtEnd(el.scrollLeft + el.clientWidth >= el.scrollWidth - 1)
  }

  // Recompute on mount, when the tab data changes, and on resize.
  useEffect(() => {
    updateEdges()
    window.addEventListener('resize', updateEdges)
    return () => window.removeEventListener('resize', updateEdges)
  }, [stats])

  const countFor = (status: JobStatus) =>
    stats.find((s) => s.status === status)?.count ?? 0

  return (
    <div className={`tabs-wrap ${atStart ? '' : 'fade-left'} ${atEnd ? '' : 'fade-right'}`}>
      <nav className="tabs" role="tablist" ref={scrollerRef} onScroll={updateEdges}>
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
    </div>
  )
}
