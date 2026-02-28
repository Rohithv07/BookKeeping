// app.js

// Determine API URL based on current environment (localhost vs github pages)
const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
const API_BASE_URL = isLocalhost ? 'http://localhost:8080/api' : 'https://bookkeeping-eizv.onrender.com/api';

// Extract CSRF Token via Endpoint instead of Cookie (Cross-Origin workaround)
let cachedCsrfToken = '';
function getCsrfToken() {
    return cachedCsrfToken;
}

async function fetchCsrfToken() {
    try {
        const response = await fetch(`${API_BASE_URL}/csrf`, {
            method: 'GET',
            credentials: 'include'
        });
        if (response.ok) {
            const data = await response.json();
            cachedCsrfToken = data.token;
        }
    } catch (e) {
        console.error('Could not fetch CSRF token', e);
    }
}

// DOM Elements
const loginContainer = document.getElementById('loginContainer');
const loginCard = document.getElementById('loginCard');
const signupCard = document.getElementById('signupCard');
const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');
const signupForm = document.getElementById('signupForm');
const signupError = document.getElementById('signupError');
const appContainer = document.getElementById('appContainer');
const logoutBtn = document.getElementById('logoutBtn');

// Toggles
const linkShowSignup = document.getElementById('showSignup');
const linkShowLogin = document.getElementById('showLogin');

const borrowerForm = document.getElementById('borrower-form');
const loanForm = document.getElementById('loan-form');
const borrowerSelect = document.getElementById('loan-borrower'); // Keep original name for consistency with other parts
const loansTableBody = document.getElementById('loans-table-body');
const btnRefresh = document.getElementById('btn-refresh');
const alertsContainer = document.getElementById('alerts-container');
const btnAddBorrower = document.getElementById('btn-add-borrower');
const btnAddLoan = document.getElementById('btn-add-loan');

// Auth Handlers
function showLogin() {
    appContainer.style.display = 'none';
    loginContainer.style.display = 'flex';
    loginCard.style.display = 'block';
    signupCard.style.display = 'none';
}

function showApp() {
    loginContainer.style.display = 'none';
    appContainer.style.display = 'block';
}

