async function fetchActivos(){ const r = await fetch('/api/v1/activos'); return r.json(); }
