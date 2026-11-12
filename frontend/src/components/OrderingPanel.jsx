import { useState } from 'react';
import { useAuth } from '../auth/LoginController';
import { createOrder, fetchOrder, fetchUserOrders } from '../api/ordering';

const EMPTY_ORDER_FORM = {
  productId: '1',
  quantity: '1',
  unitPrice: '10.00',
};

function Result({ value }) {
  return <pre>{JSON.stringify(value, null, 2)}</pre>;
}

export default function OrderingPanel() {
  const { token, keycloak } = useAuth();
  const userId = keycloak.tokenParsed?.sub;
  const [orderForm, setOrderForm] = useState(EMPTY_ORDER_FORM);
  const [createStatus, setCreateStatus] = useState('idle');
  const [createError, setCreateError] = useState(null);
  const [createdOrder, setCreatedOrder] = useState(null);

  const [lookupId, setLookupId] = useState('1');
  const [lookupStatus, setLookupStatus] = useState('idle');
  const [lookupError, setLookupError] = useState(null);
  const [lookupResult, setLookupResult] = useState(null);

  const [listStatus, setListStatus] = useState('idle');
  const [listError, setListError] = useState(null);
  const [userOrders, setUserOrders] = useState(null);

  const updateOrderField = (event) => {
    const { name, value } = event.target;
    setOrderForm((current) => ({ ...current, [name]: value }));
  };

  const handleCreateOrder = async (event) => {
    event.preventDefault();
    setCreateError(null);
    setCreatedOrder(null);

    if (!userId) {
      setCreateError('The authenticated user does not have a Keycloak subject.');
      setCreateStatus('error');
      return;
    }

    setCreateStatus('loading');

    try {
      const result = await createOrder({
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
      setCreatedOrder(result);
      setCreateStatus('success');
    } catch (createFailure) {
      setCreateError(createFailure.message);
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
    } catch (lookupFailure) {
      setLookupError(lookupFailure.message);
      setLookupStatus('error');
    }
  };

  const handleListUserOrders = async (event) => {
    event.preventDefault();
    setListError(null);
    setUserOrders(null);

    if (!userId) {
      setListError('The authenticated user does not have a Keycloak subject.');
      setListStatus('error');
      return;
    }

    setListStatus('loading');

    try {
      const result = await fetchUserOrders({ token, keycloak, userId });
      setUserOrders(result);
      setListStatus('success');
    } catch (listFailure) {
      setListError(listFailure.message);
      setListStatus('error');
    }
  };

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
        {createStatus === 'success' && createdOrder && (
          <div className="result-block">
            <h3>Created order</h3>
            <Result value={createdOrder} />
          </div>
        )}
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
            <h3>Order details</h3>
            <Result value={lookupResult} />
          </div>
        )}
      </form>

      <form onSubmit={handleListUserOrders}>
        <h3>List my orders</h3>
        <div className="form-actions">
          <button type="submit" disabled={listStatus === 'loading'}>
            {listStatus === 'loading' ? 'Loading...' : 'List orders'}
          </button>
        </div>
        {listStatus === 'error' && <p className="error-text">{listError}</p>}
        {listStatus === 'success' && userOrders && (
          <div className="result-block">
            <h3>User orders</h3>
            <Result value={userOrders} />
          </div>
        )}
      </form>
    </section>
  );
}
