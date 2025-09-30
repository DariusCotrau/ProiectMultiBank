const API_BASE = '/api';

const elements = {
    year: document.getElementById('year'),
    syncButton: document.getElementById('sync-button'),
    syncStatus: document.getElementById('sync-status'),
    refreshAccounts: document.getElementById('refresh-accounts'),
    accountsTable: document.querySelector('#accounts-table tbody'),
    transactionAccount: document.getElementById('transaction-account'),
    transactionCategory: document.getElementById('transaction-category'),
    transactionsForm: document.getElementById('transactions-form'),
    transactionsTable: document.querySelector('#transactions-table tbody'),
    refreshAnalytics: document.getElementById('refresh-analytics'),
    analyticsStatus: document.getElementById('analytics-status'),
    savingsForm: document.getElementById('savings-form'),
    savingsList: document.getElementById('savings-list'),
    savingsCategory: document.getElementById('savings-category')
};

const categories = [
    { value: 'GROCERY', label: 'Alimentație' },
    { value: 'UTILITIES', label: 'Utilități' },
    { value: 'TRANSPORT', label: 'Transport' },
    { value: 'ENTERTAINMENT', label: 'Timp liber' },
    { value: 'RENT', label: 'Chirie' },
    { value: 'INCOME', label: 'Venit' },
    { value: 'SAVINGS', label: 'Economii' },
    { value: 'OTHER', label: 'Altele' }
];

const directionLabels = {
    INFLOW: 'Intrare',
    OUTFLOW: 'Ieșire'
};

const state = {
    accounts: [],
    charts: {
        monthly: null,
        category: null
    }
};

if (elements.year) {
    elements.year.textContent = new Date().getFullYear();
}

populateCategoryOptions();

if (elements.syncButton) {
    elements.syncButton.addEventListener('click', handleSync);
}

if (elements.refreshAccounts) {
    elements.refreshAccounts.addEventListener('click', loadAccounts);
}

if (elements.transactionsForm) {
    elements.transactionsForm.addEventListener('submit', event => {
        event.preventDefault();
        loadTransactions();
    });
}

if (elements.refreshAnalytics) {
    elements.refreshAnalytics.addEventListener('click', loadAnalytics);
}

if (elements.savingsForm) {
    elements.savingsForm.addEventListener('submit', handleCreatePlan);
}

// Load initial data
loadAccounts();
loadSavingsPlans();

async function handleSync() {
    setStatus(elements.syncStatus, 'Sincronizarea este în curs...', 'pending');
    elements.syncButton.disabled = true;
    try {
        const response = await fetch(`${API_BASE}/banks/sync`, { method: 'POST' });
        if (!response.ok) {
            throw new Error('Sincronizarea nu a putut fi inițiată.');
        }
        setStatus(elements.syncStatus, 'Sincronizarea a fost inițiată cu succes. Reîncarcă datele în câteva momente.', 'success');
    } catch (error) {
        console.error(error);
        setStatus(elements.syncStatus, error.message || 'A apărut o eroare în timpul sincronizării.', 'error');
    } finally {
        elements.syncButton.disabled = false;
    }
}

async function loadAccounts() {
    if (elements.accountsTable) {
        elements.accountsTable.innerHTML = `<tr><td colspan="5" class="empty">Se încarcă...</td></tr>`;
    }
    try {
        const accounts = await request(`${API_BASE}/banks/accounts`);
        state.accounts = accounts;
        renderAccounts(accounts);
        populateAccountSelects(accounts);
    } catch (error) {
        console.error(error);
        if (elements.accountsTable) {
            elements.accountsTable.innerHTML = `<tr><td colspan="5" class="empty">${error.message}</td></tr>`;
        }
    }
}

