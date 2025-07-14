# S.T.A.N.D. Project
**Signal-Triggered Arduino Navigation & Dispatch**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A modular Java pipeline that transforms MEG-style signals into real-time motor commands, delivered over Bluetooth to Arduino-powered robotics.

---

## Table of Contents

- [Overview](#overview)
- [Modules](#modules)
    - [Main.java](#mainjava)
    - [SignalEngine.java](#signalenginejava)
    - [BluetoothMotorTransmitter.java](#bluetoothmotortransmitterjava)
- [Dependencies](#dependencies)
- [Design Highlights](#design-highlights)
- [Authors](#authors)
- [License](#license)

---

## Overview

S.T.A.N.D. (Signal-Triggered Arduino Navigation & Dispatch) listens for neural signals—formatted like MEG/EEG channel-value pairs—and maps them to motor commands. The result is streamed via HC-05/HC-06 Bluetooth modules to an Arduino controller, enabling gesture-based or signal-based robotic navigation.

---

## Modules

### Main.java

- Selects or auto-detects a COM port
- Configures serial settings (9600 baud, 8-N-1)
- Performs a `PING`→`PONG` handshake
- Launches the signal engine thread
- Handles graceful shutdown via console “exit” command

### SignalEngine.java

- Reads raw MEG input lines
- Parses channel-value pairs (e.g. `C3:480`)
- Applies mapping logic to convert signals into motor directives
- Emits formatted command strings for downstream transmission

### BluetoothMotorTransmitter.java

- Accepts newline-separated motor commands
- Writes UTF-8 bytes terminated by LF to the Bluetooth stream
- Flushes immediately and logs any transmission errors
- Can be swapped out or mocked with ease

---

## Dependencies

- **Java 21**
- **jSerialComm** (v2.x) – serial port I/O
- **JNativeHook** (v2.x) – *(optional; replaced by console shutdown)*
- **Gradle Wrapper** (`./gradlew`) for builds

---

## Design Highlights

- **Separation of Concerns**  
  Signal parsing, command mapping, and transport are each in their own module.
- **Injectable Transmitter**  
  Use `BluetoothMotorTransmitter` or swap in a mock implementation.
- **Robust Handshake & Error Handling**  
  Ensures device presence and logs failures cleanly.
- **Console-Driven Shutdown**  
  Simply type `exit` + Enter to cleanly terminate all threads and close ports.

---

## Authors

- **Saptansu Ghosh**  
  Developer │ Logical Analyst │ Concept Visualizer
    - Architected the signal-processing pipeline, serial handshake,  
      and Bluetooth sender logic.
    - Defined enums for electrode channels and motor mappings.

- **Preetha Majhi**  
  Neurological System Analyst │ Concept Visualizer
    - Designed the MEG/EEG signal-value interpretation model.
    - Specified signal ranges and mapping strategies for robust control.

- **Shreya Mukherjee**, **Kajol Dey**, **Reyansh Ghosh Dastidar**, **Yash Desai**, **Bishal Dasgupta**  
  Assistant Programmers │ Contributors │ Inspirations
    - Supported implementation, testing, and documentation.

---

## License

This project is released under the [MIT License](LICENSE).  

## **NO PUBLIC USE ALLOWED TILL THE PROJECT IS OFFICIALLY RELEASED.**