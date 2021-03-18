# Documentation for using smartcontract functionality:

## Steps to follow:

1. Place the smart contract in the folder solidityContract.
2. The token parameter is just used for the value which is passed to smart contract.
3. This parameter can be changed according to the use-case.
4. In the file "src/main/java/evm/Contract.java" name of contract and the function's of contract should be mentioned.
5. For multiple functions introduce a new variable and pass it as a parameter to the TransctSol() function of "Contracttransation.java".
6. Keep in mind to pass parameters while writing function name (eg. sfunc(uint256,address)).

Important Step:

7. For every different smart contract we have to write a different version of TransctSol() function.

In  "byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(toSend).getData());" if sending multiple parameters to smartcontract then have to add "DataWord.of(some_parameter)".

```
Example: 
    byte[] method = HashUtil.keccak256("transfer(address,uint256)".getBytes(StandardCharsets.UTF_8));
    byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(address).getData(),
                 DataWord.of(toSend).getData());
```
