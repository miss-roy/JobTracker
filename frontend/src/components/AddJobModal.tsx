import { useState } from 'react'
import { STATUS_META, type JobInput, type JobStatus } from '../types'

interface Props {
  status: JobStatus // the tab we're adding into
  onClose: () => void
  onSubmit: (input: JobInput) => Promise<void>
}

const today = () => new Date().toISOString().slice(0, 10)

/** Popup form: company, date applied, HR contact. Status comes from the active tab. */
export function AddJobModal({ status, onClose, onSubmit }: Props) {
  const [company, setCompany] = useState('')
  const [dateApplied, setDateApplied] = useState(today())
  const [hrContact, setHrContact] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await onSubmit({ company, status, dateApplied, hrContact })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong')
      setSaving(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <header className="modal-head">
          <h2>Add to {STATUS_META[status].label}</h2>
          <button className="modal-x" onClick={onClose} aria-label="Close">✕</button>
        </header>

        <form onSubmit={submit} className="modal-form">
          <label>
            Company
            <input
              autoFocus
              required
              value={company}
              onChange={(e) => setCompany(e.target.value)}
              placeholder="e.g. Acme Corp"
            />
          </label>

          <label>
            Date applied
            <input
              type="date"
              required
              value={dateApplied}
              onChange={(e) => setDateApplied(e.target.value)}
            />
          </label>

          <label>
            HR contact
            <input
              value={hrContact}
              onChange={(e) => setHrContact(e.target.value)}
              placeholder="e.g. jane@acme.com"
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? 'Saving…' : 'Save application'}
          </button>
        </form>
      </div>
    </div>
  )
}
