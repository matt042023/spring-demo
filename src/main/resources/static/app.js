// Configuration de l'API (auto-détection du backend)
let API_BASE_URL = 'http://localhost:8081';
let API_ENDPOINTS = { villes: `${API_BASE_URL}/villes`, departements: `${API_BASE_URL}/departements` };

async function fetchWithTimeout(resource, options = {}) {
    const { timeout = 3000 } = options;
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), timeout);
    try {
        const response = await fetch(resource, { ...options, signal: controller.signal });
        return response;
    } finally {
        clearTimeout(id);
    }
}

async function detectApiBase() {
    // 1) Priorité: paramètre d'URL ?api=...
    const url = new URL(window.location.href);
    const apiParam = url.searchParams.get('api');
    if (apiParam) {
        API_BASE_URL = apiParam.replace(/\/$/, '');
        API_ENDPOINTS = { villes: `${API_BASE_URL}/villes`, departements: `${API_BASE_URL}/departements` };
        console.log('[API] backend forcé via ?api=', API_BASE_URL);
        return API_BASE_URL;
    }

    // 2) Priorité: localStorage
    const saved = localStorage.getItem('api_base_url');
    if (saved) {
        API_BASE_URL = saved.replace(/\/$/, '');
        API_ENDPOINTS = { villes: `${API_BASE_URL}/villes`, departements: `${API_BASE_URL}/departements` };
        console.log('[API] backend chargé depuis localStorage:', API_BASE_URL);
        return API_BASE_URL;
    }

    const candidates = [];
    if (location.origin && location.origin.startsWith('http')) {
        candidates.push(location.origin);
    }
    candidates.push('http://localhost:8081', 'http://127.0.0.1:8081', 'http://localhost:8080', 'http://127.0.0.1:8080');

    for (const base of candidates) {
        try {
            const res = await fetchWithTimeout(`${base}/hello`, { timeout: 1500 });
            if (res.ok) {
                API_BASE_URL = base;
                API_ENDPOINTS = { villes: `${API_BASE_URL}/villes`, departements: `${API_BASE_URL}/departements` };
                console.log('[API] backend détecté sur', API_BASE_URL);
                try { localStorage.setItem('api_base_url', API_BASE_URL); } catch(_) {}
                return API_BASE_URL;
            }
        } catch (_) { /* ignore */ }
    }
    console.warn('[API] Aucun backend détecté. Utilisation par défaut:', API_BASE_URL);
    return API_BASE_URL;
}

// Variables globales
let currentVilles = [];
let currentDepartements = [];
let allDepartements = []; // Cache de tous les départements
let editingVille = null;
let editingDepartement = null;
let currentPage = 1;
const itemsPerPage = 10;

// Variables de pagination
let currentVillePage = 0;
let villePageSize = 10;
let currentDepartementPage = 0;
let departementPageSize = 10;
let currentDepartementFilter = '';
let searchTimeout = null; // Pour le debounce de la recherche

// ==================== I18N (client) ====================
var I18N = {};
var CURRENT_LOCALE = 'fr';
// Fallback embarqué pour exécution en file:// (Chrome bloque fetch file://)
const I18N_EMBED = {
  fr: {
    "http.error": "Erreur HTTP: {status}",
    "error.initialLoad": "Erreur lors du chargement des données. Vérifiez que l'API est démarrée.",
    "departments.load.error": "Impossible de charger les départements",
    "departments.empty": "Aucun département trouvé",
    "departments.all": "Tous les départements",
    "departments.select": "-- Sélectionner un département --",
    "departments.notFound": "Département non trouvé",
    "departments.edit.title": "Modifier le département",
    "departments.delete.title": "Supprimer le département",
    "departments.delete.confirm": "Êtes-vous sûr de vouloir supprimer le département \"{name}\" ({code}) ?",
    "departments.delete.blocked": "Le département \"{name}\" ({code}) contient {count} ville(s). Impossible de le supprimer.",
    "departments.delete.success": "Département supprimé avec succès",
    "departments.create.success": "Département créé avec succès",
    "departments.update.success": "Département modifié avec succès",
    "departments.names.updated": "Noms mis à jour",
    "departments.selectedNotFound": "Département sélectionné introuvable",
    "cities.notFound": "Ville non trouvée",
    "cities.add.title": "Ajouter une nouvelle ville",
    "cities.edit.title": "Modifier la ville",
    "cities.details.title": "Détails de la ville {name}",
    "cities.delete.title": "Supprimer la ville",
    "cities.delete.confirm": "Êtes-vous sûr de vouloir supprimer la ville \"{name}\" ({population} habitants) ?",
    "cities.delete.success": "Ville supprimée avec succès",
    "cities.create.success": "Ville créée avec succès",
    "cities.update.success": "Ville modifiée avec succès",
    "cities.validation.nameMin": "Le nom de la ville doit contenir au moins 2 caractères",
    "cities.validation.populationMin": "Le nombre d'habitants doit être supérieur à 0",
    "label.name": "Nom",
    "label.population": "Population",
    "label.department": "Département",
    "delete.error": "Erreur lors de la suppression: {message}",
    "load.error": "Erreur de chargement",
    "form.selectDepartment": "Veuillez sélectionner un département",
    "error.unexpected": "Une erreur inattendue s'est produite",
    "error.app": "Une erreur s'est produite dans l'application",
    "hello.success": "Hello: {msg}",
    "hello.error": "Erreur hello: {message}",
    "validation.required": "Ce champ est requis",
    "validation.required.multiple": "Tous les champs sont requis"
  },
  en: {
    "http.error": "HTTP error: {status}",
    "error.initialLoad": "Failed to load data. Make sure the API is running.",
    "departments.load.error": "Unable to load departments",
    "departments.empty": "No departments found",
    "departments.all": "All departments",
    "departments.select": "-- Select a department --",
    "departments.notFound": "Department not found",
    "departments.edit.title": "Edit department",
    "departments.delete.title": "Delete department",
    "departments.delete.confirm": "Are you sure you want to delete department \"{name}\" ({code})?",
    "departments.delete.blocked": "Department \"{name}\" ({code}) contains {count} city(ies). Cannot delete.",
    "departments.delete.success": "Department deleted successfully",
    "departments.create.success": "Department created successfully",
    "departments.update.success": "Department updated successfully",
    "departments.names.updated": "Names updated",
    "departments.selectedNotFound": "Selected department not found",
    "cities.notFound": "City not found",
    "cities.add.title": "Add a new city",
    "cities.edit.title": "Edit city",
    "cities.details.title": "City details {name}",
    "cities.delete.title": "Delete city",
    "cities.delete.confirm": "Are you sure you want to delete city \"{name}\" ({population} inhabitants)?",
    "cities.delete.success": "City deleted successfully",
    "cities.create.success": "City created successfully",
    "cities.update.success": "City updated successfully",
    "cities.validation.nameMin": "City name must be at least 2 characters",
    "cities.validation.populationMin": "Population must be greater than 0",
    "label.name": "Name",
    "label.population": "Population",
    "label.department": "Department",
    "delete.error": "Delete failed: {message}",
    "load.error": "Load error",
    "form.selectDepartment": "Please select a department",
    "error.unexpected": "An unexpected error occurred",
    "error.app": "An application error occurred",
    "hello.success": "Hello: {msg}",
    "hello.error": "Hello error: {message}"
  },
  de: {
    "http.error": "HTTP-Fehler: {status}",
    "error.initialLoad": "Daten konnten nicht geladen werden. Stellen Sie sicher, dass die API läuft.",
    "departments.load.error": "Departements konnten nicht geladen werden",
    "departments.empty": "Keine Departements gefunden",
    "departments.all": "Alle Departements",
    "departments.select": "-- Departement auswählen --",
    "departments.notFound": "Departement nicht gefunden",
    "departments.edit.title": "Departement bearbeiten",
    "departments.delete.title": "Departement löschen",
    "departments.delete.confirm": "Möchten Sie das Departement \"{name}\" ({code}) wirklich löschen?",
    "departments.delete.blocked": "Das Departement \"{name}\" ({code}) enthält {count} Stadt(en). Löschen nicht möglich.",
    "departments.delete.success": "Departement erfolgreich gelöscht",
    "departments.create.success": "Departement erfolgreich erstellt",
    "departments.update.success": "Departement erfolgreich aktualisiert",
    "departments.names.updated": "Namen aktualisiert",
    "departments.selectedNotFound": "Ausgewähltes Departement nicht gefunden",
    "cities.notFound": "Stadt nicht gefunden",
    "cities.add.title": "Neue Stadt hinzufügen",
    "cities.edit.title": "Stadt bearbeiten",
    "cities.details.title": "Stadtdetails {name}",
    "cities.delete.title": "Stadt löschen",
    "cities.delete.confirm": "Möchten Sie die Stadt \"{name}\" wirklich löschen? ({population} Einwohner)",
    "cities.delete.success": "Stadt erfolgreich gelöscht",
    "cities.create.success": "Stadt erfolgreich erstellt",
    "cities.update.success": "Stadt erfolgreich aktualisiert",
    "cities.validation.nameMin": "Der Stadtname muss mindestens 2 Zeichen enthalten",
    "cities.validation.populationMin": "Die Einwohnerzahl muss größer als 0 sein",
    "label.name": "Name",
    "label.population": "Einwohnerzahl",
    "label.department": "Departement",
    "delete.error": "Fehler beim Löschen: {message}",
    "load.error": "Ladefehler",
    "form.selectDepartment": "Bitte ein Departement auswählen",
    "error.unexpected": "Ein unerwarteter Fehler ist aufgetreten",
    "error.app": "Ein Anwendungsfehler ist aufgetreten",
    "hello.success": "Hallo: {msg}",
    "hello.error": "Hallo-Fehler: {message}"
  }
};

