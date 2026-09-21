# 🌱 Green Cloud Framework for AI-Aware Virtual Machine Placement and Energy Optimization

<p align="center">

  <h1 align="center">Green Cloud Framework</h1>

  <p align="center">
    <b>AI-Aware Virtual Machine Placement and Energy Optimization</b>
  </p>

  <p align="center">
    A CloudSim Plus based simulation framework for monitoring cloud resources,
    estimating energy consumption, analysing VM placement, tracking migrations,
    monitoring SLA violations, and visualizing cloud performance through a
    live monitoring dashboard.
  </p>

</p>

---

## 📌 Project Overview

Cloud data centers contain large numbers of physical servers that host virtual
machines (VMs) and execute computational workloads. Efficient management of
these resources is important because unnecessary resource utilization can
increase power consumption and operational cost.

This project develops a **simulation-based Green Cloud Framework** using
**Java, Maven and CloudSim Plus**.

The system creates a virtual cloud data center containing:

- Physical Hosts
- Virtual Machines (VMs)
- Cloudlets representing workloads
- VM placement and resource allocation
- Resource monitoring
- Energy estimation
- VM migration tracking
- SLA monitoring
- Live dashboard visualization
- CSV-based reporting

The framework allows different cloud configurations to be tested without
requiring a physical cloud data center.

The project establishes a monitored baseline environment for studying
energy-aware VM placement and provides the foundation for AI-aware
optimization.

---

# 🎯 Objectives

The main objectives of the project are:

1. Simulate a cloud data center using CloudSim Plus.
2. Create configurable physical hosts.
3. Create and allocate virtual machines.
4. Generate and execute cloudlet workloads.
5. Monitor host CPU and RAM utilization.
6. Monitor VM resource utilization.
7. Track cloudlet execution and completion.
8. Estimate host power consumption.
9. Calculate cumulative energy consumption.
10. Monitor VM migration events.
11. Track SLA violations.
12. Display simulation information using a live dashboard.
13. Export simulation results to CSV files.
14. Provide a baseline for AI-aware VM placement and energy optimization.
15. Allow different cloud configurations to be evaluated experimentally.

---

# 🧠 Core Idea

The basic workflow of the system is:

