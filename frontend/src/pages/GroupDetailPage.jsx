import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { groupsAPI, expensesAPI, settlementsAPI } from '../api'
import { useAuth } from '../context/AuthContext'
import {
  formatCurrency, formatDate, getCategoryEmoji,
  getGroupCategoryEmoji, getErrorMessage
} from '../utils'
import Avatar from '../components/shared/Avatar'
import Modal from '../components/shared/Modal'
import AddExpenseModal from '../components/expenses/AddExpenseModal'
import { ArrowLeft, Plus, UserPlus, Edit2, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'

export default function GroupDetailPage() {
  const { groupId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [summary, setSummary] = useState(null)
  const [expenses, setExpenses] = useState([])
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState('expenses')
  const [showAddExpense, setShowAddExpense] = useState(false)
  const [showAddMember, setShowAddMember] = useState(false)
  const [editExpense, setEditExpense] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [filterDate, setFilterDate] = useState('')

  const fetchData = useCallback(async () => {
    try {
      const [summaryRes, expensesRes] = await Promise.all([
        groupsAPI.getSummary(groupId),
        expensesAPI.getAll(groupId),
      ])
      setSummary(summaryRes.data.data)
      setExpenses(expensesRes.data.data || [])
    } catch (err) {
      toast.error('Failed to load group data')
    } finally {
      setLoading(false)
    }
  }, [groupId])

  useEffect(() => { fetchData() }, [fetchData])

  const handleDeleteExpense = async (expenseId) => {
    if (!confirm('Delete this expense?')) return
    try {
      await expensesAPI.delete(groupId, expenseId)
      setExpenses(prev => prev.filter(e => e.id !== expenseId))
      fetchData()
      toast.success('Expense deleted')
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  // Filter expenses by search and date
  const filteredExpenses = expenses.filter(expense => {
    const matchesSearch = !searchQuery ||
      expense.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      expense.paidBy?.name.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesDate = !filterDate ||
      expense.createdAt?.startsWith(filterDate)
    return matchesSearch && matchesDate
  })

  const myBalance = summary?.balances?.find(b => b.userId === user?.id)
  const group = summary?.group

  if (loading) return (
    <div className="loading-screen" style={{ minHeight: '100vh' }}>
      <div className="spinner" />
    </div>
  )

  if (!group) return (
    <div className="page-body"><p>Group not found.</p></div>
  )

  return (
    <>
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <button className="btn btn-ghost btn-sm"
            onClick={() => navigate('/groups')}
            style={{ padding: '6px 10px' }}>
            <ArrowLeft size={16} />
          </button>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ fontSize: '1.3rem' }}>
                {getGroupCategoryEmoji(group.category)}
              </span>
              <h2 className="page-title" style={{ fontSize: '1.5rem' }}>
                {group.name}
              </h2>
            </div>
            <p style={{ color: 'var(--ink-muted)', fontSize: '0.8rem', marginTop: 2 }}>
              {group.members?.length} members · {group.expenseCount || 0} expenses
            </p>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-ghost btn-sm"
            onClick={() => setShowAddMember(true)}>
            <UserPlus size={15} /> Add Member
          </button>
          <button className="btn btn-primary"
            onClick={() => setShowAddExpense(true)}>
            <Plus size={15} /> Add Expense
          </button>
        </div>
      </div>

      <div className="page-body">
        {/* Balance Banner */}
        {myBalance && (
          <div style={{
            padding: '16px 20px', borderRadius: 'var(--radius-md)',
            marginBottom: 24,
            background: myBalance.netBalance > 0 ? '#edf7f2'
              : myBalance.netBalance < 0 ? '#fdf0ed' : 'var(--cream-dark)',
            border: `1px solid ${myBalance.netBalance > 0 ? '#a8dfc2'
              : myBalance.netBalance < 0 ? '#f4bfae' : 'var(--ink-faint)'}`,
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          }}>
            <div>
              <div style={{ fontSize: '0.78rem', fontWeight: 600,
                letterSpacing: '0.08em', textTransform: 'uppercase',
                color: myBalance.netBalance > 0 ? 'var(--sage)'
                  : myBalance.netBalance < 0 ? 'var(--terracotta)' : 'var(--ink-muted)',
                marginBottom: 3 }}>
                Your Balance
              </div>
              <div style={{ fontFamily: 'var(--font-display)', fontSize: '1.8rem',
                color: myBalance.netBalance > 0 ? 'var(--sage)'
                  : myBalance.netBalance < 0 ? 'var(--danger)' : 'var(--ink)' }}>
                {myBalance.netBalance > 0 ? '+' : ''}
                {formatCurrency(myBalance.netBalance)}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--ink-muted)', marginTop: 2 }}>
                {myBalance.netBalance > 0 ? 'You are owed this amount'
                  : myBalance.netBalance < 0 ? 'You owe this amount'
                  : 'You are all settled up!'}
              </div>
            </div>
            <div style={{ textAlign: 'right', fontSize: '0.8rem', color: 'var(--ink-muted)' }}>
              <div>Paid: {formatCurrency(myBalance.totalPaid)}</div>
              <div>Share: {formatCurrency(myBalance.totalShare)}</div>
            </div>
          </div>
        )}

        {/* Tabs */}
        <div className="tab-bar">
          {[
            { id: 'expenses', label: '💳 Expenses' },
            { id: 'balances', label: '⚖️ Balances' },
            { id: 'settle', label: '✅ Settle Up' },
            { id: 'members', label: '👥 Members' },
          ].map(t => (
            <button key={t.id}
              className={`tab-btn${tab === t.id ? ' active' : ''}`}
              onClick={() => setTab(t.id)}>
              {t.label}
            </button>
          ))}
        </div>

        {/* EXPENSES TAB */}
        {tab === 'expenses' && (
          <div className="card">
            <div className="card-header">
              <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem' }}>
                Expense History
              </h3>
              <span style={{ fontSize: '0.8rem', color: 'var(--ink-muted)' }}>
                Total: {formatCurrency(summary?.totalGroupExpense || 0)}
              </span>
            </div>

            {/* Search & Filter Bar */}
            <div style={{ padding: '12px 24px',
              borderBottom: '1px solid var(--cream-dark)',
              display: 'flex', gap: 10, flexWrap: 'wrap' }}>
              <input
                className="form-input"
                placeholder="🔍 Search expenses..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                style={{ flex: 1, minWidth: 180,
                  padding: '8px 12px', fontSize: '0.85rem' }}
              />
              <input
                type="date"
                className="form-input"
                value={filterDate}
                onChange={e => setFilterDate(e.target.value)}
                style={{ width: 160, padding: '8px 12px', fontSize: '0.85rem' }}
              />
              {(searchQuery || filterDate) && (
                <button className="btn btn-ghost btn-sm"
                  onClick={() => { setSearchQuery(''); setFilterDate('') }}>
                  ✕ Clear
                </button>
              )}
            </div>

            <div className="card-body" style={{ padding: '0 24px' }}>
              {expenses.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📋</div>
                  <h3>No expenses yet</h3>
                  <p>Add your first expense to start tracking.</p>
                  <button className="btn btn-primary"
                    onClick={() => setShowAddExpense(true)}>
                    Add expense
                  </button>
                </div>
              ) : filteredExpenses.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">🔍</div>
                  <h3>No results found</h3>
                  <p>Try different search or clear filters.</p>
                  <button className="btn btn-ghost btn-sm"
                    onClick={() => { setSearchQuery(''); setFilterDate('') }}>
                    Clear filters
                  </button>
                </div>
              ) : (
                filteredExpenses.map(expense => (
                  <div key={expense.id} className="expense-item">
                    <div className="expense-icon">
                      {getCategoryEmoji(expense.category)}
                    </div>
                    <div className="expense-info">
                      <div className="expense-desc">{expense.description}</div>
                      <div className="expense-meta">
                        {expense.paidBy?.name} paid ·
                        {formatDate(expense.createdAt)} ·
                        <span className="category-chip" style={{ marginLeft: 4 }}>
                          {expense.splitType}
                        </span>
                      </div>
                    </div>
                    <div>
                      <div className="expense-amount">
                        {formatCurrency(expense.amount)}
                      </div>
                      <div className="expense-share">
                        {expense.participants?.find(p => p.userId === user?.id) &&
                          `Your share: ${formatCurrency(
                            expense.participants.find(p => p.userId === user?.id)?.shareAmount
                          )}`
                        }
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 4, flexShrink: 0 }}>
                      <button className="btn btn-ghost btn-sm"
                        style={{ padding: '5px' }}
                        onClick={() => setEditExpense(expense)}>
                        <Edit2 size={13} />
                      </button>
                      <button className="btn btn-ghost btn-sm"
                        style={{ padding: '5px', color: 'var(--danger)' }}
                        onClick={() => handleDeleteExpense(expense.id)}>
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* BALANCES TAB */}
        {tab === 'balances' && (
          <div className="card">
            <div className="card-header">
              <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem' }}>
                Member Balances
              </h3>
            </div>
            <div className="card-body" style={{ padding: '0 24px' }}>
              {summary?.balances?.map(b => (
                <div key={b.userId} style={{ display: 'flex', alignItems: 'center',
                  gap: 12, padding: '14px 0',
                  borderBottom: '1px solid var(--cream-dark)' }}>
                  <Avatar name={b.userName} color={b.avatarColor} size="md" />
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 500, fontSize: '0.9rem' }}>
                      {b.userName} {b.userId === user?.id ? '(you)' : ''}
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--ink-muted)' }}>
                      Paid {formatCurrency(b.totalPaid)} ·
                      Share {formatCurrency(b.totalShare)}
                    </div>
                  </div>
                  <div style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem',
                    color: b.netBalance > 0 ? 'var(--sage)'
                      : b.netBalance < 0 ? 'var(--danger)' : 'var(--ink-muted)' }}>
                    {b.netBalance > 0 ? '+' : ''}{formatCurrency(b.netBalance)}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* SETTLE TAB */}
        {tab === 'settle' && (
          <SettleTab
            groupId={groupId}
            debts={summary?.simplifiedDebts || []}
            currentUserId={user?.id}
            onSettled={fetchData}
            settlements={summary?.recentSettlements || []}
          />
        )}

        {/* MEMBERS TAB */}
        {tab === 'members' && (
          <div className="card">
            <div className="card-header">
              <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem' }}>
                Members ({group.members?.length})
              </h3>
              <button className="btn btn-ghost btn-sm"
                onClick={() => setShowAddMember(true)}>
                <UserPlus size={14} /> Add
              </button>
            </div>
            <div className="card-body" style={{ padding: '0 24px' }}>
              {group.members?.map(m => (
                <div key={m.id} style={{ display: 'flex', alignItems: 'center',
                  gap: 12, padding: '12px 0',
                  borderBottom: '1px solid var(--cream-dark)' }}>
                  <Avatar name={m.name} color={m.avatarColor} size="md" />
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 500 }}>
                      {m.name} {m.id === user?.id ? '(you)' : ''}
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--ink-muted)' }}>
                      {m.email}
                    </div>
                  </div>
                  {m.id === group.createdBy?.id && (
                    <span className="badge badge-info">Creator</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      <AddExpenseModal
        open={showAddExpense || !!editExpense}
        onClose={() => { setShowAddExpense(false); setEditExpense(null) }}
        groupId={groupId}
        members={group.members || []}
        currentUserId={user?.id}
        expenseToEdit={editExpense}
        onSaved={() => {
          fetchData()
          setShowAddExpense(false)
          setEditExpense(null)
        }}
      />

      <AddMemberModal
        open={showAddMember}
        onClose={() => setShowAddMember(false)}
        groupId={groupId}
        onAdded={fetchData}
      />
    </>
  )
}

function SettleTab({ groupId, debts, currentUserId, onSettled, settlements }) {
  const myDebts = debts.filter(d => d.fromUserId === currentUserId)
  const owedToMe = debts.filter(d => d.toUserId === currentUserId)
  const [settling, setSettling] = useState(null)

  const handleSettle = async (debt) => {
    setSettling(debt.toUserId)
    try {
      await settlementsAPI.create(groupId, {
        toUserId: debt.toUserId,
        amount: debt.amount,
        note: 'Settled via SplitMate',
      })
      toast.success(`Settled ${formatCurrency(debt.amount)} with ${debt.toUserName}!`)
      onSettled()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setSettling(null)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {debts.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-state-icon">🎉</div>
            <h3>All settled up!</h3>
            <p>No outstanding debts in this group.</p>
          </div>
        </div>
      ) : (
        <>
          {myDebts.length > 0 && (
            <div className="card">
              <div className="card-header">
                <h3 style={{ fontFamily: 'var(--font-display)',
                  fontSize: '1.1rem', color: 'var(--danger)' }}>
                  You Owe
                </h3>
              </div>
              <div className="card-body">
                {myDebts.map((d, i) => (
                  <div key={i} className="debt-item">
                    <Avatar name={d.toUserName} color={d.toAvatarColor} size="md" />
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 500 }}>{d.toUserName}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--ink-muted)' }}>
                        {d.toUserEmail}
                      </div>
                    </div>
                    <div className="debt-amount">{formatCurrency(d.amount)}</div>
                    <button className="btn btn-success btn-sm"
                      onClick={() => handleSettle(d)}
                      disabled={settling === d.toUserId}>
                      {settling === d.toUserId ? '…' : 'Settle Up'}
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {owedToMe.length > 0 && (
            <div className="card">
              <div className="card-header">
                <h3 style={{ fontFamily: 'var(--font-display)',
                  fontSize: '1.1rem', color: 'var(--sage)' }}>
                  Owed to You
                </h3>
              </div>
              <div className="card-body">
                {owedToMe.map((d, i) => (
                  <div key={i} className="debt-item">
                    <Avatar name={d.fromUserName} color={d.fromAvatarColor} size="md" />
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 500 }}>{d.fromUserName}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--ink-muted)' }}>
                        {d.fromUserEmail}
                      </div>
                    </div>
                    <div style={{ fontFamily: 'var(--font-display)',
                      fontSize: '1.15rem', color: 'var(--sage)' }}>
                      {formatCurrency(d.amount)}
                    </div>
                    <span className="badge badge-neutral">Pending</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {settlements.length > 0 && (
        <div className="card">
          <div className="card-header">
            <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem' }}>
              Recent Settlements
            </h3>
          </div>
          <div className="card-body" style={{ padding: '0 24px' }}>
            {settlements.map(s => (
              <div key={s.id} className="settlement-item">
                <Avatar name={s.fromUser?.name} color={s.fromUser?.avatarColor} size="sm" />
                <div style={{ flex: 1, fontSize: '0.875rem' }}>
                  <strong>{s.fromUser?.name}</strong> paid <strong>{s.toUser?.name}</strong>
                </div>
                <span style={{ fontFamily: 'var(--font-display)', color: 'var(--sage)' }}>
                  {formatCurrency(s.amount)}
                </span>
                <span className="badge badge-success">Settled</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

function AddMemberModal({ open, onClose, groupId, onAdded }) {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [addedMember, setAddedMember] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!email.trim()) return
    setLoading(true)

    try {
      await groupsAPI.addMember(groupId, {
        email: email.trim()
      })

      setAddedMember(email.trim())
      onAdded()
    } catch (err) {
      const errorMsg = getErrorMessage(err)

      if (errorMsg.includes('already a member')) {
        setAddedMember(email.trim())
      } else {
        toast.error(errorMsg)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setEmail('')
    setAddedMember(null)
    onClose()
  }

  if (addedMember) {
    return (
      <Modal
        open={open}
        onClose={handleClose}
        title="Member Added! 🎉"
        footer={
          <button className="btn btn-primary" onClick={handleClose}>
            Done
          </button>
        }
      >
        <div style={{ textAlign: 'center', padding: '20px 0' }}>
          <div style={{ fontSize: '3rem', marginBottom: 16 }}>✅</div>

          <h3 style={{ fontFamily: 'var(--font-display)', marginBottom: 8 }}>
            Member Added!
          </h3>

          <p style={{ color: 'var(--ink-muted)', fontSize: '0.9rem' }}>
            Email notification sent to <strong>{addedMember}</strong>
          </p>

          <p style={{ color: 'var(--ink-muted)', fontSize: '0.8rem', marginTop: 8 }}>
            They will receive login details in their inbox! 📧
          </p>
        </div>
      </Modal>
    )
  }

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title="Add Member"
      footer={
        <>
          <button className="btn btn-ghost" onClick={handleClose}>
            Cancel
          </button>

          <button
            className="btn btn-primary"
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? 'Adding…' : 'Add Member'}
          </button>
        </>
      }
    >
      <div className="form-group">
        <label className="form-label">Email address</label>

        <input
          className="form-input"
          type="email"
          placeholder="friend@example.com"
          value={email}
          onChange={e => setEmail(e.target.value)}
          autoFocus
        />

        <div
          style={{
            fontSize: '0.78rem',
            color: 'var(--ink-muted)',
            marginTop: 6
          }}
        >
          They will receive an email with login details automatically! 📧
        </div>
      </div>
    </Modal>
  )
}