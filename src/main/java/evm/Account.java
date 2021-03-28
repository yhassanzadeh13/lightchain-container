package evm;

import org.ethereum.vm.DataWord;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
* This class is used to represent all the data related to an account address. 
* It is further used by the RepositoryMock class.
*/
class Account {
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

