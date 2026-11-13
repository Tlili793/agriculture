import { Fragment } from 'react';

export default function DataTable({
  columns,
  rows,
  getRowKey = (row) => row.id,
  loading = false,
  error = null,
  emptyMessage = 'No records found.',
  renderExpandedRow,
  expandedRowKeys = [],
  className = '',
  ariaLabel,
}) {
  const expandedKeys = new Set(expandedRowKeys);

  return (
    <div className="table-scroll">
      <table className={`data-table ${className}`.trim()} aria-label={ariaLabel}>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key} className={column.className}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr>
              <td className="table-message" colSpan={columns.length}>
                Loading...
              </td>
            </tr>
          )}

          {!loading && error && (
            <tr>
              <td className="table-message" colSpan={columns.length}>
                <span className="error-text">{error}</span>
              </td>
            </tr>
          )}

          {!loading && !error && rows.length === 0 && (
            <tr>
              <td className="table-message" colSpan={columns.length}>
                {emptyMessage}
              </td>
            </tr>
          )}

          {!loading &&
            !error &&
            rows.map((row) => {
              const rowKey = getRowKey(row);
              const isExpanded = expandedKeys.has(rowKey);

              return (
                <Fragment key={rowKey}>
                  <tr>
                    {columns.map((column) => (
                      <td key={column.key} className={column.className}>
                        {column.render ? column.render(row) : row[column.key]}
                      </td>
                    ))}
                  </tr>
                  {renderExpandedRow && isExpanded && (
                    <tr className="detail-row">
                      <td colSpan={columns.length}>{renderExpandedRow(row)}</td>
                    </tr>
                  )}
                </Fragment>
              );
            })}
        </tbody>
      </table>
    </div>
  );
}