function detectClientLocale() {
    const lang = (navigator.language || 'fr').slice(0,2).toLowerCase();
    return ['fr','en','de'].includes(lang) ? lang : 'fr';
}

async function loadI18n(lang) {
    try {
        if (location.protocol === 'file:') {
            // Fallback embarqué quand on ouvre le fichier localement
            I18N = I18N_EMBED[lang] || I18N_EMBED['fr'] || {};
            CURRENT_LOCALE = I18N_EMBED[lang] ? lang : 'fr';
            return;
        }
        const res = await fetch(`i18n/messages_${lang}.json`, { cache: 'no-store' });
        if (res.ok) {
            I18N = await res.json();
            CURRENT_LOCALE = lang;
            return;
        }
    } catch (_) {}
    // Fallback réseau ou autre
    I18N = I18N_EMBED[lang] || I18N_EMBED['fr'] || {};
    CURRENT_LOCALE = I18N_EMBED[lang] ? lang : 'fr';
}

function t(key, params = {}) {
    let str = I18N[key] || key;
    for (const k in params) {
        if (Object.prototype.hasOwnProperty.call(params, k)) {
            str = str.replace(new RegExp(`\\{${k}\\}`, 'g'), params[k]);
        }
    }
    return str;
}

// Initialisation de l'application
document.addEventListener('DOMContentLoaded', async function() {
    const desired = (new URL(location.href)).searchParams.get('lang') || localStorage.getItem('ui_lang') || detectClientLocale();
    await loadI18n(desired);
    try { localStorage.setItem('ui_lang', CURRENT_LOCALE); } catch(_) {}
    try { document.documentElement.setAttribute('lang', CURRENT_LOCALE); } catch(_) {}
    // Initialiser la valeur du sélecteur si présent
    const sel = document.getElementById('lang-select');
    if (sel) sel.value = CURRENT_LOCALE;
    initializeApp();
});

async function onLanguageChange(lang) {
    await loadI18n(lang);
    try { localStorage.setItem('ui_lang', lang); } catch(_) {}
    try { document.documentElement.setAttribute('lang', lang); } catch(_) {}
    // CURRENT_LOCALE est maintenant mis à jour par loadI18n()
    console.log('Langue changée vers:', CURRENT_LOCALE);
    // Rafraîchir les zones dynamiques pour refléter la nouvelle langue
    await populateDepartementSelects().catch(()=>{});
    await loadDepartements().catch(()=>{});
    await loadVilles().catch(()=>{});
    updateStats();
}

async function initializeApp() {
    console.log('Initialisation de l\'application...');
    await detectApiBase();
    
    // Configuration des onglets
    setupTabs();
    
    // Configuration des formulaires
    setupForms();
    
    // Chargement initial des données
    await loadInitialData();
    
    console.log('Application initialisée avec succès');
}

// ==========================================================================
// GESTION DES ONGLETS
// ==========================================================================

function setupTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const tabName = e.target.closest('.tab-btn').dataset.tab;
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    // Mise à jour des boutons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    
    // Mise à jour du contenu
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tabName}-section`).classList.add('active');
    
    // Masquer les messages
    hideMessage();
    
    // Chargement spécifique selon l'onglet
    if (tabName === 'villes') {
        loadVilles();
    } else if (tabName === 'departements') {
        loadDepartements();
        updateStats();
    }
}

// ==========================================================================
// GESTION DES MESSAGES
// ==========================================================================

function showMessage(message, type = 'info') {
    const messageEl = document.getElementById('message');
    const iconMap = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };
    
    messageEl.innerHTML = `
        <i class="${iconMap[type]}"></i>
        <span>${message}</span>
    `;
    messageEl.className = `message ${type}`;
    messageEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    
    // Auto-hide après 5 secondes pour les messages de succès
    if (type === 'success') {
        setTimeout(hideMessage, 5000);
    }
}

function hideMessage() {
    const messageEl = document.getElementById('message');
    messageEl.classList.add('hidden');
}

// ==========================================================================
// GESTION DU LOADING
// ==========================================================================

function showLoading() {
    document.getElementById('loading').classList.remove('hidden');
}

function hideLoading() {
    document.getElementById('loading').classList.add('hidden');
}

// ==========================================================================
// GESTION DES DONNÉES INITIALES
// ==========================================================================

async function loadInitialData() {
    try {
        showLoading();
        await Promise.all([
            loadDepartements(),
            loadVilles()
        ]);
        await populateDepartementSelects();
        updateStats();
    } catch (error) {
        console.error('Erreur lors du chargement initial:', error);
        showMessage(t('error.initialLoad'), 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// GESTION DES DÉPARTEMENTS
// ==========================================================================

async function loadDepartements() {
    try {
        // Utiliser le nouveau système avec tri par défaut par nom
        const response = await fetchWithLang(`${API_ENDPOINTS.departements}?page=${currentDepartementPage}&size=${departementPageSize}&sort=nom`);
        if (!response.ok) { throw new Error(t('http.error', { status: response.status })); }
        
        const pageData = await response.json();
        currentDepartements = pageData.content || [];
        console.log('Départements chargés:', currentDepartements.length, 'sur', pageData.totalElements);
        renderDepartements();
        renderDepartementPagination(pageData);
        return currentDepartements;
    } catch (error) {
        console.error('Erreur lors du chargement des départements:', error);
        showMessage(t('departments.load.error'), 'error');
        return [];
    }
}

function renderDepartements() {
    const tbody = document.getElementById('departements-list');
    
    if (!currentDepartements.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center" style="padding: 40px;">
                    <i class="fas fa-inbox" style="font-size: 2rem; color: var(--text-secondary); margin-bottom: 10px;"></i>
                    <p>${t('departments.empty')}</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = currentDepartements.map(dept => `
        <tr>
            <td><strong>${dept.code}</strong></td>
            <td>${dept.nom}</td>
            <td>
                <span class="badge">${dept.nombreVilles || 0}</span>
            </td>
            <td>${formatPopulation(dept.populationTotale || 0)}</td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-sm btn-primary" onclick="showDepartementDetails(${dept.id})">
                        <i class="fas fa-eye"></i> Voir
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="editDepartement(${dept.id})">
                        <i class="fas fa-edit"></i> Modifier
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="confirmDeleteDepartement(${dept.id})">
                        <i class="fas fa-trash"></i> Supprimer
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function populateDepartementSelects() {
    try {
        // Charger TOUS les départements pour les selects (pas paginés)
        const response = await fetchWithLang(`${API_ENDPOINTS.departements}?page=0&size=200&sort=nom`);
        if (!response.ok) throw new Error('Erreur de chargement');
        
        const pageData = await response.json();
        allDepartements = pageData.content || []; // Mettre à jour le cache global
        
        const selects = document.querySelectorAll('#ville-departement, #departement-filter');
        
        selects.forEach(select => {
            const isFilter = select.id === 'departement-filter';
            const defaultOption = isFilter ? 
                `<option value="">${t('departments.all')}</option>` : 
                `<option value="">${t('departments.select')}</option>`;
                
            select.innerHTML = defaultOption + allDepartements.map(dept => 
                `<option value="${dept.id}">${dept.code} - ${dept.nom}</option>`
            ).join('');
        });
    } catch (error) {
        console.error('Erreur lors du chargement des départements pour les selects:', error);
    }
}

function showAddDepartementForm() {
    editingDepartement = null;
    document.getElementById('departement-form-title').innerHTML = 
        '<i class="fas fa-plus"></i> Ajouter un nouveau département';
    document.getElementById('departement-form').reset();
    document.getElementById('departement-form-container').classList.remove('hidden');
    document.getElementById('departement-code').focus();
}

function hideDepartementForm() {
    document.getElementById('departement-form-container').classList.add('hidden');
    editingDepartement = null;
}

async function editDepartement(id) {
    const departement = currentDepartements.find(d => d.id === id);
    if (!departement) {
        showMessage(t('departments.notFound'), 'error');
        return;
    }
    
    editingDepartement = departement;
    document.getElementById('departement-form-title').innerHTML = 
        `<i class="fas fa-edit"></i> ${t('departments.edit.title')}`;
    
    document.getElementById('departement-code').value = departement.code;
    document.getElementById('departement-nom').value = departement.nom;
    
    document.getElementById('departement-form-container').classList.remove('hidden');
    document.getElementById('departement-code').focus();
}

async function showDepartementDetails(id) {
    const departement = currentDepartements.find(d => d.id === id);
    if (!departement) {
        showMessage(t('departments.notFound'), 'error');
        return;
    }
    
    document.getElementById('detail-title').innerHTML = 
        `<i class="fas fa-map"></i> Détails du département ${departement.code}`;
    
    const villesHtml = departement.villes && departement.villes.length > 0 
        ? departement.villes.map(ville => `
            <div class="detail-list-item">
                <span>${ville.nom}</span>
                <span class="badge">${formatPopulation(ville.nbHabitants)}</span>
            </div>
        `).join('')
        : '<div class="detail-list-item text-center">Aucune ville</div>';
    
    document.getElementById('detail-content').innerHTML = `
        <div class="detail-section">
            <h4><i class="fas fa-info-circle"></i> Informations générales</h4>
            <div class="detail-grid">
                <div class="detail-item">
                    <label>Code</label>
                    <div class="value">${departement.code}</div>
                </div>
                <div class="detail-item">
                    <label>Nom</label>
                    <div class="value">${departement.nom}</div>
                </div>
                <div class="detail-item">
                    <label>Nombre de villes</label>
                    <div class="value">${departement.nombreVilles || 0}</div>
                </div>
                <div class="detail-item">
                    <label>Population totale</label>
                    <div class="value">${formatPopulation(departement.populationTotale || 0)}</div>
                </div>
            </div>
        </div>
        
        <div class="detail-section">
            <h4><i class="fas fa-building"></i> Villes du département</h4>
            <div class="detail-list">
                ${villesHtml}
            </div>
        </div>
    `;
    
    document.getElementById('detail-modal').classList.remove('hidden');
}

async function confirmDeleteDepartement(id) {
    const departement = currentDepartements.find(d => d.id === id);
    if (!departement) {
        showMessage(t('departments.notFound'), 'error');
        return;
    }
    
    const hasVilles = departement.nombreVilles > 0;
    const message = hasVilles 
        ? `Le département "${departement.nom}" (${departement.code}) contient ${departement.nombreVilles} ville(s). Impossible de le supprimer.`
        : `Êtes-vous sûr de vouloir supprimer le département "${departement.nom}" (${departement.code}) ?`;
    
    document.getElementById('confirm-title').textContent = t('departments.delete.title');
    document.getElementById('confirm-message').textContent = message;
    
    if (hasVilles) {
        document.getElementById('confirm-yes').style.display = 'none';
        document.getElementById('confirm-modal').classList.remove('hidden');
        return;
    }
    
    document.getElementById('confirm-yes').style.display = 'inline-flex';
    document.getElementById('confirm-yes').onclick = () => deleteDepartement(id);
    document.getElementById('confirm-modal').classList.remove('hidden');
}

