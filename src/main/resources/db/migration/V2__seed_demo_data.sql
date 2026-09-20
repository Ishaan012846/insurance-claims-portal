-- Flyway Migration V2: Seed Initial Demo Users, Policies, Claims, and Audit Logs

-- 1. Insert Seed Users (Password for all demo users is: password123)
-- BCrypt Hash for "password123": $2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq
INSERT INTO users (email, password_hash, full_name, role, created_at)
VALUES
  ('customer@example.com', '$2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq', 'Alex Policyholder', 'CUSTOMER', '2026-07-01 10:00:00+00'),
  ('priya.sharma@example.com', '$2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq', 'Priya Sharma', 'CUSTOMER', '2026-07-15 10:00:00+00'),
  ('rahul.verma@example.com', '$2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq', 'Rahul Verma', 'CUSTOMER', '2026-06-01 10:00:00+00'),
  ('handler@example.com', '$2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq', 'Sarah Claims Handler', 'HANDLER', '2026-05-01 10:00:00+00'),
  ('vikram.singh@example.com', '$2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq', 'Vikram Singh', 'HANDLER', '2026-05-01 10:00:00+00'),
  ('manager@example.com', '$2a$10$Ssv6SseyMLClzVGr3hDg0OndJR.Ux8.Tlcoxp6GI4w1.Rvo/KCtoq', 'Marcus Claims Manager', 'MANAGER', '2026-04-01 10:00:00+00');

-- 2. Insert Sample Policies
INSERT INTO policies (policy_number, type, coverage_amount, premium, valid_from, valid_to, holder_id, created_at)
VALUES
  ('POL-2026-HLTH01', 'HEALTH', 500000.00, 15000.00, '2026-01-01', '2026-12-31', (SELECT id FROM users WHERE email = 'customer@example.com'), '2026-07-01 10:00:00+00'),
  ('POL-2026-MTR02', 'MOTOR', 750000.00, 18500.00, '2026-01-01', '2026-12-31', (SELECT id FROM users WHERE email = 'customer@example.com'), '2026-07-01 10:00:00+00'),
  ('POL-2026-PROP03', 'PROPERTY', 2500000.00, 35000.00, '2026-01-01', '2026-12-31', (SELECT id FROM users WHERE email = 'customer@example.com'), '2026-07-01 10:00:00+00'),
  ('POL-2026-HLTH04', 'HEALTH', 1000000.00, 22000.00, '2026-01-01', '2026-12-31', (SELECT id FROM users WHERE email = 'priya.sharma@example.com'), '2026-07-15 10:00:00+00'),
  ('POL-2026-MTR05', 'MOTOR', 1200000.00, 42000.00, '2026-01-01', '2026-12-31', (SELECT id FROM users WHERE email = 'rahul.verma@example.com'), '2026-06-01 10:00:00+00');

-- 3. Insert Humanized Claims
-- Claim 1: SUBMITTED (Alex - Health)
INSERT INTO claims (claim_number, incident_date, description, claimed_amount, approved_amount, status, policy_id, assigned_handler_id, version, created_at, submitted_at, closed_at)
VALUES (
  'CLM-2026-100101',
  '2026-09-10',
  'Emergency hospitalization at Apollo Hospital for acute appendicitis treatment including laproscopic surgery and 3 days post-op recovery stay.',
  45000.00,
  NULL,
  'SUBMITTED',
  (SELECT id FROM policies WHERE policy_number = 'POL-2026-HLTH01'),
  (SELECT id FROM users WHERE email = 'handler@example.com'),
  1,
  '2026-09-10 09:00:00+00',
  '2026-09-11 10:30:00+00',
  NULL
);

-- Claim 2: UNDER_REVIEW (SLA overdue > 5 days for Manager Dashboard demo)
INSERT INTO claims (claim_number, incident_date, description, claimed_amount, approved_amount, status, policy_id, assigned_handler_id, version, created_at, submitted_at, closed_at)
VALUES (
  'CLM-2026-100102',
  '2026-09-02',
  'Fender bender collision on Outer Ring Road during evening rain. Front bumper and right headlight unit replaced at Hyundai Authorized Service Center.',
  32500.00,
  NULL,
  'UNDER_REVIEW',
  (SELECT id FROM policies WHERE policy_number = 'POL-2026-MTR02'),
  (SELECT id FROM users WHERE email = 'handler@example.com'),
  2,
  '2026-09-03 11:00:00+00',
  '2026-09-03 14:15:00+00',
  NULL
);

-- Claim 3: INFO_REQUESTED (Alex - Property)
INSERT INTO claims (claim_number, incident_date, description, claimed_amount, approved_amount, status, policy_id, assigned_handler_id, version, created_at, submitted_at, closed_at)
VALUES (
  'CLM-2026-100103',
  '2026-08-25',
  'Water damage to living room wooden flooring and false ceiling due to heavy monsoon pipe leakage in apartment building.',
  125000.00,
  NULL,
  'INFO_REQUESTED',
  (SELECT id FROM policies WHERE policy_number = 'POL-2026-PROP03'),
  (SELECT id FROM users WHERE email = 'vikram.singh@example.com'),
  3,
  '2026-08-26 08:30:00+00',
  '2026-08-26 09:00:00+00',
  NULL
);

-- Claim 4: APPROVED (Priya - Health)
INSERT INTO claims (claim_number, incident_date, description, claimed_amount, approved_amount, status, policy_id, assigned_handler_id, version, created_at, submitted_at, closed_at)
VALUES (
  'CLM-2026-100104',
  '2026-09-01',
  'Daycare procedure for cataract surgery with intraocular lens implantation at Narayana Nethralaya.',
  88000.00,
  82500.00,
  'APPROVED',
  (SELECT id FROM policies WHERE policy_number = 'POL-2026-HLTH04'),
  (SELECT id FROM users WHERE email = 'handler@example.com'),
  3,
  '2026-09-02 10:00:00+00',
  '2026-09-02 11:20:00+00',
  NULL
);

