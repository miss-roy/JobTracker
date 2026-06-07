import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { STATUS_META, type StatusCount } from '../types'

interface Props {
  stats: StatusCount[]
}

/** Pie chart of application counts per status. Zero-count slices are dropped. */
export function StatsPie({ stats }: Props) {
  const data = stats
    .filter((s) => s.count > 0)
    .map((s) => ({
      name: STATUS_META[s.status].label,
      value: s.count,
      color: STATUS_META[s.status].color,
    }))

  const total = data.reduce((sum, d) => sum + d.value, 0)

  if (total === 0) {
    return <p className="empty">No applications yet — tap + to add one.</p>
  }

  return (
    <div className="pie-wrap">
      <ResponsiveContainer width="100%" height={240}>
        <PieChart>
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            innerRadius={55}
            outerRadius={90}
            paddingAngle={2}
          >
            {data.map((d) => (
              <Cell key={d.name} fill={d.color} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
      <div className="pie-total">
        <span>{total}</span>
        <small>total</small>
      </div>
    </div>
  )
}
