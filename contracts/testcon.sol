pragma solidity ^0.4.24;

// TestContract receives an uint256 and returns a boolean value 
contract testcon {

    function  check(uint256 nmb) public pure returns(bool val) {
        if (nmb >= 10){
            return(true); //1
        }

        return(false); //0
    }
}