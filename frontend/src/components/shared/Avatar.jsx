export default function Avatar({ name = '?', color = '#6366f1', size = 'md', style = {} }) {
  const initial = name?.charAt(0)?.toUpperCase() || '?'
  return (
    <div
      className={`avatar avatar-${size}`}
      style={{ background: color, ...style }}
      title={name}
    >
      {initial}
    </div>
  )
}