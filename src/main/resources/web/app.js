const API = (() => {
  const base = localStorage.getItem('apiBase') || window.location.origin || 'http://localhost:8080';
  let token = localStorage.getItem('token') || null;
  let username = localStorage.getItem('username') || null;
  let role = localStorage.getItem('role') || null;

  async function request(method, path, body) {
    const url = base + '/api' + path;
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);
    const resp = await fetch(url, opts);
    const text = await resp.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text; }
    return { ok: resp.ok, status: resp.status, data };
  }

  return {
    async login(user, pass) {
      const r = await request('POST', '/auth', { username: user, password: pass });
      if (r.ok) {
        token = r.data.token; username = r.data.username; role = r.data.role;
        localStorage.setItem('token', token);
        localStorage.setItem('username', username);
        localStorage.setItem('role', role);
      }
      return r;
    },
    logout() {
      token = null; username = null; role = null;
      localStorage.removeItem('token'); localStorage.removeItem('username'); localStorage.removeItem('role');
    },
    isLoggedIn() { return !!token; },
    getUsername() { return username; },
    getRole() { return role; },
    async health() { return request('GET', '/health'); },
    async listDeceased(q) {
      const path = q ? '/deceased?q=' + encodeURIComponent(q) : '/deceased';
      return request('GET', path);
    },
    async createDeceased(data) { return request('POST', '/deceased', data); },
    async deleteDeceased(id) { return request('DELETE', '/deceased/' + id); },
    async listStorage() { return request('GET', '/storage'); },
    async listInterventions() { return request('GET', '/interventions'); },
    async metrics() { return request('GET', '/metrics'); }
  };
})();

