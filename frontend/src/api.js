async function parseError(res) {
  const text = await res.text()
  try {
    const j = JSON.parse(text)
    return j.error || text || res.statusText
  } catch {
    return text || res.statusText
  }
}

export async function api(path, { playerId, method = 'GET', body } = {}) {
  const headers = {}
  if (playerId) headers['X-Player-Id'] = playerId
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })
  if (!res.ok) {
    const err = new Error(await parseError(res))
    err.status = res.status
    throw err
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') {
    return null
  }
  const ct = res.headers.get('content-type')
  if (ct && ct.includes('application/json')) {
    return res.json()
  }
  return null
}

export function startPlayer(nickname) {
  return api('/player/start', { method: 'POST', body: { nickname } })
}

export function getGrid() {
  return api('/grid')
}

export function cellAction(playerId, x, y) {
  return api('/cell/action', { playerId, method: 'POST', body: { x, y } })
}

export function getPlayerStatus(playerId) {
  return api('/player/status', { playerId })
}

export function getSeason() {
  return api('/season')
}

export function getOverallLeaderboard() {
  return api('/leaderboard')
}

export function verifyCaptcha(playerId, answer) {
  return api('/player/captcha-verify', { playerId, method: 'POST', body: { answer } })
}