// Initialize On Load
document.addEventListener('DOMContentLoaded', () => {
    // Set default date to today for loan form
    document.getElementById('loan-date').valueAsDate = new Date();

    // Fetch initial data (will trigger auth check)
    initializeData();

    // Event Listeners
    borrowerForm.addEventListener('submit', handleBorrowerSubmit);
    loanForm.addEventListener('submit', handleLoanSubmit);
    btnRefresh.addEventListener('click', fetchActiveLoans);

    // Authentication Listeners
    linkShowSignup.addEventListener('click', (e) => {
        e.preventDefault();
        loginCard.style.display = 'none';
        signupCard.style.display = 'block';
        loginForm.reset();
        loginError.style.display = 'none';
    });

    linkShowLogin.addEventListener('click', (e) => {
        e.preventDefault();
        signupCard.style.display = 'none';
        loginCard.style.display = 'block';
        signupForm.reset();
        signupError.style.display = 'none';
    });
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const loginBtn = document.getElementById('loginBtn');
        loginBtn.disabled = true;
        loginBtn.textContent = 'Authenticating...';
        loginError.style.display = 'none';

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    username: loginForm.username.value,
                    password: loginForm.password.value
                })
            });

            if (!response.ok) {
                throw new Error('Invalid credentials');
            }

            const data = await response.json();
            if (data.token) {
                localStorage.setItem('jwtToken', data.token);
            }

            // Successfully logged in
            showApp();
            initializeData(); // Re-fetch data after successful login
        } catch (error) {
            loginError.textContent = error.message.includes('fetch') ? 'Network error. Is backend running?' : 'Invalid username or password.';
            loginError.style.display = 'block';
        } finally {
            loginBtn.disabled = false;
            loginBtn.textContent = 'Login';
        }
    });

    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const signupBtn = document.getElementById('signupBtn');
        signupBtn.disabled = true;
        signupBtn.textContent = 'Creating account...';
        signupError.style.display = 'none';

        try {
            const response = await fetch(`${API_BASE_URL}/auth/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: signupForm['reg-username'].value,
                    password: signupForm['reg-password'].value
                })
            });

            if (!response.ok) {
                const err = await response.json();
                const defaultMsg = 'Failed to create account.';
                const parsedMsg = (typeof err === 'object' && err !== null) ? (err.message || Object.values(err)[0] || defaultMsg) : defaultMsg;
                throw new Error(parsedMsg);
            }

            // Success! Flip to login view.
            signupForm.reset();
            signupCard.style.display = 'none';
            loginCard.style.display = 'block';

            // Re-use our own unified alert box (assuming they aren't completely hidden by login container overlap)
            // But since alerts container is in the hidden app... let's just use native alert for now.
            window.alert('Account created successfully! You may now login.');
        } catch (error) {
            signupError.textContent = error.message;
            signupError.style.display = 'block';
        } finally {
            signupBtn.disabled = false;
            signupBtn.textContent = 'Sign Up';
        }
    });

    logoutBtn.addEventListener('click', async () => {
        try {
            await fetch(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            });
        } finally {
            localStorage.removeItem('jwtToken');
            loginForm.reset();
            showLogin();
            showAlert('You have been safely logged out.', 'success');
        }
    });
});

// Initialize App Data
function initializeData() {
    fetchCsrfToken().then(() => {
        fetchBorrowers();
        fetchActiveLoans();
    });
}

// --- API Calls & Handlers ---

async function fetchBorrowers() {
    try {
        const response = await fetch(`${API_BASE_URL}/borrowers`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                ...getAuthHeaders()
            },
            credentials: 'include'
        });
        if (!response.ok) {
            if (response.status === 401) { showLogin(); return; }
            throw new Error(`Failed to fetch borrowers: ${response.statusText}`);
        }

        const borrowers = await response.json();

        // Populate select dropdown
        borrowerSelect.innerHTML = '<option value="" disabled selected>-- Select a Borrower --</option>';
        if (borrowers.length === 0) {
            borrowerSelect.innerHTML = `<option value="" disabled selected>No borrowers found. Add one first.</option>`;
            return;
        }
        borrowers.forEach(b => {
            const option = document.createElement('option');
            option.value = b.id;
            option.textContent = `${b.name} (${b.email})`;
            borrowerSelect.appendChild(option);
        });
    } catch (error) {
        showAlert('Error loading borrowers. Is the server running?', 'error');
        console.error(error);
    }
}

async function fetchActiveLoans() {
    loansTableBody.innerHTML = `<tr><td colspan="5" class="text-center loading-state">Fetching active loans...</td></tr>`;
    try {
        const response = await fetch(`${API_BASE_URL}/loans`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                ...getAuthHeaders()
            },
            credentials: 'include' // Sent HttpOnly JWT cookie automatically or Bearer
        });

        if (!response.ok) {
            if (response.status === 401) { showLogin(); return; }
            throw new Error(`Failed to fetch active loans: ${response.statusText}`);
        }

        const loans = await response.json();
        renderLoansTable(loans);
        showApp(); // Unhide if successful auth
    } catch (error) {
        loansTableBody.innerHTML = `<tr><td colspan="5" class="text-center empty-state">Error loading connection to server.</td></tr>`;
        console.error(error);
    }
}

async function handleBorrowerSubmit(e) {
    e.preventDefault();
    setLoading(btnAddBorrower, true);

    const borrowerData = {
        name: document.getElementById('borrower-name').value.trim(),
        email: document.getElementById('borrower-email').value.trim(),
        phone: document.getElementById('borrower-phone').value.trim()
    };

    try {
        const response = await fetch(`${API_BASE_URL}/borrowers`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
                ...getAuthHeaders()
            },
            credentials: 'include',
            body: JSON.stringify(borrowerData)
        });

        if (!response.ok) {
            if (response.status === 401) { showLogin(); return; }
            const err = await response.json();
            throw new Error(err.message || 'Validation failed');
        }

        showAlert(`Borrower ${borrowerData.name} added successfully!`, 'success');
        borrowerForm.reset();
        await fetchBorrowers(); // Refresh dropdown
    } catch (error) {
        showAlert(error.message, 'error');
    } finally {
        setLoading(btnAddBorrower, false);
    }
}

async function handleLoanSubmit(e) {
    e.preventDefault();
    setLoading(btnAddLoan, true);

    const loanData = {
        borrowerId: parseInt(document.getElementById('loan-borrower').value, 10),
        amount: parseFloat(document.getElementById('loan-amount').value),
        currency: document.getElementById('loan-currency').value,
        dateLent: document.getElementById('loan-date').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/loans`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
                ...getAuthHeaders()
            },
            credentials: 'include',
            body: JSON.stringify(loanData)
        });

        if (!response.ok) {
            if (response.status === 401) { showLogin(); return; }
            const err = await response.json();
            throw new Error(err.message || 'Validation failed');
        }

        showAlert('Loan recorded successfully!', 'success');
        loanForm.reset();
        document.getElementById('loan-date').valueAsDate = new Date(); // Reset date back to today
        await fetchActiveLoans(); // Refresh table
    } catch (error) {
        showAlert(error.message, 'error');
    } finally {
        setLoading(btnAddLoan, false);
    }
}

