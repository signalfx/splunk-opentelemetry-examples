WHENEVER SQLERROR EXIT SQL.SQLCODE
CREATE TABLE bank_transfer_signals (
    account_id NUMBER(10) NOT NULL,
    signal_payload VARCHAR2(512) NOT NULL,
    observed_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- Deliberately no index on account_id: transfers scan this full table.
INSERT INTO bank_transfer_signals (account_id, signal_payload, observed_at)
SELECT 1001 + MOD(level, 3),
       RPAD(TO_CHAR(MOD(level, 1000000)) || ':BANK_TRANSFER_RISK_SIGNAL:', 512, 'RISKDATA'),
       SYSTIMESTAMP - NUMTODSINTERVAL(MOD(level, 365), 'DAY')
FROM dual
CONNECT BY level <= 750000;
COMMIT;
