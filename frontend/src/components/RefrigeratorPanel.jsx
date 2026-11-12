import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/LoginController';
import { createFruit, fetchFruits } from '../api/refrigerator';

const EMPTY_FORM = {
  name: '',
  quantity: '',
  unit: 'piece',
  price: '',
  purchaseDate: '',
  expirationDate: '',
};

export default function RefrigeratorPanel() {
  const { token, keycloak } = useAuth();
  const [fruits, setFruits] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitStatus, setSubmitStatus] = useState('idle');
  const [submitError, setSubmitError] = useState(null);

  const loadFruits = useCallback(async () => {
    setStatus('loading');
    setError(null);

    try {
      const result = await fetchFruits({ token, keycloak });
      setFruits(result);
      setStatus('success');
    } catch (loadError) {
      setError(loadError.message);
      setStatus('error');
    }
  }, [keycloak, token]);

  useEffect(() => {
    if (token) {
      loadFruits();
    }
  }, [token, loadFruits]);

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitStatus('loading');
    setSubmitError(null);

    try {
      await createFruit({
        token,
        keycloak,
        fruit: {
          name: form.name.trim(),
          quantity: form.quantity === '' ? undefined : Number(form.quantity),
          unit: form.unit.trim() || undefined,
          price: form.price === '' ? undefined : Number(form.price),
          purchaseDate: form.purchaseDate || undefined,
          expirationDate: form.expirationDate || undefined,
        },
      });
      setForm(EMPTY_FORM);
      setSubmitStatus('success');
      await loadFruits();
    } catch (submitFailure) {
      setSubmitError(submitFailure.message);
      setSubmitStatus('error');
    }
  };

  return (
    <section className="microservice-panel">
      <h2>Refrigerator service</h2>
      <p className="muted">List and create fruits through the gateway.</p>

      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <label>
            Name
            <input name="name" value={form.name} onChange={updateField} required maxLength={100} />
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
            {submitStatus === 'loading' ? 'Adding...' : 'Add fruit'}
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={loadFruits}
            disabled={status === 'loading'}
          >
            Reload fruits
          </button>
        </div>

        {submitStatus === 'error' && <p className="error-text">{submitError}</p>}
        {submitStatus === 'success' && <p className="success-text">Fruit added.</p>}
      </form>

      <div className="result-block">
        <h3>Stored fruits</h3>
        {status === 'loading' && <p>Loading fruits...</p>}
        {status === 'error' && <p className="error-text">{error}</p>}
        {status === 'success' && fruits.length === 0 && (
          <p className="muted">No fruits stored yet.</p>
        )}
        {status === 'success' && fruits.length > 0 && (
          <dl className="user-details">
            {fruits.map((fruit) => (
              <div key={fruit.id}>
                <dt>{fruit.name}</dt>
                <dd>
                  {fruit.quantity} {fruit.unit}
                  {fruit.price != null ? ` · ${fruit.price}` : ''}
                  {fruit.expirationDate ? ` · expires ${fruit.expirationDate}` : ''}
                </dd>
              </div>
            ))}
          </dl>
        )}
      </div>
    </section>
  );
}
