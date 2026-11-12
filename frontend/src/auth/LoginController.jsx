import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import keycloak, { initKeycloak } from './keycloak';

const AuthContext = createContext(null);

export function LoginController({ children }) {
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [token, setToken] = useState(null);

  useEffect(() => {
    let active = true;

    initKeycloak()
      .then((authenticated) => {
        if (!active) {
          return;
        }

        if (!authenticated) {
          return keycloak.login({
            redirectUri: `${window.location.origin}/welcome`,
          });
        }

        setToken(keycloak.token);
        setStatus('authenticated');

        if (window.location.pathname !== '/welcome') {
          window.history.replaceState({}, '', '/welcome');
        }
      })
      .catch((initError) => {
        if (!active) {
          return;
        }

        console.error('Keycloak initialization failed', initError);
        setError(initError);
        setStatus('error');
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (status !== 'authenticated') {
      return undefined;
    }

    const interval = window.setInterval(async () => {
      try {
        const refreshed = await keycloak.updateToken(30);
        if (refreshed) {
          setToken(keycloak.token);
        }
      } catch {
        await keycloak.login({
          redirectUri: `${window.location.origin}/welcome`,
        });
      }
    }, 20000);

    return () => window.clearInterval(interval);
  }, [status]);

  const value = useMemo(
    () => {
      const roles = keycloak.tokenParsed?.realm_access?.roles ?? [];

      return {
        keycloak,
        authenticated: status === 'authenticated',
        token,
        username: keycloak.tokenParsed?.preferred_username,
        name: keycloak.tokenParsed?.name,
        email: keycloak.tokenParsed?.email,
        roles,
        isAdmin: roles.includes('ADMIN'),
        logout: () =>
          keycloak.logout({
            redirectUri: window.location.origin,
          }),
      };
    },
    [status, token],
  );

  if (status === 'loading') {
    return (
      <main className="center-screen">
        <p>Checking your session...</p>
      </main>
    );
  }

  if (status === 'error') {
    return (
      <main className="center-screen">
        <section className="card">
          <h1>Login unavailable</h1>
          <p>{error?.message ?? 'Could not connect to Keycloak.'}</p>
          <button type="button" onClick={() => window.location.reload()}>
            Try again
          </button>
        </section>
      </main>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside LoginController');
  }

  return context;
}