```text
User Configuration
        │
        ▼
CloudSim Plus Simulation
        │
        ▼
┌───────────────────────────┐
│       Data Center         │
│                           │
│  Physical Hosts           │
│       │                   │
│       ▼                   │
│  Virtual Machines         │
│       │                   │
│       ▼                   │
│  Cloudlets / Workloads    │
└───────────────────────────┘
        │
        ▼
Monitoring & Management
        │
 ┌──────┼────────┬──────────┐
 ▼      ▼        ▼          ▼
CPU    RAM     Energy    Migration
        │
        ▼
SLA Monitoring
        │
        ▼
Reports + CSV + Dashboard

The framework separates the simulation layer from the monitoring and
visualization layer so that the underlying CloudSim simulation can be
analysed without changing its core behaviour.

🏗️ System Architecture

The project consists of several major layers.

1. Input Configuration Layer

The simulation accepts configurable parameters such as:

Number of Hosts
Number of VMs
Host CPU capacity
VM CPU requirement
Cloudlet count
Cloudlet length
Simulation configuration

These values determine the scale and workload of the virtual cloud
environment.

2. CloudSim Plus Simulation Layer

CloudSim Plus is responsible for modelling the virtual cloud environment.

The simulation contains:

Data Center

Represents the complete cloud infrastructure.

Physical Hosts

Represent physical servers in the data center.

Virtual Machines

Represent virtualized computing resources running on the hosts.

Cloudlets

Represent computational tasks/workloads executed through the VMs.

The relationship can be represented as:

Data Center
     │
     ├── Host 1
     │      ├── VM 1
     │      └── VM 2
     │
     ├── Host 2
     │      ├── VM 3
     │      └── VM 4
     │
     └── Host N

Cloudlets are submitted to VMs and executed using the resources provided by
the hosts.

🖥️ VM Placement

VM placement determines which physical host is used to execute each VM.

For example:

Host 0
 ├── VM 1
 ├── VM 2
 ├── VM 3
 └── VM 4

Host 1
 ├── VM 5
 ├── VM 6
 ├── VM 7
 └── VM 8

The current demonstrated implementation provides a balanced baseline VM
allocation using identical host and VM configurations.

For example:

10 Hosts
50 VMs

50 / 10 = 5 VMs per Host

Therefore, the baseline configuration can result in approximately:

Host 0 → 5 VMs
Host 1 → 5 VMs
Host 2 → 5 VMs
...
Host 9 → 5 VMs

This produces similar resource utilization between hosts when the hosts and
VMs have identical specifications.

The balanced configuration provides a controlled baseline for evaluating
future energy-aware and AI-aware placement strategies.

🌱 Green Cloud and Energy Optimization

The Green Cloud aspect of the project focuses on measuring and analysing
energy consumption in a simulated cloud environment.

The framework estimates host power consumption using the configured energy
model.

The general relationship is:

Host Utilization
       │
       ▼
Power Estimation
       │
       ▼
Energy Calculation
       │
       ▼
Cumulative Energy

The dashboard displays:

Current power/energy information
Cumulative energy consumption
Energy versus simulation time
Host utilization
VM utilization

The energy values are simulation estimates, not measurements from
physical servers.

🤖 AI-Aware Optimization

The project title includes AI-Aware Virtual Machine Placement.

The current implementation establishes the simulation and monitoring
foundation required for intelligent VM placement.

The monitored parameters that can support an intelligent placement layer
include:

Host CPU utilization
Host RAM utilization
VM CPU requirements
VM count
Workload characteristics
Host energy/power
Migration information
SLA violations

An AI-aware placement system can use these parameters to evaluate candidate
hosts and select a suitable host for a VM.

A conceptual optimization workflow is:

VM Request
     │
     ▼
Collect Host Metrics
     │
     ├── CPU
     ├── RAM
     ├── Energy
     ├── VM Count
     └── Workload
     │
     ▼
Host Evaluation / Scoring
     │
     ▼
Select Suitable Host
     │
     ▼
Place VM
     │
     ▼
Monitor Performance
     │
     ▼
Migration / Consolidation
     │
     ▼
Energy Analysis

The current implementation should therefore be understood as the
simulation, monitoring and baseline energy-analysis foundation for the
AI-aware optimization layer.

🔄 VM Migration

VM migration refers to moving a VM from one physical host to another.

Conceptually:

Host A
   │
   │ VM Migration
   ▼
Host B

Migration information is monitored by the framework and presented through
the migration timeline and reporting components.

Migration can be used in future optimization strategies to:

Reduce host overload
Improve resource distribution
Consolidate workloads
Reduce unnecessary active hosts
Support energy optimization
📊 SLA Monitoring

SLA stands for:

Service Level Agreement

The framework records SLA-related performance information during simulation.

SLA monitoring is important because energy optimization should not be
considered independently of service quality.

The system therefore tracks:

Energy
  +
CPU
  +
RAM
  +
Workload
  +
SLA
  +
Migration

This allows energy and performance to be analysed together.

📈 Live Monitoring Dashboard

The project includes a live monitoring dashboard that displays important
simulation information.

The dashboard provides KPI cards and graphical representations of the
simulation.

Main Dashboard Metrics

The dashboard displays:

Current Energy
Average Host CPU
Average VM CPU
Active Hosts
Running VMs
Running Cloudlets
Completed Cloudlets
SLA Violations
Migrations
Simulation Time
📉 Dashboard Graphs
1. Energy vs Time

Shows the cumulative energy consumption as the simulation progresses.

Energy
  │
  │             /
  │          /
  │       /
  │    /
  │ /
  └──────────────── Time

This graph helps analyse the energy behaviour of the simulated cloud.

2. Host Utilization

Shows the CPU utilization of physical hosts during simulation.

It helps determine how much of the available physical computing capacity
is being used.

3. VM Utilization

Shows the CPU utilization of virtual machines.

This provides visibility into the workload running through the virtualized
environment.

4. Migration Timeline

Shows VM migration activity over simulation time.

It helps identify when migration events occur and how many migration events
take place.

🖼️ Project Screenshots
Dashboard – Main View

The dashboard provides a centralized view of the cloud simulation,
including energy, CPU utilization, VM count, cloudlet status, SLA
violations and migration information.

Dashboard – Detailed View

The detailed dashboard provides graphical representations of:

Energy consumption
Host utilization
VM utilization
Migration activity

It also provides host-level information such as CPU, RAM, power, energy,
VM count and status.

Input Configuration

The input configuration screen allows the simulation parameters to be
configured before execution.

Typical parameters include:

Number of hosts
Number of VMs
Cloudlet/workload configuration
Host CPU capacity
VM CPU requirement

These parameters allow multiple experimental scenarios to be tested.

Generated Report

The reporting component provides summarized simulation results for later
analysis and documentation.

🧩 Monitoring Components

The monitoring subsystem is responsible for collecting simulation data.

Major monitoring components include:

MonitoringManager

Coordinates the monitoring process.

LiveMonitor

Displays simulation information during execution.

ResourceMonitor

Collects resource utilization information.

EventLogger

Records important simulation events.

HostMetrics

Stores physical host metrics.

VmMetrics

Stores virtual machine metrics.

CloudletMetrics

Stores cloudlet execution information.

DatacenterMetrics

Stores data-center level information.

PerformanceCalculator

Calculates performance-related metrics.

ResourceSnapshot

Stores resource information at a particular simulation point.

MetricsExporter

Exports collected metrics to CSV files.

📁 Project Structure
greencloud-ai/
│
├── src/
│   └── main/
│       └── java/
│           └── ...
│
├── pom.xml
├── README.md
├── LICENSE
└── .gitignore

The src directory contains the Java implementation.

pom.xml contains the Maven project configuration and dependencies.

README.md provides project documentation.

LICENSE contains the project license.

.gitignore prevents unnecessary files such as IDE metadata and Maven
build outputs from being committed.

🛠️ Technology Stack
Java 17

Java is the primary programming language.

It is used to implement:

Simulation logic
Cloud infrastructure
Hosts
VMs
Cloudlets
Monitoring
Energy calculations
Dashboard integration
Reporting
CloudSim Plus

CloudSim Plus is the primary cloud simulation framework.

It is used to simulate:

Data centers
Hosts
Virtual machines
Cloudlets
VM allocation
Workload execution
Resource utilization

The advantage of simulation is that cloud infrastructure strategies can be
tested without deploying a physical cloud data center.

Maven

Maven manages:

Project dependencies
Build lifecycle
Compilation
Packaging
Dependency resolution

The project configuration is stored in:

pom.xml
SLF4J

SLF4J provides the logging abstraction used by the application and
supporting libraries.

Logback

Logback provides the logging implementation.

It helps produce structured simulation and application logs.

CSV

CSV files are used for exporting simulation metrics so that the results can
be inspected or analysed later.

Example exported information includes:

Host metrics
VM metrics
Cloudlet metrics
Event logs
Simulation summary
Energy information
Migration information
📦 Requirements
Hardware Requirements

Recommended:

Modern Intel/AMD processor
4 GB RAM minimum
8 GB RAM recommended
1 GB available storage
Windows, Linux or macOS

The simulation does not require a physical cloud data center.

Software Requirements
Java

Java Development Kit:

JDK 17 or later

Check installation:

java -version

Expected output should indicate Java 17 or a compatible newer version.

Maven

Maven 3.8+ is recommended.

Check installation:

mvn -version

The Maven installation should detect the configured Java JDK.

IDE

Any Java IDE can be used.

Recommended:

IntelliJ IDEA
Eclipse
Visual Studio Code

IntelliJ IDEA was used during development.

📋 requirements.txt

Important: This is a Java/Maven project, so pom.xml is the actual
dependency-management file. requirements.txt is provided only as a
human-readable environment requirement reference.

Java Development Kit (JDK) 17+
Apache Maven 3.8+
CloudSim Plus 8.5.x
SLF4J 2.0.17
Logback Core 1.5.18
Logback Classic 1.5.18

The exact Maven dependency versions used by the project should always be
taken from pom.xml.

🚀 Installation
Step 1 – Clone the Repository
git clone <YOUR-GITHUB-REPOSITORY-URL>

Example:

git clone https://github.com/Deepak-3357/Green-Cloud-Framework.git

Then enter the project:

cd Green-Cloud-Framework
Step 2 – Verify Java

Run:

java -version

Make sure JDK 17 or later is installed.

Step 3 – Verify Maven

Run:

mvn -version

Maven should detect your Java installation.

Step 4 – Build the Project

Run:

mvn clean compile

Maven will:

Clean previous build output.
Download required dependencies.
Compile the Java source code.
Report compilation errors if any.
▶️ How to Run

After successful compilation, run the project's main simulation launcher
from your IDE.

In IntelliJ IDEA:

Open Project
     ↓
Open pom.xml
     ↓
Load Maven Project
     ↓
Open src/main/java
     ↓
Locate the main simulation/launcher class
     ↓
Run the main() method

If the project provides a Maven exec configuration, it can also be run
through Maven according to the configuration in pom.xml.

⚙️ Simulation Configuration

The simulation is configurable.

Important inputs include:

Parameter	Meaning
Hosts	Number of physical servers
VMs	Number of virtual machines
Cloudlet Length	Workload size
Host CPU	Processing capacity of a physical host
VM CPU	CPU requirement of a VM
Simulation Duration	Simulation execution period
🧪 Example Test Configuration

Example:

Hosts              = 10
VMs                = 50
Cloudlet Length    = 50000
Host CPU           = 10000 MIPS
VM CPU             = 2500 MIPS

This represents a cloud environment with:

10 physical hosts
50 virtual machines

with each VM requesting the configured CPU capacity.

📊 Example Simulation Workflow
1. Start application
        ↓
2. Enter simulation parameters
        ↓
3. Create CloudSim simulation
        ↓
4. Create data center
        ↓
5. Create physical hosts
        ↓
6. Create VMs
        ↓
7. Create cloudlets
        ↓
8. Allocate VMs to hosts
        ↓
9. Execute cloudlets
        ↓
10. Start monitoring
        ↓
11. Calculate resource utilization
        ↓
12. Calculate power and energy
        ↓
13. Track migrations
        ↓
14. Track SLA violations
        ↓
15. Update dashboard
        ↓
16. Export metrics
        ↓
17. Generate summary
📁 Generated Reports and Metrics

The monitoring subsystem can generate CSV-based outputs including:

event_log.csv
host_metrics.csv
vm_metrics.csv
cloudlet_metrics.csv
simulation_summary.csv

Depending on the enabled modules/configuration, additional energy and
migration-related metrics can also be exported.

📊 Host Metrics

Host-level information includes:

Host ID
CPU utilization
RAM utilization
Power consumption
Energy consumption
VM count
Host status

Example:

Host ID | CPU % | RAM % | Power | Energy | VM Count | Status
🖥️ VM Metrics

VM-level information includes information about:

VM identity
Resource usage
CPU utilization
Host association
Execution state

This allows the relationship between virtual workloads and physical
resources to be analysed.

☁️ Cloudlet Metrics

Cloudlets represent computational workloads.

The system tracks information such as:

Cloudlet status
Execution
Completion
Workload progress
VM association
⚡ Energy Metrics

Energy is monitored throughout the simulation.

The framework can analyse:

Host Utilization
       ↓
Power
       ↓
Energy
       ↓
Cumulative Energy

The Energy vs Time graph provides a visual representation of this behaviour.

🔄 Migration Metrics

VM migrations are tracked to understand workload movement between hosts.

Migration data can be used to study:

Resource balancing
Host overload
Host consolidation
Energy optimization strategies
🚨 SLA Metrics

SLA violations are monitored during simulation.

The purpose is to ensure that optimization strategies are evaluated not
only for energy consumption but also for service quality.

🧠 AI Optimization Roadmap

The framework is designed to support an AI-aware optimization layer.

A future intelligent placement engine can consider:

CPU Utilization
       +
RAM Utilization
       +
Energy
       +
VM Count
       +
Workload
       +
SLA
       ↓
Host Suitability Score
       ↓
VM Placement Decision

Potential future optimization capabilities include:

Intelligent VM placement
Workload-aware host scoring
Dynamic VM migration
Host consolidation
Energy-aware resource allocation
SLA-aware optimization
Energy comparison between baseline and optimized placement
🔬 Experimental Analysis

The system supports changing simulation parameters to study different
cloud scenarios.

For example:

Scenario 1 – Low Workload
Hosts = 50
VMs = 10
Cloudlet Length = 10000
Scenario 2 – Balanced Workload
Hosts = 20
VMs = 40
Cloudlet Length = 30000
Scenario 3 – High VM Density
Hosts = 5
VMs = 40
Cloudlet Length = 50000
Scenario 4 – Higher CPU Requirement
Hosts = 10
VMs = 20
Cloudlet Length = 50000
VM CPU = 5000
Scenario 5 – Heavy Workload
Hosts = 10
VMs = 50
Cloudlet Length = 100000
VM CPU = 5000

These scenarios allow changes in workload intensity and infrastructure
capacity to be studied.

📌 Important Interpretation

The values displayed by the dashboard are simulation results.

For example:

Power = W
Energy = Wh
CPU = %
RAM = %

These values represent the behaviour of the simulated cloud infrastructure
under the selected configuration.

They should not be interpreted as direct measurements from physical cloud
servers.

🔍 Why Use Cloud Simulation?

Testing energy optimization directly in a real cloud data center can be:

Expensive
Difficult to reproduce
Resource intensive
Risky for production infrastructure

Cloud simulation allows different configurations to be tested safely and
repeatedly.

The same experimental methodology can be used to compare different VM
placement and resource-management strategies.

📚 Key Concepts Demonstrated

This project demonstrates practical concepts in:

Cloud Computing
Virtualization
VM Placement
Resource Allocation
Cloud Simulation
CPU Utilization
RAM Utilization
Energy Modelling
Power Consumption
VM Migration
SLA Monitoring
Workload Scheduling
Performance Monitoring
Data Export
Dashboard Visualization
Energy-Aware Computing
AI-Aware Cloud Optimization
👨‍💻 Contributions
Primary areas:

Simulation architecture
CloudSim Plus setup
Java/Maven integration
Monitoring dashboard
CSV reporting
System integration
Testing
Debugging
Output validation
Graph analysis
Architecture and implementation documentation
Project analysis
Experiment configuration
Configuration design
Experiment preparation
Validation support
Test-case comparison
Result checking
Result interpretation
Literature review
Analysis
Conclusion
Supporting documentation

The project was completed collaboratively, with both members participating
in integration decisions, verification and final demonstration.

📜 License

This project is provided for academic and educational purposes.

See the LICENSE file for the applicable license terms.

👥 Authors
Deepak R

Computer Science and Engineering
SIMATS Engineering

🌱 Project Summary

The Green Cloud Framework for AI-Aware Virtual Machine Placement and Energy
Optimization provides a simulation-based environment for studying virtualized
cloud infrastructure.

The framework combines:

Cloud Simulation
      +
VM Placement
      +
Resource Monitoring
      +
Energy Estimation
      +
Migration Tracking
      +
SLA Monitoring
      +
Live Dashboard
      +
CSV Reporting
      +
AI-Aware Optimization Foundation

The current implementation establishes a configurable and measurable baseline
for cloud resource management. The collected resource, energy, workload,
migration and SLA information provides the foundation for evaluating
intelligent VM placement and energy optimization strategies.

⭐ Project Highlights
☁️ CloudSim Plus based cloud simulation
🖥️ Physical host and VM modelling
⚙️ Configurable simulation parameters
📊 Live monitoring dashboard
📈 Energy and utilization graphs
⚡ Energy consumption estimation
🔄 VM migration monitoring
🚨 SLA violation monitoring
📁 CSV data export
🧪 Multiple workload configurations
🤖 AI-aware optimization foundation
🌱 Green cloud and energy-efficiency focus