// Repay loan (Partial or Full)
async function markLoanRepaid(loanId) {
    const amountStr = prompt('Enter the amount repaid:');
    if (amountStr === null) return; // User cancelled

    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount <= 0) {
        showAlert('Please enter a valid amount greater than 0.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/loans/${loanId}/repay`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeaders()
            },
            body: JSON.stringify({ amount: amount }),
            credentials: 'include'
        });

        if (!response.ok) {
            if (response.status === 401) { showLogin(); return; }
            throw new Error('Failed to process repayment');
        }

        showAlert('Repayment processed successfully.', 'success');
        fetchActiveLoans(); // Refresh table
    } catch (error) {
        console.error('Error processing repayment:', error);
        showAlert('An error occurred while processing the repayment.', 'error');
    }
}

// --- UI Rendering ---

// The populateBorrowerSelect function was integrated directly into fetchBorrowers for the new logic.
// Keeping this empty for now as the new logic is in fetchBorrowers.
function populateBorrowerSelect(borrowers) {
    borrowers.forEach(b => {
        const option = document.createElement('option');
        option.value = b.id;
        option.textContent = `${b.name} (${b.email})`;
        borrowerSelect.appendChild(option);
    });
}

function renderLoansTable(loans) {
    if (loans.length === 0) {
        loansTableBody.innerHTML = `<tr><td colspan="5" class="text-center empty-state">No active loans found. You're all caught up!</td></tr>`;
        return;
    }

    loansTableBody.innerHTML = '';
    loans.forEach(loan => {
        // Safe check for borrower name directly mapped from LoanDto
        const borrowerName = loan.borrowerName || `Borrower #${loan.borrowerId}`;
        const currencyCode = loan.currency || 'USD';
        const amountFormatted = new Intl.NumberFormat(undefined, { style: 'currency', currency: currencyCode }).format(loan.amount);

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${escapeHtml(borrowerName)}</strong></td>
            <td class="text-right">${amountFormatted}</td>
            <td>${formatDate(loan.dateLent)}</td>
            <td>${formatDate(loan.dueDate)}</td>
            <td class="text-center">
                <button class="btn btn-secondary btn-sm" onclick="markLoanRepaid(${loan.id})" aria-label="Repay loan ${loan.id}">
                    <i class="fas fa-money-bill-wave"></i> Repay
                </button>
            </td>
        `;
        loansTableBody.appendChild(row);
    });
}

// --- Utilities ---

function showAlert(message, type) {
    const alertId = Date.now();
    const alertEl = document.createElement('div');
    alertEl.className = `alert alert-${type}`;
    alertEl.id = `alert-${alertId}`;
    alertEl.textContent = message;

    alertsContainer.appendChild(alertEl);

    // Auto dismiss after 5 seconds
    setTimeout(() => {
        const el = document.getElementById(`alert-${alertId}`);
        if (el) {
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 300); // Wait for fade transition
        }
    }, 5000);
}

function setLoading(btnElement, isLoading) {
    if (isLoading) {
        btnElement.dataset.originalText = btnElement.textContent;
        btnElement.textContent = 'Processing...';
        btnElement.disabled = true;
    } else {
        btnElement.textContent = btnElement.dataset.originalText;
        btnElement.disabled = false;
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const parts = dateString.split('-');
    // Ensuring exact local rendering from YYYY-MM-DD string
    return `${parts[1]}/${parts[2]}/${parts[0]}`;
}

function escapeHtml(unsafe) {
    return (unsafe || "").toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function getAuthHeaders() {
    const token = localStorage.getItem('jwtToken');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
}
