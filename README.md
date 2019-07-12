# LightChain

Current Branches
----------------
* **Master:** This branch contains the up-to-date blockchain development classes and methods.

* **Mass_Deployment:**  

* **testing:**  
--- 
Installation
---------------

* Clone The project to your local machine using:  ``` git clone https://github.com/NazirNayal8/LightChain ```  

* Open Eclipse IDE.  

* From the upper left side, File section, choose import from the menu.  

* Choose the option: Projects from Git.  

* Choose the option: Existing local repository.  

* Choose the option: Add, situated to the upper right.  

* Browse for the repository that you have cloned in `C:\Users\USER`, and choose it.  

* It will appear in the eclipse page, choose it from there.  

* Tick the "Import Existing Eclipse projects", then press next.  

* Choose Finish, and then the project is installed and ready to be built and run.  
---

Running Tests
--------------

* **In Master Branch:** All the blockchain related methods and functionalities are in the blockchain package. You should run the LightChainNode class in order to test these implementations. In the LightChainNode class there is a method names ask(), which can be used to control the node and perfrom queries such as searching and insertion of blocks and transactions. It can me modified to do more queries depending on what functionalities you would like to test.  Essentially, when you run the LightChainNode class, it will first ask you to enter the following:  
   - Address of introducer. If this is the first node you can simply enter "none".  
   - RMI Port. The port to which the RMI registry will be bound.  
   - Name of Private Key, which is the name of the file in which you would like to store your private key on disk.  
   - Name of Public Key, which is the name of the file in which you would like to store your public key on disk.  
   - Mode of Node. You will choose whether the node you are running is malicious or honest.  
 After that, you will have the information of the node printed, and you will also see the menu of queries that you would like to test.
