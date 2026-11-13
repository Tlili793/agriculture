import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../../auth/LoginController';
import {
  createBudget,
  deleteBudget,
  fetchBudgets,
  updateBudget,
} from '../../api/refrigerator';
import DataTable from '../DataTable';
import { formatAmount, formatDateTime } from '../../utils/format';

const MONTHS = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
];

function emptyBudgetForm(userId = '') {
  const now = new Date();

  return {
    userId,
    monthlyLimit: '',
    month: String(now.getMonth() + 1),
    year: String(now.getFullYear()),
  };
}

function formFrom(budget) {
  return {
    userId: budget.userId ?? '',
    monthlyLimit: budget.monthlyLimit ?? '',
    month: String(budget.month ?? ''),
    year: String(budget.year ?? ''),
  };
}

export default function BudgetResourcePanel() {
  const { token, keycloak } = useAuth();
  const currentUserId = keycloak.tokenParsed?.sub;
  const [rows, setRows] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [form, setForm] = useState(() => emptyBudgetForm(currentUserId));
  const [editingId, setEditingId] = useState(null);
  const [submitStatus, setSubmitStatus] = useState('idle');
  const [submitError, setSubmitError] = useState(null);

  const load = useCallback(async () => {
    setStatus('loading');
    setError(null);

    try {
      const result = await fetchBudgets({ token, keycloak });
      setRows(result ?? []);
      setStatus('success');
    } catch (loadError) {
      setError(loadError.message);
      setStatus('error');
    }
  }, [keycloak, token]);

  useEffect(() => {
    if (token) {
      load();
    }
  }, [token, load]);

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const resetForm = () => {
    setForm(emptyBudgetForm(currentUserId));
    setEditingId(null);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitStatus('loading');
    setSubmitError(null);

    const budget = {
      userId: form.userId || currentUserId || undefined,
      monthlyLimit: Number(form.monthlyLimit),
      month: Number(form.month),
      year: Number(form.year),
    };

    try {
      if (editingId === null) {
        await createBudget({ token, keycloak, budget });
      } else {
        await updateBudget({ token, keycloak, id: editingId, budget });
      }

      resetForm();
      setSubmitStatus('success');
      await load();
    } catch (failure) {
      setSubmitError(failure.message);
      setSubmitStatus('error');
    }
  };

  const handleEdit = (budget) => {
    setForm(formFrom(budget));
    setEditingId(budget.id);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleDelete = async (budget) => {
    if (!window.confirm(`Delete the ${MONTHS[budget.month - 1]} ${budget.year} budget?`)) {
      return;
    }

    setSubmitStatus('loading');
    setSubmitError(null);

    try {
      await deleteBudget({ token, keycloak, id: budget.id });
      if (editingId === budget.id) {
        resetForm();
      }
      setSubmitStatus('success');
      await load();
    } catch (failure) {
      setSubmitError(failure.message);
      setSubmitStatus('error');
    }
  };

  const columns = [
    { key: 'id', header: 'ID' },
    {
      key: 'month',
      header: 'Month',
      render: (row) => MONTHS[row.month - 1] ?? row.month,
    },
    { key: 'year', header: 'Year' },
    {
      key: 'monthlyLimit',
      header: 'Monthly limit',
      render: (row) => formatAmount(row.monthlyLimit),
    },
    { key: 'userId', header: 'Owner' },
    {
      key: 'createdAt',
      header: 'Created',
      render: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'cell-actions',
      render: (row) => (
        <div className="table-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => handleEdit(row)}
            disabled={submitStatus === 'loading'}
          >
            Edit
          </button>
          <button
            type="button"
            className="danger-button"
            onClick={() => handleDelete(row)}
            disabled={submitStatus === 'loading'}
          >
            Delete
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="resource-panel">
      <h3>{editingId === null ? 'Add monthly budget' : 'Edit monthly budget'}</h3>
      <p className="muted">Each month/year combination can have one budget.</p>

      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <label>
            Monthly limit
            <input
              name="monthlyLimit"
              type="number"
              min="0"
              step="0.01"
              value={form.monthlyLimit}
              onChange={updateField}
              required
            />
          </label>
          <label>
            Month
            <select name="month" value={form.month} onChange={updateField} required>
              {MONTHS.map((month, index) => (
                <option key={month} value={index + 1}>
                  {month}
                </option>
              ))}
            </select>
          </label>
          <label>
            Year
            <input
              name="year"
              type="number"
              min="2000"
              value={form.year}
              onChange={updateField}
              required
            />
          </label>
        </div>

        <div className="form-actions">
          <button type="submit" disabled={submitStatus === 'loading'}>
            {submitStatus === 'loading'
              ? 'Saving...'
              : editingId === null
                ? 'Add budget'
                : 'Save budget'}
          </button>
          {editingId !== null && (
            <button
              type="button"
              className="secondary-button"
              onClick={resetForm}
              disabled={submitStatus === 'loading'}
            >
              Cancel
            </button>
          )}
          <button
            type="button"
            className="secondary-button"
            onClick={load}
            disabled={status === 'loading'}
          >
            Reload
          </button>
        </div>

        {submitStatus === 'error' && <p className="error-text">{submitError}</p>}
        {submitStatus === 'success' && <p className="success-text">Saved.</p>}
      </form>

      <div className="result-block">
        <h3>Budgets</h3>
        <DataTable
          columns={columns}
          rows={rows}
          loading={status === 'loading'}
          error={error}
          emptyMessage="No budgets found."
          ariaLabel="Budgets table"
        />
      </div>
    </div>
  );
}