-- Claim 5: SETTLED (Rahul - Motor)
INSERT INTO claims (claim_number, incident_date, description, claimed_amount, approved_amount, status, policy_id, assigned_handler_id, version, created_at, submitted_at, closed_at)
VALUES (
  'CLM-2026-100105',
  '2026-08-10',
  'Windshield glass crack replacement and side mirror repair following road debris incident on highway.',
  65000.00,
  65000.00,
  'SETTLED',
  (SELECT id FROM policies WHERE policy_number = 'POL-2026-MTR05'),
  (SELECT id FROM users WHERE email = 'vikram.singh@example.com'),
  4,
  '2026-08-11 14:00:00+00',
  '2026-08-11 16:45:00+00',
  '2026-08-18 12:00:00+00'
);

-- Claim 6: DRAFT (Alex - Health)
INSERT INTO claims (claim_number, incident_date, description, claimed_amount, approved_amount, status, policy_id, assigned_handler_id, version, created_at, submitted_at, closed_at)
VALUES (
  'CLM-2026-100106',
  '2026-09-18',
  'Outpatient MRI scan of lower back and orthopedic consultation for lumbar strain.',
  12000.00,
  NULL,
  'DRAFT',
  (SELECT id FROM policies WHERE policy_number = 'POL-2026-HLTH01'),
  NULL,
  0,
  '2026-09-19 08:30:00+00',
  NULL,
  NULL
);

-- 4. Insert Audit Logs for Claims
-- Logs for Claim 1 (CLM-2026-100101)
INSERT INTO claim_audit_logs (claim_id, from_status, to_status, actor_id, remarks, timestamp)
VALUES
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100101'), NULL, 'SUBMITTED', (SELECT id FROM users WHERE email = 'customer@example.com'), 'Submitted hospital admission details and discharge summary.', '2026-09-11 10:30:00+00');

-- Logs for Claim 2 (CLM-2026-100102)
INSERT INTO claim_audit_logs (claim_id, from_status, to_status, actor_id, remarks, timestamp)
VALUES
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100102'), NULL, 'SUBMITTED', (SELECT id FROM users WHERE email = 'customer@example.com'), 'Claim filed with Hyundai garage repair estimate.', '2026-09-03 14:15:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100102'), 'SUBMITTED', 'UNDER_REVIEW', (SELECT id FROM users WHERE email = 'handler@example.com'), 'Assigned surveyor for physical vehicle inspection.', '2026-09-04 09:30:00+00');

-- Logs for Claim 3 (CLM-2026-100103)
INSERT INTO claim_audit_logs (claim_id, from_status, to_status, actor_id, remarks, timestamp)
VALUES
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100103'), NULL, 'SUBMITTED', (SELECT id FROM users WHERE email = 'customer@example.com'), 'Submitted claim for property water restoration.', '2026-08-26 09:00:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100103'), 'SUBMITTED', 'UNDER_REVIEW', (SELECT id FROM users WHERE email = 'vikram.singh@example.com'), 'Reviewing property damage estimate and photo evidence.', '2026-08-27 11:00:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100103'), 'UNDER_REVIEW', 'INFO_REQUESTED', (SELECT id FROM users WHERE email = 'vikram.singh@example.com'), 'Requested plumber bill and Housing Society NOC certificate.', '2026-08-29 14:20:00+00');

-- Logs for Claim 4 (CLM-2026-100104)
INSERT INTO claim_audit_logs (claim_id, from_status, to_status, actor_id, remarks, timestamp)
VALUES
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100104'), NULL, 'SUBMITTED', (SELECT id FROM users WHERE email = 'priya.sharma@example.com'), 'Submitted cataract surgery daycare bills.', '2026-09-02 11:20:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100104'), 'SUBMITTED', 'UNDER_REVIEW', (SELECT id FROM users WHERE email = 'handler@example.com'), 'Verified hospital registration and tariff chart.', '2026-09-03 10:15:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100104'), 'UNDER_REVIEW', 'APPROVED', (SELECT id FROM users WHERE email = 'handler@example.com'), 'Approved payout of ₹82,500 after deducting ₹5,500 non-medical items.', '2026-09-05 16:00:00+00');

-- Logs for Claim 5 (CLM-2026-100105)
INSERT INTO claim_audit_logs (claim_id, from_status, to_status, actor_id, remarks, timestamp)
VALUES
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100105'), NULL, 'SUBMITTED', (SELECT id FROM users WHERE email = 'rahul.verma@example.com'), 'Submitted windshield replacement bill.', '2026-08-11 16:45:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100105'), 'SUBMITTED', 'UNDER_REVIEW', (SELECT id FROM users WHERE email = 'vikram.singh@example.com'), 'Verified glass damage photos and invoice.', '2026-08-12 09:30:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100105'), 'UNDER_REVIEW', 'APPROVED', (SELECT id FROM users WHERE email = 'vikram.singh@example.com'), 'Fully approved claim for ₹65,000.', '2026-08-15 11:10:00+00'),
  ((SELECT id FROM claims WHERE claim_number = 'CLM-2026-100105'), 'APPROVED', 'SETTLED', (SELECT id FROM users WHERE email = 'manager@example.com'), 'Bank NEFT payout settlement completed successfully.', '2026-08-18 12:00:00+00');
