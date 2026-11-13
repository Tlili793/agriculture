import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/LoginController';
import {
  acceptDeliveryAssignment,
  createDelivery,
  createWarehouse,
  deleteDelivery,
  deleteWarehouse,
  fetchDeliveries,
  fetchWarehouses,
  requestDeliveryAssignment,
  updateDelivery,
  updateDeliveryStatus,
  updateWarehouse,
} from '../api/logistics';
import DataTable from './DataTable';
import ResourceTabs from './ResourceTabs';
import { formatAmount, formatDateTime, formatStatus } from '../utils/format';

const DELIVERY_STATUSES = [
  'PENDING',
  'DISPATCHED',
  'IN_TRANSIT',
  'DELIVERED',
  'CANCELLED',
];

const EMPTY_DELIVERY_FORM = {
  orderId: '',
  destination: '',
};

const EMPTY_WAREHOUSE_FORM = {
  name: '',
  fridgeCapacity: '',
  normalCapacity: '',
  addressLocale: '',
};

function deliveryFormFrom(delivery) {
  return {
    orderId: String(delivery.orderId ?? ''),
    destination: delivery.destination ?? '',
  };
}

function warehouseFormFrom(warehouse) {
  return {
    name: warehouse.name ?? '',
    fridgeCapacity: String(warehouse.fridgeCapacity ?? ''),
    normalCapacity: String(warehouse.normalCapacity ?? ''),
    addressLocale: warehouse.addressLocale ?? '',
  };
}

function DeliveryStatusBadge({ status }) {
  return (
    <span className={`status-badge status-${status.toLowerCase()}`}>
      {formatStatus(status)}
    </span>
  );
}

