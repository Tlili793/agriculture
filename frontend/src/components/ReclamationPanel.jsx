import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/LoginController';
import {
  createReclamation,
  deleteReclamation,
  fetchAllReclamations,
  fetchReclamation,
  fetchUserReclamations,
  updateReclamationStatus,
} from '../api/reclamations';

const EMPTY_FORM = {
  orderId: '',
  subject: '',
  description: '',
};

const STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'];

function formatDate(value) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function AdminControls({ reclamation, token, keycloak, onChanged, onDeleted }) {
  const [status, setStatus] = useState(reclamation.status);
  const [adminResponse, setAdminResponse] = useState(reclamation.adminResponse ?? '');
  const [actionStatus, setActionStatus] = useState('idle');
  const [actionError, setActionError] = useState(null);

  const handleUpdate = async (event) => {
    event.preventDefault();
    setActionStatus('loading');
    setActionError(null);

    try {
      await updateReclamationStatus({
        token,
        keycloak,
        id: reclamation.id,
        status,
        adminResponse: adminResponse.trim() || undefined,
      });
      setActionStatus('success');
      await onChanged();
    } catch (updateFailure) {
      setActionError(updateFailure.message);
      setActionStatus('error');
    }
  };

  const handleDelete = async () => {
    if (!window.confirm(`Delete reclamation #${reclamation.id}?`)) {
      return;
    }

    setActionStatus('loading');
    setActionError(null);

    try {
      await deleteReclamation({ token, keycloak, id: reclamation.id });
      await onDeleted();
    } catch (deleteFailure) {
      setActionError(deleteFailure.message);
      setActionStatus('error');
    }
  };

  return (
    <form className="admin-controls" onSubmit={handleUpdate}>
      <h4>Admin response</h4>
      <div className="form-grid">
        <label>
          Status
          <select value={status} onChange={(event) => setStatus(event.target.value)}>
            {STATUSES.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
        <label className="wide-field">
          Answer
          <textarea
            value={adminResponse}
            onChange={(event) => setAdminResponse(event.target.value)}
            rows={3}
            maxLength={1000}
            placeholder="Explain the decision to the consumer"
          />
        </label>
      </div>
      <div className="form-actions">
        <button type="submit" disabled={actionStatus === 'loading'}>
          {actionStatus === 'loading' ? 'Saving...' : 'Save response'}
        </button>
        <button
          type="button"
          className="danger-button"
          onClick={handleDelete}
          disabled={actionStatus === 'loading'}
        >
          Delete
        </button>
      </div>
      {actionStatus === 'error' && <p className="error-text">{actionError}</p>}
      {actionStatus === 'success' && <p className="success-text">Reclamation updated.</p>}
    </form>
  );
}

function ReclamationCard({ reclamation, isAdmin, token, keycloak, onChanged, onDeleted }) {
  return (
    <article className="reclamation-item">
      <div className="reclamation-header">
        <h3>
          #{reclamation.id} · {reclamation.subject}
        </h3>
        <span className={`status-badge status-${reclamation.status.toLowerCase()}`}>
          {reclamation.status}
        </span>
      </div>
      <p>{reclamation.description}</p>
      <dl className="reclamation-meta">
        <div>
          <dt>Order</dt>
          <dd>{reclamation.orderId}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd>{formatDate(reclamation.createdAt)}</dd>
        </div>
        {isAdmin && (
          <div>
            <dt>User ID</dt>
            <dd>{reclamation.userId}</dd>
          </div>
        )}
      </dl>
      {reclamation.adminResponse ? (
        <div className="admin-response">
          <strong>Admin response</strong>
          <p>{reclamation.adminResponse}</p>
        </div>
      ) : (
        <p className="muted">No admin response yet.</p>
      )}
      {isAdmin && (
        <AdminControls
          reclamation={reclamation}
          token={token}
          keycloak={keycloak}
          onChanged={onChanged}
          onDeleted={onDeleted}
        />
      )}
    </article>
  );
}

export default function ReclamationPanel() {
  const { token, keycloak, isAdmin } = useAuth();
  const userId = keycloak.tokenParsed?.sub;
  const [form, setForm] = useState(EMPTY_FORM);
  const [createStatus, setCreateStatus] = useState('idle');
  const [createError, setCreateError] = useState(null);
  const [reclamations, setReclamations] = useState([]);
  const [listStatus, setListStatus] = useState('idle');
  const [listError, setListError] = useState(null);
  const [lookupId, setLookupId] = useState('');
  const [lookupStatus, setLookupStatus] = useState('idle');
  const [lookupError, setLookupError] = useState(null);
  const [lookupResult, setLookupResult] = useState(null);

  const loadReclamations = useCallback(async () => {
    setListStatus('loading');
    setListError(null);

    try {
      const result = isAdmin
        ? await fetchAllReclamations({ token, keycloak })
        : await fetchUserReclamations({ token, keycloak, userId });
      setReclamations(result ?? []);
      setListStatus('success');
    } catch (loadFailure) {
      setListError(loadFailure.message);
      setListStatus('error');
    }
  }, [isAdmin, keycloak, token, userId]);

  useEffect(() => {
    if (token) {
      loadReclamations();
    }
  }, [token, loadReclamations]);

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    setCreateError(null);

    if (!userId) {
      setCreateError('The authenticated user does not have a Keycloak subject.');
      setCreateStatus('error');
      return;
    }

    setCreateStatus('loading');

    try {
      await createReclamation({
        token,
        keycloak,
        reclamation: {
          userId,
          orderId: Number(form.orderId),
          subject: form.subject.trim(),
          description: form.description.trim(),
        },
      });
      setForm(EMPTY_FORM);
      setCreateStatus('success');
      await loadReclamations();
    } catch (createFailure) {
      setCreateError(createFailure.message);
      setCreateStatus('error');
    }
  };

  const handleLookup = async (event) => {
    event.preventDefault();
    setLookupStatus('loading');
    setLookupError(null);
    setLookupResult(null);

    try {
      const result = await fetchReclamation({ token, keycloak, id: lookupId });
      setLookupResult(result);
      setLookupStatus('success');
    } catch (lookupFailure) {
      setLookupError(lookupFailure.message);
      setLookupStatus('error');
    }
  };

  const handleChanged = async () => {
    setLookupResult(null);
    await loadReclamations();
  };

  const handleDeleted = async () => {
    setLookupResult(null);
    await loadReclamations();
  };

  return (
    <section className="microservice-panel">
      <h2>Reclamation service</h2>
      <p className="muted">
        Consumers create complaints for their own orders and follow the admin answer. Admins
        can review every complaint and change its status.
      </p>
      <p className="muted">User ID: {userId ?? 'not available'}</p>

      <form onSubmit={handleCreate}>
        <h3>Create reclamation</h3>
        <div className="form-grid">
          <label>
            Order ID
            <input
              name="orderId"
              type="number"
              min="1"
              value={form.orderId}
              onChange={updateField}
              required
            />
          </label>
          <label className="wide-field">
            Subject
            <input
              name="subject"
              value={form.subject}
              onChange={updateField}
              maxLength={255}
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
              maxLength={1000}
              required
            />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" disabled={createStatus === 'loading'}>
            {createStatus === 'loading' ? 'Creating...' : 'Create reclamation'}
          </button>
        </div>
        {createStatus === 'error' && <p className="error-text">{createError}</p>}
        {createStatus === 'success' && <p className="success-text">Reclamation created.</p>}
      </form>

      {isAdmin && (
        <form onSubmit={handleLookup}>
          <h3>Get reclamation by ID</h3>
          <div className="form-grid">
            <label>
              Reclamation ID
              <input
                type="number"
                min="1"
                value={lookupId}
                onChange={(event) => setLookupId(event.target.value)}
                required
              />
            </label>
          </div>
          <div className="form-actions">
            <button type="submit" disabled={lookupStatus === 'loading'}>
              {lookupStatus === 'loading' ? 'Loading...' : 'Get reclamation'}
            </button>
          </div>
          {lookupStatus === 'error' && <p className="error-text">{lookupError}</p>}
          {lookupStatus === 'success' && lookupResult && (
            <div className="result-block">
              <ReclamationCard
                reclamation={lookupResult}
                isAdmin={isAdmin}
                token={token}
                keycloak={keycloak}
                onChanged={handleChanged}
                onDeleted={handleDeleted}
              />
            </div>
          )}
        </form>
      )}

      <div className="result-block">
        <div className="reclamation-list-header">
          <h3>{isAdmin ? 'All reclamations' : 'My reclamations'}</h3>
          <button
            type="button"
            className="secondary-button"
            onClick={loadReclamations}
            disabled={listStatus === 'loading'}
          >
            Reload
          </button>
        </div>
        {listStatus === 'loading' && <p>Loading reclamations...</p>}
        {listStatus === 'error' && <p className="error-text">{listError}</p>}
        {listStatus === 'success' && reclamations.length === 0 && (
          <p className="muted">No reclamations yet.</p>
        )}
        {listStatus === 'success' && reclamations.length > 0 && (
          <div className="reclamation-list">
            {reclamations.map((reclamation) => (
              <ReclamationCard
                key={reclamation.id}
                reclamation={reclamation}
                isAdmin={isAdmin}
                token={token}
                keycloak={keycloak}
                onChanged={handleChanged}
                onDeleted={handleDeleted}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
