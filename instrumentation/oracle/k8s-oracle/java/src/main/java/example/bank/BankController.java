package example.bank;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BankController {
    private final JdbcTemplate jdbc;

    public BankController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        jdbc.queryForObject("SELECT 1 FROM dual", Integer.class);
        return Map.of("status", "ok", "database", "connected");
    }

    @GetMapping("/accounts")
    public List<Account> accounts() {
        return jdbc.query("""
            SELECT account_id, owner_name, account_type, balance
            FROM bank_accounts ORDER BY account_id
            """, (rs, row) -> new Account(rs.getLong("account_id"), rs.getString("owner_name"),
                rs.getString("account_type"), rs.getBigDecimal("balance")));
    }

    @GetMapping("/accounts/{id}")
    public Account account(@PathVariable long id) {
        List<Account> rows = jdbc.query("""
            SELECT account_id, owner_name, account_type, balance
            FROM bank_accounts WHERE account_id = ?
            """, (rs, row) -> new Account(rs.getLong("account_id"), rs.getString("owner_name"),
                rs.getString("account_type"), rs.getBigDecimal("balance")), id);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return rows.get(0);
    }

    @GetMapping("/activity")
    public List<Activity> activity(@RequestParam(required = false) Long accountId) {
        String sql = accountId == null ? """
            SELECT transaction_id, from_account, to_account, amount, memo, created_at
            FROM bank_transactions ORDER BY created_at DESC FETCH FIRST 20 ROWS ONLY
            """ : """
            SELECT transaction_id, from_account, to_account, amount, memo, created_at
            FROM bank_transactions WHERE from_account = ? OR to_account = ?
            ORDER BY created_at DESC FETCH FIRST 20 ROWS ONLY
            """;
        var mapper = (org.springframework.jdbc.core.RowMapper<Activity>) (rs, row) -> new Activity(
            rs.getLong("transaction_id"), rs.getLong("from_account"), rs.getLong("to_account"),
            rs.getBigDecimal("amount"), rs.getString("memo"), rs.getTimestamp("created_at"));
        return accountId == null ? jdbc.query(sql, mapper)
            : jdbc.query(sql, mapper, accountId, accountId);
    }

    @PostMapping("/transfer")
    @Transactional
    public Map<String, Object> transfer(@RequestBody Transfer transfer) {
        if (transfer.source() == transfer.destination() || transfer.amount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose different accounts and a positive amount");
        }
        if (transfer.slow()) {
            // Deliberately unindexed lookup used to create a database-side slow query.
            jdbc.queryForMap("""
                SELECT COUNT(*), SUM(LENGTH(STANDARD_HASH(signal_payload, 'SHA256')))
                FROM bank_transfer_signals WHERE account_id = ?
                """, transfer.source());
        }
        int debited = jdbc.update("""
            UPDATE bank_accounts SET balance = balance - ?
            WHERE account_id = ? AND balance >= ?
            """, transfer.amount(), transfer.source(), transfer.amount());
        if (debited != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source account not found or insufficient funds");
        }
        int credited = jdbc.update("UPDATE bank_accounts SET balance = balance + ? WHERE account_id = ?",
                transfer.amount(), transfer.destination());
        if (credited != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination account not found");
        }
        jdbc.update("""
            INSERT INTO bank_transactions (from_account, to_account, amount, memo)
            VALUES (?, ?, ?, ?)
            """, transfer.source(), transfer.destination(), transfer.amount(), transfer.memo());
        return Map.of("status", "transferred", "source", transfer.source(),
                "destination", transfer.destination(), "amount", transfer.amount());
    }

    public record Account(long id, String owner, String kind, BigDecimal balance) {}
    public record Activity(long id, long source, long destination, BigDecimal amount, String memo, Timestamp created_at) {}
    public record Transfer(long source, long destination, BigDecimal amount, String memo, boolean slow) {}
}
