import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../../auth/LoginController';
import {
  createMeal,
  deleteMeal,
  fetchMeals,
  updateMeal,
} from '../../api/refrigerator';
import DataTable from '../DataTable';
import { formatAmount, formatDate, formatDateTime } from '../../utils/format';

const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'];

const EMPTY_FORM = {
  userId: '',
  name: '',
  description: '',
  mealType: 'BREAKFAST',
  plannedDate: '',
  estimatedCost: '',
  instructions: '',
};

function formFrom(meal) {
  return {
    userId: meal.userId ?? '',
    name: meal.name ?? '',
    description: meal.description ?? '',
    mealType: meal.mealType ?? 'BREAKFAST',
    plannedDate: meal.plannedDate ?? '',
    estimatedCost: meal.estimatedCost ?? '',
    instructions: meal.instructions ?? '',
  };
}

export default function MealResourcePanel() {
  const { token, keycloak } = useAuth();
  const currentUserId = keycloak.tokenParsed?.sub;
  const [rows, setRows] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [submitStatus, setSubmitStatus] = useState('idle');
  const [submitError, setSubmitError] = useState(null);

  const load = useCallback(async () => {
    setStatus('loading');
    setError(null);

    try {
      const result = await fetchMeals({ token, keycloak });
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
    setForm({ ...EMPTY_FORM, userId: currentUserId ?? '' });
    setEditingId(null);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitStatus('loading');
    setSubmitError(null);

    const meal = {
      userId: form.userId || currentUserId || undefined,
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      mealType: form.mealType,
      plannedDate: form.plannedDate || undefined,
      estimatedCost:
        form.estimatedCost === '' ? undefined : Number(form.estimatedCost),
      instructions: form.instructions.trim() || undefined,
    };

    try {
      if (editingId === null) {
        await createMeal({ token, keycloak, meal });
      } else {
        await updateMeal({ token, keycloak, id: editingId, meal });
      }

      resetForm();
      setSubmitStatus('success');
      await load();
    } catch (failure) {
      setSubmitError(failure.message);
      setSubmitStatus('error');
    }
  };

  const handleEdit = (meal) => {
    setForm(formFrom(meal));
    setEditingId(meal.id);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleDelete = async (meal) => {
    if (!window.confirm(`Delete meal "${meal.name}"?`)) {
      return;
    }

    setSubmitStatus('loading');
    setSubmitError(null);

    try {
      await deleteMeal({ token, keycloak, id: meal.id });
      if (editingId === meal.id) {
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
    { key: 'name', header: 'Name' },
    {
      key: 'mealType',
      header: 'Type',
      render: (row) => row.mealType?.replaceAll('_', ' '),
    },
    {
      key: 'plannedDate',
      header: 'Planned',
      render: (row) => formatDate(row.plannedDate),
    },
    {
      key: 'estimatedCost',
      header: 'Est. cost',
      render: (row) => formatAmount(row.estimatedCost),
    },
    { key: 'description', header: 'Description' },
    { key: 'instructions', header: 'Instructions' },
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
      <h3>{editingId === null ? 'Add meal' : 'Edit meal'}</h3>
      <p className="muted">Create and maintain planned meals.</p>

      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <label>
            Name
            <input
              name="name"
              value={form.name}
              onChange={updateField}
              required
              maxLength={100}
            />
          </label>
          <label>
            Meal type
            <select name="mealType" value={form.mealType} onChange={updateField}>
              {MEAL_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </label>
          <label>
            Planned date
            <input
              name="plannedDate"
              type="date"
              value={form.plannedDate}
              onChange={updateField}
            />
          </label>
          <label>
            Estimated cost
            <input
              name="estimatedCost"
              type="number"
              min="0"
              step="0.01"
              value={form.estimatedCost}
              onChange={updateField}
            />
          </label>
          <label className="wide-field">
            Description
            <textarea
              name="description"
              rows={3}
              value={form.description}
              onChange={updateField}
            />
          </label>
          <label className="wide-field">
            Instructions
            <textarea
              name="instructions"
              rows={3}
              value={form.instructions}
              onChange={updateField}
            />
          </label>
        </div>

        <div className="form-actions">
          <button type="submit" disabled={submitStatus === 'loading'}>
            {submitStatus === 'loading'
              ? 'Saving...'
              : editingId === null
                ? 'Add meal'
                : 'Save meal'}
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
        <h3>Meals</h3>
        <DataTable
          columns={columns}
          rows={rows}
          loading={status === 'loading'}
          error={error}
          emptyMessage="No meals found."
          ariaLabel="Meals table"
        />
      </div>
    </div>
  );
}
