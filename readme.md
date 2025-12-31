[![][tpi-logo]][tpi-url]
# digiRunner:The Unified Control Plane for APIs & AI Services
Bridge your infrastructure to the AI revolution. Securely.
An enterprise-grade API Gateway built to govern Microservices and orchestrate Large Language Models (LLMs) with precision.
[TPI.dev](https://tpi.dev) | [Documentation](https://docs.tpi.dev/) | [Blog](https://tpi.dev/blog) | [Community](https://github.com/TPIsoftwareOSPO/digiRunner-Open-Source/discussions)| [LinkedIn](https://www.linkedin.com/company/opentpi/)
## Table of contents
- [Overview](#overview)
- [Quick Start](#quick-start)
  - [Using Container](#1-using-container)
    - [Option 1: Docker](#option-1-docker)
    - [Option 2: Docker-Compose](#option-2-docker-compose)
    - [Option 3: Kubernetes](#option-3-kubernetes)
    - [Option 4: Helm](#option-4-helm)
  - [Run digiRunner Instantly on Your Local Machine](#2-run-digirunner-instantly-on-your-local-machine)
  - [Run Your Own Build](#3-run-your-own-build)
- [Create a Simple Api Proxy](#create-a-simple-api-proxy)
- [Documentation](#documentation)
- [Build Your Own JAR](#build-your-own-jar)
- [Run digiRunner in a Local Container Registry](#run-digirunner-in-a-local-container-registry)

## Overview

digiRunner is a lightweight, high-performance API Gateway designed for the modern hybrid cloud. As enterprises race to adopt Generative AI, they face a new set of challenges: unpredictable costs, vendor lock-in, and security risks associated with exposing internal data to public models.
digiRunner evolves beyond traditional traffic management to become your AI Gateway. It acts as a smart bridge between your applications and AI providers (like OpenAI, Azure, or local Ollama instances), allowing you to decouple your business logic from the rapidly changing AI landscape.
With digiRunner, you stop hardcoding API keys and start managing AI as a governed infrastructure asset.


## Why digiRunner AI Gateway?

We provide the Guardrails and Governance missing from standard model APIs.

1. 🛡️ Zero Vendor Lock-in (Universal Interface)

Stop rewriting code every time a new model is released.
- Provider Abstraction: Define your AI Provider (e.g., OpenAI, Azure) and Model configurations centrally. Your apps call a unified digiRunner endpoint, allowing you to switch backend models instantly without deploying new code.
- Secure Key Management: Never expose provider API keys in client-side code again. digiRunner injects credentials securely at the gateway layer.

2. 💰 FinOps & Tokenomics Control

Traditional rate limiting (Requests Per Minute) fails with LLMs, where a single request can consume 10k+ tokens. digiRunner understands Tokenomics.
- Granular Cost Control: Enforce strict Input Limits (prevent long context abuse) and Output Limits (prevent runaway generation).
- Flexible Policy: Choose between Reject (hard stop) to cap budget, or Use Anyway to allow traffic while flagging overages for audit.

3. 📝 Prompt Engineering as Infrastructure

Treat your Prompts like APIs—versioned, managed, and visible.
- Template Registry: Use the built-in AI Prompt Template engine to create, update, and enable/disable prompts globally.
- Standardization: Ensure all applications use approved, optimized prompts to reduce hallucinations and ensure brand consistency.


## Solving Real API Challenges

| **Challenge**                             | **digiRunner Solution**                                                                  |
|-------------------------------------------|------------------------------------------------------------------------------------------|
| Inconsistent access control               | Visual RBAC, API keys, OAuth2 & OIDC support                                             |
| Disorganized microservice endpoints       | Unified API Gateway with intelligent routing                                             |
| High learning curve for devs & ops        | UI-driven config + AI-powered documentation                                              |
| Siloed logs and monitoring gaps           | Built-in dashboards and real-time analytics                                              |
| Slow APIs dragging down overall system performance.        | Intelligent traffic shaping with "Fast Lanes" prioritizes fast APIs, preventing system bottlenecks.              |
| Limited visibility into performance       | Track API performance and catch anomalies before users do                                |
| Lack of scalability and governance        | Build API-first apps with policy-based traffic control and enterprise-grade security     |



## Core API Management Features
Under the hood, digiRunner remains a powerhouse for standard RESTful services:
- High Performance: Low-latency routing designed for high-concurrency environments.
- K8s & Hybrid Ready: Seamlessly integrates with Kubernetes (K3s/Rancher) for local or cloud deployments.
- Full Lifecycle Management: Design, Publish, Secure, and Analyze your APIs from a single dashboard.


## Where digiRunner Fits: Real-World Scenarios

digiRunner empowers teams across industries to implement mission-critical API management use cases with ease:

•	Financial Services – Enforce security standards and control access to customer transaction APIs across digital banking and fintech ecosystems.

•	Retail & E-commerce – Manage catalog, inventory, and checkout APIs with built-in rate limiting, version control, and monitoring during high-traffic seasons.

•	Healthcare & Insurance – Govern sensitive API endpoints with fine-grained access policies, audit trails.

•	Software & SaaS Providers – Offer a scalable and secure API layer to partners and developers with clear documentation and usage analytics.

•	Government & Public Sector – Unify legacy and modern service APIs under a single gateway to simplify external integrations and ensure compliance.

These use cases build on digiRunner’s core strengths in observability, scalability, and governance—giving teams a stable foundation for growth and agility.


## Service Structure

<img width="1344" height="768" alt="Readme_Image_2025_12_16" src="https://github.com/user-attachments/assets/a8dacaf4-cdbf-4e0f-99f2-d74bd48da746" />




## Quick Start

> Before installing digiRunner, make sure your machine meets the following minimum system requirements:
>
> * CPU >= 2 Core
>
> * RAM >= 4 GiB

### 1. Using Container

choose one of the following options to launch service by container

#### Option 1: Docker

```shell
docker run -it -d -p 31080:18080 tpisoftwareopensource/digirunner-open-source
```

#### Option 2: Docker-Compose

> Based on the content of [deploys/docker-compose/docker-compose.yml](deploys/docker-compose/docker-compose.yml)

```yml
name: digirunner-open-source
services:
    dgr:
        image: tpisoftwareopensource/digirunner-open-source
        ports:
            - "31080:18080"
        environment:
            - TZ=Asia/Taipei
```

- save above configuration to `opendgr-compose.yml`
- run `docker-compose -f opendgr-compose.yml up -d` at the same directory with `opendgr-compose.yml`

#### Option 3: Kubernetes

> Based on the content of [deploys/kubernetes/digirunner-open-source.yml](deploys/kubernetes/digirunner-open-source.yml)


```yml
apiVersion: v1
kind: Service
metadata:
  name: digirunner-open-source-svc
spec:
  ports:
    - name: tcp
      nodePort: 31080
      port: 18080
      protocol: TCP
      targetPort: 18080
  selector:
    app: digirunner
  sessionAffinity: None
  type: NodePort

---

apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: digirunner
  name: digirunner-open-source-deploy
spec:
  replicas: 1
  selector:
    matchLabels:
      app: digirunner
  template:
    metadata:
      labels:
        app: digirunner
      namespace: digirunner-open-source-ns
    spec:
      containers:
        - env:
            - name: TZ
              value: Asia/Taipei
          image: tpisoftwareopensource/digirunner-open-source
          imagePullPolicy: Always
          name: digirunner
          ports:
            - containerPort: 18080
              name: tcp
              protocol: TCP
          workingDir: /opt/digirunner
```

- save above configuration to `digirunner-open-source.yml`
- run `kubectl apply -f digirunner-open-source.yml`

#### Option 4: Helm

Contributions:

- **[how-to-package-digirunner-using-helm](https://github.com/vulcanshen-tpi/how-to-package-digirunner-using-helm)**
  - Step-by-step guide on how to package the digirunner open source project using Helm.
  - Quickly install the example

#### Connect to service

- Open your browser and navigate to: http://localhost:31080/dgrv4/login
- Use the default credentials to login: 
  - username: `manager`
  - password: `manager123`
---


### 2. Run digiRunner Instantly on Your Local Machine

If you want to **try digiRunner quickly without installation or setup**, you can use our pre-packaged version for your operating system.

#### 🧩 Step 1. Download the Package
Choose your OS and download the corresponding file from the [release](https://github.com/TPIsoftwareOSPO/digiRunner-Open-Source/releases/):

- **macOS (ARM64):** [`digirunner-opensource-macos-arm64-vX.X.X.X(version).zip`]  
- **Windows (AMD64):** [`digirunner-opensource-windows-amd64-vX.X.X.X(version).zip`]
  
#### ⚙️ Step 2. Extract and Run
1. Unzip the downloaded package.  
2. Open the extracted folder.  
3. Double-click **`quickstart.exe`** to launch digiRunner on your local machine.  

You can now start exploring digiRunner immediately — no installation, no configuration required!

#### 🌐 Step 3. Access digiRunner in Your Browser
After launching digiRunner, open your browser and go to:

👉 [http://localhost:18080/dgrv4/login](http://localhost:18080/dgrv4/login)

Use the following default credentials to log in:

```

username: manager
password: manager123

```

Once logged in, you can start exploring digiRunner’s management console and test its features locally.

#### ⚠️ Note
You may receive a **security or firewall warning** from macOS or Windows when you run the file for the first time.  
This is because we haven’t yet registered with Apple or Microsoft developer programs.  

> Simply **allow or bypass the warning** (e.g., “Keep Anyway” / “Allow app to run”) to continue.  
> Once launched, digiRunner will run safely and locally on your machine.

#### 🧹 Step 4. Clean Up
After testing, you can simply **delete the entire extracted folder** — digiRunner does not modify or install anything on your system.

---

This Quickstart version is ideal for users who want to:
- Test digiRunner locally in just a few minutes  
- Explore API management features without setup overhead  
- Safely remove everything after testing  

Enjoy your hands-on experience with digiRunner! 💡



### 3. Run Your Own Build

### Pre-requisites

- OpenJDK 25

---

1. Clone the repository: 
    ```sh
    git clone https://github.com/TPIsoftwareOSPO/digiRunner-Open-Source.git
    ```
2. Change directory:
    ```shell
    cd digiRunner-Open-Source/
    ```
3. Run the service:
    ```sh
    ./gradlew :dgrv4_Gateway_serv:bootRun
    ```

4. Wait for the digiRunner banner to appear.

 ```
      _       ____                                      _  _   
   __| | __ _|  _ \ _   _ _ __  _ __   ___ _ __  __   __ || |  
  / _` |/ _` | |_) | | | | '_ \| '_ \ / _ \ '__| \ \ / / || |_ 
 | (_| | (_| |  _ <| |_| | | | | | | |  __/ |     \ V /|__   _|
  \__,_|\__, |_| \_\\__,_|_| |_|_| |_|\___|_|      \_/    |_|  
        |___/                                                  
========== dgRv4 web server info ============
...
```

5. Open your browser and navigate to: http://localhost:18080/dgrv4/login
6. Use the default credentials to login: 
   - username: `manager`
   - password: `manager123`

## Create a Simple API Proxy

- [Documentation/create_a_simple_api_proxy](https://docs.tpi.dev/get-started/registering-your-first-apis-with-digirunner)

## Documentation

- [Documentation](https://docs.tpi.dev/)

## Build Your Own JAR

1. Change to digiRunner directory:
    ```sh
    cd digiRunner/
    ```
2. Build the JAR: 
    ```sh
    ./gradlew :dgrv4_Gateway_serv:clean :dgrv4_Gateway_serv:bootJar
    ```
   
3. Locate the JAR file: `dgrv4_Gateway_serv/build/libs/digiRunner-{version}.jar`
4. Run the JAR:
    ```sh
    java -jar dgrv4_Gateway_serv/build/libs/digiRunner-{version}.jar --digiRunner.token.key-store.path=$PWD/dgrv4_Gateway_serv/keys
    ```

## Run digiRunner in a Local Container Registry

### 1. Build the Image

#### Change to digiRunner directory:

```sh
cd digiRunner/
```
#### Build the Docker image:

```sh
docker build -t digirunner .
```

### 2. Run the container

```sh
docker run -p 18080:18080 digirunner
```

Open your browser and navigate to: http://localhost:18080/dgrv4/login




[tpi-url]: https://tpi.dev/
[tpi-logo]: /digiRunner.png

