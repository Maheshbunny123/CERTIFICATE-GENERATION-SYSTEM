

# 🎓 Certificate Generation System

## 📘 Project Description

Educational institutions frequently need to generate and issue certificates such as **Course Completion**, **Participation**, or **Achievement Certificates**.
Manually preparing certificates for each student is **time-consuming**, **error-prone**, and **hard to track**.

The **Certificate Generation System** solves this problem by providing an **automated platform** that can create, issue, and verify certificates digitally using **Java**, **Servlets**, **JSP**, **XML**, and **QR Code integration**.

Each generated certificate contains a **unique Certificate ID** and a **QR Code** that links to its verification page, ensuring authenticity and preventing tampering.

---

## ⚙️ Features

### 🎨 1. Certificate Creation

* Admin inputs student details such as:

  * Student Name
  * Course Name
  * Grade or Percentage
  * Date of Issue
* The system generates a professional-looking PDF certificate instantly.

### 🔢 2. Unique ID & QR Code Generation

* Every certificate gets a **unique Certificate ID** (auto-generated).
* A **QR Code** is printed on each certificate for online verification.
* Scanning the QR Code redirects users to a verification page showing certificate details.

### 🧾 3. XML-Based Record Storage

* Certificate details (ID, Name, Course, Date, etc.) are stored securely in an XML file.
* XML ensures portability, readability, and easy data exchange between systems.

### 🧩 4. Multiple Certificate Templates

* Support for multiple templates like:

  * Course Completion Certificate
  * Event Participation Certificate
  * Certificate of Excellence
* Each template includes institution logo, signatures, and custom designs.

### ⚡ 5. Bulk Certificate Generation

* Generate multiple certificates at once for an entire class or batch.
* Auto-fills student data from XML or CSV input files.

### 🔍 6. Verification Portal

* Students or employers can verify certificates using QR code.
* Fetches data from XML and displays authenticity status.

### 🛡️ 7. Security & Integrity

* XML records are tamper-proof.
* QR codes ensure that fake certificates can’t be created manually.
* Optional digital signature integration for extra authenticity.

---

## 🏗️ Tech Stack

| Component | Technology                                         |
| --------- | -------------------------------------------------- |
| Frontend  | HTML, CSS, JSP                                     |
| Backend   | Java Servlets                                      |
| Database  | XML File Storage                                   |
| Libraries | iTextPDF (for PDF generation), ZXing (for QR code) |
| Server    | Apache Tomcat 10.1+                                |
| IDE       | Eclipse / VS Code / IntelliJ                       |
| Language  | Java SE 17+                                        |

---

## 📂 Folder Structure

```
CertificateGenerator/
│
├── src/
│   ├── CertificateGenerator.java       # Main logic for PDF + QR creation
│   ├── GenerateCertificateServlet.java # Servlet to handle form submission
│   └── VerifyCertificateServlet.java   # QR verification logic
│
├── WebContent/
│   ├── index.jsp                       # Admin input page
│   ├── verify.jsp                      # Verification portal
│   └── templates/                      # Certificate templates
│
├── lib/
│   ├── itextpdf-5.5.13.2.jar
│   ├── zxing-core-3.5.1.jar
│   └── zxing-javase-3.5.1.jar
│
├── certificates/
│   └── generated PDFs saved here
│
└── conf/
    └── certificate_records.xml         # Stores certificate data
```

---

## 🚀 How to Run the Project

### 🧩 Step 1 — Prerequisites

Install:

* **Java JDK 17+**
* **Apache Tomcat 10.1+**
* **VS Code / Eclipse IDE**
* **ZXing JAR** and **iTextPDF JAR** files placed inside `/lib` folder.

---

### 🧩 Step 2 — Compilation (Using CMD)

```bash
cd Desktop/CertificateGenerator
javac -cp "lib/*" -d . src/CertificateGenerator.java
```

---

### 🧩 Step 3 — Execution

```bash
java -cp ".;lib/*" CertificateGenerator
```

---

### 🧩 Step 4 — Access in Browser

Start Tomcat server and open:

```
http://localhost:8080/CertificateGenerator/
```

You’ll see a form where admin can enter:

* Student Name
* Course
* Date
* Grade

Then click **Generate Certificate**, and a PDF with QR code will be created.

---

## 🖼️ Sample Output

✅ Example Certificate:

```
Certificate of Completion
This is to certify that
     MAHESH ADAPA
has successfully completed the course
     "JAVA PROGRAMMING"
on 08-Nov-2025
Grade: A+
Certificate ID: C-1024
[QR CODE HERE]
```

---

## 📜 Verification Example

When QR code is scanned, it redirects to:

```
http://localhost:8080/CertificateGenerator/verify.jsp?certid=C-1024
```

It displays:

```
Certificate ID: C-1024
Student Name: MAHESH ADAPA
Course: JAVA PROGRAMMING
Status: ✅ Verified
```

---

## 🧠 Future Enhancements

* Integration with MySQL instead of XML
* Email auto-send feature for generated certificates
* Admin dashboard with statistics and filters
* Role-based login (Admin, Student, Verifier)

---

## 👨‍💻 Author

**Mahesh Adapa**
📍 Project under: *Bytexl Java Mini Project Series*
💡 Technologies: Java | JSP | Servlets | XML | Tomcat | QR Code

---

