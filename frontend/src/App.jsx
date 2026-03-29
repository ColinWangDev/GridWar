import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import './App.css'
import {
  cellAction,
  getGrid,
  getOverallLeaderboard,
  getPlayerStatus,
  getSeason,
  startPlayer,
  verifyCaptcha,
} from './api.js'

const STORAGE_ID = 'gridwar_player_id'
const STORAGE_NAME = 'gridwar_nickname'
const COOLDOWN_MS = 3000

function colorForOwner(ownerId) {
  if (!ownerId) return '#3d4454'
  let h = 0
  for (let i = 0; i < ownerId.length; i++) {
    h = ownerId.charCodeAt(i) + ((h << 5) - h)
  }
  const hue = Math.abs(h) % 360
  return `hsl(${hue} 50% 42%)`
}

function formatClock(seconds) {
  const s = Math.max(0, Math.floor(seconds))
  const m = Math.floor(s / 60)
  const r = s % 60
  return `${m}:${r.toString().padStart(2, '0')}`
}

function formatSeasonCountdownDhm(iso) {
  if (!iso) return '—'
  const ms = Math.max(0, new Date(iso).getTime() - Date.now())
  const d = Math.floor(ms / 86400000)
  const h = Math.floor((ms % 86400000) / 3600000)
  const min = Math.floor((ms % 3600000) / 60000)
  return `${d} D ${h} H ${min} M`
}

function TaglineBar({ compact = false }) {
  return (
    <header
      className={`tagline-bar ${compact ? 'tagline-bar--compact' : ''}`}
      aria-label="Slogan"
    >
      <span className="tagline-mark" aria-hidden="true" />
      <p className="tagline-text">Own Grid, Earn More</p>
    </header>
  )
}

