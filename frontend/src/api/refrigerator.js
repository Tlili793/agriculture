import { apiRequest } from './client';

const REFRIGERATOR_BASE = import.meta.env.VITE_REFRIGERATOR_BASE_ENDPOINT ?? '/refrigerator';
const FRUITS_ENDPOINT =
  import.meta.env.VITE_REFRIGERATOR_ENDPOINT ?? `${REFRIGERATOR_BASE}/fruits`;

const PRODUCE_ENDPOINTS = {
  fruits: FRUITS_ENDPOINT,
  vegetables: `${REFRIGERATOR_BASE}/vegetables`,
};

const MEALS_ENDPOINT = `${REFRIGERATOR_BASE}/meals`;
const BUDGETS_ENDPOINT = `${REFRIGERATOR_BASE}/budgets`;

function produceEndpoint(resource) {
  const endpoint = PRODUCE_ENDPOINTS[resource];

  if (!endpoint) {
    throw new Error(`Unknown produce resource: ${resource}`);
  }

  return endpoint;
}

export function fetchProduce(resource, { token, keycloak } = {}) {
  return apiRequest(produceEndpoint(resource), { token, keycloak });
}

export function createProduce(resource, { token, keycloak, produce }) {
  return apiRequest(produceEndpoint(resource), {
    token,
    keycloak,
    method: 'POST',
    body: produce,
  });
}

export function updateProduce(resource, { token, keycloak, id, produce }) {
  return apiRequest(`${produceEndpoint(resource)}/${id}`, {
    token,
    keycloak,
    method: 'PUT',
    body: produce,
  });
}

export function deleteProduce(resource, { token, keycloak, id }) {
  return apiRequest(`${produceEndpoint(resource)}/${id}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}

export function fetchMeals({ token, keycloak } = {}) {
  return apiRequest(MEALS_ENDPOINT, { token, keycloak });
}

export function createMeal({ token, keycloak, meal }) {
  return apiRequest(MEALS_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: meal,
  });
}

export function updateMeal({ token, keycloak, id, meal }) {
  return apiRequest(`${MEALS_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'PUT',
    body: meal,
  });
}

export function deleteMeal({ token, keycloak, id }) {
  return apiRequest(`${MEALS_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}

export function fetchBudgets({ token, keycloak } = {}) {
  return apiRequest(BUDGETS_ENDPOINT, { token, keycloak });
}

export function createBudget({ token, keycloak, budget }) {
  return apiRequest(BUDGETS_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: budget,
  });
}

export function updateBudget({ token, keycloak, id, budget }) {
  return apiRequest(`${BUDGETS_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'PUT',
    body: budget,
  });
}

export function deleteBudget({ token, keycloak, id }) {
  return apiRequest(`${BUDGETS_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}
