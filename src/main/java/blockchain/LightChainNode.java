package blockchain;

import hashing.Hasher;
import hashing.HashingTools;
import org.apache.log4j.Logger;
import signature.DigitalSignature;
import signature.SignedBytes;
import simulation.SimLog;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;
import underlay.InterfaceType;
import underlay.Underlay;
import underlay.rmi.RMIUnderlay;
import underlay.requests.lightchain.GetPublicKeyRequest;
import underlay.requests.lightchain.PoVRequest;
import underlay.requests.lightchain.RemoveFlagNodeRequest;
import underlay.responses.PublicKeyResponse;
import underlay.responses.SignatureResponse;
import util.Const;
import util.Util;

import java.io.FileNotFoundException;
import java.security.PublicKey;
import java.util.*;

import static underlay.responses.PublicKeyResponse.PublicKeyResponseOf;
import static underlay.responses.SignatureResponse.SignatureResponseOf;

public class LightChainNode extends SkipNode implements LightChainInterface {

  private static final long serialVersionUID = 1L;
  private List<Transaction> transactions;
  private DigitalSignature digitalSignature;
  private Hasher hasher;
  private Validator validator;
  public View view;
  public boolean mode;
  private int balance = 20;
  private SimLog simLog = new SimLog(true);
  public Logger logger;
  private Parameters params;
  private int token; // Is used to store the value of tokens owned by a node.
  private CorrectnessVerifier cv;
  public int Tmode; // Defines the mode for every node eg. 1 -> consumer | 2 -> producer
  private Underlay underlay;

  public Underlay getUnderlay() {
    return underlay;
  }

  /**
   * @param params contains necessary information for the node to function
   * @param introducer the address of the introducer node
   * @param isInitial a flag signaling whether this node is the first node in the network
   *     <p>TODO: add a specific LightChain config that includes Mode and remove it from params
   */
  public LightChainNode(Parameters params, int port, String introducer, boolean isInitial, Underlay underlay)
      {
    super(port, params.getLevels(), introducer, underlay);
    this.params = params;
    this.digitalSignature = new DigitalSignature();
    this.hasher = new HashingTools();
    this.transactions = new ArrayList<>();
    this.view = new View();
    this.mode = params.getMode();
    this.token = params.getInitialToken();
    this.logger = Logger.getLogger(port + "");
    Tmode = (int) Math.round(Math.random());
    String name = hasher.getHash(digitalSignature.getPublicKey().getEncoded(), params.getLevels());
    super.setNumID(Integer.parseInt(name, 2));
    name = hasher.getHash(name, params.getLevels());
    super.setNameID(name);

    if (isInitial) isInserted = true;

    // adds values of numID and nameID to lookup table
    NodeInfo peer = new NodeInfo(address, numID, nameID);
    addPeerNode(peer);

    this.underlay = underlay;
    underlay.setLightChainNode(this);

    if (!isInitial) {
      insertNode(peer);
    }

    view.updateToken(getNumID(), this.token);

    // This selects the mode for the lightchain working
    if (params.getChain() == params.CONTRACT_MODE) {
      cv = new ContractCV(this); // LightChainCV extends CorrectnessVerifier for native LightChain
    } else {
      cv = new LightChainCV(this); // ContractCV extends CorrectnessVerifier for the contract mode
    }
  }

