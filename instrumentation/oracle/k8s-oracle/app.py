"""Small Oracle-backed banking demo for local Kubernetes clusters."""

import os
import random
import threading
from contextlib import contextmanager
from datetime import datetime

import oracledb
import requests
from flask import Flask, abort, g, redirect, render_template_string, request, url_for

app = Flask(__name__)
app.secret_key = os.getenv("FLASK_SECRET_KEY", "local-demo-only")

_slow_query_enabled = os.getenv("SLOW_QUERY_ENABLED", "false").lower() == "true"
_settings_lock = threading.Lock()

PAGE = """<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Northstar Bank</title><style>
:root{font:16px system-ui,sans-serif;color:#17243b;background:#f4f7fb}*{box-sizing:border-box}body{margin:0}
header{background:#142846;color:white;padding:22px max(24px,calc((100% - 1000px)/2));display:flex;justify-content:space-between;align-items:center}
header a{color:#b8d6ff;text-decoration:none;margin-left:22px}.wrap{max-width:1000px;margin:36px auto;padding:0 20px}.eyebrow{color:#536b89;text-transform:uppercase;letter-spacing:.12em;font-size:.75rem;font-weight:700}
h1{margin:.35rem 0 1.4rem}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(210px,1fr));gap:16px}.card,.panel{background:white;border:1px solid #e0e7f0;border-radius:12px;padding:20px;box-shadow:0 4px 18px #19304a0a}
.card small,.muted{color:#687b94}.balance{font-size:1.7rem;font-weight:700;margin-top:10px}.panel{margin-top:22px}table{width:100%;border-collapse:collapse;margin-top:12px}th,td{text-align:left;padding:12px 8px;border-bottom:1px solid #e8edf4}th{color:#687b94;font-size:.8rem;text-transform:uppercase}
form{display:flex;gap:10px;flex-wrap:wrap;align-items:end}label{display:grid;gap:6px;font-size:.85rem;color:#536b89}input,select{padding:10px;border:1px solid #ccd6e3;border-radius:7px;font:inherit}button{padding:10px 16px;border:0;border-radius:7px;color:white;background:#1769c2;font:inherit;font-weight:650;cursor:pointer}button.danger{background:#b83245}.notice{padding:12px;background:#eaf4ff;border-radius:8px;color:#204b79}.toggle{display:flex;align-items:center;gap:10px}
@media(max-width:600px){header{padding:18px 20px}header nav{display:flex;gap:12px}header a{margin:0}table{font-size:.87rem}}
</style></head><body><header><strong>✦ Northstar Bank</strong><nav><a href="{{ url_for('home') }}">Accounts</a><a href="{{ url_for('admin') }}">Admin</a></nav></header>
<main class="wrap">{{ body|safe }}</main></body></html>"""


def connect():
    return oracledb.connect(
        user=os.getenv("ORACLE_USER", "bankapp"),
        password=os.getenv("ORACLE_PASSWORD", "bankpass"),
        dsn=os.getenv("ORACLE_DSN", "oracle:1521/FREEPDB1"),
    )


@contextmanager
def database():
    conn = connect()
    try:
        with conn.cursor() as cur:
            yield conn, cur
    finally:
        conn.close()


def choose_backend():
    return random.choice(("python", "java"))


def backend_url():
    return os.getenv("JAVA_SERVICE_URL", "http://bank-java:8081")


def backend_request(backend, path, *, method="get", json_body=None):
    try:
        response = requests.request(
            method,
            backend_url() + path,
            json=json_body,
            timeout=60 if method == "post" else 30,
        )
    except requests.RequestException as exc:
        abort(502, f"{backend} banking service unavailable: {exc}")
    if response.status_code == 404:
        return None
    if not response.ok:
        abort(400 if response.status_code < 500 else 502, f"{backend} banking service returned {response.status_code}")
    try:
        return response.json()
    except requests.JSONDecodeError:
        abort(502, f"{backend} banking service returned invalid JSON")


def query_accounts(backend="python"):
    if backend != "python":
        return backend_request(backend, "/accounts")
    with database() as (_, cur):
        cur.execute("SELECT account_id, owner_name, account_type, balance FROM bank_accounts ORDER BY account_id")
        return [dict(id=row[0], owner=row[1], kind=row[2], balance=float(row[3])) for row in cur]


