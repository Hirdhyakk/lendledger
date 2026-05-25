#!/usr/bin/env node
/**
 * Headless browser smoke: admin login + nav; borrower login.
 * Requires: stack running (./run.sh), npx playwright install chromium (once).
 */
import { chromium } from 'playwright';

const BASE = process.env.UI_BASE || 'http://localhost:5173';
let passed = 0;
let failed = 0;

function ok(name) {
  console.log(`  PASS: ${name}`);
  passed++;
}
function bad(name, err) {
  console.log(`  FAIL: ${name}${err ? ` — ${err}` : ''}`);
  failed++;
}

async function waitForText(page, text, timeout = 15000) {
  await page.getByText(text, { exact: false }).first().waitFor({ state: 'visible', timeout });
}

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext();
const page = await context.newPage();

console.log(`=== UI browser smoke (${BASE}) ===\n`);

try {
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
  if (await page.getByRole('heading', { name: 'LendLedger' }).isVisible().catch(() => false)) {
    ok('login page loads');
  } else {
    bad('login page loads');
  }

  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.waitForURL(/\/admin\/dashboard/, { timeout: 20000 });
  ok('admin redirects to dashboard');

  await waitForText(page, 'Active loans');
  await waitForText(page, 'Outstanding');
  ok('dashboard KPI cards visible');

  const nav = page.locator('nav.navbar');
  await nav.getByRole('link', { name: 'Borrowers' }).click();
  await page.waitForURL(/\/admin\/borrowers/);
  await waitForText(page, 'Borrowers');
  ok('borrowers page');

  await nav.getByRole('link', { name: 'Loans' }).click();
  await page.waitForURL(/\/admin\/loans/);
  await waitForText(page, 'Loans');
  ok('loans page');

  await nav.getByRole('link', { name: 'Reports' }).click();
  await page.waitForURL(/\/admin\/reports/);
  await waitForText(page, 'Reports');
  ok('reports page');

  // Logout and borrower flow
  await page.locator('nav.navbar .dropdown-toggle').click();
  await page.getByText('Logout', { exact: true }).click();
  await page.waitForURL(/\/login/);
  ok('logout returns to login');

  await page.locator('input[type="email"]').fill('borrower1@lendledger.local');
  await page.locator('input[type="password"]').fill('password');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.waitForURL(/\/borrower\/dashboard/, { timeout: 20000 });
  await waitForText(page, 'My Loans');
  ok('borrower dashboard');

  await context.close();
  await browser.close();

  console.log(`\n=== Results: ${passed} passed, ${failed} failed ===`);
  process.exit(failed > 0 ? 1 : 0);
} catch (e) {
  await browser.close();
  console.error('\nBrowser test error:', e.message);
  process.exit(1);
}
