# Kubernetes Setup With kOps

## Introduction to kOps

```kOps (Kubernetes Opertations)``` is used to create and manage a production-grade Kubernetes cluster on cloud. It’s integrated with AWS and Google Cloud (GCE) and some other platforms. 
It’s a command line tool that offers simple commands to create, update and delete the cluster on cloud environment.

## Environment

### Cluster

You can identify the instance type fits your usage from ![here](https://aws.amazon.com/ec2/instance-types)
```I will be using t3.large instance type as an example```

1. AWS EC2 Instance with admin privileges(1 x t3.large with 30 GiB with Amazon Linux OS (default)) - We will use this Instance to install kOps, kubectl and setup a cluster

```We will be creating a cluster with 1 Master and 2 Nodes having t3.large instance types```

2. SSH Client with a .pem file for accessing EC2 instance. ```Eg: MobaXterm, PuTTy```

### Workloads

1. 1 httpbin services x 2 replicas
2. Each service requires 0.5 vCPU, 256 MiB RAM

## How-To

### Configure AWS CLI

1. SSH into the ec2 instance and follow the steps below.

For general use, the ```aws configure``` command is the fastest way to set up your AWS CLI installation

The following example shows sample values. Replace them with your own values as described in the following sections.

```$ aws configure
   AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
   AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
   Default region name [None]: us-west-2
   Default output format [None]: json
```

Ref:![CLI configure](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html)

Run update packages / metadata / libraries

```$ sudo yum update -y```

### Install kOps

1. Download the binary file
```$ curl -Lo kops https://github.com/kubernetes/kops/releases/download/$(curl -s https://api.github.com/repos/kubernetes/kops/releases/latest | grep tag_name | cut -d '"' -f 4)/kops-linux-amd64```

2. Apply execute permission to the binary
```$ chmod +x kops```

3. Move the file to a proper destination
```$ sudo mv kops /usr/local/bin/kops```

### Install kubectl

1. Download the binary file.
```$ curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"```

2. Apply execute permission to the binary
```$ chmod +x ./kubectl```

3. Copy the binary to the folder in your PATH.
```$ mkdir $HOME/bin && cp ./kubectl $HOME/bin/kubectl && export PATH=$HOME/bin:$PATH```

4. Add the $HOME/bin path to your shell initialization file.
```$ echo 'export PATH=$HOME/bin:$PATH' >> ~/.bashrc```

5. After kubectl installation, verify its version.
```$ kubectl version --client --output=yaml```

### S3 bucket

1. To see existing bucket
```$ aws s3 ls```

2. Create a s3 bucket for kops to store its state and configurations.
```$ aws s3 mb s3://helloworld-k8-state-store --region us-east-2```
NOTE: change the bucket name (must be unique) and region (change as per your region)

3. Enable versioning to store / restore prev versions.
```$ aws s3api put-bucket-versioning --bucket helloworld-k8-state-store --versioning-configuration Status=Enabled```

4. Export the variable KOPS_STATE_STORE.
```$ export KOPS_STATE_STORE=s3://helloworld-k8-state-store```

5. Add the variable to your shell initialization file.
```$ echo 'export KOPS_STATE_STORE=s3://saltsecurity-k8-state-store' >> ~/.bashrc```

### Kubernetes Cluster

***Work in progress***





