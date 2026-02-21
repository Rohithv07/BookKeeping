// app.js

const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const borrowerForm = document.getElementById('borrower-form');
const loanForm = document.getElementById('loan-form');
const borrowerSelect = document.getElementById('loan-borrower');
const loansTableBody = document.getElementById('loans-table-body');
const btnRefresh = document.getElementById('btn-refresh');
const alertsContainer = document.getElementById('alerts-container');
const btnAddBorrower = document.getElementById('btn-add-borrower');
const btnAddLoan = document.getElementById('btn-add-loan');

// Initialize On Load
document.addEventListener('DOMContentLoaded', () => {
    // Set default date to today for loan form
    document.getElementById('loan-date').valueAsDate = new Date();
    
    // Fetch initial data
    fetchBorrowers();
    fetchActiveLoans();

    // Event Listeners
    borrowerForm.addEventListener('submit', handleBorrowerSubmit);
    loanForm.addEventListener('submit', handleLoanSubmit);
    btnRefresh.addEventListener('click', fetchActiveLoans);
});

// --- API Calls & Handlers ---

async function fetchBorrowers() {
    try {
        const response = await fetch(`${API_BASE_URL}/borrowers`);
        if (!response.ok) throw new Error('Failed to fetch borrowers');
        
        const borrowers = await response.json();
        populateBorrowerSelect(borrowers);
    } catch (error) {
        showAlert('Error loading borrowers. Is the server running?', 'error');
        console.error(error);
    }
}

async function fetchActiveLoans() {
    loansTableBody.innerHTML = `<tr><td colspan="5" class="text-center loading-state">Fetching active loans...</td></tr>`;
    try {
        const response = await fetch(`${API_BASE_URL}/loans`);
        if (!response.ok) throw new Error('Failed to fetch loans');
        
        const loans = await response.json();
        renderLoansTable(loans);
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
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(borrowerData)
        });

        if (!response.ok) {
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
        dateLent: document.getElementById('loan-date').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/loans`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(loanData)
        });

        if (!response.ok) {
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

async function handleMarkRepaid(loanId) {
    if (!confirm('Are you sure you want to mark this loan as fully repaid?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/loans/${loanId}/repay`, {
            method: 'PUT'
        });

        if (!response.ok) throw new Error('Failed to update loan status');
        
        showAlert('Loan marked as repaid.', 'success');
        await fetchActiveLoans(); // Refresh table
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

// --- UI Rendering ---

function populateBorrowerSelect(borrowers) {
    if (borrowers.length === 0) {
        borrowerSelect.innerHTML = `<option value="" disabled selected>No borrowers found. Add one first.</option>`;
        return;
    }

    borrowerSelect.innerHTML = `<option value="" disabled selected>-- Select a Borrower --</option>`;
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
        const amountFormatted = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(loan.amount);
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${escapeHtml(borrowerName)}</strong></td>
            <td class="text-right">${amountFormatted}</td>
            <td>${formatDate(loan.dateLent)}</td>
            <td>${formatDate(loan.dueDate)}</td>
            <td class="text-center">
                <button class="btn btn-secondary btn-sm" onclick="handleMarkRepaid(${loan.id})" aria-label="Mark loan ${loan.id} as repaid">
                    Mark Repaid
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
        if(el) {
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
