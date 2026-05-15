import { useState, useEffect } from 'react'
import { expensesAPI } from '../../api'
import { EXPENSE_CATEGORIES, getErrorMessage } from '../../utils'
import Avatar from '../shared/Avatar'
import Modal from '../shared/Modal'
import toast from 'react-hot-toast'

const SPLIT_TYPES = [
  { value: 'EQUAL', label: 'Equal Split' },
  { value: 'EXACT', label: 'Exact Amounts' },
  { value: 'PERCENTAGE', label: 'By Percentage' },
]

export default function AddExpenseModal({
  open, onClose, groupId, members,
  currentUserId, expenseToEdit, onSaved
}) {
  const isEdit = !!expenseToEdit
  const [form, setForm] = useState({
    description: '', amount: '',
    paidById: currentUserId, splitType: 'EQUAL',
    category: 'OTHER',
    participantIds: members.map(m => m.id),
    exactSplits: {}, percentageSplits: {},
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (expenseToEdit) {
      const participantIds = expenseToEdit.participants?.map(p => p.userId)
        || members.map(m => m.id)
      const exactSplits = {}
      const percentageSplits = {}
      expenseToEdit.participants?.forEach(p => {
        exactSplits[p.userId] = p.shareAmount
        if (p.percentage) percentageSplits[p.userId] = p.percentage
      })
      setForm({
        description: expenseToEdit.description || '',
        amount: expenseToEdit.amount?.toString() || '',
        paidById: expenseToEdit.paidBy?.id || currentUserId,
        splitType: expenseToEdit.splitType || 'EQUAL',
        category: expenseToEdit.category || 'OTHER',
        participantIds, exactSplits, percentageSplits,
      })
    } else {
      setForm(f => ({
        ...f, description: '', amount: '',
        paidById: currentUserId, splitType: 'EQUAL',
        category: 'OTHER', participantIds: members.map(m => m.id),
        exactSplits: {}, percentageSplits: {},
      }))
    }
  }, [expenseToEdit, open])

  const toggleParticipant = (userId) => {
    setForm(f => ({
      ...f,
      participantIds: f.participantIds.includes(userId)
        ? f.participantIds.filter(id => id !== userId)
        : [...f.participantIds, userId]
    }))
  }

  const equalShare = () => {
    const amt = parseFloat(form.amount) || 0
    const count = form.participantIds.length
    return count > 0 ? (amt / count).toFixed(2) : '0.00'
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.description.trim()) return toast.error('Description is required')
    const amount = parseFloat(form.amount)
    if (!amount || amount <= 0) return toast.error('Valid amount is required')
    if (form.participantIds.length === 0)
      return toast.error('Select at least one participant')

    if (form.splitType === 'EXACT') {
      const total = Object.values(form.exactSplits)
        .reduce((s, v) => s + parseFloat(v || 0), 0)
      if (Math.abs(total - amount) > 0.01)
        return toast.error(`Exact splits must sum to ${amount}`)
    }

    if (form.splitType === 'PERCENTAGE') {
      const total = Object.values(form.percentageSplits)
        .reduce((s, v) => s + parseFloat(v || 0), 0)
      if (Math.abs(total - 100) > 0.01)
        return toast.error(`Percentages must sum to 100`)
    }

    setLoading(true)
    try {
      const payload = {
        description: form.description.trim(),
        amount, paidById: form.paidById,
        splitType: form.splitType, category: form.category,
        participantIds: form.splitType === 'EQUAL' ? form.participantIds : undefined,
        exactSplits: form.splitType === 'EXACT'
          ? Object.fromEntries(Object.entries(form.exactSplits)
              .map(([k, v]) => [k, parseFloat(v)])) : undefined,
        percentageSplits: form.splitType === 'PERCENTAGE'
          ? Object.fromEntries(Object.entries(form.percentageSplits)
              .map(([k, v]) => [k, parseFloat(v)])) : undefined,
      }

      if (isEdit) {
        await expensesAPI.update(groupId, expenseToEdit.id, payload)
        toast.success('Expense updated!')
      } else {
        await expensesAPI.create(groupId, payload)
        toast.success('Expense added!')
      }
      onSaved()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      open={open} onClose={onClose}
      title={isEdit ? 'Edit Expense' : 'Add Expense'}
      maxWidth={560}
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Saving…' : isEdit ? 'Update' : 'Add Expense'}
          </button>
        </>
      }
    >
      <div className="form-group">
        <label className="form-label">Description *</label>
        <input className="form-input" placeholder="Dinner at restaurant"
          value={form.description}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div className="form-group">
          <label className="form-label">Amount (₹) *</label>
          <input className="form-input" type="number" step="0.01" min="0.01"
            placeholder="1500.00" value={form.amount}
            onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
        </div>
        <div className="form-group">
          <label className="form-label">Category</label>
          <select className="form-select" value={form.category}
            onChange={e => setForm(f => ({ ...f, category: e.target.value }))}>
            {EXPENSE_CATEGORIES.map(c => (
              <option key={c.value} value={c.value}>{c.emoji} {c.label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Paid by</label>
        <select className="form-select" value={form.paidById}
          onChange={e => setForm(f => ({ ...f, paidById: parseInt(e.target.value) }))}>
          {members.map(m => (
            <option key={m.id} value={m.id}>
              {m.name}{m.id === currentUserId ? ' (you)' : ''}
            </option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">Split Type</label>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {SPLIT_TYPES.map(st => (
            <button key={st.value} type="button"
              style={{
                padding: '8px 14px', borderRadius: 'var(--radius-sm)',
                border: '1.5px solid',
                borderColor: form.splitType === st.value
                  ? 'var(--terracotta)' : 'var(--cream-dark)',
                background: form.splitType === st.value
                  ? 'rgba(201,98,63,0.06)' : 'transparent',
                color: form.splitType === st.value
                  ? 'var(--terracotta-dark)' : 'var(--ink-muted)',
                fontSize: '0.82rem', fontWeight: 500,
                cursor: 'pointer', fontFamily: 'var(--font-body)',
              }}
              onClick={() => setForm(f => ({ ...f, splitType: st.value }))}>
              {st.label}
            </button>
          ))}
        </div>
      </div>

      {form.splitType === 'EQUAL' && (
        <div className="form-group">
          <label className="form-label">Split Among</label>
          <div className="checkbox-group">
            {members.map(m => (
              <div key={m.id}
                className={`member-checkbox${form.participantIds.includes(m.id) ? ' selected' : ''}`}
                onClick={() => toggleParticipant(m.id)}>
                <Avatar name={m.name} color={m.avatarColor} size="sm" />
                <span style={{ fontSize: '0.85rem' }}>
                  {m.name}{m.id === currentUserId ? ' (you)' : ''}
                </span>
              </div>
            ))}
          </div>
          {form.participantIds.length > 0 && form.amount && (
            <div style={{ fontSize: '0.8rem', color: 'var(--ink-muted)', marginTop: 8 }}>
              Each pays: ₹{equalShare()}
            </div>
          )}
        </div>
      )}

      {form.splitType === 'EXACT' && (
        <div className="form-group">
          <label className="form-label">Exact Amounts</label>
          {members.map(m => (
            <div key={m.id} style={{ display: 'flex', alignItems: 'center',
              gap: 10, marginBottom: 8 }}>
              <Avatar name={m.name} color={m.avatarColor} size="sm" />
              <span style={{ flex: 1, fontSize: '0.875rem' }}>{m.name}</span>
              <input className="form-input" type="number" step="0.01"
                min="0" placeholder="0.00" style={{ width: 110 }}
                value={form.exactSplits[m.id] || ''}
                onChange={e => setForm(f => ({
                  ...f, exactSplits: { ...f.exactSplits, [m.id]: e.target.value }
                }))} />
            </div>
          ))}
          <div style={{ fontSize: '0.8rem', color: 'var(--ink-muted)', marginTop: 4 }}>
            Sum: ₹{Object.values(form.exactSplits)
              .reduce((s, v) => s + (parseFloat(v) || 0), 0).toFixed(2)}
            {' '}/ ₹{parseFloat(form.amount || 0).toFixed(2)}
          </div>
        </div>
      )}

      {form.splitType === 'PERCENTAGE' && (
        <div className="form-group">
          <label className="form-label">Percentages</label>
          {members.map(m => (
            <div key={m.id} style={{ display: 'flex', alignItems: 'center',
              gap: 10, marginBottom: 8 }}>
              <Avatar name={m.name} color={m.avatarColor} size="sm" />
              <span style={{ flex: 1, fontSize: '0.875rem' }}>{m.name}</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <input className="form-input" type="number" step="0.01"
                  min="0" max="100" placeholder="0" style={{ width: 80 }}
                  value={form.percentageSplits[m.id] || ''}
                  onChange={e => setForm(f => ({
                    ...f, percentageSplits: {
                      ...f.percentageSplits, [m.id]: e.target.value }
                  }))} />
                <span style={{ fontSize: '0.85rem', color: 'var(--ink-muted)' }}>%</span>
              </div>
            </div>
          ))}
          <div style={{ fontSize: '0.8rem', color: 'var(--ink-muted)', marginTop: 4 }}>
            Total: {Object.values(form.percentageSplits)
              .reduce((s, v) => s + (parseFloat(v) || 0), 0).toFixed(1)}% / 100%
          </div>
        </div>
      )}
    </Modal>
  )
}