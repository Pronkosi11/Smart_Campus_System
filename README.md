# 🎓 Smart Campus Management System

> **SCOA021 Group Assignment** — A Java console application simulating how a university stores and processes campus data using custom data structures.

---

## 📚 What Is This Assignment About?

Modern universities manage enormous volumes of information daily — student records, course registrations, library resources, hostel allocation, help desk tickets, and event bookings. The performance of these systems depends heavily on choosing the **correct data structure** for the right problem.

This project challenges you to **design and implement a Smart Campus Management System in Java** that simulates how a university stores and processes campus data. The system runs as a **menu-driven console application** with role-based access for **Admins** and **Students**.

### 🎯 Learning Objectives

By contributing to this project, you will develop skills in:

- ✅ Selecting appropriate data structures for specific real-world problems
- ✅ Implementing **custom data structures from scratch** (no Java Collections!)
- ✅ Designing modular, object-oriented systems using clean package structure
- ✅ Handling real-world data management challenges

---

## 🏗️ System Modules

| Module | Status | Data Structures Used |
|---|---|---|
| Authentication (Login) | ✅ Implemented | CustomHashMap |
| Student Management | ✅ Implemented | CustomHashMap, CustomArrayList |
| Course Registration | ✅ Implemented | CustomHashMap, CustomArrayList |
| Library Management | 🔲 Planned | CustomHashMap, CustomQueue |
| Hostel Allocation | 🔲 Planned | CustomHashMap |
| Help Desk Tickets | 🔲 Planned | CustomQueue, CustomStack |
| Event Booking | 🔲 Planned | CustomArrayList |

### Custom Data Structures (All must be built from scratch)

| Structure | File | Purpose |
|---|---|---|
| CustomArrayList | `src/datastructures/CustomArrayList.java` | Dynamic lists |
| CustomLinkedList | `src/datastructures/CustomLinkedList.java` | Dynamic collections |
| CustomHashMap | `src/datastructures/CustomHashMap.java` | Fast ID-based lookups |
| CustomQueue | `src/datastructures/CustomQueue.java` | FIFO ticket/waiting list processing |
| CustomStack | `src/datastructures/CustomStack.java` | LIFO status history |

---

## 🗂️ Project Structure

```
Smart_Campus_System/
├── data/                        # JSON data files
│   ├── students.json
│   ├── courses.json
│   ├── books.json               # (planned)
│   ├── hostels.json             # (planned)
│   ├── tickets.json             # (planned)
│   └── events.json              # (planned)
├── src/
│   ├── Main.java                # Entry point
│   ├── ui/
│   │   └── ConsoleUI.java       # Menu system
│   ├── service/
│   │   ├── LoginService.java
│   │   ├── StudentService.java
│   │   ├── CourseService.java
│   │   ├── LibraryService.java  # (planned)
│   │   ├── HostelService.java   # (planned)
│   │   ├── HelpDeskService.java # (planned)
│   │   └── EventService.java    # (planned)
│   ├── model/
│   │   ├── User.java
│   │   ├── Admin.java
│   │   ├── Student.java
│   │   ├── Course.java
│   │   ├── LibraryBook.java     # (planned)
│   │   ├── HostelRoom.java      # (planned)
│   │   ├── Ticket.java          # (planned)
│   │   └── Event.java           # (planned)
│   ├── persistence/
│   │   ├── DataPersistence.java
│   │   └── JsonFileHandler.java
│   └── datastructures/
│       ├── CustomArrayList.java
│       ├── CustomLinkedList.java
│       ├── CustomHashMap.java
│       ├── CustomQueue.java
│       └── CustomStack.java
```

---

## 🚀 How to Contribute — Step-by-Step Guide

Follow every step carefully. This workflow ensures everyone's changes are isolated on their own branch and don't break the main codebase.

---

### Step 1 — Prerequisites

Make sure the following tools are installed before you begin:

