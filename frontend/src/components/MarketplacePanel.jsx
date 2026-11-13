import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/LoginController';
import {
  createListing,
  deleteListing,
  fetchListings,
  updateListing,
} from '../api/marketplace';
import DataTable from './DataTable';
import { formatAmount, formatDateTime } from '../utils/format';

const EMPTY_FORM = {
  userId: '',
  title: '',
  description: '',
  price: '',
  quantity: '1',
};

function formFrom(listing) {
  return {
    userId: listing.userId ?? '',
    title: listing.title ?? '',
    description: listing.description ?? '',
    price: listing.price ?? '',
    quantity: String(listing.quantity ?? ''),
  };
}

export default function MarketplacePanel() {
  const { token, keycloak } = useAuth();
  const userId = keycloak.tokenParsed?.sub;
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
      const result = await fetchListings({ token, keycloak });
      setRows(result ?? []);
      setStatus('success');
    } catch (failure) {
      setError(failure.message);
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
    setForm({ ...EMPTY_FORM, userId: userId ?? '' });
    setEditingId(null);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitStatus('loading');
    setSubmitError(null);

    if (!userId) {
      setSubmitError('The authenticated user does not have a Keycloak subject.');
      setSubmitStatus('error');
      return;
    }

    const listing = {
      userId: form.userId || userId,
      title: form.title.trim(),
      description: form.description.trim() || undefined,
      price: Number(form.price),
      quantity: Number(form.quantity),
    };

    try {
      if (editingId === null) {
        await createListing({ token, keycloak, listing });
      } else {
        await updateListing({ token, keycloak, id: editingId, listing });
      }

      resetForm();
      setSubmitStatus('success');
      await load();
    } catch (failure) {
      setSubmitError(failure.message);
      setSubmitStatus('error');
    }
  };

  const handleEdit = (listing) => {
    setForm(formFrom(listing));
    setEditingId(listing.id);
    setSubmitStatus('idle');
    setSubmitError(null);
  };

  const handleDelete = async (listing) => {
    if (!userId) {
      setSubmitError('The authenticated user does not have a Keycloak subject.');
      setSubmitStatus('error');
      return;
    }

    if (!window.confirm(`Delete listing "${listing.title}"?`)) {
      return;
    }

    setSubmitStatus('loading');
    setSubmitError(null);

    try {
      await deleteListing({
        token,
        keycloak,
        id: listing.id,
        userId,
      });
      if (editingId === listing.id) {
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
    { key: 'title', header: 'Title' },
    { key: 'description', header: 'Description', render: (row) => row.description || '-' },
    {
      key: 'price',
      header: 'Price',
      render: (row) => formatAmount(row.price),
    },
    { key: 'quantity', header: 'Quantity' },
    { key: 'userId', header: 'Owner' },
    {
      key: 'updatedAt',
      header: 'Updated',
      render: (row) => formatDateTime(row.updatedAt),
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'cell-actions',
      render: (row) =>
        row.userId === userId ? (
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
        ) : (
          <span className="muted">Owner only</span>
        ),
    },
  ];

  return (
    <section className="microservice-panel">
      <h2>Marketplace service</h2>
      <p className="muted">
        Browse every marketplace listing. Listings can only be edited or deleted by their
        owner.
      </p>
      <p className="muted">User ID: {userId ?? 'not available'}</p>

      <form onSubmit={handleSubmit}>
        <h3>{editingId === null ? 'Create listing' : 'Edit listing'}</h3>
        <div className="form-grid">
          <label>
            Title
            <input
              name="title"
              value={form.title}
              onChange={updateField}
              maxLength={150}
              required
            />
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
              required
            />
          </label>
          <label>
            Quantity
            <input
              name="quantity"
              type="number"
              min="0"
              value={form.quantity}
              onChange={updateField}
              required
            />
          </label>
          <label className="wide-field">
            Description
            <textarea
              name="description"
              value={form.description}
              onChange={updateField}
              rows={3}
              maxLength={2000}
            />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" disabled={submitStatus === 'loading'}>
            {submitStatus === 'loading'
              ? 'Saving...'
              : editingId === null
                ? 'Create listing'
                : 'Save listing'}
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
        <h3>All listings</h3>
        <DataTable
          columns={columns}
          rows={rows}
          loading={status === 'loading'}
          error={error}
          emptyMessage="No marketplace listings found."
          ariaLabel="Marketplace listings table"
        />
      </div>
    </section>
  );
}