export default function App() {
  const [playerId, setPlayerId] = useState(() => localStorage.getItem(STORAGE_ID) || '')
  const [nickname, setNickname] = useState(() => localStorage.getItem(STORAGE_NAME) || '')
  const [joinName, setJoinName] = useState('')
  const [joinError, setJoinError] = useState('')
  const [grid, setGrid] = useState(null)
  const [status, setStatus] = useState(null)
  const [season, setSeason] = useState(null)
  const [overall, setOverall] = useState(null)
  const [message, setMessage] = useState('')
  const [captchaOpen, setCaptchaOpen] = useState(false)
  const [captchaInput, setCaptchaInput] = useState('')
  const lastActionRef = useRef(0)
  const [cooldownUntil, setCooldownUntil] = useState(0)
  const [, setCountdownTick] = useState(0)

  const cellMap = useMemo(() => {
    const m = new Map()
    if (!grid?.cells) return m
    for (const c of grid.cells) {
      m.set(`${c.x},${c.y}`, c)
    }
    return m
  }, [grid])

  const tickCooldown = useCallback(() => {
    const left = Math.max(0, cooldownUntil - Date.now())
    if (left <= 0 && cooldownUntil > 0) setCooldownUntil(0)
  }, [cooldownUntil])

  useEffect(() => {
    const t = setInterval(tickCooldown, 200)
    return () => clearInterval(t)
  }, [tickCooldown])

  const refreshGrid = useCallback(async () => {
    try {
      const g = await getGrid()
      setGrid(g)
    } catch {
      /* ignore poll errors */
    }
  }, [])

  const refreshStatus = useCallback(async () => {
    if (!playerId) return
    try {
      const s = await getPlayerStatus(playerId)
      setStatus(s)
    } catch {
      setStatus(null)
    }
  }, [playerId])

  const refreshSeason = useCallback(async () => {
    try {
      const s = await getSeason()
      setSeason(s)
    } catch {
      /* ignore */
    }
  }, [])

  const refreshOverall = useCallback(async () => {
    try {
      const o = await getOverallLeaderboard()
      setOverall(o)
    } catch {
      /* ignore */
    }
  }, [])

  useEffect(() => {
    refreshGrid()
    const id = setInterval(refreshGrid, 2500)
    return () => clearInterval(id)
  }, [refreshGrid])

  useEffect(() => {
    refreshSeason()
    const id = setInterval(refreshSeason, 4000)
    return () => clearInterval(id)
  }, [refreshSeason])

  useEffect(() => {
    refreshOverall()
    const id = setInterval(refreshOverall, 5000)
    return () => clearInterval(id)
  }, [refreshOverall])

  useEffect(() => {
    refreshStatus()
    const id = setInterval(refreshStatus, 3000)
    return () => clearInterval(id)
  }, [refreshStatus])

  useEffect(() => {
    const id = setInterval(() => setCountdownTick((n) => n + 1), 10000)
    return () => clearInterval(id)
  }, [])

  const onJoin = async (e) => {
    e.preventDefault()
    setJoinError('')
    const name = joinName.trim()
    if (!name) {
      setJoinError('Enter a nickname.')
      return
    }
    try {
      const res = await startPlayer(name)
      localStorage.setItem(STORAGE_ID, res.playerId)
      localStorage.setItem(STORAGE_NAME, res.nickname)
      setPlayerId(res.playerId)
      setNickname(res.nickname)
    } catch (err) {
      setJoinError(err.message || 'Could not join.')
    }
  }

  const leaveSession = () => {
    localStorage.removeItem(STORAGE_ID)
    localStorage.removeItem(STORAGE_NAME)
    setPlayerId('')
    setNickname('')
    setStatus(null)
  }

  const beginCooldown = () => {
    const until = Date.now() + COOLDOWN_MS
    lastActionRef.current = until
    setCooldownUntil(until)
  }

  const onCellClick = async (x, y) => {
    if (!playerId) return
    if (status?.needsCaptcha) {
      setCaptchaOpen(true)
      setMessage('Complete verification to continue.')
      return
    }
    if (Date.now() < lastActionRef.current) {
      setMessage('Wait for the action cooldown.')
      return
    }
    if (!status || status.energy <= 0) {
      setMessage('No energy. Wait for regeneration.')
      return
    }
    beginCooldown()
    try {
      const res = await cellAction(playerId, x, y)
      setMessage(
        res.outcome === 'captured'
          ? 'Captured +10'
          : res.success
            ? 'Attack won +20'
            : 'Attack failed +2',
      )
      await Promise.all([refreshGrid(), refreshStatus(), refreshSeason()])
      if (res.needsCaptcha) {
        setMessage((m) => m + ' (verification soon)')
      }
    } catch (err) {
      if (err.status === 429 && err.message === 'needs_captcha') {
        setCaptchaOpen(true)
        setMessage('Too many actions — verify to continue.')
      } else if (err.status === 429 && err.message === 'cooldown') {
        setMessage('Server cooldown — wait a moment.')
      } else if (err.status === 409 && err.message === 'no_energy') {
        setMessage('No energy left.')
      } else if (err.status === 409 && err.message === 'already_owned') {
        setMessage('You already own this cell.')
      } else {
        setMessage(err.message || 'Action failed.')
      }
    }
  }

  const submitCaptcha = async (e) => {
    e.preventDefault()
    try {
      await verifyCaptcha(playerId, captchaInput)
      setCaptchaOpen(false)
      setCaptchaInput('')
      setMessage('Verified. Play on.')
      await refreshStatus()
    } catch {
      setMessage('Wrong answer. Hint: game name, lowercase.')
    }
  }

  const size = grid?.size ?? 30
  const inCooldown = Date.now() < cooldownUntil
  const cursorClass = inCooldown ? 'grid--wait' : 'grid--play'

  if (!playerId) {
    return (
      <div className="shell">
        <TaglineBar />
        <div className="card join">
          <h1>GridWar</h1>
          <p className="muted">Enter a nickname to play as a guest.</p>
          <form onSubmit={onJoin}>
            <input
              className="input"
              placeholder="Nickname"
              value={joinName}
              onChange={(e) => setJoinName(e.target.value)}
              maxLength={64}
              autoFocus
            />
            <button type="submit" className="btn primary">
              Play
            </button>
          </form>
          {joinError ? <p className="error">{joinError}</p> : null}
        </div>
      </div>
    )
  }

  const energyPct = status
    ? Math.round((100 * status.energy) / Math.max(1, status.maxEnergy))
    : 0

  return (
    <div className="shell game">
      <TaglineBar />
      {captchaOpen ? (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal">
            <TaglineBar compact />
            <h2>Quick check</h2>
            <p className="muted">Type the game name in lowercase to continue.</p>
            <form onSubmit={submitCaptcha}>
              <input
                className="input"
                value={captchaInput}
                onChange={(e) => setCaptchaInput(e.target.value)}
                autoFocus
              />
              <div className="modal-actions">
                <button type="submit" className="btn primary">
                  Submit
                </button>
                <button
                  type="button"
                  className="btn"
                  onClick={() => setCaptchaOpen(false)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      <header className="top">
        <div>
          <h1>GridWar</h1>
          <p className="muted small">
            Season ends in {formatSeasonCountdownDhm(season?.seasonEndsAt)} · New week Mondays 00:00
            Sydney · Guest: <strong>{nickname}</strong>
          </p>
        </div>
        <button type="button" className="btn ghost" onClick={leaveSession}>
          New player
        </button>
      </header>

      <div className="layout">
        <aside className="panel">
          <h2>Your stats</h2>
          {status ? (
            <>
              <p>
                Score <strong>{status.score}</strong>
              </p>
              <p>
                Cells owned <strong>{status.ownedCells}</strong>
              </p>
              <p className="small muted">
                Next +1 energy in {formatClock(status.secondsUntilNextEnergy)}
              </p>
              <div className="energy-wrap" aria-label="Energy">
                <div className="energy-bar" style={{ width: `${energyPct}%` }} />
              </div>
              <p className="small">
                Energy {status.energy}/{status.maxEnergy}
              </p>
              {status.needsCaptcha ? (
                <>
                  <p className="warn">Verification required before next action.</p>
                  <button type="button" className="btn primary small-btn" onClick={() => setCaptchaOpen(true)}>
                    Verify
                  </button>
                </>
              ) : null}
            </>
          ) : (
            <p className="muted">Loading…</p>
          )}
          {message ? <p className="toast">{message}</p> : null}
          {inCooldown ? <p className="small muted">Cursor: cooldown (3s)</p> : null}
        </aside>

        <main className="main">
          <div
            className={`grid ${cursorClass}`}
            style={{ gridTemplateColumns: `repeat(${size}, 1fr)` }}
          >
            {Array.from({ length: size * size }, (_, i) => {
              const x = i % size
              const y = Math.floor(i / size)
              const c = cellMap.get(`${x},${y}`)
              const owner = c?.ownerPlayerId
              const mine = owner && playerId && owner === playerId
              return (
                <button
                  key={`${x},${y}`}
                  type="button"
                  className={`cell ${mine ? 'cell--mine' : ''}`}
                  style={{ background: colorForOwner(owner) }}
                  title={c?.ownerNickname ? c.ownerNickname : 'Empty'}
                  onClick={() => onCellClick(x, y)}
                  disabled={inCooldown}
                />
              )
            })}
          </div>
        </main>

        <aside className="panel boards">
          <section>
            <h2>Season (live)</h2>
            <ol className="lb">
              {(season?.leaderboard ?? []).map((row) => (
                <li key={row.playerId || row.rank}>
                  <span className="lb-rank">{row.rank}</span>
                  <span className="lb-name">{row.nickname}</span>
                  <span className="lb-num">{row.score}</span>
                  <span className="lb-sub">{row.ownedCells}</span>
                </li>
              ))}
            </ol>
          </section>
          <section>
            <h2>Overall</h2>
            <p className="muted small">First season — no overall history yet.</p>
            <ol className="lb">
              {(overall?.entries ?? []).length === 0 ? (
                <li className="muted">No entries</li>
              ) : (
                (overall?.entries ?? []).map((row) => (
                  <li key={row.playerId || row.rank}>
                    <span className="lb-rank">{row.rank}</span>
                    <span className="lb-name">{row.nickname}</span>
                    <span className="lb-num">{row.score}</span>
                  </li>
                ))
              )}
            </ol>
          </section>
        </aside>
      </div>
    </div>
  )
}
