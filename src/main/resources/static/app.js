// Configuration de l'API
const API_BASE_URL = 'http://localhost:8081';
const API_ENDPOINTS = {
    villes: `${API_BASE_URL}/villes`,
    departements: `${API_BASE_URL}/departements`
};

// Variables globales
let currentVilles = [];
let currentDepartements = [];
let editingVille = null;
let editingDepartement = null;
let currentPage = 1;
const itemsPerPage = 10;

// Initialisation de l'application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

async function initializeApp() {
    console.log('Initialisation de l\'application...');
    
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
        showMessage('Erreur lors du chargement des données. Vérifiez que l\'API est démarrée.', 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// GESTION DES DÉPARTEMENTS
// ==========================================================================

async function loadDepartements() {
    try {
        const response = await fetch(API_ENDPOINTS.departements);
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        currentDepartements = await response.json();
        console.log('Départements chargés:', currentDepartements.length);
        renderDepartements();
        return currentDepartements;
    } catch (error) {
        console.error('Erreur lors du chargement des départements:', error);
        showMessage('Impossible de charger les départements', 'error');
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
                    <p>Aucun département trouvé</p>
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
    const selects = document.querySelectorAll('#ville-departement, #departement-filter');
    
    selects.forEach(select => {
        const isFilter = select.id === 'departement-filter';
        const defaultOption = isFilter ? 
            '<option value="">Tous les départements</option>' : 
            '<option value="">-- Sélectionner un département --</option>';
            
        select.innerHTML = defaultOption + currentDepartements.map(dept => 
            `<option value="${dept.id}">${dept.code} - ${dept.nom}</option>`
        ).join('');
    });
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
        showMessage('Département non trouvé', 'error');
        return;
    }
    
    editingDepartement = departement;
    document.getElementById('departement-form-title').innerHTML = 
        '<i class="fas fa-edit"></i> Modifier le département';
    
    document.getElementById('departement-code').value = departement.code;
    document.getElementById('departement-nom').value = departement.nom;
    
    document.getElementById('departement-form-container').classList.remove('hidden');
    document.getElementById('departement-code').focus();
}

async function showDepartementDetails(id) {
    const departement = currentDepartements.find(d => d.id === id);
    if (!departement) {
        showMessage('Département non trouvé', 'error');
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
        showMessage('Département non trouvé', 'error');
        return;
    }
    
    const hasVilles = departement.nombreVilles > 0;
    const message = hasVilles 
        ? `Le département "${departement.nom}" (${departement.code}) contient ${departement.nombreVilles} ville(s). Impossible de le supprimer.`
        : `Êtes-vous sûr de vouloir supprimer le département "${departement.nom}" (${departement.code}) ?`;
    
    document.getElementById('confirm-title').textContent = 'Supprimer le département';
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
        const response = await fetch(`${API_ENDPOINTS.departements}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Erreur HTTP: ${response.status}`);
        }
        
        await loadDepartements();
        await populateDepartementSelects();
        await loadVilles(); // Recharger les villes pour mettre à jour les filtres
        updateStats();
        hideConfirmModal();
        showMessage('Département supprimé avec succès', 'success');
    } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        hideConfirmModal();
        showMessage(`Erreur lors de la suppression: ${error.message}`, 'error');
    } finally {
        hideLoading();
    }
}

// ==========================================================================
// GESTION DES VILLES
// ==========================================================================

async function loadVilles() {
    try {
        const response = await fetch(API_ENDPOINTS.villes);
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        currentVilles = await response.json();
        console.log('Villes chargées:', currentVilles.length);
        renderVilles();
        return currentVilles;
    } catch (error) {
        console.error('Erreur lors du chargement des villes:', error);
        showMessage('Impossible de charger les villes', 'error');
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
        showMessage('Ville non trouvée', 'error');
        return;
    }
    
    editingVille = ville;
    document.getElementById('ville-form-title').innerHTML = 
        '<i class="fas fa-edit"></i> Modifier la ville';
    
    document.getElementById('ville-nom').value = ville.nom;
    document.getElementById('ville-habitants').value = ville.nbHabitants;
    document.getElementById('ville-departement').value = ville.departement?.id || '';
    
    document.getElementById('ville-form-container').classList.remove('hidden');
    document.getElementById('ville-nom').focus();
}

async function showVilleDetails(id) {
    const ville = currentVilles.find(v => v.id === id);
    if (!ville) {
        showMessage('Ville non trouvée', 'error');
        return;
    }
    
    document.getElementById('detail-title').innerHTML = 
        `<i class="fas fa-building"></i> Détails de la ville ${ville.nom}`;
    
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
        showMessage('Ville non trouvée', 'error');
        return;
    }
    
    document.getElementById('confirm-title').textContent = 'Supprimer la ville';
    document.getElementById('confirm-message').textContent = 
        `Êtes-vous sûr de vouloir supprimer la ville "${ville.nom}" (${formatPopulation(ville.nbHabitants)} habitants) ?`;
    
    document.getElementById('confirm-yes').style.display = 'inline-flex';
    document.getElementById('confirm-yes').onclick = () => deleteVille(id);
    document.getElementById('confirm-modal').classList.remove('hidden');
}