  /**
   * This method goes to the tail of the blockchain and iterates over the transactions pointing at
   * it, and then updating the entries corresponding to the owners of the transactions in the view
   * table.
   *
   * <p>TODO: investigate storing only the index of the block in the view table instead of its numID
   */
  public View updateView() {
    try {
      logger.debug("Updating view");
      // get the tail of blockchain
      Block blk = getLatestBlock();
      // change numID to nameID format to search for transactions
      String name = numToName(blk.getNumID());
      // get transactions pointing at the tail
      List<Transaction> tList = getTransactionsWithNameID(name);
      if (tList == null) return null;
      // iterate over found transactions pointing at the blockchain
      for (int i = 0; i < tList.size(); ++i) {
        int owner = tList.get(i).getOwner();
        view.updateLastBlk(owner, blk.getNumID());
        // updating token in view
        view.updateToken(owner, this.token);
      }
      logger.debug("view successfully updated");
      return view;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This method finds the latest block on the blockchain and takes its numID and uses it to find
   * all transactions that have this numID as nameID and uses getTransactionsWithNameID() method to
   * get such transactions, and if the number of found transactions is at least TX_MIN, the
   * transactions are casted into a block and the block is sent for validation.
   */
  public Block mineAttempt() {
    try {
      long startTotal = System.currentTimeMillis();

      logger.debug("Attempting to mine");

      Block blk = getLatestBlock();
      // Change numID to a nameID string
      if (blk == null) {
        logger.error("Mining Failed: Failed to get latest block");
        return null;
      }

      logger.debug("Found Latest Block: " + blk.getNumID());

      String name = numToName(blk.getNumID());

      logger.debug("getting transaction batch");
      // Get all transaction with this nameID
      List<Transaction> tList = getTransactionsWithNameID(name);
      // If number of transactions obtained is less than TX_MIN then we terminate the
      // process
      if (tList == null || tList.size() < params.getTxMin()) {
        logger.debug("Mining Failed: not enough transaction found: " + tList.size());
        simLog.logMineAttemptLog(false, false, System.currentTimeMillis() - startTotal, -1);
        return null;
      }

      // If there are TX_MIN transaction then add them into a new block
      Block newBlk =
          new Block(
              blk.getHash(),
              getNumID(),
              getAddress(),
              tList,
              blk.getIndex() + 1,
              params.getLevels());
      // send the new block for PoV validation
      logger.debug("Validating new Block ...");

      long startValid = System.currentTimeMillis();
      boolean isValidated = validateBlock(newBlk);
      long endValid = System.currentTimeMillis();
      // TODO: find a way to avoid this null return
      if (!isValidated) {
        logger.debug("Block validation failed");
        simLog.logMineAttemptLog(
            true, false, System.currentTimeMillis() - startTotal, endValid - startValid);
        return null;
      }

      logger.debug("Mining Sucessful");

      // insert new block after it was validated
      insertBlock(newBlk, blk.getAddress());

      long endTotal = System.currentTimeMillis();
      simLog.logMineAttemptLog(true, true, endTotal - startTotal, endValid - startValid);

      return newBlk;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * inserts a flag node that points to the latest block
   *
   * @param blk the block for which a flag node will be inserted
   */
  private void insertFlagNode(Block blk) {
    super.insertDataNode(Const.ZERO_ID, blk.getHash());
  }

  /** removes the flag node pointing to the latest block that was inserted by this node */
  public void removeFlagNode() {
    super.delete(Const.ZERO_ID);
  }

  /**
   * This method creates a transaction of a given content and sends the transaction for validation
   * then returns the transaction
   *
   * @param cont content of transaction
   *     <p>TODO: When validating a transaction as it is being rejected, attempt a constant number
   *     of retries until it is accepted or else drop it. TODO: After inserting a transaction
   */
  public Transaction makeTransaction(String cont) {
    try {

      boolean success = false;
      int waitCount = 3;
      while (!success && waitCount > 0) {
        Block lstBlk = getLatestBlock();
        logger.debug("Prev found: " + lstBlk.getNumID());
        Transaction t =
            new Transaction(lstBlk.getHash(), getNumID(), cont, getAddress(), params.getLevels());
        boolean verified = validateTransaction(t);
        if (verified) {
          insertTransaction(t);
          logger.debug("Transaction Successfully Added");
          return t;
        }
        waitCount--;
      }
      logger.debug("Transaction Making Failed");
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * inserts a new transaction into the overlay
   *
   * @param t transaction to be inserted
   */
  public void insertTransaction(Transaction t) {

    super.insertNode(t);
  }

  /**
   * inserts a new block into the overlay
   *
   * @param blk block to be inserted into the overlay
   */
  public void insertBlock(Block blk, String prevAddress)
      throws FileNotFoundException {
    if (!prevAddress.equals(getAddress())) {
      underlay.sendMessage(
          new RemoveFlagNodeRequest(), prevAddress);

      insertNode(blk);
      insertFlagNode(blk);
    }
  }

  /** inserts the first block to the blockchain */
  public Block insertGenesis() {
    StringBuilder st = new StringBuilder();
    for (int i = 0; i < params.getLevels(); i++) {
      st.append("0");
    }
    String prev = st.toString();
    int index = 0;
    Block b = new Block(prev, getNumID(), getAddress(), index, params.getLevels());
    // use current address as prev when inserting genesis block
    insertNode(b);
    insertFlagNode(b);
    logger.debug("Inserting Genesis Block " + b.getNumID());
    return b;
  }

  /**
   * This method finds the latest block on the blockchain.
   *
   * @return the latest block on the ledger
   *     <p>TODO: this should be refactored
   */
  public Block getLatestBlock() {
    try {
      logger.debug("searching for flag");

      NodeInfo flag = searchByNumID(Const.ZERO_ID);

      logger.debug("searching for block");
      int num = Integer.parseInt(flag.getNameID(), 2);
      NodeInfo blk = searchByNumID(num);
      if (blk instanceof Block) return (Block) blk;
      else {
        logger.error(
            blk.getNumID()
                + " was returned when "
                + num
                + " was expected, its type is "
                + blk.getClass());
        logLevel(Const.ZERO_LEVEL);
        Thread.sleep(100);
        return getLatestBlock();
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("NullPointer: ", e);
      logLevel(Const.ZERO_LEVEL);
      return null;
    }
  }

  /**
   * This method gets the numID of a peer and returns its latest transactions using transaction
   * pointers
   *
   * @return the latest transaction for a particular owner
   */
  // TODO: this should be refactored, also check where it is being called
  public Transaction getLatestTransaction(int numID) {

    try {
      Transaction t = (Transaction) searchByNameID(numToName(numID));
      return t;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function retreives transactions with a certain nameID
   *
   * @param name the nameID for which we want to collect transactions that have it as nameID
   * @return a list of transactions that have name as nameID
   */
  public List<Transaction> getTransactionsWithNameID(String name) {

    List<NodeInfo> list = getNodesWithNameID(name);
    List<Transaction> tList = new ArrayList<>();
    logger.debug("found " + list.size() + " nodes");
    for (NodeInfo t : list) {
      if (t instanceof Transaction) tList.add((Transaction) t);
      if (tList.size() == params.getTxMin()) break;
    }
    return tList;
  }

  /**
   * This method is called by a node to validate a block that it has created. First it gets the
   * validators of the block, and then contacts each validator and asks them to validate the block
   * using PoV and then gets their signatures of the hash value of the block if the validation was
   * successful and gets null otherwise.
   *
   * @param blk is the block to be validated
   * @return true of block is valid, and false if block is not valid
   */
  public boolean validateBlock(Block blk) {
    try {

      List<NodeInfo> validators = getValidators(blk.toString());

      // add the owner's signature to the block
      SignedBytes mySignature = digitalSignature.signString(blk.getHash());

      blk.addSignature(mySignature);
      // iterate over validators and ask them to validate the block
      for (int i = 0; i < validators.size(); ++i) {
        // TODO: add a dummy signedBytes value
        SignatureResponse response = SignatureResponseOf(underlay.sendMessage(
                new PoVRequest(blk),
                validators.get(i).getAddress()));
        SignedBytes signature = response.result;

        // if one validator returns null, then validation has failed
        if (signature == null) {
          logger.debug("Block Rejected");
          return false;
        }
        blk.addSignature(signature);
      }
      // update the sigma array of the block with all the signatures
      logger.debug("Block Approved");

      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * The method is called by a node to validate a transaction that it has created First it gets the
   * validators of the transaction, and then contacts each of them to get their signatures and
   * stores their signatures in the sigma list.
   *
   * @param t transaction to be validated
   * @return true if transaction is valid, and false if it is not valid
   */
  public boolean validateTransaction(Transaction t) {

    long start = System.currentTimeMillis();

    int isAuthenticated = 0;
    int isSound = 0;
    int isCorrect = 0;
    int hasBalance = 0;

    try {

      // obtain validators
      List<NodeInfo> validators = getValidators(t.toString());

      // add the owner's signature of the transaction's hash value to the sigma
      SignedBytes mySignature = digitalSignature.signString(t.getHash());

      t.addSignature(mySignature);

      boolean validated = true;
      int numValidations = 0;
      long timePerValidator = 0;

      // iterate over validators and use RMI to ask them to validate the transaction
      for (int i = 0; i < validators.size(); ++i) {
        SignatureResponse response = SignatureResponseOf(underlay.sendMessage(
                new PoVRequest(t),
                validators.get(i).getAddress()));
        SignedBytes signature = response.result;

        if (signature.isAuth()) isAuthenticated++;
        if (signature.isCorrect()) isCorrect++;
        if (signature.isSound()) isSound++;
        if (signature.hasBalance()) hasBalance++;

        // if a validators returns null that means the validation has failed
        if (signature.getBytes() != null) {
          numValidations++;
          timePerValidator += signature.getValidationTime();
        }

        t.addSignature(signature);
      }

      validated = (numValidations >= params.getSignaturesThreshold());
      long end = System.currentTimeMillis();

      if (numValidations != 0) timePerValidator /= numValidations;

      simLog.logTransaction(
          validated,
          isAuthenticated,
          isSound,
          isCorrect,
          hasBalance,
          end - start,
          timePerValidator);

      if (validated) logger.debug("valid transaction");
      else logger.debug("invalid transaction");

      return validated;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method is used to validate a block. It checks : 1) Authenticity 2) Consistency 3)
   * Authenticity and soundness of the transactions it contains.
   *
   * @param blk is block to be validated using proof of validation
   * @return signature of validator in case block is valid, or null if block is invalid
   */
  public SignedBytes PoV(Block blk) {
    try {
      updateView();
      boolean isAuth = isAuthenticated(blk);
      boolean isCons = isConsistent(blk);
      boolean val = isAuth && isCons;
      if (val == false) return new SignedBytes(null, isAuth, true, true, true);
      // iterate over transactions and check them one by one
      List<Transaction> ts = blk.getTransactionSet();
      for (int i = 0; i < ts.size(); ++i) {
        if (!isAuthenticated(ts.get(i)) /* || !isSound(ts.get(i)) */) {
          logger.debug("Transaction inside block is not authentic");
          return new SignedBytes(null, isAuth, true, true, true);
        }
      }
      logger.debug("Block Approved");
      SignedBytes signedHash =
          new SignedBytes(
              digitalSignature.signString(blk.getHash()).getBytes(), isAuth, true, true, true);
      return signedHash;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This method recieves a block and checks if: 1) its hash value is generated properly 2) checks
   * if it contains the signature of its owner
   *
   * @param blk is block whose authenticity is to be checked
   * @return true if block is authentic, or false if block is unauthentic
   */
  public boolean isAuthenticated(Block blk) {
    try {

      // concatenate the string values of transactions
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < blk.getTransactionSet().size(); ++i) {
        sb.append(blk.getTransactionSet().get(i).toString());
      }
      // generate the hash value and then compare it with the block's
      String hash =
          hasher.getHash(blk.getPrev() + blk.getOwner() + sb.toString(), params.getLevels());
      if (!hash.equals(blk.getHash())) {
        logger.debug("Hash of block not generated properly");
        return false;
      }
      // get the sigma array of the block
      List<SignedBytes> blkSigma = blk.getSigma();
      // retrieve the public key of the owner of the block
      PublicKey ownerPublicKey = getOwnerPublicKey(blk.getOwner());
      boolean verified = false;
      if (ownerPublicKey == null) return false;
      // iterate over the sigma array looking for the signature of the owner
      for (int i = 0; i < blkSigma.size(); ++i) {
        boolean is = digitalSignature.verifyString(hash, blkSigma.get(i), ownerPublicKey);
        if (is) verified = true;
      }
      if (verified == false) {
        logger.debug("Block does not contain signature of owner");
      }

      return verified;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * A block is said to be consistent if its prev points to the current tail of the block, so at the
   * time of validation, validators should make sure that prev is still pointing to the tail of the
   * blockchain, or otherwise if the tail was updated is should terminate the validation process.
   *
   * @param blk whose consistency is to be checked
   * @return true of block is consistent, or false if it is not consistent
   */
  public boolean isConsistent(Block blk) {
    try {

      Block lstBlk = getLatestBlock();
      boolean res = blk.getPrev().equals(lstBlk.getHash());
      if (res == false) {
        logger.debug("Block not consistent");
      }

      return res;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method takes the hash of a transaction or a block and returns
   *
   * @param str hash of transaction or block whose validators are to be fetched
   * @return a list of validators for the given transactions
   */
  public List<NodeInfo> getValidators(String str) {
    try {
      // stores the validators to be returned
      List<NodeInfo> validators = new ArrayList<>();
      // taken is used to gaurantee taking only unique validators
      Map<String, Integer> taken = new HashMap<>();
      int validFound = 0;
      taken.put(address, 1); // To not take the node itself or any data node belonging to it.
      for (int i = 0; validFound < params.getAlpha() && i < 200; ++i) {
        String hash = hasher.getHash(str + i, params.getLevels());
        int num = Integer.parseInt(hash, 2);
        NodeInfo node = searchByNumID(num);
        if (taken.containsKey(node.getAddress())) continue;
        taken.put(node.getAddress(), 1);
        validators.add(node);
        validFound++;
      }
      return validators;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Checks the soundness, correctness and authenticity of a transaction using other methods. Checks
   * if the owner of the transaction has enough balance to cover validation fees. returns null in
   * case any of the tests fails, otherwise it signs the hashvalue of the transaction and sends the
   * signed value to the owner.
   *
   * @param t transaction to be validated by proof of validation
   * @return signature of validators in case transaction is valid, or null if not
   */
  public SignedBytes PoV(Transaction t) {
    boolean isAuth = false;
    boolean isCorrect = false;
    boolean isSound = false;
    boolean hasBalance = false;
    try {
      long startTime = System.currentTimeMillis();
      updateView();
      isAuth = isAuthenticated(t);
      isCorrect = cv.isCorrect(t);
      isSound = isSound(t);
      hasBalance = hasBalanceCompliance(t);

      boolean val = isAuth && isCorrect && isSound && hasBalance;
      if (val == false) return new SignedBytes(null, isAuth, isSound, isCorrect, hasBalance);
      logger.debug("Transaction Approved");
      SignedBytes signedHash =
          new SignedBytes(
              digitalSignature.signString(t.getHash()).getBytes(),
              isAuth,
              isSound,
              isCorrect,
              hasBalance);
      long endTime = System.currentTimeMillis();
      signedHash.setValidationTime(endTime - startTime);
      return signedHash;
    } catch (Exception e) {

      e.printStackTrace();
      return new SignedBytes(null, isAuth, isSound, isCorrect, hasBalance);
    }
  }

  /**
   * This method validated the soundness of a given transaction It checks if the given transaction
   * is associated with a block which does not precede the block that contains the transaction's
   * owner's last transaction.
   *
   * @param t transaction whose soundness is to be checked
   * @return true if transaction is sound, or false if it is not
   */
  public boolean isSound(Transaction t) {
    try {

      int prev = Integer.parseInt(t.getPrev(), 2);
      // TODO: rethink this assumption
      // assuming that if a node is not in the view yet then this is the first
      // transaction;
      if (!view.hasLastBlkEntry(t.getOwner())) {
        view.updateLastBlk(t.getOwner(), prev);
        return true;
      }

      // get the hash value of the block that contains the owner's latest transaction
      int blkNumID = view.getLastBlk(t.getOwner());

      // TODO: remove these checks after implementing type search
      NodeInfo b1 = searchByNumID(prev);
      if (!(b1 instanceof Block)) {
        logger.error("search for prev did not return a block: " + prev);
        return false;
      }
      NodeInfo b2 = searchByNumID(blkNumID);
      if (!(b2 instanceof Block)) {
        logger.error("search for latest block of owner did not return a block: " + prev);
        return false;
      }

      Block prevBlk = (Block) b1;
      Block thisBlk = (Block) b2;

      int tIdx = prevBlk.getIndex();
      int bIdx = thisBlk.getIndex();

      if (tIdx <= bIdx) logger.debug("Transaction not sound, Prev: " + tIdx + ", Latest: " + bIdx);
      else logger.debug("Transaction is sound");
      return tIdx > bIdx;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method receives a transaction and verifies two things: 1- The hashvalue of the transaction
   * was generated according to the correct equation 2- The sigma of the transaction contains the
   * transaction's owner signed value of the hashvalue
   *
   * @param t transaction whose authenticity is to be verified
   * @return true if transaction is authentic, or false if not.
   */
  public boolean isAuthenticated(Transaction t) {
    try {

      // generate the hash using the equation to check if it was generated correctly
      String hash = hasher.getHash(t.getPrev() + t.getOwner() + t.getCont(), params.getLevels());
      // return false if it was not generated properly
      if (!hash.equals(t.getHash())) {
        logger.debug("Transaction hash value not generated properly");
        return false;
      }
      // now get the sigma array from transaction and iterate over signatures it
      // contains
      List<SignedBytes> tSigma = t.getSigma();
      PublicKey ownerPublicKey = getOwnerPublicKey(t.getOwner());
      boolean verified = false;
      if (ownerPublicKey == null) return false;
      for (int i = 0; i < tSigma.size(); ++i) {
        // if we find one signature which belongs to the owner then we set verified to
        // true
        boolean is = digitalSignature.verifyString(hash, tSigma.get(i), ownerPublicKey);
        if (is) verified = true;
      }
      if (verified == false) {
        logger.debug("Transaction does not contain signature of owner");
      }

      return verified;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Checks if the owner of the transaction has enough balance to cover the fees of validation
   *
   * @param t transaction whose owner will be checked for enough balance compliance
   * @return true if owner of transaction has enough balance to cover validation fees
   */
  public boolean hasBalanceCompliance(Transaction t) {
    try {
      long start = System.currentTimeMillis();

      if (!view.hasBalanceEntry(t.getOwner())) {
        view.updateBalance(t.getOwner(), params.getInitialBalance());
        return true;
      }
      int ownerBalance = view.getBalance(t.getOwner());

      return ownerBalance >= params.getValidationFees();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method recieves the numID of an owner of a transaction or a block and first verifies of
   * the given public key truly belongs to the owner by hashing the provided public key and
   * comparing it with the given numID if the test fails it prints it to console and return null.
   * otherwise it returns the public key of the owner.
   *
   * @param num numerical ID of node whose public key is to be retrieved
   * @return the public key of the node whose numerical ID was supplied
   */
  public PublicKey getOwnerPublicKey(int num) {
    try {
      // find owner from the network
      NodeInfo owner = searchByNumID(num);

      if (owner.getNumID() != num) {
        logger.debug("no node was found with given numID");
        logger.debug("Expected: " + num + ", Found: " + owner.getNumID());
        return null;
      }

      PublicKeyResponse response = PublicKeyResponseOf(underlay.sendMessage(
              new GetPublicKeyRequest(),
              owner.getAddress()));
      PublicKey pk = response.result;

      // Hash the public key and store the hash value as int
      int hashedKey = Integer.parseInt(hasher.getHash(pk.getEncoded(), params.getLevels()), 2);
      // if hashedKey is not equal to the provided numID, then there is a problem
      // and it is printed to the console
      if (hashedKey != num) {
        logger.debug("given numID does not match with hash value of public key.");
        return null;
      }
      return pk;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return null;
    }
  }

  public PublicKey getPublicKey() {
    return digitalSignature.getPublicKey();
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public int getBalance() {
    return balance;
  }

  public String numToName(int num) {
    String name = Integer.toBinaryString(num);
    while (name.length() < params.getLevels()) {
      name = "0" + name;
    }
    return name;
  }

  public boolean getMode() {
    return mode;
  }

  // TODO: decide on keeping or removing those after refactoring simulation
  // approach

  public SimLog startSim(int numTransactions, int pace) {

    simLog = new SimLog(Const.HONEST);
    Random rnd = new Random();
    try {
      for (int i = 0; i < numTransactions; i++) {
        int randomWait = rnd.nextInt(500) + 500;
        Thread.sleep(randomWait);
        logger.debug("Making Transaction ...");
        makeTransaction(Util.getRandomString(135));
        if (true) {
          randomWait = rnd.nextInt(500) + 500;
          Thread.sleep(randomWait);
          logger.debug("Mining ...");
          mineAttempt();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return simLog;
  }

  public void shutDown() {
    System.exit(0);
  }

  @Override
  public int getToken() {
    return token;
  }
}
