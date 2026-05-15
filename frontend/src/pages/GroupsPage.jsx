import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { groupsAPI } from '../api'
import {
  formatCurrency, getGroupCategoryEmoji,
  GROUP_CATEGORIES, getErrorMessage
} from '../utils'
import Avatar from '../components/shared/Avatar'
import Modal from '../components/shared/Modal'
import { Plus, Users, Receipt, X } from 'lucide-react'
import toast from 'react-hot-toast'

export default function GroupsPage() {
  const [groups, setGroups] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)

  const fetchGroups = () => {
    groupsAPI.getAll()
      .then(res => setGroups(res.data.data || []))
      .catch(() => toast.error('Failed to load groups'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchGroups() }, [])

  const handleCreated = (group) => {
    setGroups(prev => [group, ...prev])
    setShowCreate(false)
    toast.success(`Group "${group.name}" created!`)
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h2 className="page-title">My Groups</h2>
          <p style={{ color: 'var(--ink-muted)', fontSize: '0.875rem', marginTop: 2 }}>
            {groups.length} group{groups.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
          <Plus size={16} /> New Group
        </button>
      </div>

      <div className="page-body">
        {loading ? (
          <div className="loading-screen"><div className="spinner" /></div>
        ) : groups.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🌍</div>
            <h3>No groups yet</h3>
            <p>Create your first group for a trip, meal, or shared expense.</p>
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              Create your first group
            </button>
          </div>
        ) : (
          <div className="groups-grid">
            {groups.map(group => (
              <Link key={group.id} to={`/groups/${group.id}`} className="group-card">
                <div style={{ display: 'flex', alignItems: 'center',
                  gap: 10, marginBottom: 12 }}>
                  <span style={{ fontSize: '1.6rem' }}>
                    {getGroupCategoryEmoji(group.category)}
                  </span>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div className="group-card-name">{group.name}</div>
                    {group.description && (
                      <div style={{ fontSize: '0.78rem', color: 'var(--ink-muted)',
                        marginTop: 2, overflow: 'hidden',
                        textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {group.description}
                      </div>
                    )}
                  </div>
                </div>

                <div className="group-card-meta">
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Users size={12} /> {group.members?.length || 0} members
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Receipt size={12} /> {group.expenseCount || 0} expenses
                  </span>
                  <span className="category-chip">{group.category}</span>
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
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontFamily: 'var(--font-display)', fontSize: '1rem' }}>
                      {formatCurrency(group.totalAmount || 0)}
                    </div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--ink-muted)' }}>
                      total spent
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      <CreateGroupModal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        onCreated={handleCreated}
      />
    </>
  )
}

function CreateGroupModal({ open, onClose, onCreated }) {
  const [form, setForm] = useState({
    name: '', description: '', category: 'OTHER', memberEmails: ['']
  })
  const [loading, setLoading] = useState(false)

  const addEmailField = () =>
    setForm(f => ({ ...f, memberEmails: [...f.memberEmails, ''] }))

  const removeEmailField = (i) =>
    setForm(f => ({ ...f, memberEmails: f.memberEmails.filter((_, idx) => idx !== i) }))

  const updateEmail = (i, val) =>
    setForm(f => ({ ...f, memberEmails: f.memberEmails.map((e, idx) => idx === i ? val : e) }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) return toast.error('Group name is required')
    setLoading(true)
    try {
      const validEmails = form.memberEmails.filter(e =>
        e.trim() && /\S+@\S+\.\S+/.test(e.trim()))
      const res = await groupsAPI.create({
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        category: form.category,
        memberEmails: validEmails,
      })
      onCreated(res.data.data)
      setForm({ name: '', description: '', category: 'OTHER', memberEmails: [''] })
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Create New Group"
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Creating…' : 'Create Group'}
          </button>
        </>
      }
    >
      <div className="form-group">
        <label className="form-label">Group Name *</label>
        <input className="form-input" placeholder="Goa Trip 2025"
          value={form.name}
          onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
          autoFocus />
      </div>

      <div className="form-group">
        <label className="form-label">Description</label>
        <input className="form-input" placeholder="Optional description"
          value={form.description}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
      </div>

      <div className="form-group">
        <label className="form-label">Category</label>
        <select className="form-select" value={form.category}
          onChange={e => setForm(f => ({ ...f, category: e.target.value }))}>
          {GROUP_CATEGORIES.map(c => (
            <option key={c.value} value={c.value}>{c.emoji} {c.label}</option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">Invite Members by Email</label>
        <p style={{ fontSize: '0.78rem', color: 'var(--ink-muted)', marginBottom: 10 }}>
          New users will get auto-created with password: Splitmate@123
        </p>
        {form.memberEmails.map((email, i) => (
          <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
            <input className="form-input" type="email"
              placeholder="friend@example.com"
              value={email}
              onChange={e => updateEmail(i, e.target.value)} />
            {form.memberEmails.length > 1 && (
              <button type="button" className="btn btn-ghost btn-sm"
                onClick={() => removeEmailField(i)}>
                <X size={14} />
              </button>
            )}
          </div>
        ))}
        <button type="button" className="btn btn-ghost btn-sm"
          onClick={addEmailField}>
          <Plus size={14} /> Add another
        </button>
      </div>
    </Modal>
  )
}