async function deleteDepartement(id) {
    try {
        showLoading();
        const response = await fetchWithLang(`${API_ENDPOINTS.departements}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || t('http.error', { status: response.status }));
        }
        
        await loadDepartements();
        await populateDepartementSelects();
        await loadVilles(); // Recharger les villes pour mettre à jour les filtres
        updateStats();
        hideConfirmModal();
        showMessage(t('departments.delete.success'), 'success');
    } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        hideConfirmModal();
        showMessage(t('delete.error', { message: error.message }), 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// GESTION DES VILLES
// ==========================================================================

async function loadVilles() {
    try {
        const departementFilter = document.getElementById('departement-filter')?.value || '';
        let url;
        
        if (departementFilter) {
            // Si un département est sélectionné, utiliser le cache
            const departementSelectionne = allDepartements.find(d => d.id.toString() === departementFilter);
            if (departementSelectionne) {
                url = `${API_ENDPOINTS.villes}/departement/${encodeURIComponent(departementSelectionne.code)}`;
                console.log('Chargement villes pour département:', departementSelectionne.code, departementSelectionne.nom);
            } else {
                console.error('Département introuvable dans le cache:', departementFilter);
                url = `${API_ENDPOINTS.villes}?page=${currentVillePage}&size=${villePageSize}&sort=nom`;
            }
        } else {
            // Sinon, utiliser l'endpoint paginé normal
            url = `${API_ENDPOINTS.villes}?page=${currentVillePage}&size=${villePageSize}&sort=nom`;
        }
        
        const response = await fetchWithLang(url);
        if (!response.ok) { throw new Error(t('http.error', { status: response.status })); }
        
        const data = await response.json();
        
        // Si c'est une liste simple (endpoint département), la transformer en format paginé
        if (Array.isArray(data)) {
            currentVilles = data;
            console.log('Villes chargées (filtrées par département):', currentVilles.length);
            renderVilles();
            // Pas de pagination pour les résultats filtrés
            document.getElementById('villes-pagination').innerHTML = '';
        } else {
            // Format paginé normal
            currentVilles = data.content || [];
            console.log('Villes chargées:', currentVilles.length, 'sur', data.totalElements);
            renderVilles();
            renderVillePagination(data);
        }
        
        return currentVilles;
    } catch (error) {
        console.error('Erreur lors du chargement des villes:', error);
        showMessage(t('loading.error.cities'), 'error');
        return [];
    }
}

function renderVilles() {
    const tbody = document.getElementById('villes-list');
    
    if (!currentVilles.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center" style="padding: 40px;">
                    <i class="fas fa-inbox" style="font-size: 2rem; color: var(--text-secondary); margin-bottom: 10px;"></i>
                    <p>Aucune ville trouvée</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = currentVilles.map(ville => `
        <tr>
            <td><strong>${ville.nom}</strong></td>
            <td>${formatPopulation(ville.nbHabitants)}</td>
            <td>
                ${ville.departement ? 
                    `<span class="badge">${ville.departement.code}</span> ${ville.departement.nom}` : 
                    '<span class="text-secondary">Non défini</span>'
                }
            </td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-sm btn-primary" onclick="showVilleDetails(${ville.id})">
                        <i class="fas fa-eye"></i> Voir
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="editVille(${ville.id})">
                        <i class="fas fa-edit"></i> Modifier
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="confirmDeleteVille(${ville.id})">
                        <i class="fas fa-trash"></i> Supprimer
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function showAddVilleForm() {
    editingVille = null;
    document.getElementById('ville-form-title').innerHTML = 
        '<i class="fas fa-plus"></i> Ajouter une nouvelle ville';
    document.getElementById('ville-form').reset();
    document.getElementById('ville-form-container').classList.remove('hidden');
    document.getElementById('ville-nom').focus();
}

function hideVilleForm() {
    document.getElementById('ville-form-container').classList.add('hidden');
    editingVille = null;
}

async function editVille(id) {
    const ville = currentVilles.find(v => v.id === id);
    if (!ville) {
        showMessage(t('cities.notFound'), 'error');
        return;
    }
    
    editingVille = ville;
    document.getElementById('ville-form-title').innerHTML = 
        `<i class="fas fa-edit"></i> ${t('cities.edit.title')}`;
    
    document.getElementById('ville-nom').value = ville.nom;
    document.getElementById('ville-habitants').value = ville.nbHabitants;
    document.getElementById('ville-departement').value = ville.departement?.id || '';
    
    document.getElementById('ville-form-container').classList.remove('hidden');
    document.getElementById('ville-nom').focus();
}

async function showVilleDetails(id) {
    const ville = currentVilles.find(v => v.id === id);
    if (!ville) {
        showMessage(t('cities.not.found'), 'error');
        return;
    }
    
    document.getElementById('detail-title').innerHTML = 
        `<i class="fas fa-building"></i> ${t('cities.details.title', { name: ville.nom })}`;
    
    document.getElementById('detail-content').innerHTML = `
        <div class="detail-section">
            <h4><i class="fas fa-info-circle"></i> Informations générales</h4>
            <div class="detail-grid">
                <div class="detail-item">
                    <label>Nom</label>
                    <div class="value">${ville.nom}</div>
                </div>
                <div class="detail-item">
                    <label>Population</label>
                    <div class="value">${formatPopulation(ville.nbHabitants)}</div>
                </div>
                <div class="detail-item">
                    <label>Département</label>
                    <div class="value">
                        ${ville.departement ? 
                            `${ville.departement.code} - ${ville.departement.nom}` : 
                            'Non défini'
                        }
                    </div>
                </div>
                <div class="detail-item">
                    <label>ID</label>
                    <div class="value">#${ville.id}</div>
                </div>
            </div>
        </div>
    `;
    
    document.getElementById('detail-modal').classList.remove('hidden');
}

async function confirmDeleteVille(id) {
    const ville = currentVilles.find(v => v.id === id);
    if (!ville) {
        showMessage(t('cities.notFound'), 'error');
        return;
    }
    
    document.getElementById('confirm-title').textContent = t('cities.delete.title');
    document.getElementById('confirm-message').textContent = 
        t('cities.delete.confirm', { name: ville.nom, population: formatPopulation(ville.nbHabitants) });
    
    document.getElementById('confirm-yes').style.display = 'inline-flex';
    document.getElementById('confirm-yes').onclick = () => deleteVille(id);
    document.getElementById('confirm-modal').classList.remove('hidden');
}

async function deleteVille(id) {
    try {
        showLoading();
        const response = await fetchWithLang(`${API_ENDPOINTS.villes}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Erreur HTTP: ${response.status}`);
        }
        
        await loadVilles();
        await loadDepartements(); // Recharger pour mettre à jour les stats
        updateStats();
        hideConfirmModal();
        showMessage(t('cities.delete.success'), 'success');
    } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        hideConfirmModal();
        showMessage(t('error.delete', {message: error.message}), 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// GESTION DES FORMULAIRES
// ==========================================================================

function setupForms() {
    // Formulaire des villes
    document.getElementById('ville-form').addEventListener('submit', handleVilleSubmit);
    
    // Formulaire des départements
    document.getElementById('departement-form').addEventListener('submit', handleDepartementSubmit);
    
    // Validation en temps réel
    setupFormValidation();
}

function setupFormValidation() {
    // Code département - format français
    const codeInput = document.getElementById('departement-code');
    codeInput.addEventListener('input', function() {
        this.value = this.value.toUpperCase();
    });
    
    // Population - formatage des nombres
    const habitantsInput = document.getElementById('ville-habitants');
    habitantsInput.addEventListener('blur', function() {
        if (this.value) {
            this.value = parseInt(this.value).toString();
        }
    });
}

async function handleVilleSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const departementId = parseInt(formData.get('departement'));
    
    if (!departementId) {
        showMessage(t('form.selectDepartment'), 'error');
        return;
    }
    
    // Chercher le département dans la liste complète des départements
    let departementSelectionne = currentDepartements.find(d => d.id === departementId);
    
    // Si pas trouvé dans currentDepartements, le récupérer via API
    if (!departementSelectionne) {
        try {
            const response = await fetchWithLang(`${API_ENDPOINTS.departements}/${departementId}`);
            if (response.ok) {
                departementSelectionne = await response.json();
            }
        } catch (error) {
            console.error('Erreur lors de la récupération du département:', error);
        }
    }
    
    if (!departementSelectionne) {
        showMessage(t('departments.selectedNotFound'), 'error');
        return;
    }
    
    const villeData = {
        nom: formData.get('nom').trim(),
        nbHabitants: parseInt(formData.get('nbHabitants')),
        departement: {
            id: departementSelectionne.id,
            code: departementSelectionne.code,
            nom: departementSelectionne.nom
        }
    };
    
    // Validation
    if (!villeData.nom || villeData.nom.length < 2) {
        showMessage(t('cities.validation.nameMin'), 'error');
        return;
    }
    
    if (!villeData.nbHabitants || villeData.nbHabitants < 1) {
        showMessage(t('cities.validation.populationMin'), 'error');
        return;
    }
    
    
    try {
        showLoading();
        let response;
        
        if (editingVille) {
            // Modification
            response = await fetchWithLang(`${API_ENDPOINTS.villes}/${editingVille.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(villeData)
            });
        } else {
            // Création
            response = await fetchWithLang(API_ENDPOINTS.villes, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(villeData)
            });
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = t('http.error', { status: response.status });
            
            try {
                const errorData = JSON.parse(errorText);
                if (errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (jsonError) {
                // Si ce n'est pas du JSON, utiliser le texte brut
                errorMessage = errorText || errorMessage;
            }
            
            throw new Error(errorMessage);
        }
        
        await loadVilles();
        await loadDepartements(); // Recharger pour mettre à jour les stats
        updateStats();
        
        const messageKey = editingVille ? 'cities.update.success' : 'cities.create.success';
        hideVilleForm();
        showMessage(t(messageKey), 'success');
        
    } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        showMessage(t('error.save', {message: error.message}), 'error');
    } finally {
        hideLoading();
    }
}

async function handleDepartementSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const departementData = {
        code: formData.get('code').trim().toUpperCase(),
        nom: formData.get('nom').trim()
    };
    
    // Validation
    if (!departementData.code || departementData.code.length < 2 || departementData.code.length > 3) {
        showMessage(t('validation.department.code.length'), 'error');
        return;
    }
    
    if (!departementData.nom || departementData.nom.length < 2) {
        showMessage(t('validation.department.name.length'), 'error');
        return;
    }
    
    try {
        showLoading();
        let response;
        
        if (editingDepartement) {
            // Modification
            response = await fetchWithLang(`${API_ENDPOINTS.departements}/${editingDepartement.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(departementData)
            });
        } else {
            // Création
            response = await fetchWithLang(API_ENDPOINTS.departements, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(departementData)
            });
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = t('http.error', { status: response.status });
            
            try {
                const errorData = JSON.parse(errorText);
                if (errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (jsonError) {
                // Si ce n'est pas du JSON, utiliser le texte brut
                errorMessage = errorText || errorMessage;
            }
            
            throw new Error(errorMessage);
        }
        
        await loadDepartements();
        await populateDepartementSelects();
        updateStats();
        
        const messageKey = editingDepartement ? 'departments.update.success' : 'departments.create.success';
        hideDepartementForm();
        showMessage(t(messageKey), 'success');
        
    } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        showMessage(t('error.save', {message: error.message}), 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// GESTION DES MODALS
// ==========================================================================

function hideConfirmModal() {
    document.getElementById('confirm-modal').classList.add('hidden');
}

function hideDetailModal() {
    document.getElementById('detail-modal').classList.add('hidden');
}

// Fermeture des modals en cliquant à l'extérieur
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        e.target.classList.add('hidden');
    }
});

// ==========================================================================
// FILTRES ET RECHERCHE
// ==========================================================================

function filterVilles() {
    // Debounce pour éviter trop d'appels API
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }
    
    searchTimeout = setTimeout(() => {
        performVilleSearch();
    }, 300); // Attendre 300ms après la dernière frappe
}

async function performVilleSearch() {
    const searchTerm = document.getElementById('ville-search').value.trim();
    const departementFilter = document.getElementById('departement-filter').value;
    
    // Si changement de département, recharger depuis le serveur
    if (departementFilter !== currentDepartementFilter) {
        currentDepartementFilter = departementFilter;
        currentVillePage = 0; // Reset pagination
        await loadVilles();
        return;
    }
    
    // Si il y a une recherche par nom, utiliser l'API de recherche
    if (searchTerm.length >= 2) {
        try {
            showLoading();
            let url;
            
            if (departementFilter) {
                // Si département sélectionné ET recherche nom -> combiner les deux
                const departementSelectionne = allDepartements.find(d => d.id.toString() === departementFilter);
                if (departementSelectionne) {
                    // Récupérer toutes les villes du département puis filtrer
                    const response = await fetchWithLang(`${API_ENDPOINTS.villes}/departement/${encodeURIComponent(departementSelectionne.code)}`);
                    if (response.ok) {
                        const toutes = await response.json();
                        const filtrees = toutes.filter(ville => 
                            ville.nom.toLowerCase().includes(searchTerm.toLowerCase())
                        );
                        currentVilles = filtrees;
                        renderVilles();
                        document.getElementById('villes-pagination').innerHTML = '';
                        return;
                    }
                }
            } else {
                // Recherche globale par nom
                url = `${API_ENDPOINTS.villes}/search/nom-contient?nom=${encodeURIComponent(searchTerm)}`;
                const response = await fetchWithLang(url);
                if (response.ok) {
                    const results = await response.json();
                    currentVilles = results;
                    renderVilles();
                    document.getElementById('villes-pagination').innerHTML = '';
                    return;
                }
            }
        } catch (error) {
            console.error('Erreur lors de la recherche:', error);
        } finally {
            hideLoading();
        }
    } else if (searchTerm.length === 0) {
        // Si la recherche est vidée, recharger les données normales
        await loadVilles();
        return;
    } else {
        // Moins de 2 caractères, filtrer localement
        const filteredVilles = currentVilles.filter(ville => {
            const matchesSearch = ville.nom.toLowerCase().includes(searchTerm.toLowerCase());
            return matchesSearch;
        });
        renderFilteredVilles(filteredVilles);
    }
}

function renderFilteredVilles(villes) {
    const tbody = document.getElementById('villes-list');
    
    if (!villes.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center" style="padding: 40px;">
                    <i class="fas fa-search" style="font-size: 2rem; color: var(--text-secondary); margin-bottom: 10px;"></i>
                    <p>Aucune ville ne correspond aux critères de recherche</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = villes.map(ville => `
        <tr>
            <td><strong>${ville.nom}</strong></td>
            <td>${formatPopulation(ville.nbHabitants)}</td>
            <td>
                ${ville.departement ? 
                    `<span class="badge">${ville.departement.code}</span> ${ville.departement.nom}` : 
                    '<span class="text-secondary">Non défini</span>'
                }
            </td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-sm btn-primary" onclick="showVilleDetails(${ville.id})">
                        <i class="fas fa-eye"></i> Voir
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="editVille(${ville.id})">
                        <i class="fas fa-edit"></i> Modifier
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="confirmDeleteVille(${ville.id})">
                        <i class="fas fa-trash"></i> Supprimer
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Variable pour le délai de recherche
let departementSearchTimeout = null;

async function filterDepartements() {
    // Annuler le délai précédent
    if (departementSearchTimeout) {
        clearTimeout(departementSearchTimeout);
    }
    
    // Attendre 300ms avant de faire la recherche
    departementSearchTimeout = setTimeout(async () => {
        const searchTerm = document.getElementById('departement-search').value.trim();
        
        // Si la recherche est vide, afficher les départements de la page courante
        if (!searchTerm) {
            renderDepartements();
            return;
        }
        
        try {
            showLoading();
            const response = await fetchWithLang(`${API_ENDPOINTS.departements}/search?q=${encodeURIComponent(searchTerm)}`);
            
            if (!response.ok) {
                throw new Error(t('http.error', { status: response.status }));
            }
            
            const departements = await response.json();
            renderFilteredDepartements(departements);
        } catch (error) {
            console.error('Erreur lors de la recherche de départements:', error);
            showMessage(t('error.search.departments'), 'error');
        } finally {
            hideLoading();
        }
    }, 300);
}

function renderFilteredDepartements(departements) {
    const tbody = document.getElementById('departements-list');
    
    if (!tbody) {
        console.error('Élément departements-list non trouvé !');
        return;
    }
    
    if (!departements.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center" style="padding: 40px;">
                    <i class="fas fa-search" style="font-size: 2rem; color: var(--text-secondary); margin-bottom: 10px;"></i>
                    <p>${t('search.no.results.departments')}</p>
                </td>
            </tr>
        `;
        return;
    }
    tbody.innerHTML = departements.map(dept => `
        <tr>
            <td><strong>${dept.code}</strong></td>
            <td>${dept.nom}</td>
            <td>
                <span class="badge">${dept.nombreVilles || 0}</span>
            </td>
            <td>${formatPopulation(dept.populationTotale || 0)}</td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-sm btn-primary" onclick="showDepartementDetails(${dept.id})">
                        <i class="fas fa-eye"></i> Voir
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="editDepartement(${dept.id})">
                        <i class="fas fa-edit"></i> Modifier
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="confirmDeleteDepartement(${dept.id})">
                        <i class="fas fa-trash"></i> Supprimer
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function sortVilles() {
    const sortBy = document.getElementById('ville-sort').value;
    
    // Vider la recherche si elle existe et recharger avec le nouveau tri
    const searchInput = document.getElementById('ville-search');
    if (searchInput) {
        searchInput.value = '';
    }
    
    // Réinitialiser le filtre par département si il existe
    const departementFilter = document.getElementById('departement-filter');
    if (departementFilter) {
        departementFilter.value = '';
    }
    
    try {
        showLoading();
        
        // Appeler l'API avec le tri côté serveur
        const response = await fetchWithLang(
            `${API_ENDPOINTS.villes}?page=${currentVillePage}&size=${villePageSize}&sort=${encodeURIComponent(sortBy)}`
        );
        
        if (!response.ok) {
            throw new Error(t('http.error', { status: response.status }));
        }
        
        const pageData = await response.json();
        currentVilles = pageData.content || [];
        
        renderVilles();
        renderVillePagination(pageData);
        
    } catch (error) {
        console.error('Erreur lors du tri des villes:', error);
        showMessage(t('error.sort.cities'), 'error');
    } finally {
        hideLoading();
    }
}

async function sortDepartements() {
    const sortBy = document.getElementById('departement-sort').value;
    
    // Vider la recherche si elle existe et recharger avec le nouveau tri
    const searchInput = document.getElementById('departement-search');
    if (searchInput) {
        searchInput.value = '';
    }
    
    try {
        showLoading();
        
        // Appeler l'API avec le tri côté serveur
        const response = await fetchWithLang(
            `${API_ENDPOINTS.departements}?page=${currentDepartementPage}&size=${departementPageSize}&sort=${encodeURIComponent(sortBy)}`
        );
        
        if (!response.ok) {
            throw new Error(t('http.error', { status: response.status }));
        }
        
        const pageData = await response.json();
        currentDepartements = pageData.content || [];
        
        renderDepartements();
        renderDepartementPagination(pageData);
        
    } catch (error) {
        console.error('Erreur lors du tri des départements:', error);
        showMessage(t('error.sort.departments'), 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// STATISTIQUES
// ==========================================================================

function updateStats() {
    const totalDepartements = currentDepartements.length;
    const totalVilles = currentVilles.length;
    const totalPopulation = currentDepartements.reduce((total, dept) => {
        return total + (dept.populationTotale || 0);
    }, 0);
    
    document.getElementById('total-departements').textContent = totalDepartements;
    document.getElementById('total-villes-stats').textContent = totalVilles;
    document.getElementById('total-population').textContent = formatPopulation(totalPopulation);
}

// ==========================================================================
// GESTION DE LA PAGINATION
// ==========================================================================

function renderDepartementPagination(pageData) {
    const container = document.getElementById('departements-pagination');
    if (!container) return;
    
    const { number: currentPage, totalPages, totalElements, size } = pageData;
    
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }
    
    const pagination = createPaginationHTML(currentPage, totalPages, totalElements, size, 'departement');
    container.innerHTML = pagination;
}

function renderVillePagination(pageData) {
    const container = document.getElementById('villes-pagination');
    if (!container) return;
    
    const { number: currentPage, totalPages, totalElements, size } = pageData;
    
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }
    
    const pagination = createPaginationHTML(currentPage, totalPages, totalElements, size, 'ville');
    container.innerHTML = pagination;
}

function createPaginationHTML(currentPage, totalPages, totalElements, size, type) {
    const startItem = (currentPage * size) + 1;
    const endItem = Math.min((currentPage + 1) * size, totalElements);
    
    let html = `
        <div class="pagination-info">
            Affichage de ${startItem}-${endItem} sur ${totalElements} résultats
        </div>
        <div class="pagination-controls">
            <div class="page-size-selector">
                <label>Afficher:</label>
                <select onchange="changePageSize('${type}', this.value)">
                    <option value="10" ${size === 10 ? 'selected' : ''}>10</option>
                    <option value="20" ${size === 20 ? 'selected' : ''}>20</option>
                    <option value="30" ${size === 30 ? 'selected' : ''}>30</option>
                    <option value="50" ${size === 50 ? 'selected' : ''}>50</option>
                    <option value="100" ${size === 100 ? 'selected' : ''}>100</option>
                </select>
                par page
            </div>
            <div class="page-buttons">
    `;
    
    // Bouton précédent
    if (currentPage > 0) {
        html += `<button onclick="changePage('${type}', ${currentPage - 1})" class="btn btn-sm">← Précédent</button>`;
    }
    
    // Numéros de page
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);
    
    if (startPage > 0) {
        html += `<button onclick="changePage('${type}', 0)" class="btn btn-sm">1</button>`;
        if (startPage > 1) {
            html += `<span>...</span>`;
        }
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage ? 'btn-primary' : '';
        html += `<button onclick="changePage('${type}', ${i})" class="btn btn-sm ${isActive}">${i + 1}</button>`;
    }
    
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            html += `<span>...</span>`;
        }
        html += `<button onclick="changePage('${type}', ${totalPages - 1})" class="btn btn-sm">${totalPages}</button>`;
    }
    
    // Bouton suivant
    if (currentPage < totalPages - 1) {
        html += `<button onclick="changePage('${type}', ${currentPage + 1})" class="btn btn-sm">Suivant →</button>`;
    }
    
    html += `
            </div>
        </div>
    `;
    
    return html;
}

function changePage(type, page) {
    if (type === 'ville') {
        currentVillePage = page;
        loadVilles();
    } else if (type === 'departement') {
        currentDepartementPage = page;
        loadDepartements();
    }
}

function changePageSize(type, size) {
    const newSize = parseInt(size);
    
    if (type === 'ville') {
        villePageSize = newSize;
        currentVillePage = 0; // Reset to first page
        loadVilles();
    } else if (type === 'departement') {
        departementPageSize = newSize;
        currentDepartementPage = 0; // Reset to first page
        loadDepartements();
    }
}

// ==========================================================================
// UTILITAIRES
// ==========================================================================

function formatPopulation(population) {
    if (!population || population === 0) return '0';
    const locale = CURRENT_LOCALE === 'fr' ? 'fr-FR' : CURRENT_LOCALE === 'de' ? 'de-DE' : 'en-US';
    return population.toLocaleString(locale);
}

// ==========================================================================
// GESTION DES ERREURS GLOBALES
// ==========================================================================

window.addEventListener('unhandledrejection', function(event) {
    console.error('Erreur non gérée:', event.reason);
    showMessage(t('error.unexpected'), 'error');
});

window.addEventListener('error', function(event) {
    console.error('Erreur JavaScript:', event.error);
    showMessage(t('error.app'), 'error');
});

// Export des fonctions globales pour les onclick HTML
window.showAddVilleForm = showAddVilleForm;
window.hideVilleForm = hideVilleForm;
window.editVille = editVille;
window.showVilleDetails = showVilleDetails;
window.confirmDeleteVille = confirmDeleteVille;
window.showAddDepartementForm = showAddDepartementForm;
window.hideDepartementForm = hideDepartementForm;
window.editDepartement = editDepartement;
window.showDepartementDetails = showDepartementDetails;
window.confirmDeleteDepartement = confirmDeleteDepartement;
window.hideConfirmModal = hideConfirmModal;
window.hideDetailModal = hideDetailModal;
window.filterVilles = filterVilles;
window.filterDepartements = filterDepartements;
window.sortVilles = sortVilles;
window.sortDepartements = sortDepartements;
window.changePage = changePage;
window.changePageSize = changePageSize;
window.onLanguageChange = onLanguageChange;

// ==========================================================================
// OUTILS GÉNÉRIQUES AVANCÉS
// ==========================================================================

// Wrapper pour fetch avec gestion automatique de la langue
function fetchWithLang(url, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    options.headers['Accept-Language'] = CURRENT_LOCALE;
    return fetch(url, options);
}

async function apiFetch(url, options = {}) {
    // Ajouter l'en-tête Accept-Language pour synchroniser la locale avec le backend
    if (!options.headers) {
        options.headers = {};
    }
    options.headers['Accept-Language'] = CURRENT_LOCALE;
    
    const res = await fetch(url, options);
    if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || `HTTP ${res.status}`);
    }
    const contentType = res.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return res.json();
    }
    return res.text();
}

function showJSONInModal(title, data) {
    document.getElementById('detail-title').innerHTML = title;
    const pre = document.createElement('pre');
    pre.style.whiteSpace = 'pre-wrap';
    pre.textContent = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
    const container = document.getElementById('detail-content');
    container.innerHTML = '';
    container.appendChild(pre);
    document.getElementById('detail-modal').classList.remove('hidden');
}

// ==========================================================================
// HELLO
// ==========================================================================

async function helloPing() {
    try {
        showLoading();
        const msg = await apiFetch(`${API_BASE_URL}/hello`);
        showMessage(t('hello.success', { msg }), 'success');
    } catch (e) {
        showMessage(t('hello.error', { message: e.message }), 'error');
    } finally { hideLoading(); }
}

// ==========================================================================
// ROUTES AVANCÉES VILLES
// ==========================================================================

async function villesSearchNom() {
    const nom = document.getElementById('ville-search-nom').value.trim();
    if (!nom) return showMessage(t('validation.required.name'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/search/nom?nom=${encodeURIComponent(nom)}`); showJSONInModal('Ville (nom exact)', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesSearchNomContient() {
    const nom = document.getElementById('ville-search-contient').value.trim();
    if (!nom) return showMessage(t('validation.required.criteria'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/search/nom-contient?nom=${encodeURIComponent(nom)}`); showJSONInModal('Villes (nom contient)', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesNomCommence() {
    const p = document.getElementById('ville-search-prefix').value.trim();
    if (!p) return showMessage(t('validation.required.prefix'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/search/nom-commence?prefix=${encodeURIComponent(p)}`); showJSONInModal('Villes (nom commence par)', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesPopMin() {
    const v = document.getElementById('ville-pop-min').value;
    if (!v) return showMessage(t('validation.required.min'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/search/population-min?min=${encodeURIComponent(v)}`); showJSONInModal('Villes (population >= min)', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesPopPlage() {
    const min = document.getElementById('ville-pop-min2').value;
    const max = document.getElementById('ville-pop-max2').value;
    if (!min || !max) return showMessage(t('validation.required.min.max'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/search/population-plage?min=${encodeURIComponent(min)}&max=${encodeURIComponent(max)}`); showJSONInModal('Villes (population dans plage)', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesDeptMin() {
    const code = document.getElementById('ville-dept-code').value.trim();
    const min = document.getElementById('ville-dept-min').value;
    if (!code) return showMessage('Renseignez code', 'warning');
    const url = min ? `${API_ENDPOINTS.villes}/departement/${encodeURIComponent(code)}?min=${encodeURIComponent(min)}` : `${API_ENDPOINTS.villes}/departement/${encodeURIComponent(code)}`;
    try { showLoading(); const data = await apiFetch(url); showJSONInModal('Villes par département', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesDeptPlage() {
    const code = document.getElementById('ville-dept-code2').value.trim();
    const min = document.getElementById('ville-dept-min2').value;
    const max = document.getElementById('ville-dept-max2').value;
    if (!code || !min || !max) return showMessage(t('validation.required.code.min.max'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/departement/${encodeURIComponent(code)}/plage?min=${encodeURIComponent(min)}&max=${encodeURIComponent(max)}`); showJSONInModal('Villes du département (plage)', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesDeptTop() {
    const code = document.getElementById('ville-dept-code3').value.trim();
    const n = document.getElementById('ville-dept-topn').value || 10;
    if (!code) return showMessage('Renseignez code', 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/departement/${encodeURIComponent(code)}/top?n=${encodeURIComponent(n)}`); showJSONInModal('Top N villes par département', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesDeptStats() {
    const code = document.getElementById('ville-dept-code4').value.trim();
    if (!code) return showMessage('Renseignez code', 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/departement/${encodeURIComponent(code)}/stats`); showJSONInModal('Stats département', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesDeptPlusPeuplee() {
    const code = document.getElementById('ville-dept-code4').value.trim();
    if (!code) return showMessage('Renseignez code', 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/departement/${encodeURIComponent(code)}/plus-peuplee`); showJSONInModal('Ville la plus peuplée', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesCount() {
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/count`); showJSONInModal('Total villes', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesPaginated() {
    const page = document.getElementById('ville-page').value || 0;
    const size = document.getElementById('ville-size').value || 20;
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&sort=nom`); showJSONInModal('Villes paginées', data); } catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesExportByDept() {
    const code = document.getElementById('ville-dept-code4').value.trim();
    if (!code) return showMessage('Renseignez code', 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/export/departement/${encodeURIComponent(code)}`); showJSONInModal('Export villes du département', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesUpdatePopulation() {
    const id = document.getElementById('ville-update-id').value;
    const nv = document.getElementById('ville-update-pop').value;
    if (!id || !nv) return showMessage(t('validation.required.id.population'), 'warning');
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/${encodeURIComponent(id)}/population?nouveauNb=${encodeURIComponent(nv)}`, { method: 'PUT' }); showJSONInModal('Ville mise à jour', data); await loadVilles(); updateStats();} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesCreationRapide() {
    const nom = document.getElementById('ville-quick-nom').value.trim();
    const pop = document.getElementById('ville-quick-pop').value;
    const dept = document.getElementById('ville-quick-dept').value.trim();
    if (!nom || !pop || !dept) return showMessage(t('validation.required.city.details'), 'warning');
    const url = `${API_ENDPOINTS.villes}/creation-rapide?nom=${encodeURIComponent(nom)}&nbHabitants=${encodeURIComponent(pop)}&codeDepartement=${encodeURIComponent(dept)}`;
    try { showLoading(); const data = await apiFetch(url, { method: 'POST' }); showJSONInModal('Ville créée', data); await loadVilles(); await loadDepartements(); updateStats();} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesImport() {
    const txt = document.getElementById('ville-import-json').value.trim();
    if (!txt) return showMessage(t('validation.required.json'), 'warning');
    let payload;
    try { payload = JSON.parse(txt); } catch (e) { return showMessage(t('validation.json.invalid'), 'error'); }
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/import`, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload)}); showJSONInModal('Import résultat', data); await loadVilles(); await loadDepartements(); updateStats();} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}
async function villesRechercheAvancee() {
    const nom = document.getElementById('ville-adv-nom').value.trim();
    const min = document.getElementById('ville-adv-min').value;
    const max = document.getElementById('ville-adv-max').value;
    const dept = document.getElementById('ville-adv-dept').value.trim();
    const params = new URLSearchParams();
    if (nom) params.set('nom', nom);
    if (min) params.set('minPop', min);
    if (max) params.set('maxPop', max);
    if (dept) params.set('dept', dept);
    try { showLoading(); const data = await apiFetch(`${API_ENDPOINTS.villes}/search/avancee?${params.toString()}`); showJSONInModal('Recherche avancée', data);} catch(e){showMessage(e.message,'error');} finally{hideLoading();}
}

// Expose villes advanced
Object.assign(window, {
    helloPing,
    villesSearchNom, villesSearchNomContient, villesNomCommence,
    villesPopMin, villesPopPlage,
    villesDeptMin, villesDeptPlage, villesDeptTop, villesDeptStats, villesDeptPlusPeuplee,
    villesCount, villesExportByDept, villesUpdatePopulation, villesCreationRapide, villesImport, villesRechercheAvancee,
    villesPaginated,
});

// ==========================================================================
// ROUTES AVANCÉES DÉPARTEMENTS
// ==========================================================================

async function depSearchNom(){ const nom = document.getElementById('dep-search-nom').value.trim(); if(!nom) return showMessage(t('validation.required.name'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/search/nom?nom=${encodeURIComponent(nom)}`); showJSONInModal('Département (nom)', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depAvecNom(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/avec-nom`); showJSONInModal('Départements avec nom', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depSansNom(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/sans-nom`); showJSONInModal('Départements sans nom', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depAvecVilles(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/avec-villes`); showJSONInModal('Départements avec villes', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depMinVilles(){ const min=document.getElementById('dep-min-villes').value; if(!min) return showMessage(t('validation.required.min'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/min-villes?min=${encodeURIComponent(min)}`); showJSONInModal('Départements (min villes)', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depMinPopulation(){ const min=document.getElementById('dep-min-pop').value; if(!min) return showMessage(t('validation.required.min'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/min-population?min=${encodeURIComponent(min)}`); showJSONInModal('Départements (min population)', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depMetropolitains(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/metropolitains`); showJSONInModal('Départements métropolitains', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depOutreMer(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/outre-mer`); showJSONInModal('Départements d\'outre-mer', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depCorse(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/corse`); showJSONInModal('Départements Corse', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depCodeCommence(){ const p=document.getElementById('dep-code-prefix').value.trim(); if(!p) return showMessage(t('validation.required.prefix'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code-commence?prefix=${encodeURIComponent(p)}`); showJSONInModal('Départements code commence', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depVillesByCode(){ const code=document.getElementById('dep-code-villes').value.trim(); if(!code) return showMessage(t('validation.required.code'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/villes`); showJSONInModal('Villes du département', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depTopByCode(){ const code=document.getElementById('dep-code-top').value.trim(); const n=document.getElementById('dep-top-n').value||10; if(!code) return showMessage(t('validation.required.code'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/villes/top?n=${encodeURIComponent(n)}`); showJSONInModal('Top N villes', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depVillesPopMin(){ const code=document.getElementById('dep-code-min').value.trim(); const min=document.getElementById('dep-min').value; if(!code||!min) return showMessage(t('validation.required.code.min'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/villes/population?min=${encodeURIComponent(min)}`); showJSONInModal('Villes min population', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depVillesPopPlage(){ const code=document.getElementById('dep-code-range').value.trim(); const min=document.getElementById('dep-range-min').value; const max=document.getElementById('dep-range-max').value; if(!code||!min||!max) return showMessage('Code, min, max requis','warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/villes/population-plage?min=${encodeURIComponent(min)}&max=${encodeURIComponent(max)}`); showJSONInModal('Villes plage population', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depCount(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/count`); showJSONInModal('Total départements', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depStatsByCode(){ const code=document.getElementById('dep-code-stats').value.trim(); if(!code) return showMessage(t('validation.required.code'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/stats`); showJSONInModal('Stats département', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depPopulationTotaleByCode(){ const code=document.getElementById('dep-code-stats').value.trim(); if(!code) return showMessage(t('validation.required.code'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/population-totale`); showJSONInModal('Population totale', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depNombreVillesByCode(){ const code=document.getElementById('dep-code-stats').value.trim(); if(!code) return showMessage(t('validation.required.code'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/nombre-villes`); showJSONInModal('Nombre de villes', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depCreationRapide(){ const code=document.getElementById('dep-create-code').value.trim(); const nom=document.getElementById('dep-create-nom').value.trim(); if(!code) return showMessage(t('validation.required.code'),'warning'); const params=new URLSearchParams({code}); if(nom) params.set('nom',nom); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/creation-rapide?${params.toString()}`,{method:'POST'}); showJSONInModal('Département créé', d); await loadDepartements(); await loadVilles(); updateStats();}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depUpdateNomByCode(){ const code=document.getElementById('dep-update-code').value.trim(); const nom=document.getElementById('dep-update-nom').value.trim(); if(!code||!nom) return showMessage(t('validation.required.code.name'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/code/${encodeURIComponent(code)}/nom?nom=${encodeURIComponent(nom)}`,{method:'PUT'}); showJSONInModal('Nom mis à jour', d); await loadDepartements();}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depUpdateNomsManquants(){ try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/update-noms-manquants`,{method:'PUT'}); showMessage(t('departments.names.updated'),'success');}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}
async function depExistsByCode(){ const code=document.getElementById('dep-exists-code').value.trim(); if(!code) return showMessage(t('validation.required.code'),'warning'); try{showLoading(); const d=await apiFetch(`${API_ENDPOINTS.departements}/exists/code/${encodeURIComponent(code)}`); showJSONInModal('Existe ?', d);}catch(e){showMessage(e.message,'error');}finally{hideLoading();}}

Object.assign(window, {
    depSearchNom, depAvecNom, depSansNom, depAvecVilles,
    depMinVilles, depMinPopulation,
    depMetropolitains, depOutreMer, depCorse, depCodeCommence,
    depVillesByCode, depTopByCode, depVillesPopMin, depVillesPopPlage,
    depCount, depStatsByCode, depPopulationTotaleByCode, depNombreVillesByCode,
    depCreationRapide, depUpdateNomByCode, depUpdateNomsManquants, depExistsByCode,
});
