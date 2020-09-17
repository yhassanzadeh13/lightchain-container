package evm;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.util.ByteArrayWrapper;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class RepositoryMock implements Repository {

    private Map<ByteArrayWrapper, Account> accounts = new HashMap<>();
    private RepositoryMock parent;

    public RepositoryMock() {
        this(null);
    }

    public RepositoryMock(RepositoryMock parent) {
        this.parent = parent;
    }

    protected Account getAccount(byte[] address) {
        ByteArrayWrapper key = new ByteArrayWrapper(address);

        if (accounts.containsKey(key)) {
            return accounts.get(key);
        } else if (parent != null && parent.exists(address)) {
            Account account = parent.getAccount(address);
            Account accountTrack = new Account(account);
            accounts.put(key, accountTrack);
            return accountTrack;
        } else {
            return null;
        }
    }

    @Override
    public boolean exists(byte[] address) {
        ByteArrayWrapper key = new ByteArrayWrapper(address);

        if (accounts.containsKey(new ByteArrayWrapper(address))) {
            return accounts.get(key) != null;
        } else if (parent != null) {
            return parent.exists(address);
        } else {
            return false;
        }
    }

    @Override
    public void createAccount(byte[] address) {
        if (!exists(address)) {
            accounts.put(new ByteArrayWrapper(address), new Account());
        }
    }

    @Override
    public void delete(byte[] address) {
        accounts.put(new ByteArrayWrapper(address), null);
    }

    @Override
    public long increaseNonce(byte[] address) {
        createAccount(address);
        return getAccount(address).nonce += 1;
    }

    @Override
    public long setNonce(byte[] address, long nonce) {
        createAccount(address);
        return (getAccount(address).nonce = nonce);
    }

    @Override
    public long getNonce(byte[] address) {
        Account account = getAccount(address);
        return account == null ? 0 : account.nonce;
    }

    @Override
    public void saveCode(byte[] address, byte[] code) {
        createAccount(address);
        getAccount(address).code = code;
    }

    @Override
    public byte[] getCode(byte[] address) {
        Account account = getAccount(address);
        return account == null ? null : account.code;
    }

    @Override
    public void putStorageRow(byte[] address, DataWord key, DataWord value) {
        createAccount(address);
        getAccount(address).storage.put(key, value);
    }

    @Override
    public DataWord getStorageRow(byte[] address, DataWord key) {
        Account account = getAccount(address);
        return account == null ? null : account.storage.get(key);
    }

    @Override
    public BigInteger getBalance(byte[] address) {
        Account account = getAccount(address);
        return account == null ? BigInteger.ZERO : account.balance;
    }

    @Override
    public BigInteger addBalance(byte[] address, BigInteger value) {
        createAccount(address);
        Account account = getAccount(address);
        return account.balance = account.balance.add(value);
    }

    @Override
    public RepositoryMock startTracking() {
        return new RepositoryMock(this);
    }

    @Override
    public Repository clone() {
        RepositoryMock copy = new RepositoryMock(parent);
        for (Map.Entry<ByteArrayWrapper, Account> entry : accounts.entrySet()) {
            copy.accounts.put(entry.getKey(), entry.getValue().clone());
        }
        return copy;
    }

    @Override
    public void commit() {
        if (parent != null) {
            parent.accounts.putAll(accounts);
        }
    }

    @Override
    public void rollback() {
        accounts.clear();
    }

    static class Account {
        public long nonce = 0;
        public BigInteger balance = BigInteger.ZERO;
        public byte[] code = new byte[0];
        public Map<DataWord, DataWord> storage = new HashMap<>();

        public Account() {
        }

        public Account(Account parent) {
            this.nonce = parent.nonce;
            this.balance = parent.balance;
            this.code = parent.code;
            this.storage = new HashMap<>(parent.storage);
        }

        public Account clone() {
            Account a = new Account();
            a.nonce = nonce;
            a.balance = balance;
            a.code = code.clone();
            a.storage = new HashMap<>(storage);

            return a;
        }
    }
}
