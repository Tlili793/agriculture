import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../../auth/LoginController';
import {
  createProduce,
  deleteProduce,
  fetchProduce,
  updateProduce,
} from '../../api/refrigerator';
import DataTable from '../DataTable';
import { formatAmount, formatDate, formatDateTime } from '../../utils/format';

const EMPTY_FORM = {
  userId: '',
  name: '',
  quantity: '',
  unit: 'piece',
  price: '',
  purchaseDate: '',
  expirationDate: '',
};

function formFrom(item) {
  return {
    userId: item.userId ?? '',
    name: item.name ?? '',
    quantity: item.quantity ?? '',
    unit: item.unit ?? 'piece',
    price: item.price ?? '',
    purchaseDate: item.purchaseDate ?? '',
    expirationDate: item.expirationDate ?? '',
  };
}

export default function ProduceResourcePanel({ resource, title, singular }) {
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
      const result = await fetchProduce(resource, { token, keycloak });
      setRows(result ?? []);
      setStatus('success');
    } catch (loadError) {
      setError(loadError.message);
      setStatus('error');
    }
  }, [keycloak, resource, token]);

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

    const produce = {
      userId: form.userId || currentUserId || undefined,
      name: form.name.trim(),
      quantity: form.quantity === '' ? undefined : Number(form.quantity),
      unit: form.unit.trim() || undefined,
      price: form.price === '' ? undefined : Number(form.price),
      purchaseDate: form.purchaseDate || undefined,
      expirationDate: form.expirationDate || undefined,
    };

    try {
      if (editingId === null) {
        await createProduce(resource, { token, keycloak, produce });
      } else {
        await updateProduce(resource, {
          token,
          keycloak,
          id: editingId,
          produce,
        });
      }

      resetForm();
      setSubmitStatus('success');
      await load();
    } catch (failure) {
      setSubmitError(failure.message);
      setSubmitStatus('error');
    }
  };

  const handleEdit = (item) => {
    setForm(formFrom(item));
    setEditingId(item.id);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleDelete = async (item) => {
    if (!window.confirm(`Delete ${singular} "${item.name}"?`)) {
      return;
    }

    setSubmitStatus('loading');
    setSubmitError(null);

    try {
      await deleteProduce(resource, { token, keycloak, id: item.id });
      if (editingId === item.id) {
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
    { key: 'quantity', header: 'Qty', render: (row) => formatAmount(row.quantity) },
    { key: 'unit', header: 'Unit' },
    { key: 'price', header: 'Price', render: (row) => formatAmount(row.price) },
    {
      key: 'purchaseDate',
      header: 'Purchased',
      render: (row) => formatDate(row.purchaseDate),
    },
    {
      key: 'expirationDate',
      header: 'Expires',
      render: (row) => formatDate(row.expirationDate),
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
      <div className="resource-header">
        <div>
          <h3>{editingId === null ? `Add ${singular}` : `Edit ${singular}`}</h3>
          <p className="muted">Manage every field stored in the {title.toLowerCase()} table.</p>
        </div>
      </div>

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
            Quantity
            <input
              name="quantity"
              type="number"
              min="0"
              step="0.01"
              value={form.quantity}
              onChange={updateField}
            />
          </label>
          <label>
            Unit
            <input name="unit" value={form.unit} onChange={updateField} maxLength={20} />
          </label>
          <label>
            Price
            <input
              name="price"
              type="number"
              min="0"
              step="0.01"
              value={form.price}
              onChange={updateField}
            />
          </label>
          <label>
            Purchase date
            <input
              name="purchaseDate"
              type="date"
              value={form.purchaseDate}
              onChange={updateField}
            />
          </label>
          <label>
            Expiration date
            <input
              name="expirationDate"
              type="date"
              value={form.expirationDate}
              onChange={updateField}
            />
          </label>
        </div>

        <div className="form-actions">
          <button type="submit" disabled={submitStatus === 'loading'}>
            {submitStatus === 'loading'
              ? 'Saving...'
              : editingId === null
                ? `Add ${singular}`
                : `Save ${singular}`}
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
        <h3>{title}</h3>
        <DataTable
          columns={columns}
          rows={rows}
          loading={status === 'loading'}
          error={error}
          emptyMessage={`No ${title.toLowerCase()} found.`}
          ariaLabel={`${title} table`}
        />
      </div>
    </div>
  );
}