function renderAccounts(accounts) {
    if (!elements.accountsTable) return;
    if (!accounts.length) {
        elements.accountsTable.innerHTML = `<tr><td colspan="5" class="empty">Nu există conturi sincronizate momentan.</td></tr>`;
        return;
    }

    elements.accountsTable.innerHTML = accounts.map(account => `
        <tr>
            <td>${sanitize(account.bankName)}</td>
            <td>${sanitize(account.accountNumber)}</td>
            <td>${sanitize(account.iban)}</td>
            <td>${sanitize(account.type)}</td>
            <td>${formatCurrency(account.balance, account.currency)}</td>
        </tr>
    `).join('');
}

function populateAccountSelects(accounts) {
    if (!elements.transactionAccount) return;
    elements.transactionAccount.innerHTML = '<option value="">Toate</option>' + accounts.map(account =>
        `<option value="${account.id}">${sanitize(account.bankName)} - ${sanitize(account.accountNumber)}</option>`
    ).join('');
}

function populateCategoryOptions() {
    if (elements.transactionCategory) {
        elements.transactionCategory.innerHTML = '<option value="">Toate</option>' + categories.map(category =>
            `<option value="${category.value}">${category.label}</option>`
        ).join('');
    }

    if (elements.savingsCategory) {
        elements.savingsCategory.innerHTML = '<option value="">Fără categorie</option>' + categories.map(category =>
            `<option value="${category.value}">${category.label}</option>`
        ).join('');
    }
}

async function loadTransactions() {
    if (!elements.transactionsTable) return;
    elements.transactionsTable.innerHTML = `<tr><td colspan="6" class="empty">Se încarcă...</td></tr>`;

    const params = new URLSearchParams();
    const account = elements.transactionAccount?.value;
    const category = elements.transactionCategory?.value;
    const startDate = document.getElementById('start-date')?.value;
    const endDate = document.getElementById('end-date')?.value;

    if (account) params.append('accountId', account);
    if (category) params.append('category', category);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const query = params.toString() ? `?${params.toString()}` : '';

    try {
        const transactions = await request(`${API_BASE}/transactions${query}`);
        renderTransactions(transactions);
    } catch (error) {
        console.error(error);
        elements.transactionsTable.innerHTML = `<tr><td colspan="6" class="empty">${error.message}</td></tr>`;
    }
}

function renderTransactions(transactions) {
    if (!transactions.length) {
        elements.transactionsTable.innerHTML = `<tr><td colspan="6" class="empty">Nu au fost găsite tranzacții pentru filtrele selectate.</td></tr>`;
        return;
    }

    elements.transactionsTable.innerHTML = transactions.map(tx => `
        <tr>
            <td>${formatDate(tx.bookingDate)}</td>
            <td>${sanitize(tx.description)}</td>
            <td>${sanitize(tx.merchant)}</td>
            <td>${categoryLabel(tx.category)}</td>
            <td>${directionLabels[tx.direction] || tx.direction}</td>
            <td class="${tx.direction === 'INFLOW' ? 'text-success' : 'text-danger'}">${formatCurrency(tx.amount, tx.currency)}</td>
        </tr>
    `).join('');
}

async function loadAnalytics() {
    setStatus(elements.analyticsStatus, 'Se colectează datele pentru rapoarte...', 'pending');
    try {
        const params = new URLSearchParams();
        const account = elements.transactionAccount?.value;
        const category = elements.transactionCategory?.value;
        const startDate = document.getElementById('start-date')?.value;
        const endDate = document.getElementById('end-date')?.value;

        if (account) params.append('accountId', account);
        if (category) params.append('category', category);
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);

        const query = params.toString() ? `?${params.toString()}` : '';
        const [monthly, categoryTotals] = await Promise.all([
            request(`${API_BASE}/analytics/monthly-spending${query}`),
            request(`${API_BASE}/analytics/category-totals${query}`)
        ]);

        const monthlyPoints = Array.isArray(monthly) ? monthly : [];
        const categoryData = categoryTotals && typeof categoryTotals === 'object' ? categoryTotals : {};

        renderMonthlyChart(monthlyPoints);
        renderCategoryChart(categoryData);

        if (!monthlyPoints.length && !Object.keys(categoryData).length) {
            setStatus(elements.analyticsStatus, 'Nu există date de analiză pentru filtrele selectate.', '');
        } else {
            setStatus(elements.analyticsStatus, 'Graficele au fost actualizate.', 'success');
        }
    } catch (error) {
        console.error(error);
        setStatus(elements.analyticsStatus, error.message || 'Nu s-au putut încărca datele de analiză.', 'error');
    }
}

