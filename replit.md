# Smart Campus Management System

## Overview
A Java console application that simulates a university's campus management system (students, courses, library, hostel, help desk, events). Built for the SCOA021 group assignment using **custom data structures from scratch** (no Java Collections for the core structures) and JSON-based persistence.

## Tech Stack
- **Language:** Java (OpenJDK 19, GraalVM CE 22.3)
- **Runtime/UI:** Menu-driven console application
- **Persistence:** Hand-written JSON file handling (`data/*.json`)
- **No external dependencies** — uses only the Java standard library

## Project Structure
```
src/
├── Main.java                # Entry point
├── ui/                      # ConsoleUI, BoxUI (menu rendering)
├── service/                 # Login, Student, Course, Library, Hostel, HelpDesk, Event services
├── model/                   # User, Admin, Student, Course, Ticket
├── persistence/             # DataPersistence, JsonFileHandler, JsonSchemaValidator
└── datastructures/          # CustomArrayList, CustomLinkedList, CustomHashMap, CustomQueue, CustomStack
data/                        # JSON data files
schemas/                     # JSON schemas for validation
out/                         # Compiled .class files (generated)
run.sh                       # Compile + run helper
```

## Running
The `Smart Campus System` workflow runs `bash run.sh`, which compiles every `.java` file under `src/` into `out/` and launches `java -cp out Main`. Output type is `console` since this is an interactive TUI, not a web app.

## Recent Changes
- **2026-04-28:** Initial Replit setup — added `run.sh` build/run script and configured the `Smart Campus System` console workflow.
