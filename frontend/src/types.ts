// Mirrors the backend enum (JobStatus.java) and DTOs.

export const STATUSES = [
  'APPLIED',
  'INTERVIEWING',
  'OFFERED',
  'REJECTED',
  'GHOSTED',
] as const

export type JobStatus = (typeof STATUSES)[number]

export interface Job {
  id: number
  company: string
  status: JobStatus
  dateApplied: string // ISO yyyy-MM-dd
  hrContact: string | null
}

export interface JobInput {
  company: string
  status: JobStatus
  dateApplied: string
  hrContact: string
}

export interface StatusCount {
  status: JobStatus
  count: number
}

// A friendly label + colour per status, reused by tabs and the pie chart.
export const STATUS_META: Record<JobStatus, { label: string; color: string }> = {
  APPLIED: { label: 'Applied', color: '#3b82f6' },
  INTERVIEWING: { label: 'Interviewing', color: '#f59e0b' },
  OFFERED: { label: 'Offered', color: '#22c55e' },
  REJECTED: { label: 'Rejected', color: '#ef4444' },
  GHOSTED: { label: 'Ghosted', color: '#8b5cf6' },
}