def query_account(account_id, backend="python"):
    if backend != "python":
        return backend_request(backend, f"/accounts/{account_id}")
    with database() as (_, cur):
        cur.execute("""SELECT account_id, owner_name, account_type, balance
                       FROM bank_accounts WHERE account_id = :id""", id=account_id)
        row = cur.fetchone()
        return None if row is None else dict(id=row[0], owner=row[1], kind=row[2], balance=float(row[3]))


def query_activity(account_id=None, backend="python"):
    """Return recent transaction activity."""
    if backend != "python":
        path = "/activity" + (f"?accountId={account_id}" if account_id is not None else "")
        rows = backend_request(backend, path) or []
        for row in rows:
            timestamp = row.get("created_at")
            if isinstance(timestamp, str):
                try:
                    row["created_at"] = datetime.fromisoformat(timestamp.replace("Z", "+00:00")).replace(tzinfo=None)
                except ValueError:
                    pass
        return rows
    with database() as (_, cur):
        if account_id is None:
            cur.execute("""SELECT transaction_id, from_account, to_account, amount, memo, created_at
                           FROM bank_transactions ORDER BY created_at DESC FETCH FIRST 20 ROWS ONLY""")
        else:
            cur.execute("""SELECT transaction_id, from_account, to_account, amount, memo, created_at
                           FROM bank_transactions WHERE from_account = :id OR to_account = :id
                           ORDER BY created_at DESC FETCH FIRST 20 ROWS ONLY""", id=account_id)
        return [dict(id=r[0], source=r[1], destination=r[2], amount=float(r[3]), memo=r[4], created_at=r[5]) for r in cur]


@app.after_request
def report_database_backend(response):
    backend = getattr(g, "db_backend", None)
    if backend:
        response.headers["X-DB-Backend"] = backend
    return response


@app.get("/")
def home():
    backend = choose_backend()
    g.db_backend = backend
    accounts = query_accounts(backend)
    activity = query_activity(backend=backend)
    body = render_template_string("""
    <p class="eyebrow">Everyday banking</p><h1>Your accounts</h1>
    <section class="cards">{% for a in accounts %}<a class="card" href="{{ url_for('account_detail', account_id=a.id) }}" style="text-decoration:none;color:inherit"><small>{{ a.owner }} · {{ a.kind }}</small><div class="balance">${{ '%.2f'|format(a.balance) }}</div><small>Account #{{ a.id }} · View activity →</small></a>{% endfor %}</section>
    <section class="panel"><h2>Move money</h2><form method="post" action="{{ url_for('transfer') }}">
    <label>From<select name="source" required>{% for a in accounts %}<option value="{{ a.id }}">{{ a.owner }} · #{{ a.id }}</option>{% endfor %}</select></label>
    <label>To<select name="destination" required>{% for a in accounts %}<option value="{{ a.id }}">{{ a.owner }} · #{{ a.id }}</option>{% endfor %}</select></label>
    <label>Amount<input name="amount" type="number" min="0.01" step="0.01" required placeholder="25.00"></label><label>Note<input name="memo" maxlength="120" placeholder="Optional"></label><button>Transfer</button></form></section>
    <section class="panel"><h2>Recent activity</h2><table><thead><tr><th>When</th><th>From → To</th><th>Note</th><th>Amount</th></tr></thead><tbody>
    {% for t in activity %}<tr><td>{{ t.created_at.strftime('%b %d, %H:%M') if t.created_at else '' }}</td><td>#{{ t.source }} → #{{ t.destination }}</td><td>{{ t.memo }}</td><td>${{ '%.2f'|format(t.amount) }}</td></tr>{% else %}<tr><td colspan="4" class="muted">No transfers yet.</td></tr>{% endfor %}</tbody></table></section>
    """, accounts=accounts, activity=activity)
    return render_template_string(PAGE, body=body)