const App = {
  init() {
    this.bindNav();
    this.bindLogin();
    this.bindDeceased();
    this.bindLogout();
    if (API.isLoggedIn()) this.showMain();
  },

  showLogin() {
    document.getElementById('login-view').classList.add('active');
    document.getElementById('main-view').classList.remove('active');
  },

  showMain() {
    document.getElementById('login-view').classList.remove('active');
    document.getElementById('main-view').classList.add('active');
    document.getElementById('user-info').textContent = API.getUsername() + ' (' + API.getRole() + ')';
    this.navigate('dashboard');
  },

  bindNav() {
    document.querySelectorAll('.nav-item').forEach(el => {
      el.addEventListener('click', e => {
        e.preventDefault();
        this.navigate(el.dataset.view);
      });
    });
  },

  navigate(view) {
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const nav = document.querySelector(`.nav-item[data-view="${view}"]`);
    const page = document.getElementById('view-' + view);
    if (nav) nav.classList.add('active');
    if (page) page.classList.add('active');
    if (view === 'dashboard') this.loadDashboard();
    else if (view === 'deceased') this.loadDeceased();
    else if (view === 'storage') this.loadStorage();
    else if (view === 'interventions') this.loadInterventions();
    else if (view === 'metrics') this.loadMetrics();
  },

  bindLogin() {
    document.getElementById('login-btn').addEventListener('click', () => this.doLogin());
    document.getElementById('login-password').addEventListener('keydown', e => {
      if (e.key === 'Enter') this.doLogin();
    });
    document.getElementById('login-username').addEventListener('keydown', e => {
      if (e.key === 'Enter') document.getElementById('login-password').focus();
    });
    if (API.isLoggedIn()) this.showMain();
  },

  async doLogin() {
    const user = document.getElementById('login-username').value.trim();
    const pass = document.getElementById('login-password').value;
    const err = document.getElementById('login-error');
    if (!user || !pass) { err.textContent = 'Veuillez saisir vos identifiants'; return; }
    err.textContent = '';
    const r = await API.login(user, pass);
    if (r.ok) { this.showMain(); }
    else { err.textContent = r.data.error || 'Identifiants invalides'; }
  },

  bindLogout() {
    document.getElementById('logout-btn').addEventListener('click', () => {
      API.logout();
      this.showLogin();
    });
  },

  async loadDashboard() {
    const [h, dec] = await Promise.all([API.health(), API.listDeceased()]);
    const el = document.getElementById('health-info');
    if (h.ok) el.textContent = JSON.stringify(h.data, null, 2);
    else el.textContent = 'Erreur: ' + JSON.stringify(h.data);
    document.getElementById('stat-deceased').textContent = dec.ok && Array.isArray(dec.data) ? dec.data.length : '-';
    const s = await API.listStorage();
    if (s.ok && Array.isArray(s.data)) {
      document.getElementById('stat-total').textContent = s.data.length;
      document.getElementById('stat-occupied').textContent = s.data.filter(l => l.occupied).length;
    }
    const iv = await API.listInterventions();
    if (iv.ok && Array.isArray(iv.data)) document.getElementById('stat-interventions').textContent = iv.data.length;
  },

  async loadDeceased() {
    const r = await API.listDeceased();
    const tbody = document.getElementById('deceased-body');
    tbody.innerHTML = '';
    if (r.ok && Array.isArray(r.data)) {
      r.data.forEach(d => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${d.dossierNumber || '-'}</td><td>${d.lastName || ''}</td><td>${d.firstName || ''}</td>
          <td>${d.deathDate || '-'}</td><td>${d.gender || '-'}</td>
          <td><button class="btn btn-sm btn-danger" onclick="App.deleteDeceased(${d.id})">Suppr.</button></td>`;
        tbody.appendChild(tr);
      });
    } else { tbody.innerHTML = '<tr><td colspan="6">Aucun défunt trouvé</td></tr>'; }
  },

  bindDeceased() {
    document.getElementById('deceased-search-btn').addEventListener('click', () => this.searchDeceased());
    document.getElementById('deceased-refresh-btn').addEventListener('click', () => this.loadDeceased());
    document.getElementById('deceased-search').addEventListener('keydown', e => {
      if (e.key === 'Enter') this.searchDeceased();
    });
    document.getElementById('dec-save-btn').addEventListener('click', () => this.createDeceased());
  },

  async searchDeceased() {
    const q = document.getElementById('deceased-search').value.trim();
    const r = await API.listDeceased(q);
    const tbody = document.getElementById('deceased-body');
    tbody.innerHTML = '';
    if (r.ok && Array.isArray(r.data)) {
      r.data.forEach(d => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${d.dossierNumber || '-'}</td><td>${d.lastName || ''}</td><td>${d.firstName || ''}</td>
          <td>${d.deathDate || '-'}</td><td>${d.gender || '-'}</td>
          <td><button class="btn btn-sm btn-danger" onclick="App.deleteDeceased(${d.id})">Suppr.</button></td>`;
        tbody.appendChild(tr);
      });
    }
  },

  async createDeceased() {
    const data = {
      lastName: document.getElementById('dec-lastname').value.trim(),
      firstName: document.getElementById('dec-firstname').value.trim(),
      gender: document.getElementById('dec-gender').value || null,
      nir: document.getElementById('dec-nir').value.trim() || null,
      birthDate: document.getElementById('dec-birth').value || null,
      deathDate: document.getElementById('dec-death').value || null,
      placeOfDeath: document.getElementById('dec-place').value.trim() || null
    };
    const err = document.getElementById('deceased-form-error');
    if (!data.lastName || !data.firstName) { err.textContent = 'Nom et prénom sont obligatoires'; return; }
    err.textContent = '';
    const r = await API.createDeceased(data);
    if (r.ok) {
      document.getElementById('dec-lastname').value = '';
      document.getElementById('dec-firstname').value = '';
      document.getElementById('dec-gender').value = '';
      document.getElementById('dec-nir').value = '';
      document.getElementById('dec-birth').value = '';
      document.getElementById('dec-death').value = '';
      document.getElementById('dec-place').value = '';
      err.textContent = '';
      this.loadDeceased();
    } else {
      err.textContent = r.data.error || 'Erreur lors de la création';
    }
  },

  async deleteDeceased(id) {
    if (!confirm('Supprimer ce défunt ?')) return;
    const r = await API.deleteDeceased(id);
    if (r.ok) this.loadDeceased();
    else alert('Erreur: ' + (r.data.error || 'Suppression impossible'));
  },

  async loadStorage() {
    const r = await API.listStorage();
    const tbody = document.getElementById('storage-body');
    tbody.innerHTML = '';
    if (r.ok && Array.isArray(r.data)) {
      r.data.forEach(l => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${l.code}</td><td>${l.label}</td><td>${l.zone}</td>
          <td>${l.temperature || '-'}°C</td><td>${l.occupied ? 'OCCUPÉ' : 'LIBRE'}</td>`;
        tbody.appendChild(tr);
      });
    }
  },

  async loadInterventions() {
    const r = await API.listInterventions();
    const tbody = document.getElementById('interventions-body');
    tbody.innerHTML = '';
    if (r.ok && Array.isArray(r.data)) {
      r.data.forEach(iv => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${iv.deceased ? iv.deceased.fullName : '-'}</td><td>${iv.type || '-'}</td>
          <td>${iv.scheduledAt ? new Date(iv.scheduledAt).toLocaleString('fr') : '-'}</td>
          <td>${iv.status || '-'}</td>`;
        tbody.appendChild(tr);
      });
    }
  },

  async loadMetrics() {
    const r = await API.metrics();
    document.getElementById('metrics-display').textContent = r.ok
      ? JSON.stringify(r.data, null, 2)
      : JSON.stringify({error: 'Authentification requise', details: r.data});
  }
};

document.addEventListener('DOMContentLoaded', () => App.init());
