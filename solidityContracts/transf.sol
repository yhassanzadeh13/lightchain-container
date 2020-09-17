pragma solidity ^0.4.24;

//Test contract
contract transf {

    function  check(uint256 nmb) public pure returns(bool val) {
        if (nmb >= 10){
            return(true); //1
        }

        return(false); //0
    }
}