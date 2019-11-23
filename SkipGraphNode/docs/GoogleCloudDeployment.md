# Scenario of Google Cloud Deployment

### Step 1:
Two main docker images will need to be constructed:
- slave  image
- master image

### Step 2:
Deploy the master image to one server with a static public IP address

`Rational`: Using one master to initialize the testing is one of the easiest
approaches to get a functional skipGraph up and running.

### Step 3:
Deploy the desired number of slave images. They will automatically connect with
the master container, and get their configurations.

### Notes:
The deployment will be carried out using the command line tool gcloud to deploy
the containers via kubernetes.