@app.get("/accounts/<int:account_id>")
def account_detail(account_id):
    backend = choose_backend()
    g.db_backend = backend
    account = query_account(account_id, backend)
    if account is None:
        abort(404, "Account not found")
    activity = query_activity(account_id, backend)
    body = render_template_string("""
    <p class="eyebrow"><a href="{{ url_for('home') }}">← All accounts</a></p>
    <h1>{{ account.kind }}</h1><section class="card"><small>{{ account.owner }} · Account #{{ account.id }}</small><div class="balance">${{ '%.2f'|format(account.balance) }}</div></section>
    <section class="panel"><h2>Account activity</h2><table><thead><tr><th>When</th><th>From → To</th><th>Note</th><th>Amount</th></tr></thead><tbody>
    {% for t in activity %}<tr><td>{{ t.created_at.strftime('%b %d, %H:%M') if t.created_at else '' }}</td><td>#{{ t.source }} → #{{ t.destination }}</td><td>{{ t.memo }}</td><td>${{ '%.2f'|format(t.amount) }}</td></tr>{% else %}<tr><td colspan="4" class="muted">No activity for this account.</td></tr>{% endfor %}</tbody></table></section>
    """, account=account, activity=activity)
    return render_template_string(PAGE, body=body)


@app.post("/transfer")
def transfer():
    try:
        source, destination = int(request.form["source"]), int(request.form["destination"])
        amount = round(float(request.form["amount"]), 2)
    except (KeyError, ValueError):
        abort(400, "Provide valid accounts and an amount")
    if source == destination or amount <= 0:
        abort(400, "Choose two different accounts and a positive amount")
    memo = request.form.get("memo", "")[:120]
    backend = choose_backend()
    g.db_backend = backend
    if backend != "python":
        result = backend_request(backend, "/transfer", method="post", json_body={
            "source": source,
            "destination": destination,
            "amount": amount,
            "memo": memo,
            "slow": _slow_query_enabled,
        })
        return redirect(url_for("home"))
    with database() as (conn, cur):
        if _slow_query_enabled:
            # Intentional missing-index workload: scan and hash the full risk signal
            # history for this source account before allowing the transfer to proceed.
            cur.execute("""SELECT COUNT(*), SUM(LENGTH(STANDARD_HASH(signal_payload, 'SHA256')))
                           FROM bank_transfer_signals WHERE account_id = :id""", id=source)
            cur.fetchone()
        cur.execute("UPDATE bank_accounts SET balance = balance - :amount WHERE account_id = :id AND balance >= :amount", amount=amount, id=source)
        if cur.rowcount != 1:
            conn.rollback()
            abort(400, "Account not found or insufficient funds")
        cur.execute("UPDATE bank_accounts SET balance = balance + :amount WHERE account_id = :id", amount=amount, id=destination)
        if cur.rowcount != 1:
            conn.rollback()
            abort(400, "Destination account not found")
        cur.execute("INSERT INTO bank_transactions (from_account, to_account, amount, memo) VALUES (:src, :dst, :amount, :memo)", src=source, dst=destination, amount=amount, memo=memo)
        conn.commit()
    return redirect(url_for("home"))


@app.get("/admin")
def admin():
    body = render_template_string("""
    <p class="eyebrow">Application controls</p><h1>Admin</h1><section class="panel"><h2>Query behavior</h2>
    <p class="muted">When enabled, each transfer checks the large risk-signal history table. Its account lookup has no index, so Oracle scans and hashes many rows before completing the transfer.</p>
    <form method="post" action="{{ url_for('toggle_slow_query') }}"><label class="toggle"><input type="checkbox" name="enabled" value="true" {% if enabled %}checked{% endif %}> Slow transfer query enabled</label><button class="{{ 'danger' if enabled else '' }}">Save setting</button></form>
    <p class="notice">Current state: <strong>{{ 'ON — transfers run the unindexed risk scan' if enabled else 'OFF — transfers skip the risk scan' }}</strong></p></section>
    """, enabled=_slow_query_enabled)
    return render_template_string(PAGE, body=body)


@app.post("/admin/slow-query")
def toggle_slow_query():
    global _slow_query_enabled
    with _settings_lock:
        _slow_query_enabled = request.form.get("enabled") == "true"
    return redirect(url_for("admin"))


@app.get("/health")
def health():
    try:
        with database() as (conn, _):
            conn.ping()
        return {"status": "ok", "database": "connected", "time": datetime.utcnow().isoformat() + "Z"}
    except Exception as exc:
        return {"status": "error", "database": str(exc)}, 503


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("PORT", "8080")))