async function deleteVille(id) {
    try {
        showLoading();
        const response = await fetch(`${API_ENDPOINTS.villes}/${id}`, {
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
        showMessage('Ville supprimée avec succès', 'success');
    } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        hideConfirmModal();
        showMessage(`Erreur lors de la suppression: ${error.message}`, 'error');
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
    const departementSelectionne = currentDepartements.find(d => d.id === departementId);
    
    if (!departementSelectionne) {
        showMessage('Veuillez sélectionner un département', 'error');
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
        showMessage('Le nom de la ville doit contenir au moins 2 caractères', 'error');
        return;
    }
    
    if (!villeData.nbHabitants || villeData.nbHabitants < 1) {
        showMessage('Le nombre d\'habitants doit être supérieur à 0', 'error');
        return;
    }
    
    
    try {
        showLoading();
        let response;
        
        if (editingVille) {
            // Modification
            response = await fetch(`${API_ENDPOINTS.villes}/${editingVille.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(villeData)
            });
        } else {
            // Création
            response = await fetch(API_ENDPOINTS.villes, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(villeData)
            });
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Erreur HTTP: ${response.status}`);
        }
        
        await loadVilles();
        await loadDepartements(); // Recharger pour mettre à jour les stats
        updateStats();
        
        const action = editingVille ? 'modifiée' : 'créée';
        hideVilleForm();
        showMessage(`Ville ${action} avec succès`, 'success');
        
    } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        showMessage(`Erreur lors de la sauvegarde: ${error.message}`, 'error');
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
        showMessage('Le code du département doit contenir 2 ou 3 caractères', 'error');
        return;
    }
    
    if (!departementData.nom || departementData.nom.length < 2) {
        showMessage('Le nom du département doit contenir au moins 2 caractères', 'error');
        return;
    }
    
    try {
        showLoading();
        let response;
        
        if (editingDepartement) {
            // Modification
            response = await fetch(`${API_ENDPOINTS.departements}/${editingDepartement.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(departementData)
            });
        } else {
            // Création
            response = await fetch(API_ENDPOINTS.departements, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(departementData)
            });
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Erreur HTTP: ${response.status}`);
        }
        
        await loadDepartements();
        await populateDepartementSelects();
        updateStats();
        
        const action = editingDepartement ? 'modifié' : 'créé';
        hideDepartementForm();
        showMessage(`Département ${action} avec succès`, 'success');
        
    } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        showMessage(`Erreur lors de la sauvegarde: ${error.message}`, 'error');
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
    const searchTerm = document.getElementById('ville-search').value.toLowerCase();
    const departementFilter = document.getElementById('departement-filter').value;
    
    const filteredVilles = currentVilles.filter(ville => {
        const matchesSearch = ville.nom.toLowerCase().includes(searchTerm);
        const matchesDepartement = !departementFilter || 
            ville.departement?.id?.toString() === departementFilter;
        
        return matchesSearch && matchesDepartement;
    });
    
    renderFilteredVilles(filteredVilles);
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

function filterDepartements() {
    const searchTerm = document.getElementById('departement-search').value.toLowerCase();
    
    const filteredDepartements = currentDepartements.filter(dept => {
        return dept.nom.toLowerCase().includes(searchTerm) ||
               dept.code.toLowerCase().includes(searchTerm);
    });
    
    renderFilteredDepartements(filteredDepartements);
}

function renderFilteredDepartements(departements) {
    const tbody = document.getElementById('departements-list');
    
    if (!departements.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center" style="padding: 40px;">
                    <i class="fas fa-search" style="font-size: 2rem; color: var(--text-secondary); margin-bottom: 10px;"></i>
                    <p>Aucun département ne correspond aux critères de recherche</p>
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

function sortVilles() {
    const sortBy = document.getElementById('ville-sort').value;
    
    const sorted = [...currentVilles].sort((a, b) => {
        switch (sortBy) {
            case 'nom-asc':
                return a.nom.localeCompare(b.nom);
            case 'nom-desc':
                return b.nom.localeCompare(a.nom);
            case 'population-asc':
                return (a.nbHabitants || 0) - (b.nbHabitants || 0);
            case 'population-desc':
                return (b.nbHabitants || 0) - (a.nbHabitants || 0);
            case 'departement-asc':
                const deptA = a.departement?.nom || '';
                const deptB = b.departement?.nom || '';
                return deptA.localeCompare(deptB);
            case 'departement-desc':
                const deptA2 = a.departement?.nom || '';
                const deptB2 = b.departement?.nom || '';
                return deptB2.localeCompare(deptA2);
            default:
                return 0;
        }
    });
    
    renderFilteredVilles(sorted);
}

function sortDepartements() {
    const sortBy = document.getElementById('departement-sort').value;
    
    const sorted = [...currentDepartements].sort((a, b) => {
        switch (sortBy) {
            case 'nom':
                return a.nom.localeCompare(b.nom);
            case 'code':
                return a.code.localeCompare(b.code);
            case 'population':
                return (b.populationTotale || 0) - (a.populationTotale || 0);
            case 'nombreVilles':
                return (b.nombreVilles || 0) - (a.nombreVilles || 0);
            default:
                return 0;
        }
    });
    
    renderFilteredDepartements(sorted);
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
// UTILITAIRES
// ==========================================================================

function formatPopulation(population) {
    if (!population || population === 0) return '0';
    return population.toLocaleString('fr-FR');
}

// ==========================================================================
// GESTION DES ERREURS GLOBALES
// ==========================================================================

window.addEventListener('unhandledrejection', function(event) {
    console.error('Erreur non gérée:', event.reason);
    showMessage('Une erreur inattendue s\'est produite', 'error');
});

window.addEventListener('error', function(event) {
    console.error('Erreur JavaScript:', event.error);
    showMessage('Une erreur s\'est produite dans l\'application', 'error');
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