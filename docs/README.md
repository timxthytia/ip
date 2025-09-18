# Tim

Tim is a task manager application designed for fast typists who prefer a **CLI-first workflow** with **JavaFX GUI feedback**.  
It helps users keep track of **todos, deadlines, and events**, and now supports **reminders** that pop up in the interface when deadlines or event start times are near.

---

## Features

### Core Task Management
- **Todo** – simple tasks without a time constraint  
  Example: `todo read book`

- **Deadline** – tasks with a deadline  
  Example: `deadline return book /by 15/12/2024 1800`

- **Event** – tasks with a start and end time  
  Example: `event project meeting /from 20/12/2024 1400 /to 20/12/2024 1600`

- **List all tasks** – `list`

- **Mark / Unmark tasks** – mark tasks as completed or uncompleted  
  Example: `mark 2`, `unmark 3`

- **Delete tasks** – remove a task by index  
  Example: `delete 4`

- **Find tasks** – search tasks by keywords  
  Example: `find book`

- **View tasks on a date** –  
  Example: `on 15/12/2024`

### Reminders
- Automatically scans your **TaskList** for upcoming **Deadlines** and **Events**.
- Displays a **popup reminder bar** at the top of the GUI.
- Reminders can be **dismissed** with one click.

---

## Getting Started

### Prerequisites
- Java **17** or later (with JavaFX support).
- Gradle (project includes a Gradle wrapper).

### Build and Run
Clone the repository and run:
```bash
./gradlew run