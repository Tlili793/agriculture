export async function apiRequest(path, { token, keycloak, method = 'GET', body } = {}) {
  const send = async (authToken) => {
    const headers = {};
    if (authToken) {
      headers.Authorization = `Bearer ${authToken}`;
    }
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (response.status === 401) {
      const error = new Error('The access token was rejected by the gateway.');
      error.status = 401;
      throw error;
    }

    if (!response.ok) {
      const text = await response.text();
      let message = text;
      try {
        const problem = JSON.parse(text);
        message = problem.detail || problem.message || text;
      } catch {
        // keep the raw response body
      }
      const error = new Error(message || `Request failed with status ${response.status}`);
      error.status = response.status;
      throw error;
    }

    if (response.status === 204) {
      return null;
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
  };

  try {
    return await send(token);
  } catch (error) {
    if (error.status !== 401 || !keycloak) {
      throw error;
    }

    await keycloak.updateToken(-1);
    return send(keycloak.token);
  }
}