- **Java JDK 17+** — [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Git** — [Download here](https://git-scm.com/downloads)
- **IntelliJ IDEA** (Community Edition is free) — [Download here](https://www.jetbrains.com/idea/download/)

---

### Step 2 — Clone the Repository

Cloning downloads a full copy of the project to your computer.

**2a.** Navigate to your **Documents** folder in File Explorer.

**2b.** Right-click on an empty area inside the Documents folder and select **"Open in Terminal"**.

> 💡 On Windows 11 you may need to click "Show more options" first to see "Open in Terminal".

**2c.** In the terminal that opens, run the following command:

```bash
git clone https://github.com/Pronkosi11/Smart_Campus_System.git
```

**2d.** Once cloning is complete, navigate into the project folder:

```bash
cd Smart_Campus_System
```

You should now be inside the project directory:
```
PS C:\Users\YourName\Documents\Smart_Campus_System>
```

---

### Step 3 — Open the Project in IntelliJ IDEA

**3a.** Open **File Explorer** and navigate to:
```
Documents > Smart_Campus_System
```

**3b.** Right-click on an **empty area** inside the `Smart_Campus_System` folder (not on a file).

**3c.** Select **"Open Folder as IntelliJ IDEA Project"** from the context menu.

**3d.** IntelliJ will open. Wait for it to finish indexing the project. You will see the project files in the left panel under the **Project** tab.

> ⚠️ If IntelliJ asks about trusting the project, click **"Trust Project"**.

---

### Step 4 — Open the Built-in Terminal in IntelliJ

All Git commands from here should be run inside IntelliJ's terminal so you stay in the correct project directory.

**4a.** At the very bottom-left of IntelliJ, click the **Terminal icon** (looks like `>_`).

**4b.** A terminal panel will open at the bottom of the screen. You will see:
```
PS C:\Users\YourName\Documents\Smart_Campus_System>
```

**4c.** To use Git Bash (recommended), click the **dropdown arrow** `∨` next to the `+` button at the top of the terminal panel and select **"Git Bash"**.

A new Git Bash tab will open. You are now ready to run Git commands.

---

### Step 5 — Create Your Own Branch

> ⚠️ **Never work directly on the `main` branch.** Always create your own branch first.

Replace `Group-name` below with your actual group name (e.g., `Group3-Library`):

```bash
git checkout -b Group-name
```

Example:
```bash
git checkout -b Group3-Library
```

You should see:
```
Switched to a new branch 'Group3-Library'
```

Your terminal prompt will now show your branch name in brackets:
```
202356843@L1412 MINGW64 ~/Documents/Smart_Campus_System (Group3-Library)
```

---

### Step 6 — Make Your Changes

Now implement your assigned module(s) in IntelliJ. Refer to the **Technical Design Document** (`SCOA021.md`) in the project root for:

- Model class definitions (fields, methods, constructors)
- Service class method signatures
- Data structure assignments per module
- ConsoleUI integration rules

**General rules:**
- `ConsoleUI.java` should only call service methods — do NOT put business logic there
- Each service class owns its own module flow
- Follow the existing `StudentService` and `CourseService` as reference patterns
- Add your JSON data files under `data/` and schema files under `schemas/`

---

### Step 7 — Stage and Commit Your Changes

Once you have made changes, save all your files in IntelliJ (`Ctrl+S`), then run the following in Git Bash:

**7a.** Stage all changed files:
```bash
git add .
```

**7b.** Commit your changes with a clear message describing what you did:
```bash
git commit -m "Add LibraryService with borrow/return and waiting list logic"
```

> 💡 Write meaningful commit messages. Bad: `"changes"`. Good: `"Implement CustomQueue enqueue and dequeue with FIFO order"`.

You will see output like:
```
[Group3-Library a1b2c3d] Add LibraryService with borrow/return and waiting list logic
 5 files changed, 210 insertions(+)
```

---

### Step 8 — Push Your Branch to GitHub

Upload your branch so it appears on the remote repository:

```bash
git push -u origin Group-name
```

Example:
```bash
git push -u origin Group3-Library
```

**8a.** A **"Connect to GitHub — Sign in"** popup may appear in IntelliJ. Click **"Sign in with your browser"** and complete the GitHub login in the browser window that opens.

**8b.** After authentication, the push will complete and your branch will appear on GitHub under **Branches**.

---

### Step 9 — Verify on GitHub

**9a.** Go to [https://github.com/Pronkosi11/Smart_Campus_System](https://github.com/Pronkosi11/Smart_Campus_System)

**9b.** Click on **"Branches"** (shown near the top of the repository page).

**9c.** Under **"Your branches"** or **"Active branches"**, you should see your branch listed with `0 behind | 1 ahead` status — this confirms your push was successful.

---

### Step 10 — Keeping Your Branch Up to Date

If other groups push changes to `main` while you are working, you need to pull those changes into your branch to avoid conflicts:

```bash
# Switch to main and pull the latest changes
git checkout main
git pull origin main

# Switch back to your branch and merge in the updates
git checkout Group-name
git merge main
```

Resolve any merge conflicts in IntelliJ if they appear (IntelliJ has a built-in merge conflict tool).

---

## ▶️ How to Run the Application

From the project root in PowerShell or Git Bash:

```powershell
# Compile all Java files
javac -d out (Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName })

# Run the application
java -cp out Main
```

Or in Git Bash:
```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out Main
```

**Default admin login:**
- Username: `admin`
- Password: `admin123`

---

## 📋 Contribution Checklist

Before pushing your branch, confirm the following:

- [ ] Model class created with all fields, constructor, getters/setters, and `toString()`
- [ ] Service class implemented with all required methods
- [ ] `DataPersistence` updated to load/save your module's data
- [ ] JSON data file added under `data/`
- [ ] JSON schema file added under `schemas/`
- [ ] Admin menu flow added in `ConsoleUI`
- [ ] Student menu flow added in `ConsoleUI`
- [ ] Input validation and duplicate checks in place
- [ ] Code compiles without errors
- [ ] Branch pushed to GitHub

---

## 👥 Team Responsibilities

| Group | Module | Key Data Structures |
|---|---|---|
| Group 1 | Main, LoginService, ConsoleUI, Persistence | All (integration) |
| Group 2 | StudentService, CourseService | CustomArrayList, CustomHashMap |
| Group 3 | Library, Hostel, Help Desk | CustomQueue, CustomStack, CustomHashMap |
| Group 4 | Event Booking, Custom Data Structures | CustomLinkedList, CustomArrayList |

---

## ⚠️ Important Rules

1. **Never push directly to `main`** — always work on your own branch
2. **Do not use Java's built-in Collections** (`ArrayList`, `HashMap`, etc.) — use the custom implementations in `src/datastructures/`
3. **Do not modify** `StudentService.java` or `CourseService.java` unless coordinating with Group 2
4. **Communicate via GitHub Issues** if you find a bug that affects another group's work
5. Commit often with descriptive messages — do not make one giant commit at the end

---

## 🆘 Common Issues & Fixes

| Problem | Fix |
|---|---|
| `git` is not recognized | Install Git from https://git-scm.com and restart terminal |
| `javac` is not recognized | Install JDK and add it to your system PATH |
| Push rejected | Run `git pull origin main` first, resolve conflicts, then push again |
| IntelliJ shows red errors everywhere | Go to File → Project Structure → SDK and set it to your installed JDK |
| GitHub sign-in popup doesn't appear | Run `git push` in Git Bash manually and enter your GitHub credentials |

---

*For full technical details, model definitions, API specs, and schema validation rules, refer to `SCOA021.md` in the project root.*
