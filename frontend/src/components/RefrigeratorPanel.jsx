import { useState } from 'react';
import ResourceTabs from './ResourceTabs';
import ProduceResourcePanel from './refrigerator/ProduceResourcePanel';
import MealResourcePanel from './refrigerator/MealResourcePanel';
import BudgetResourcePanel from './refrigerator/BudgetResourcePanel';

const TABS = [
  { id: 'fruits', label: 'Fruits' },
  { id: 'vegetables', label: 'Vegetables' },
  { id: 'meals', label: 'Meals' },
  { id: 'budgets', label: 'Budgets' },
];

export default function RefrigeratorPanel() {
  const [activeResource, setActiveResource] = useState('fruits');

  return (
    <section className="microservice-panel">
      <h2>Refrigerator service</h2>
      <p className="muted">
        Fruits, vegetables, planned meals, and monthly budgets are stored by this service.
      </p>

      <ResourceTabs
        tabs={TABS}
        activeId={activeResource}
        onChange={setActiveResource}
        ariaLabel="Refrigerator resources"
      />

      {activeResource === 'fruits' && (
        <ProduceResourcePanel resource="fruits" title="Fruits" singular="fruit" />
      )}
      {activeResource === 'vegetables' && (
        <ProduceResourcePanel
          resource="vegetables"
          title="Vegetables"
          singular="vegetable"
        />
      )}
      {activeResource === 'meals' && <MealResourcePanel />}
      {activeResource === 'budgets' && <BudgetResourcePanel />}
    </section>
  );
}