async function handleCreatePlan(event) {
    event.preventDefault();
    const name = document.getElementById('savings-name').value.trim();
    const target = document.getElementById('savings-target').value;
    const date = document.getElementById('savings-date').value;
    const focus = elements.savingsCategory?.value;

    if (!name || !target || !date) {
        return;
    }

    try {
        const payload = {
            name,
            targetAmount: parseFloat(target),
            targetDate: date,
            focusCategory: focus || null
        };
        await request(`${API_BASE}/savings`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        elements.savingsForm.reset();
        populateCategoryOptions();
        loadSavingsPlans();
    } catch (error) {
        alert(error.message || 'Nu s-a putut crea planul.');
    }
}

async function loadSavingsPlans() {
    if (!elements.savingsList) return;
    elements.savingsList.innerHTML = '<p class="empty">Se încarcă planurile existente...</p>';
    try {
        const plans = await request(`${API_BASE}/savings`);
        renderSavings(plans || []);
    } catch (error) {
        console.error(error);
        elements.savingsList.innerHTML = `<p class="empty">${error.message}</p>`;
    }
}

function renderSavings(plans) {
    if (!plans.length) {
        elements.savingsList.innerHTML = '<p class="empty">Nu există planuri salvate momentan.</p>';
        return;
    }

    elements.savingsList.innerHTML = plans.map(plan => {
        const numericProgress = typeof plan.progress === 'number' ? plan.progress : Number(plan.progress || 0);
        const safeProgress = Number.isFinite(numericProgress) ? numericProgress : 0;
        const cappedProgress = Math.min(Math.max(safeProgress, 0), 100);
        return `
        <article class="savings-card">
            <header>
                <h3>${sanitize(plan.name)}</h3>
                ${plan.focusCategory ? `<span class="badge">${categoryLabel(plan.focusCategory)}</span>` : ''}
            </header>
            <div class="savings-meta">
                <span>Țintă: <strong>${formatCurrency(plan.targetAmount, 'RON')}</strong></span>
                <span>Economisit: <strong>${formatCurrency(plan.currentAmount, 'RON')}</strong></span>
                <span>Termen: <strong>${formatDate(plan.targetDate)}</strong></span>
            </div>
            <div class="progress-bar">
                <span style="width: ${cappedProgress}%;"></span>
            </div>
            <p class="status-text">Progres: ${safeProgress.toFixed(2)}%</p>
            <div class="savings-actions">
                <button class="ghost" data-action="contribute" data-id="${plan.id}">Adaugă contribuție</button>
                <button class="ghost" data-action="delete" data-id="${plan.id}">Șterge</button>
            </div>
        </article>
    `;
    }).join('');

    elements.savingsList.querySelectorAll('button[data-action]').forEach(button => {
        button.addEventListener('click', handleSavingsAction);
    });
}

async function handleSavingsAction(event) {
    const action = event.currentTarget.dataset.action;
    const id = event.currentTarget.dataset.id;
    if (!id) return;

    if (action === 'contribute') {
        const value = prompt('Introdu suma contribuției (RON):');
        if (!value) return;
        const amount = parseFloat(value);
        if (Number.isNaN(amount) || amount <= 0) {
            alert('Introdu o sumă validă mai mare decât zero.');
            return;
        }
        try {
            await request(`${API_BASE}/savings/${id}/contribute`, {
                method: 'POST',
                body: JSON.stringify({ amount })
            });
            loadSavingsPlans();
        } catch (error) {
            alert(error.message || 'Nu s-a putut adăuga contribuția.');
        }
    }

    if (action === 'delete') {
        const confirmDelete = confirm('Ești sigur că dorești să ștergi acest plan?');
        if (!confirmDelete) return;
        try {
            await request(`${API_BASE}/savings/${id}`, { method: 'DELETE' });
            loadSavingsPlans();
        } catch (error) {
            alert(error.message || 'Nu s-a putut șterge planul.');
        }
    }
}

function renderMonthlyChart(points) {
    const ctx = document.getElementById('monthly-chart');
    if (!ctx) return;

    const labels = points.map(point => formatYearMonth(point.period));
    const data = points.map(point => Number(point.totalAmount));

    if (state.charts.monthly) {
        state.charts.monthly.data.labels = labels;
        state.charts.monthly.data.datasets[0].data = data;
        state.charts.monthly.update();
    } else {
        state.charts.monthly = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Cheltuieli totale',
                    data,
                    fill: false,
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.2)',
                    tension: 0.3
                }]
            },
            options: {
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        ticks: {
                            callback: value => `${value.toLocaleString('ro-RO')} RON`
                        }
                    }
                }
            }
        });
    }
}

