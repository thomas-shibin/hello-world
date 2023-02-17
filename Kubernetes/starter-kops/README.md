# Kubernetes Setup With kOps

## Introduction to kOps

```kOps (Kubernetes Opertations)``` is used to create and manage a production-grade Kubernetes cluster on cloud. It’s integrated with AWS and Google Cloud (GCE) and some other platforms. <br />
It’s a command line tool that offers simple commands to create, update and delete the cluster on cloud environment.

## Environment

### Cluster

You can identify the instance type fits your usage from ![here](https://aws.amazon.com/ec2/instance-types)
```I will be using t3.large instance type as an example```

1. AWS EC2 Instance with admin privileges(1 x t3.large with 30 GiB with Amazon Linux OS (default)) - We will use this Instance to install kOps, kubectl and setup a cluster <br />
```We will be creating a cluster with 1 Master and 2 Nodes having t3.large instance types```

2. SSH Client with a .pem file for accessing EC2 instance. ```Eg: MobaXterm, PuTTy```

### Workloads

1. 1 httpbin services x 2 replicas
2. Each service requires 0.5 vCPU, 256 MiB RAM

## How-To

### Configure AWS CLI

1. SSH into the ec2 instance and follow the steps below. <br />
For general use, the ```aws configure``` command is the fastest way to set up your AWS CLI installation

The following example shows sample values. Replace them with your own values as described in the following sections. <br />
```$ aws configure
   AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
   AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
   Default region name [None]: us-west-2
   Default output format [None]: json
```

Ref:![CLI configure](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html)

Run update packages / metadata / libraries <br />
```$ sudo yum update -y```

### Install kOps

1. Download the binary file <br />
```$ curl -Lo kops https://github.com/kubernetes/kops/releases/download/$(curl -s https://api.github.com/repos/kubernetes/kops/releases/latest | grep tag_name | cut -d '"' -f 4)/kops-linux-amd64```

2. Apply execute permission to the binary <br />
```$ chmod +x kops```

3. Move the file to a proper destination <br />
```$ sudo mv kops /usr/local/bin/kops```

### Install kubectl

1. Download the binary file. <br />
```$ curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"```

2. Apply execute permission to the binary <br />
```$ chmod +x ./kubectl```

3. Copy the binary to the folder in your PATH. <br />
```$ mkdir $HOME/bin && cp ./kubectl $HOME/bin/kubectl && export PATH=$HOME/bin:$PATH```

4. Add the $HOME/bin path to your shell initialization file. <br />
```$ echo 'export PATH=$HOME/bin:$PATH' >> ~/.bashrc```

5. After kubectl installation, verify its version. <br />
```$ kubectl version --client --output=yaml```

### S3 bucket

1. To see existing bucket <br />
```$ aws s3 ls```

2. Create a s3 bucket for kops to store its state and configurations. <br />
```$ aws s3 mb s3://helloworld-k8-state-store --region us-east-2``` <br />
NOTE: change the bucket name (must be unique) and region (change as per your region)

3. Enable versioning to store / restore prev versions. <br />
```$ aws s3api put-bucket-versioning --bucket helloworld-k8-state-store --versioning-configuration Status=Enabled```

4. Export the variable KOPS_STATE_STORE. <br />
```$ export KOPS_STATE_STORE=s3://helloworld-k8-state-store```

5. Add the variable to your shell initialization file. <br />
```$ echo 'export KOPS_STATE_STORE=s3://saltsecurity-k8-state-store' >> ~/.bashrc```

### Kubernetes Cluster

1. Create the cluster with 2 nodes of size t3.large <br />
NOTE: change the zone (change as per your region) and (optional) vpc if you want the cluster on any specific vpc, <br />
(optional) subnet if you want to choose the subnet from any of the availability zone in the vpc  <br />
```$ kops create cluster --node-count=2 --node-size=t3.large --zones=us-east-2a --name=helloworld.k8s.local  (optional)--vpc=vpc-fsjh7878221h (optional)--subnets=subnet-asjkda33434```

2. Get the cluster info to see if the above command created cluster. <br />
```$ kops get cluster
NAME                    CLOUD   ZONES
helloworld.k8s.local    aws     us-east-2
```

3. Update the cluster.<br />
```$ kops update cluster --name helloworld.k8s.local --yes --admin```<br />
NOTE: If you face the error ```ssh public key must be specified when running with aws``` while running the above command, follow the steps below.<br />
   - ```$ cd ~./ssh```<br />
   - ```$ vi id_rsa.pub```  and copy the public key here using PuttyGen(use the .pem file to generate this).<br />
   - ```$ kops create secret --name helloworld.k8s.local sshpublickey admin -i ~/.ssh/id_rsa.pub```<br />
   - Run the ```$ kops update cluster --name helloworld.k8s.local --yes --admin```

4. rolling update command updates a kubernetes cluster to match the cloud and kOps specifications.<br />
```$  kops rolling-update cluster```<br />
NOTE: sometimes you need to add ```--cloudonly``` with the above command if the cluster ask for it.<br />
```
NAME                 STATUS  NEEDUPDATE      READY   MIN     TARGET  MAX     NODES
master-us-east-2a    Ready   0               1       1       1       1       1
nodes-us-east-2a     Ready   0               2       2       2       2       2
```

5. Validate the cluster
```
$ kops validate cluster

INSTANCE GROUPS
NAME                    ROLE    MACHINETYPE     MIN     MAX     SUBNETS
xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

NODE STATUS
NAME                    ROLE    READY
i-3242424223343dsfd     node    True
i-0c0928491cff9e4d7     node    True
i-0cb2584cd47bbb4c6     master  True

Your cluster helloworld.k8s.local is ready
```
NOTE: If you face this issue ```Validation failed: unexpected error during validation: error listing nodes: Unauthorized``` (You won't find this issue with the latest kOps) run this.<br />
```$ kops export kubecfg --admin```

## Kubernetes control plane

If the cluster master and nodes are READY we can set up the dashboard.

1. Get the cluster info.<br />
```$ kubectl cluster-info
Kubernetes control plane is running at https://api-xxx.elb.amazonaws.com
```

2. Run the config file to access the dashboard <br />
```$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml```

## Create An Authentication Token (RBAC)

To find out how to create sample user and log in follow ![Creating sample user](https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/creating-sample-user.md) guide.<br />
NOTE: use --duration=000h (eg: --duration=720h) to set expiration, default is 1 hour

## Access

1. To access Dashboard from your local workstation you must create a secure channel to your Kubernetes cluster. Run the following command:<br />
```kubectl proxy (optional) --port=8001```<br />

2. Run this in cmd (for Windows).<br />
```ssh -i "helloworld.pem" -L 8001:localhost:8001 <ec2 user-name>@<ec2 Public IPv4 DNS>```

3. Now access Dashboard at:<br />
```http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/```<br />
Use the Authentication Token from previos step to access the dashboard


**Happy Kubernetes** :rocket: :rocket: :rocket:




