# Introdution to using smart contract functionality on LightChain:
This is a very first implementation of smart contracts on LightChain. 
<br>Using this brief guide I will walk you through the step by step process to try and run the simulation by taking our "testcon.sol" as an example.
<br>Note: If you want to add new contract or rename this contract, make changes in the make file and pass the new name.  
## Steps to follow:
1. Place the smart contract in the folder **contracts** (currently testcon.sol is already placed there).
2. The token parameter is used to represent an asset, aside from the balance parameter.
3. This parameter can be changed according to the different use-cases.
4. In the file **Contract.java** name of contract and the function's of contract should be mentioned.
```
Example:
    public String contractName1 = "testcon.bin";
    public String functname1 = "check(uint256)";
```
5. These parameters will be passed to the TransctSol() method which is called in isCorrect() function of ContractCV.java.
6. For multiple functions introduce another variable and pass it as a parameter to the TransctSol() function of **ContractTransaction.java**.
7. Keep in mind to pass parameters while writing function name (eg. newFunc(uint256,address)).
8. For every different smart contract we have to write a different function in **ContractTransaction.java** just like TransctSol() function.

```
BigInteger toSend = new BigInteger(String.valueOf(token)); 

//This takes the parameter which we want to send to the smart contract function, like token in our case.
```

9. We can also pass more values like if _amtSell_ is the value we want to deduct from token.
```
BigInteger psell = new BigInteger(String.valueOf(amtSell));
```
10. New contracts are created by this function and then their address is returned. 
```
byte[ ] contractAddress1 = createContract(contractloc, origin, nonce, gas);
```
11. **byte[] method** contains the function with which we want to interact.

12. **byte[] data** are the combination of different data we want to send to the function.
13. In **byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(toSend).getData());**. 
<br>If sending multiple parameters to smartcontract then have to add **DataWord.of(some_parameter)**.

```
Example: 
    byte[] method = HashUtil.keccak256("transfer(address,uint256)".getBytes(StandardCharsets.UTF_8));
    byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(address).getData(),
                 DataWord.of(toSend).getData());
```
14. Transaction creates a mock transaction and then TransactionExecutor runs this transaction.
15. Output is obtained in the receipt, which can be viewed by **receipt.getReturnData()**