function renderCategoryChart(totals) {
    const ctx = document.getElementById('category-chart');
    if (!ctx) return;

    const entries = Object.entries(totals || {});
    const labels = entries.map(([key]) => categoryLabel(key));
    const data = entries.map(([, value]) => Number(value));

    if (state.charts.category) {
        state.charts.category.data.labels = labels;
        state.charts.category.data.datasets[0].data = data;
        state.charts.category.update();
    } else {
        state.charts.category = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels,
                datasets: [{
                    label: 'Total',
                    data,
                    backgroundColor: [
                        '#2563eb', '#4f46e5', '#0891b2', '#059669', '#f59e0b', '#ef4444', '#6366f1', '#0ea5e9'
                    ]
                }]
            },
            options: {
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }
}

async function request(url, options = {}) {
    const headers = options.headers ? { ...options.headers } : {};
    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(url, { ...options, headers });
    if (!response.ok) {
        let message = `Request failed (${response.status})`;
        try {
            const data = await response.json();
            message = data.message || JSON.stringify(data);
        } catch (error) {
            // ignore parsing error
        }
        throw new Error(message);
    }
    if (response.status === 204) {
        return null;
    }
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}

function setStatus(element, message, type = '') {
    if (!element) return;
    element.textContent = message;
    element.classList.remove('success', 'error');
    if (type === 'success') {
        element.classList.add('success');
    }
    if (type === 'error') {
        element.classList.add('error');
    }
}

function formatCurrency(amount, currency = 'RON') {
    const value = typeof amount === 'number' ? amount : Number(amount);
    if (Number.isNaN(value)) {
        return '-';
    }
    try {
        return new Intl.NumberFormat('ro-RO', { style: 'currency', currency: currency || 'RON' }).format(value);
    } catch (error) {
        return `${value.toFixed(2)} ${currency || 'RON'}`;
    }
}

function formatDate(date) {
    if (!date) return '-';
    const value = Array.isArray(date) ? date.join('-') : date;
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
        return value;
    }
    return parsed.toLocaleDateString('ro-RO', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatYearMonth(value) {
    if (!value) return '';
    if (Array.isArray(value)) {
        const [year, month] = value;
        return `${month.toString().padStart(2, '0')}/${year}`;
    }
    const [year, month] = String(value).split('-');
    if (!year || !month) return value;
    return `${month}/${year}`;
}

function categoryLabel(value) {
    const entry = categories.find(category => category.value === value);
    return entry ? entry.label : (value || '-');
}

function sanitize(value) {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}
