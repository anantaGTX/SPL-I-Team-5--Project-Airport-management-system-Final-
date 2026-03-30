Airport Management System (AMS)
Project Overview

The Airport Management System (AMS) is a terminal-based Command-Line Interface (CLI) application designed to simulate the operations of a small airport. It integrates core functionalities including flight management, resource allocation (gates and runways), passenger booking and check-in, and dynamic flight rescheduling under both normal and adverse conditions. The system is developed using Java and follows an object-oriented design to ensure modularity, scalability, and maintainability.

Features
Flight Management: Tracks flight schedules, statuses (on-time, delayed, cancelled), passenger counts, and operational conditions.
Gate and Runway Allocation: Manages critical airport resources to avoid conflicts and maximize operational efficiency.
Passenger Booking System: Allows passengers to sign up, book flights, view seat availability, and receive booking confirmations. Supports conditional refund policies.
Weather Management: Simulates real-time weather conditions that influence flight scheduling and operational decisions.
Chaos Mode: Introduces random disruptions to test the system’s ability to manage delays, cancellations, and rescheduling.
Administrative Access: Admin users can manage flights, monitor operations, and make real-time decisions based on flight status and resources.
Simulation Engine: Mimics real-world airport operations, handling flight boarding, arrival processing, and dynamic rescheduling during disruptions.
Technologies Used
Java: The main programming language used for implementing object-oriented logic, resource management, and user interaction.
CLI Interface: A text-based interface for user interaction, ensuring that the system is accessible without a graphical interface.
File-Based Storage: The system uses text files for storing data such as flight schedules, passenger details, and login credentials, ensuring persistence across sessions.
GitHub: Version control and collaboration platform used for managing the codebase and allowing for distributed team development.
Installation and Setup
Prerequisites

To run the Airport Management System, ensure that you have the following installed on your system:

Java Development Kit (JDK) – Version 8 or higher.
IDE (Optional) – Recommended IDE: IntelliJ IDEA or any text editor of your choice.
Steps to Run

Clone the repository to your local machine:

git clone https://github.com/yourusername/airport-management-system.git

Navigate to the project directory:

cd airport-management-system

Compile and run the project:

javac Main.java
java Main
Usage
Guest Mode: Explore available flights without logging in.
Passenger Mode: Sign up, log in, select flights, and manage bookings.
Admin Mode: Use the admin login to manage flight schedules, gates, runways, and other operational tasks.
Weather and Chaos Mode: Trigger random weather events and operational chaos to test the system’s dynamic rescheduling capabilities.
Contributors
Sheikh Alimun Alahi: Gate and runway management system, seat layout, simulation engine, weather type, and weather slot features.
Rakibur Rahman Ananta: Passenger management, app user authentication, and password security.
Md. Irfan Sadik Chy: Flight management, departure/arrival handling, booking management, weather manager.
Future Work
Real-Time API Integration: Integrate with real-time airline APIs for live flight data.
Mobile Application: Develop a mobile app for passenger booking and flight status tracking.
AI-Based Delay Prediction: Implement AI for better delay forecasting and automated operational adjustments.
Database Integration: Migrate to a database-backed architecture for enhanced data management and scalability.