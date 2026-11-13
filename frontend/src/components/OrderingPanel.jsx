import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/LoginController';
import { createOrder, fetchOrder, fetchUserOrders } from '../api/ordering';
import DataTable from './DataTable';
import { formatAmount, formatDateTime, formatStatus } from '../utils/format';

const EMPTY_ORDER_FORM = {
  productId: '1',
  quantity: '1',
  unitPrice: '10.00',
};

const ITEM_COLUMNS = [
  { key: 'id', header: 'Item ID' },
  { key: 'productId', header: 'Product ID' },
  { key: 'quantity', header: 'Quantity' },
  {
    key: 'unitPrice',
    header: 'Unit price',
    render: (item) => formatAmount(item.unitPrice),
  },
  {
    key: 'lineTotal',
    header: 'Line total',
    render: (item) => formatAmount(Number(item.quantity) * Number(item.unitPrice)),
  },
];

function OrderItems({ items }) {
  return (
    <div className="nested-table-block">
      <h4>Order items</h4>
      <DataTable
        columns={ITEM_COLUMNS}
        rows={items ?? []}
        emptyMessage="This order has no items."
        ariaLabel="Order items table"
      />
    </div>
  );
}

export default function OrderingPanel() {
  const { token, keycloak } = useAuth();
  const userId = keycloak.tokenParsed?.sub;
  const [orderForm, setOrderForm] = useState(EMPTY_ORDER_FORM);
  const [createStatus, setCreateStatus] = useState('idle');
  const [createError, setCreateError] = useState(null);

  const [orders, setOrders] = useState([]);
  const [listStatus, setListStatus] = useState('idle');
  const [listError, setListError] = useState(null);
  const [expandedOrderIds, setExpandedOrderIds] = useState([]);

  const [lookupId, setLookupId] = useState('');
  const [lookupStatus, setLookupStatus] = useState('idle');
  const [lookupError, setLookupError] = useState(null);
  const [lookupResult, setLookupResult] = useState(null);

  const loadOrders = useCallback(async () => {
    setListError(null);

    if (!userId) {
      setListError('The authenticated user does not have a Keycloak subject.');
      setListStatus('error');
      return;
    }

    setListStatus('loading');

    try {
      const result = await fetchUserOrders({ token, keycloak, userId });
      setOrders(result ?? []);
      setListStatus('success');
    } catch (failure) {
      setListError(failure.message);
      setListStatus('error');
    }
  }, [keycloak, token, userId]);

  useEffect(() => {
    if (token) {
      loadOrders();
    }
  }, [token, loadOrders]);

  const updateOrderField = (event) => {
    const { name, value } = event.target;
    setOrderForm((current) => ({ ...current, [name]: value }));
  };

  const toggleOrder = (id) => {
    setExpandedOrderIds((current) =>
      current.includes(id) ? current.filter((value) => value !== id) : [...current, id],
    );
  };

  const handleCreateOrder = async (event) => {
    event.preventDefault();
    setCreateError(null);
    setCreateStatus('loading');

    if (!userId) {
      setCreateError('The authenticated user does not have a Keycloak subject.');
      setCreateStatus('error');
      return;
    }

    try {
      await createOrder({
        token,
        keycloak,
        order: {
          userId,
          items: [
            {
              productId: Number(orderForm.productId),
              quantity: Number(orderForm.quantity),
              unitPrice: Number(orderForm.unitPrice),
            },
          ],
        },
      });
      setOrderForm(EMPTY_ORDER_FORM);
      setCreateStatus('success');
      await loadOrders();
    } catch (failure) {
      setCreateError(failure.message);
      setCreateStatus('error');
    }
  };

  const handleLookupOrder = async (event) => {
    event.preventDefault();
    setLookupStatus('loading');
    setLookupError(null);
    setLookupResult(null);

    try {
      const result = await fetchOrder({ token, keycloak, id: lookupId });
      setLookupResult(result);
      setLookupStatus('success');
    } catch (failure) {
      setLookupError(failure.message);
      setLookupStatus('error');
    }
  };

  const columns = [
    { key: 'id', header: 'Order ID' },
    {
      key: 'orderDate',
      header: 'Order date',
      render: (row) => formatDateTime(row.orderDate),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => formatStatus(row.status),
    },
    {
      key: 'totalAmount',
      header: 'Total',
      render: (row) => formatAmount(row.totalAmount),
    },
    {
      key: 'items',
      header: 'Items',
      render: (row) => row.items?.length ?? 0,
    },
    { key: 'userId', header: 'User ID' },
    {
      key: 'actions',
      header: 'Actions',
      className: 'cell-actions',
      render: (row) => (
        <button
          type="button"
          className="secondary-button"
          onClick={() => toggleOrder(row.id)}
        >
          {expandedOrderIds.includes(row.id) ? 'Hide items' : 'View items'}
        </button>
      ),
    },
  ];

  return (
    <section className="microservice-panel">
      <h2>Ordering service</h2>
      <p className="muted">
        Orders are created for the authenticated Keycloak user. Product IDs and prices are
        supplied by this form until the product service is available.
      </p>
      <p className="muted">User ID: {userId ?? 'not available'}</p>

      <form onSubmit={handleCreateOrder}>
        <h3>Create order</h3>
        <div className="form-grid">
          <label>
            Product ID
            <input
              name="productId"
              type="number"
              min="1"
              value={orderForm.productId}
              onChange={updateOrderField}
              required
            />
          </label>
          <label>
            Quantity
            <input
              name="quantity"
              type="number"
              min="1"
              value={orderForm.quantity}
              onChange={updateOrderField}
              required
            />
          </label>
          <label>
            Unit price
            <input
              name="unitPrice"
              type="number"
              min="0"
              step="0.01"
              value={orderForm.unitPrice}
              onChange={updateOrderField}
              required
            />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" disabled={createStatus === 'loading'}>
            {createStatus === 'loading' ? 'Creating...' : 'Create order'}
          </button>
        </div>
        {createStatus === 'error' && <p className="error-text">{createError}</p>}
        {createStatus === 'success' && <p className="success-text">Order created.</p>}
      </form>

      <form onSubmit={handleLookupOrder}>
        <h3>Get order by ID</h3>
        <div className="form-grid">
          <label>
            Order ID
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
            {lookupStatus === 'loading' ? 'Loading...' : 'Get order'}
          </button>
        </div>
        {lookupStatus === 'error' && <p className="error-text">{lookupError}</p>}
        {lookupStatus === 'success' && lookupResult && (
          <div className="result-block">
            <h3>Order #{lookupResult.id}</h3>
            <DataTable
              columns={columns}
              rows={[lookupResult]}
              loading={false}
              expandedRowKeys={expandedOrderIds}
              renderExpandedRow={(row) => <OrderItems items={row.items} />}
              ariaLabel="Looked up order table"
            />
          </div>
        )}
      </form>

      <div className="result-block">
        <div className="reclamation-list-header">
          <h3>My orders</h3>
          <button
            type="button"
            className="secondary-button"
            onClick={loadOrders}
            disabled={listStatus === 'loading'}
          >
            Reload
          </button>
        </div>
        <DataTable
          columns={columns}
          rows={orders}
          loading={listStatus === 'loading'}
          error={listError}
          emptyMessage="No orders found for this user."
          expandedRowKeys={expandedOrderIds}
          renderExpandedRow={(row) => <OrderItems items={row.items} />}
          ariaLabel="My orders table"
        />
      </div>
    </section>
  );
}
