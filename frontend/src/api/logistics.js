import { apiRequest } from './client';

const DELIVERIES_ENDPOINT =
  import.meta.env.VITE_LOGISTICS_DELIVERIES_ENDPOINT ?? '/api/deliveries';
const WAREHOUSES_ENDPOINT =
  import.meta.env.VITE_LOGISTICS_WAREHOUSES_ENDPOINT ?? '/api/warehouses';

function deliveryQuery({ status, orderId } = {}) {
  const params = new URLSearchParams();

  if (status) {
    params.set('status', status);
  }
  if (orderId !== undefined && orderId !== null && String(orderId).trim() !== '') {
    params.set('orderId', orderId);
  }

  const query = params.toString();
  return query ? `?${query}` : '';
}

export function fetchDeliveries({ token, keycloak, status, orderId } = {}) {
  return apiRequest(`${DELIVERIES_ENDPOINT}${deliveryQuery({ status, orderId })}`, {
    token,
    keycloak,
  });
}

export function fetchDelivery({ token, keycloak, id }) {
  return apiRequest(`${DELIVERIES_ENDPOINT}/${id}`, { token, keycloak });
}

export function createDelivery({ token, keycloak, delivery }) {
  return apiRequest(DELIVERIES_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: delivery,
  });
}

export function updateDelivery({ token, keycloak, id, delivery }) {
  return apiRequest(`${DELIVERIES_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'PUT',
    body: delivery,
  });
}

export function updateDeliveryStatus({ token, keycloak, id, status }) {
  const params = new URLSearchParams({ status });

  return apiRequest(`${DELIVERIES_ENDPOINT}/${id}/status?${params.toString()}`, {
    token,
    keycloak,
    method: 'PATCH',
  });
}

export function deleteDelivery({ token, keycloak, id }) {
  return apiRequest(`${DELIVERIES_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}

export function requestDeliveryAssignment({ token, keycloak, id, transporterId }) {
  return apiRequest(`${DELIVERIES_ENDPOINT}/${id}/assignment-request`, {
    token,
    keycloak,
    method: 'POST',
    body: { transporterId },
  });
}

export function acceptDeliveryAssignment({ token, keycloak, id }) {
  return apiRequest(`${DELIVERIES_ENDPOINT}/${id}/assignment-accept`, {
    token,
    keycloak,
    method: 'POST',
  });
}

export function fetchWarehouses({ token, keycloak } = {}) {
  return apiRequest(WAREHOUSES_ENDPOINT, { token, keycloak });
}

export function createWarehouse({ token, keycloak, warehouse }) {
  return apiRequest(WAREHOUSES_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: warehouse,
  });
}

export function updateWarehouse({ token, keycloak, id, warehouse }) {
  return apiRequest(`${WAREHOUSES_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'PUT',
    body: warehouse,
  });
}

export function deleteWarehouse({ token, keycloak, id }) {
  return apiRequest(`${WAREHOUSES_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}
