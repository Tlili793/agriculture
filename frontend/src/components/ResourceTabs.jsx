export default function ResourceTabs({ tabs, activeId, onChange, ariaLabel = 'Resources' }) {
  return (
    <nav className="resource-tabs" aria-label={ariaLabel}>
      {tabs.map((tab) => {
        const isActive = tab.id === activeId;

        return (
          <button
            key={tab.id}
            type="button"
            className={`resource-tab${isActive ? ' is-active' : ''}`}
            aria-pressed={isActive}
            onClick={() => onChange(tab.id)}
          >
            {tab.label}
          </button>
        );
      })}
    </nav>
  );
}
