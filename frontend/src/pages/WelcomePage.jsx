import { useAuth } from '../auth/LoginController';
import RefrigeratorPanel from '../components/RefrigeratorPanel';
import OrderingPanel from '../components/OrderingPanel';

export default function WelcomePage() {
  const { username, name, email, token, logout } = useAuth();
  const displayName = name || username;

  return (
    <main className="center-screen">
      <section className="card">
        <p className="eyebrow">Login successful</p>
        <h1>Welcome{displayName ? `, ${displayName}` : ''}!</h1>
        <p className="muted">You are authenticated with Keycloak.</p>

        <dl className="user-details">
          <div>
            <dt>Username</dt>
            <dd>{username ?? '-'}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{email ?? '-'}</dd>
          </div>
        </dl>

        <div className="panel-grid">
          <RefrigeratorPanel />
          <OrderingPanel />
        </div>

        <details>
          <summary>JWT access token</summary>
          <pre>{token ?? 'No token available'}</pre>
        </details>

        <button type="button" onClick={logout}>
          Log out
        </button>
      </section>
    </main>
  );
}
