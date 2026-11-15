# Student Management System

A desktop application built with **Java Swing** for managing student records through a clean dashboard and an interactive records table. It supports creating, viewing, updating, deleting, and searching student details, with records saved locally in a tab-separated text file.

> **Project type:** Java desktop application  
> **UI:** Java Swing  
> **Storage:** Local `Table.txt` file (UTF-8, tab-separated values)  
> **External libraries:** None required

---

## Overview

The application brings common student-record tasks into one interface. A dashboard presents an at-a-glance summary of the stored records, while a form and table make it possible to manage individual student entries.

## Features

### Dashboard

- Displays the **total number of students**.
- Shows a **gender distribution** based on the saved records.
- Shows the number of students in each **course**.
- Refreshes the statistics when records are added, edited, or deleted.

### Student Record Management

- **Add students** with Student ID, full name, age, course, and gender.
- **Update records** by selecting a student in the table and editing the populated form.
- **Delete records** with a confirmation prompt before the deletion is saved.
- **Clear the form** to begin entering a new record.
- Prevents duplicate Student IDs (ID comparisons are case-insensitive).

### Search and Table

- Filters records as you type.
- Searches Student ID, name, course, and gender without case sensitivity.
- Includes a **Show all** control to clear the search filter.
- Supports sorting from the table column headers.
- Selecting a table row loads that student's details into the form for editing.

### Validation and Data Safety

- Requires Student ID, name, age, and course.
- Accepts IDs containing letters, numbers, hyphens, and underscores, up to 40 characters.
- Checks that age is a whole number from **1 to 120**.
- Rejects tabs and line breaks in names and course names so the tab-separated data remains readable.
- Checks for duplicate IDs before adding or updating a record.
- Writes the updated dataset to a temporary file before replacing `Table.txt`, reducing the chance of leaving a partially written data file.
- Loads valid records at startup and skips malformed or duplicate rows.

---

## Technology Stack

| Technology | Role |
|---|---|
| Java | Application logic and data handling |
| Java Swing | Desktop GUI components, forms, buttons, and tables |
| Java AWT | Layouts, colours, fonts, and UI behaviour |
| Java NIO (`java.nio.file`) | Reading and writing the local data file |
| Java Collections and Streams | Managing, filtering, and summarising student records |

The project uses only Java standard-library APIs; no third-party dependencies or database server are required.

## Requirements

- **JDK 17 or later** recommended. The source uses Java records, which require Java 16 or later.
- Windows, macOS, or Linux with a graphical desktop environment.

Check your Java installation:

```bash
java -version
javac -version
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/codeforge83/StudentManagementSystem.git
cd StudentManagementSystem
```

Alternatively, download the repository as a ZIP and extract it.

### 2. Check the Project Files

Keep these files in the same project directory:

```text
StudentManagementSystem/
├── StudentManagementSystem.java
├── Table.txt
└── README.md
```

`Table.txt` is the application's local data file. The program looks for it in the **current working directory** from which the application is launched.

### 3. Compile the Application

```bash
javac StudentManagementSystem.java
```

### 4. Run the Application

```bash
java StudentManagementSystem
```

A desktop window should open. If `Table.txt` is not present, the application starts with no records and creates the data file when the first student is successfully saved.

---

## How to Use

1. Launch the application using the commands above.
2. Enter the student's ID, full name, age, course, and gender in the **Student details** form.
3. Select **Add student** to save a new record.
4. Select a row in **Student records** to load its details into the form.
5. Choose **Update selected** to save changes to the selected student.
6. Choose **Delete selected** and confirm to remove a record.
7. Type in the search field to filter records, or choose **Show all** to clear the filter.
8. Choose **Clear form** when you want to start a fresh entry.

The dashboard statistics and records table update after successful changes.

---

## Data Storage

Student records are stored locally in `Table.txt` using **UTF-8 tab-separated values (TSV)**. The file contains these five columns:

| Column | Description |
|---|---|
| Student ID | Unique student identifier |
| Name | Student's full name |
| Age | Student's age as a whole number |
| Course | Course or department label |
| Gender | Selected gender value (`Male`, `Female`, or `Other`) |

The application reads the file at startup and writes the complete updated set of records after a successful add, update, or delete. Keep a backup of `Table.txt` if you need to preserve the records.

> **Privacy note:** Do not commit real student personal information to a public repository. Use fictional/sample records in `Table.txt`, or remove sensitive records before publishing the project.

---

## Implementation Notes

- The interface is created with Java Swing and launched on the Swing Event Dispatch Thread.
- `JTable` displays the records, with `DefaultTableModel` providing the table model and `TableRowSorter` supporting filtering and sorting.
- Student details are represented by a Java `record`.
- The application uses a local text file rather than a relational database. It is intended as a lightweight, single-user desktop project; it does not provide multi-user synchronisation, remote access, or account-based authentication.

## Possible Future Improvements

- Add database integration (for example, MySQL or PostgreSQL) for larger datasets.
- Add export and import options such as CSV.
- Add role-based login and access control.
- Add dedicated student profile views and more detailed reports.
- Add automated tests for validation and record-management operations.

---

## Author

Developed as a Java desktop application project to practise object-oriented programming, GUI development, data validation, and file handling.

## Repository

[GitHub – Student Management System](https://github.com/codeforge83/StudentManagementSystem)
