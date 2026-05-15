import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { groupsAPI } from '../api'
import { formatCurrency, getGroupCategoryEmoji } from '../utils'
import Avatar from '../components/shared/Avatar'
import { Plus, Users, Receipt } from 'lucide-react'
import toast from 'react-hot-toast'

export default function DashboardPage() {
  const { user } = useAuth()
  const [groups, setGroups] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    groupsAPI.getAll()
      .then(res => setGroups(res.data.data || []))
      .catch(() => toast.error('Failed to load groups'))
      .finally(() => setLoading(false))
  }, [])

  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning'
    : hour < 17 ? 'Good afternoon' : 'Good evening'

  return (
    <>
      <div className="page-header">
        <div>
          <h2 className="page-title">
            {greeting}, {user?.name?.split(' ')[0]}.
          </h2>
          <p style={{ color: 'var(--ink-muted)', fontSize: '0.875rem', marginTop: 2 }}>
            Here's your expense overview
          </p>
        </div>
        <Link to="/groups" className="btn btn-primary">
          <Plus size={16} /> New Group
        </Link>
      </div>

      <div className="page-body">
        {/* Stats */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">Total Groups</div>
            <div className="stat-value">{groups.length}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Expenses</div>
            <div className="stat-value">
              {groups.reduce((sum, g) => sum + (g.expenseCount || 0), 0)}
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Spent</div>
            <div className="stat-value">
              {formatCurrency(groups.reduce((sum, g) =>
                sum + parseFloat(g.totalAmount || 0), 0))}
            </div>
          </div>
        </div>

        {/* Groups */}
        <div style={{ display: 'flex', alignItems: 'center',
          justifyContent: 'space-between', marginBottom: 16 }}>
          <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.3rem' }}>
            Your Groups
          </h3>
          <Link to="/groups" className="btn btn-ghost btn-sm">View all</Link>
        </div>

        {loading ? (
          <div className="loading-screen"><div className="spinner" /></div>
        ) : groups.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">👥</div>
            <h3>No groups yet</h3>
            <p>Create your first group to start splitting expenses.</p>
            <Link to="/groups" className="btn btn-primary">
              Create a group
            </Link>
          </div>
        ) : (
          <div className="groups-grid">
            {groups.slice(0, 6).map(group => (
              <Link key={group.id} to={`/groups/${group.id}`} className="group-card">
                <div style={{ display: 'flex', alignItems: 'center',
                  gap: 10, marginBottom: 10 }}>
                  <span style={{ fontSize: '1.5rem' }}>
                    {getGroupCategoryEmoji(group.category)}
                  </span>
                  <div>
                    <div className="group-card-name">{group.name}</div>
                    <span className="category-chip">{group.category}</span>
                  </div>
                </div>

                <div className="group-card-meta">
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Users size={12} /> {group.members?.length || 0} members
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Receipt size={12} /> {group.expenseCount || 0} expenses
                  </span>
                </div>

                <div className="group-card-footer">
                  <div className="member-stack">
                    {group.members?.slice(0, 4).map(m => (
                      <Avatar key={m.id} name={m.name}
                        color={m.avatarColor} size="sm" />
                    ))}
                    {group.members?.length > 4 && (
                      <div className="avatar avatar-sm"
                        style={{ background: 'var(--ink-faint)', fontSize: '0.6rem' }}>
                        +{group.members.length - 4}
                      </div>
                    )}
                  </div>
                  <span style={{ fontFamily: 'var(--font-display)', fontSize: '1rem' }}>
                    {formatCurrency(group.totalAmount || 0)}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </>
  )
}