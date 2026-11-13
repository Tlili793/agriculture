import { useState } from 'react';
import { useAuth } from '../auth/LoginController';
import RefrigeratorPanel from '../components/RefrigeratorPanel';
import OrderingPanel from '../components/OrderingPanel';
import ReclamationPanel from '../components/ReclamationPanel';
import LogisticsPanel from '../components/LogisticsPanel';
import MarketplacePanel from '../components/MarketplacePanel';

export default function WelcomePage() {
  const { username, name, email, roles, token, logout, isAdmin, isTransporter } = useAuth();
  const displayName = name || username;
  const [activeServiceId, setActiveServiceId] = useState('refrigerator');

  const services = [
    {
      id: 'refrigerator',
      label: 'Refrigerator',
      description: 'Fruits, vegetables, meals, budgets',
      Component: RefrigeratorPanel,
    },
    {
      id: 'ordering',
      label: 'Ordering',
      description: 'Create and review orders',
      Component: OrderingPanel,
    },
    {
      id: 'reclamation',
      label: 'Reclamations',
      description: 'Complaints and admin replies',
      Component: ReclamationPanel,
    },
    {
      id: 'marketplace',
      label: 'Marketplace',
      description: 'Listings and owner-managed products',
      Component: MarketplacePanel,
    },
    ...(isAdmin || isTransporter
      ? [
          {
            id: 'logistics',
            label: 'Logistics',
            description: 'Deliveries, assignments, warehouses',
            Component: LogisticsPanel,
          },
        ]
      : []),
  ];

  const activeService =
    services.find((service) => service.id === activeServiceId) ?? services[0];
  const ActiveServicePanel = activeService.Component;

  return (
    <main className="app-shell">
      <header className="app-header">
        <div className="app-heading">
          <p className="eyebrow">Agriculture platform</p>
          <h1>Welcome{displayName ? `, ${displayName}` : ''}!</h1>
          <p className="muted">
            Choose a service below. Only the selected service is shown.
          </p>
        </div>

        <aside className="account-card" aria-label="Account details">
          <p className="account-name">{displayName ?? 'Authenticated user'}</p>
          {username && <p className="account-meta">@{username}</p>}
          {email && <p className="account-meta">{email}</p>}
          <div className="role-list" aria-label="Roles">
            {roles.length > 0 ? (
              roles.map((role) => (
                <span className="role-chip" key={role}>
                  {role}
                </span>
              ))
            ) : (
              <span className="muted">No roles assigned</span>
            )}
          </div>
          <button type="button" className="secondary-button" onClick={logout}>
            Log out
          </button>
        </aside>
      </header>

      <div className="workspace">
        <aside className="service-sidebar">
          <p className="service-nav-title">Services</p>
          <div className="service-nav" role="tablist" aria-label="Services">
            {services.map((service) => {
              const isActive = service.id === activeService.id;

              return (
                <button
                  key={service.id}
                  type="button"
                  role="tab"
                  id={`service-tab-${service.id}`}
                  aria-selected={isActive}
                  aria-controls="service-panel"
                  className={`service-nav-item${isActive ? ' is-active' : ''}`}
                  onClick={() => setActiveServiceId(service.id)}
                >
                  <span className="service-nav-label">{service.label}</span>
                  <span className="service-nav-description">{service.description}</span>
                </button>
              );
            })}
          </div>

          <details className="session-details">
            <summary>JWT access token</summary>
            <pre>{token ?? 'No token available'}</pre>
          </details>
        </aside>

        <section
          className="service-workspace"
          id="service-panel"
          role="tabpanel"
          aria-labelledby={`service-tab-${activeService.id}`}
          tabIndex={0}
        >
          <ActiveServicePanel />
        </section>
      </div>
    </main>
  );
}
