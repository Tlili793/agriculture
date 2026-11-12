import { apiRequest } from './client';

const ORDERS_ENDPOINT = import.meta.env.VITE_ORDERS_ENDPOINT ?? '/api/orders';

export function createOrder({ token, keycloak, order }) {
  return apiRequest(ORDERS_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: order,
  });
}

export function fetchOrder({ token, keycloak, id }) {
  return apiRequest(`${ORDERS_ENDPOINT}/${id}`, { token, keycloak });
}

export function fetchUserOrders({ token, keycloak, userId }) {
  return apiRequest(`${ORDERS_ENDPOINT}/user/${userId}`, { token, keycloak });
}