function DeliveryDetails({
  delivery,
  isAdmin,
  isTransporter,
  userId,
  token,
  keycloak,
  onChanged,
}) {
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState(() => deliveryFormFrom(delivery));
  const [editStatus, setEditStatus] = useState('idle');
  const [editError, setEditError] = useState(null);
  const [status, setStatus] = useState(delivery.status);
  const [statusUpdate, setStatusUpdate] = useState('idle');
  const [statusError, setStatusError] = useState(null);
  const [actionStatus, setActionStatus] = useState('idle');
  const [actionError, setActionError] = useState(null);

  useEffect(() => {
    setStatus(delivery.status);
    if (!editing) {
      setEditForm(deliveryFormFrom(delivery));
    }
  }, [delivery, editing]);

  const hasPendingRequest = Boolean(delivery.requestedTransporterId);
  const isAssigned = Boolean(delivery.assignedTransporterId);
  const pendingByMe = delivery.requestedTransporterId === userId;
  const assignedToMe = delivery.assignedTransporterId === userId;
  const busy = actionStatus === 'loading';

  const updateEditField = (event) => {
    const { name, value } = event.target;
    setEditForm((current) => ({ ...current, [name]: value }));
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();
    setEditStatus('loading');
    setEditError(null);

    try {
      await updateDelivery({
        token,
        keycloak,
        id: delivery.id,
        delivery: {
          orderId: Number(editForm.orderId),
          destination: editForm.destination.trim(),
        },
      });
      setEditing(false);
      setEditStatus('success');
      await onChanged();
    } catch (failure) {
      setEditError(failure.message);
      setEditStatus('error');
    }
  };

  const handleStatusSubmit = async (event) => {
    event.preventDefault();
    setStatusUpdate('loading');
    setStatusError(null);

    try {
      await updateDeliveryStatus({
        token,
        keycloak,
        id: delivery.id,
        status,
      });
      setStatusUpdate('success');
      await onChanged();
    } catch (failure) {
      setStatusError(failure.message);
      setStatusUpdate('error');
    }
  };

  const runAction = async (action) => {
    setActionStatus('loading');
    setActionError(null);

    try {
      await action();
      setActionStatus('success');
      await onChanged();
    } catch (failure) {
      setActionError(failure.message);
      setActionStatus('error');
    }
  };

  const handleAcceptAssignment = () =>
    runAction(() =>
      acceptDeliveryAssignment({ token, keycloak, id: delivery.id }),
    );

  const handleDelete = () => {
    if (!window.confirm(`Delete delivery #${delivery.id}?`)) {
      return;
    }

    runAction(() => deleteDelivery({ token, keycloak, id: delivery.id }));
  };

  const handleRequestAssignment = () => {
    if (!userId) {
      setActionError('The authenticated user does not have a Keycloak subject.');
      setActionStatus('error');
      return;
    }

    runAction(() =>
      requestDeliveryAssignment({
        token,
        keycloak,
        id: delivery.id,
        transporterId: userId,
      }),
    );
  };

  return (
    <div className="expanded-details">
      <dl className="delivery-meta">
        <div>
          <dt>Dispatched</dt>
          <dd>{formatDateTime(delivery.dispatchedAt)}</dd>
        </div>
        <div>
          <dt>Requested by</dt>
          <dd>{delivery.requestedTransporterId || '-'}</dd>
        </div>
        <div>
          <dt>Requested at</dt>
          <dd>{formatDateTime(delivery.assignmentRequestedAt)}</dd>
        </div>
        <div>
          <dt>Assigned to</dt>
          <dd>{delivery.assignedTransporterId || '-'}</dd>
        </div>
        <div>
          <dt>Assigned at</dt>
          <dd>{formatDateTime(delivery.assignedAt)}</dd>
        </div>
      </dl>

      {isTransporter && !isAssigned && !hasPendingRequest && (
        <div className="assignment-actions">
          <button type="button" onClick={handleRequestAssignment} disabled={busy}>
            {busy ? 'Requesting...' : 'Request assignment'}
          </button>
        </div>
      )}
      {isTransporter && hasPendingRequest && pendingByMe && (
        <p className="assignment-note assignment-pending">
          Your assignment request is pending admin approval.
        </p>
      )}
      {isTransporter && hasPendingRequest && !pendingByMe && (
        <p className="assignment-note assignment-pending">
          Another transporter has requested this delivery.
        </p>
      )}
      {isTransporter && isAssigned && assignedToMe && (
        <p className="assignment-note assignment-assigned">
          This delivery is assigned to you.
        </p>
      )}
      {isTransporter && isAssigned && !assignedToMe && (
        <p className="assignment-note">
          This delivery is assigned to another transporter.
        </p>
      )}
      {isTransporter && actionError && <p className="error-text">{actionError}</p>}
      {isTransporter && !isAdmin && actionStatus === 'success' && (
        <p className="success-text">Assignment request sent.</p>
      )}

      {isAdmin && (
        <>
          <form className="delivery-status-form" onSubmit={handleStatusSubmit}>
            <label>
              Status
              <select value={status} onChange={(event) => setStatus(event.target.value)}>
                {DELIVERY_STATUSES.map((option) => (
                  <option key={option} value={option}>
                    {formatStatus(option)}
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" disabled={statusUpdate === 'loading' || busy}>
              {statusUpdate === 'loading' ? 'Updating...' : 'Update status'}
            </button>
          </form>
          {statusUpdate === 'error' && <p className="error-text">{statusError}</p>}

          <div className="delivery-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={() => setEditing((current) => !current)}
              disabled={busy}
            >
              {editing ? 'Close editor' : 'Edit details'}
            </button>
            {hasPendingRequest && !isAssigned && (
              <button type="button" onClick={handleAcceptAssignment} disabled={busy}>
                {busy ? 'Accepting...' : 'Accept assignment'}
              </button>
            )}
            <button
              type="button"
              className="danger-button"
              onClick={handleDelete}
              disabled={busy}
            >
              Delete
            </button>
          </div>
          {actionError && <p className="error-text">{actionError}</p>}
          {actionStatus === 'success' && (
            <p className="success-text">Delivery updated.</p>
          )}

          {editing && (
            <form className="inline-edit-form" onSubmit={handleEditSubmit}>
              <div className="form-grid">
                <label>
                  Order ID
                  <input
                    name="orderId"
                    type="number"
                    min="1"
                    value={editForm.orderId}
                    onChange={updateEditField}
                    required
                  />
                </label>
                <label className="wide-field">
                  Destination
                  <input
                    name="destination"
                    value={editForm.destination}
                    onChange={updateEditField}
                    maxLength={255}
                    required
                  />
                </label>
              </div>
              <div className="form-actions">
                <button type="submit" disabled={editStatus === 'loading'}>
                  {editStatus === 'loading' ? 'Saving...' : 'Save details'}
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setEditing(false)}
                  disabled={editStatus === 'loading'}
                >
                  Cancel
                </button>
              </div>
              {editStatus === 'error' && <p className="error-text">{editError}</p>}
            </form>
          )}
        </>
      )}
    </div>
  );
}

export default function LogisticsPanel() {
  const { token, keycloak, isAdmin, isTransporter } = useAuth();
  const userId = keycloak.tokenParsed?.sub;
  const canView = isAdmin || isTransporter;

  const [activeResource, setActiveResource] = useState('deliveries');
  const [expandedDeliveryIds, setExpandedDeliveryIds] = useState([]);

  const [deliveries, setDeliveries] = useState([]);
  const [deliveryListStatus, setDeliveryListStatus] = useState('idle');
  const [deliveryListError, setDeliveryListError] = useState(null);
  const [deliveryFilters, setDeliveryFilters] = useState({ status: '', orderId: '' });
  const [newDeliveryForm, setNewDeliveryForm] = useState(EMPTY_DELIVERY_FORM);
  const [createStatus, setCreateStatus] = useState('idle');
  const [createError, setCreateError] = useState(null);

  const [warehouses, setWarehouses] = useState([]);
  const [warehouseListStatus, setWarehouseListStatus] = useState('idle');
  const [warehouseListError, setWarehouseListError] = useState(null);
  const [warehouseForm, setWarehouseForm] = useState(EMPTY_WAREHOUSE_FORM);
  const [editingWarehouseId, setEditingWarehouseId] = useState(null);
  const [warehouseSubmitStatus, setWarehouseSubmitStatus] = useState('idle');
  const [warehouseSubmitError, setWarehouseSubmitError] = useState(null);
  const [warehouseActionStatus, setWarehouseActionStatus] = useState('idle');
  const [warehouseActionError, setWarehouseActionError] = useState(null);

  const tabs = [
    { id: 'deliveries', label: 'Deliveries' },
    ...(isAdmin ? [{ id: 'warehouses', label: 'Warehouses' }] : []),
  ];

  const loadDeliveries = useCallback(
    async (filters = {}) => {
      if (!canView) {
        return;
      }

      setDeliveryListStatus('loading');
      setDeliveryListError(null);

      try {
        const result = await fetchDeliveries({
          token,
          keycloak,
          status: filters.status || undefined,
          orderId: filters.orderId || undefined,
        });
        setDeliveries(result ?? []);
        setDeliveryListStatus('success');
      } catch (failure) {
        setDeliveryListError(failure.message);
        setDeliveryListStatus('error');
      }
    },
    [canView, keycloak, token],
  );

  const loadWarehouses = useCallback(async () => {
    if (!isAdmin) {
      return;
    }

    setWarehouseListStatus('loading');
    setWarehouseListError(null);

    try {
      const result = await fetchWarehouses({ token, keycloak });
      setWarehouses(result ?? []);
      setWarehouseListStatus('success');
    } catch (failure) {
      setWarehouseListError(failure.message);
      setWarehouseListStatus('error');
    }
  }, [isAdmin, keycloak, token]);

  useEffect(() => {
    if (token && canView) {
      loadDeliveries({});
    }
  }, [token, canView, loadDeliveries]);

  useEffect(() => {
    if (token && isAdmin) {
      loadWarehouses();
    }
  }, [token, isAdmin, loadWarehouses]);

  const handleDeliveriesChanged = useCallback(
    () => loadDeliveries(deliveryFilters),
    [deliveryFilters, loadDeliveries],
  );

  const toggleDelivery = (id) => {
    setExpandedDeliveryIds((current) =>
      current.includes(id) ? current.filter((value) => value !== id) : [...current, id],
    );
  };

  const updateDeliveryField = (event) => {
    const { name, value } = event.target;
    setNewDeliveryForm((current) => ({ ...current, [name]: value }));
  };

  const handleFilterSubmit = (event) => {
    event.preventDefault();
    loadDeliveries(deliveryFilters);
  };

  const handleClearFilters = () => {
    const cleared = { status: '', orderId: '' };
    setDeliveryFilters(cleared);
    loadDeliveries(cleared);
  };

  const handleCreateDelivery = async (event) => {
    event.preventDefault();
    setCreateStatus('loading');
    setCreateError(null);

    try {
      await createDelivery({
        token,
        keycloak,
        delivery: {
          orderId: Number(newDeliveryForm.orderId),
          destination: newDeliveryForm.destination.trim(),
        },
      });
      setNewDeliveryForm(EMPTY_DELIVERY_FORM);
      setCreateStatus('success');
      await loadDeliveries(deliveryFilters);
    } catch (failure) {
      setCreateError(failure.message);
      setCreateStatus('error');
    }
  };

  const updateWarehouseField = (event) => {
    const { name, value } = event.target;
    setWarehouseForm((current) => ({ ...current, [name]: value }));
  };

  const resetWarehouseForm = () => {
    setWarehouseForm(EMPTY_WAREHOUSE_FORM);
    setEditingWarehouseId(null);
    setWarehouseSubmitStatus('idle');
    setWarehouseSubmitError(null);
  };

  const handleEditWarehouse = (warehouse) => {
    setWarehouseForm(warehouseFormFrom(warehouse));
    setEditingWarehouseId(warehouse.id);
    setWarehouseSubmitStatus('idle');
    setWarehouseSubmitError(null);
    setWarehouseActionStatus('idle');
    setWarehouseActionError(null);
  };

  const handleWarehouseSubmit = async (event) => {
    event.preventDefault();
    setWarehouseSubmitStatus('loading');
    setWarehouseSubmitError(null);

    const warehouse = {
      name: warehouseForm.name.trim(),
      fridgeCapacity: Number(warehouseForm.fridgeCapacity),
      normalCapacity: Number(warehouseForm.normalCapacity),
      addressLocale: warehouseForm.addressLocale.trim(),
    };

    try {
      if (editingWarehouseId === null) {
        await createWarehouse({ token, keycloak, warehouse });
      } else {
        await updateWarehouse({
          token,
          keycloak,
          id: editingWarehouseId,
          warehouse,
        });
      }
      resetWarehouseForm();
      setWarehouseSubmitStatus('success');
      await loadWarehouses();
    } catch (failure) {
      setWarehouseSubmitError(failure.message);
      setWarehouseSubmitStatus('error');
    }
  };

  const handleDeleteWarehouse = async (warehouse) => {
    if (!window.confirm(`Delete warehouse "${warehouse.name}"?`)) {
      return;
    }

    setWarehouseActionStatus('loading');
    setWarehouseActionError(null);

    try {
      await deleteWarehouse({ token, keycloak, id: warehouse.id });
      if (editingWarehouseId === warehouse.id) {
        resetWarehouseForm();
      }
      setWarehouseActionStatus('success');
      await loadWarehouses();
    } catch (failure) {
      setWarehouseActionError(failure.message);
      setWarehouseActionStatus('error');
    }
  };

  if (!canView) {
    return null;
  }

  const deliveryColumns = [
    { key: 'id', header: 'ID' },
    { key: 'orderId', header: 'Order' },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <DeliveryStatusBadge status={row.status} />,
    },
    { key: 'destination', header: 'Destination' },
    {
      key: 'dispatchedAt',
      header: 'Dispatched',
      render: (row) => formatDateTime(row.dispatchedAt),
    },
    {
      key: 'requestedTransporterId',
      header: 'Requested by',
      render: (row) => row.requestedTransporterId || '-',
    },
    {
      key: 'assignedTransporterId',
      header: 'Assigned to',
      render: (row) => row.assignedTransporterId || '-',
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'cell-actions',
      render: (row) => (
        <button
          type="button"
          className="secondary-button"
          onClick={() => toggleDelivery(row.id)}
        >
          {expandedDeliveryIds.includes(row.id) ? 'Hide' : 'Details'}
        </button>
      ),
    },
  ];

  const warehouseColumns = [
    { key: 'id', header: 'ID' },
    { key: 'name', header: 'Name' },
    {
      key: 'fridgeCapacity',
      header: 'Fridge capacity',
      render: (row) => formatAmount(row.fridgeCapacity),
    },
    {
      key: 'normalCapacity',
      header: 'Normal capacity',
      render: (row) => formatAmount(row.normalCapacity),
    },
    { key: 'addressLocale', header: 'Address' },
    {
      key: 'actions',
      header: 'Actions',
      className: 'cell-actions',
      render: (row) => (
        <div className="table-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => handleEditWarehouse(row)}
            disabled={warehouseActionStatus === 'loading'}
          >
            Edit
          </button>
          <button
            type="button"
            className="danger-button"
            onClick={() => handleDeleteWarehouse(row)}
            disabled={warehouseActionStatus === 'loading'}
          >
            Delete
          </button>
        </div>
      ),
    },
  ];

  return (
    <section className="microservice-panel">
      <h2>Logistics service</h2>
      <p className="muted">
        {isAdmin
          ? 'Manage deliveries and warehouses, change delivery statuses, and accept transporter requests.'
          : 'Review deliveries and request an assignment for your own transporter account.'}
      </p>
      <p className="muted">User ID: {userId ?? 'not available'}</p>

      <ResourceTabs
        tabs={tabs}
        activeId={activeResource}
        onChange={setActiveResource}
        ariaLabel="Logistics resources"
      />

      {activeResource === 'deliveries' && (
        <>
          {isAdmin && (
            <form onSubmit={handleCreateDelivery}>
              <h3>Create delivery</h3>
              <div className="form-grid">
                <label>
                  Order ID
                  <input
                    name="orderId"
                    type="number"
                    min="1"
                    value={newDeliveryForm.orderId}
                    onChange={updateDeliveryField}
                    required
                  />
                </label>
                <label className="wide-field">
                  Destination
                  <input
                    name="destination"
                    value={newDeliveryForm.destination}
                    onChange={updateDeliveryField}
                    maxLength={255}
                    required
                  />
                </label>
              </div>
              <div className="form-actions">
                <button type="submit" disabled={createStatus === 'loading'}>
                  {createStatus === 'loading' ? 'Creating...' : 'Create delivery'}
                </button>
              </div>
              {createStatus === 'error' && <p className="error-text">{createError}</p>}
              {createStatus === 'success' && (
                <p className="success-text">Delivery created.</p>
              )}
            </form>
          )}

          <form onSubmit={handleFilterSubmit}>
            <h3>Filter deliveries</h3>
            <div className="form-grid">
              <label>
                Status
                <select
                  name="status"
                  value={deliveryFilters.status}
                  onChange={(event) =>
                    setDeliveryFilters((current) => ({
                      ...current,
                      status: event.target.value,
                    }))
                  }
                >
                  <option value="">All statuses</option>
                  {DELIVERY_STATUSES.map((option) => (
                    <option key={option} value={option}>
                      {formatStatus(option)}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Order ID
                <input
                  name="orderId"
                  type="number"
                  min="1"
                  value={deliveryFilters.orderId}
                  onChange={(event) =>
                    setDeliveryFilters((current) => ({
                      ...current,
                      orderId: event.target.value,
                    }))
                  }
                />
              </label>
            </div>
            <div className="form-actions">
              <button type="submit" disabled={deliveryListStatus === 'loading'}>
                Apply filters
              </button>
              <button
                type="button"
                className="secondary-button"
                onClick={handleClearFilters}
                disabled={deliveryListStatus === 'loading'}
              >
                Clear filters
              </button>
            </div>
          </form>

          <div className="result-block">
            <div className="reclamation-list-header">
              <h3>{isAdmin ? 'All deliveries' : 'Deliveries'}</h3>
              <button
                type="button"
                className="secondary-button"
                onClick={() => loadDeliveries(deliveryFilters)}
                disabled={deliveryListStatus === 'loading'}
              >
                Reload
              </button>
            </div>
            <DataTable
              columns={deliveryColumns}
              rows={deliveries}
              loading={deliveryListStatus === 'loading'}
              error={deliveryListError}
              emptyMessage="No deliveries match the current filters."
              expandedRowKeys={expandedDeliveryIds}
              renderExpandedRow={(delivery) => (
                <DeliveryDetails
                  delivery={delivery}
                  isAdmin={isAdmin}
                  isTransporter={isTransporter}
                  userId={userId}
                  token={token}
                  keycloak={keycloak}
                  onChanged={handleDeliveriesChanged}
                />
              )}
              ariaLabel="Deliveries table"
            />
          </div>
        </>
      )}

      {activeResource === 'warehouses' && isAdmin && (
        <>
          <h3>{editingWarehouseId === null ? 'Create warehouse' : 'Edit warehouse'}</h3>
          <form onSubmit={handleWarehouseSubmit}>
            <div className="form-grid">
              <label className="wide-field">
                Name
                <input
                  name="name"
                  value={warehouseForm.name}
                  onChange={updateWarehouseField}
                  maxLength={100}
                  required
                />
              </label>
              <label>
                Fridge capacity
                <input
                  name="fridgeCapacity"
                  type="number"
                  min="0"
                  step="0.01"
                  value={warehouseForm.fridgeCapacity}
                  onChange={updateWarehouseField}
                  required
                />
              </label>
              <label>
                Normal capacity
                <input
                  name="normalCapacity"
                  type="number"
                  min="0"
                  step="0.01"
                  value={warehouseForm.normalCapacity}
                  onChange={updateWarehouseField}
                  required
                />
              </label>
              <label className="wide-field">
                Address
                <input
                  name="addressLocale"
                  value={warehouseForm.addressLocale}
                  onChange={updateWarehouseField}
                  maxLength={255}
                  required
                />
              </label>
            </div>
            <div className="form-actions">
              <button type="submit" disabled={warehouseSubmitStatus === 'loading'}>
                {warehouseSubmitStatus === 'loading'
                  ? 'Saving...'
                  : editingWarehouseId === null
                    ? 'Create warehouse'
                    : 'Save warehouse'}
              </button>
              {editingWarehouseId !== null && (
                <button
                  type="button"
                  className="secondary-button"
                  onClick={resetWarehouseForm}
                  disabled={warehouseSubmitStatus === 'loading'}
                >
                  Cancel
                </button>
              )}
            </div>
            {warehouseSubmitStatus === 'error' && (
              <p className="error-text">{warehouseSubmitError}</p>
            )}
            {warehouseSubmitStatus === 'success' && (
              <p className="success-text">Warehouse saved.</p>
            )}
          </form>

          <div className="result-block">
            <div className="reclamation-list-header">
              <h3>Warehouses</h3>
              <button
                type="button"
                className="secondary-button"
                onClick={loadWarehouses}
                disabled={warehouseListStatus === 'loading'}
              >
                Reload
              </button>
            </div>
            {warehouseActionError && (
              <p className="error-text">{warehouseActionError}</p>
            )}
            {warehouseActionStatus === 'success' && (
              <p className="success-text">Warehouse deleted.</p>
            )}
            <DataTable
              columns={warehouseColumns}
              rows={warehouses}
              loading={warehouseListStatus === 'loading'}
              error={warehouseListError}
              emptyMessage="No warehouses found."
              ariaLabel="Warehouses table"
            />
          </div>
        </>
      )}
    </section>
  );
}
