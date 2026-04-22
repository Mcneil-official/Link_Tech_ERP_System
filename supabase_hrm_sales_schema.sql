-- Additive schema for HRM-Sales delivery performance linkage
-- These tables do not modify existing employees or orders schemas

-- Maps each sales order to an assigned delivery staff member (HRM employee)
create table delivery_assignments (
  id bigint primary key generated always as identity,
  order_id int not null,
  employee_id text not null,
  assigned_date timestamp with time zone default current_timestamp,
  updated_at timestamp with time zone default current_timestamp
);

-- Tracks delivery performance metrics per HRM employee
create table employee_delivery_metrics (
  id bigint primary key generated always as identity,
  employee_id text not null unique,
  deliveries_completed int default 0,
  on_time_deliveries int default 0,
  total_ratings int default 0,
  sum_ratings double precision default 0.0,
  average_rating double precision default 0.0,
  last_delivery_date timestamp with time zone,
  updated_at timestamp with time zone default current_timestamp
);

-- Indexes for performance
create index idx_delivery_assignments_order_id on delivery_assignments(order_id);
create index idx_delivery_assignments_employee_id on delivery_assignments(employee_id);
create index idx_employee_delivery_metrics_employee_id on employee_delivery_metrics(employee_id);
