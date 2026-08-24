Healthcare Appointment Management System

![System Architecture](system-architecture.png)

A full-stack healthcare appointment management system built with Spring Boot (Java 17) and React (Vite). The platform supports separate workflows for Patients, Doctors, and Administrators, with JWT authentication, AI-powered visit summaries using Gemini, Google Calendar synchronization, PostgreSQL persistence, and a serverless email relay.

⸻

Features

Patient

* Patient registration and authentication
* Browse available doctors
* Filter doctors by specialization
* View available appointment slots
* Book appointments
* Cancel appointments
* View appointment history
* Submit symptoms before an appointment
* View AI-generated post-visit summaries
* View prescribed medications

Doctor

* View scheduled appointments
* Review patient symptoms and pre-visit summaries
* Complete appointments
* Add visit notes
* Generate AI-powered post-visit summaries
* Add prescriptions and medication details

Administrator

* Create Doctor accounts
* Assign doctor specializations
* Manage doctor leave dates
* Block appointment availability for specific dates

Additional Integrations

* JWT-based authentication
* Gemini API for AI-generated summaries
* Google Calendar API for appointment synchronization
* Serverless email relay using Vercel Functions
* PostgreSQL database
* Optimistic locking for appointment concurrency

⸻

Technology Stack

Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* JWT
* Maven
* PostgreSQL

Frontend

* React
* Vite
* JavaScript
* REST APIs

External Services

* Google Gemini API
* Google Calendar API
* Google Cloud Service Account
* Vercel Serverless Functions

Deployment

* Frontend: Vercel
* Backend: Render
* Database: PostgreSQL

⸻

System Architecture

                    React + Vite
                     Frontend
                       │
                       │ REST API
                       │ JWT
                       ▼
                Spring Boot Backend
                       │
          ┌────────────┼────────────┐
          │            │            │
          ▼            ▼            ▼
     PostgreSQL    Gemini API   Google Calendar
      Database     AI Summary       API
                       │
                       │
                       ▼
                Email Relay
             Vercel Serverless

⸻

Test Credentials

Admin

Email:    admin@admin.com
Password: admin123
Role:     ADMIN

The Admin account can be used to create Doctor accounts and manage doctor leave dates.

You can also register your own Patient account through the application.

For testing the complete workflow:

1. Log in as Admin.
2. Create a Doctor with an email address and specialization.
3. Register a Patient account.
4. Log in as the Patient.
5. Select a Doctor and available appointment slot.
6. Book an appointment.
7. Log in as the Doctor and complete the appointment.
8. Review the generated AI summary and prescription information.

⸻

Local Development

Prerequisites

Install the following before running the project:

* Java 17+
* Node.js 18+
* PostgreSQL 14+
* Maven

⸻

Backend Setup

Navigate to the backend directory:

cd backend

Create:

backend/src/main/resources/application.properties

Configure the required properties:

# Database
spring.datasource.url=jdbc:postgresql://<YOUR_DB_HOST>:<YOUR_DB_PORT>/<YOUR_DB_NAME>
spring.datasource.username=<YOUR_DB_USER>
spring.datasource.password=<YOUR_DB_PASSWORD>
spring.jpa.hibernate.ddl-auto=update
# JWT
jwt.secret=YOUR_SUPER_SECRET_JWT_KEY_STRING_OF_AT_LEAST_32_BYTES
# Gemini API
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent
# Google Calendar
google.credentials.path=classpath:healthcare-appointment-key.json
google.calendar.id=YOUR_CALENDAR_ID@group.calendar.google.com
# Email Relay
email.relay.url=https://<YOUR_VERCEL_DOMAIN>.vercel.app/api/send-email
app.test.email=YOUR_TEST_EMAIL@gmail.com

Compile the project:

./mvnw clean compile

Run the application:

./mvnw spring-boot:run

The backend runs by default on:

http://localhost:8080

⸻

Frontend Setup

Navigate to the frontend directory:

cd frontend

Install dependencies:

npm install

Create a .env file:

VITE_API_URL=http://localhost:8080/api

Start the development server:

npm run dev

The frontend will normally be available at:

http://localhost:5173

⸻

API Documentation

Authentication

Method	Endpoint	Description
POST	/api/auth/register	Register a new Patient
POST	/api/auth/login	Authenticate a user and return a JWT

Patient

Method	Endpoint	Description
GET	/api/doctors	Retrieve all doctors
GET	/api/doctors?specialization={name}	Filter doctors by specialization
GET	/api/doctors/{id}/slots?date=YYYY-MM-DD	Retrieve available appointment slots
GET	/api/appointments/patient/{patientId}	Retrieve patient appointments
POST	/api/appointments/book	Book an appointment
POST	/api/appointments/{id}/cancel	Cancel an appointment

Doctor

Method	Endpoint	Description
GET	/api/appointments/doctor/{doctorId}	Retrieve doctor’s appointments
POST	/api/appointments/{id}/complete	Complete an appointment and generate the post-visit summary

Admin

Method	Endpoint	Description
POST	/api/admin/doctors	Create a Doctor account
POST	/api/admin/leaves	Add a doctor leave date

⸻

Database Design

The application uses PostgreSQL with Hibernate/JPA for persistence.

Core Entities

User

The central authentication entity.

Supported roles:

PATIENT
DOCTOR
ADMIN

DoctorProfile

A @OneToOne relationship with a Doctor User.

Stores doctor-specific information such as specialization.

Appointment

The central transaction entity.

Relationships:

Appointment
 ├── Patient → User
 └── Doctor  → User

The entity stores:

* Appointment time
* Appointment status
* Patient
* Doctor
* Pre-visit summary
* Post-visit summary
* Google Calendar event ID

Appointments use JPA @Version for optimistic locking to help prevent conflicting updates.

Medication

Stores medication information associated with a patient and appointment.

Includes:

* Patient
* Appointment
* Medication
* Dosage
* Frequency

DoctorLeave

Stores dates on which a doctor is unavailable.

These dates are used when calculating available appointment slots.

⸻

AI Integration

The system uses Google Gemini 1.5 Flash to convert unstructured patient and doctor input into structured summaries.

Pre-Visit Summary

Patient-provided symptoms are processed using a prompt similar to:

You are a helpful medical assistant.
A patient has provided the following symptoms and complaints:
{symptoms}
Summarize these symptoms clearly and concisely for a doctor to read before the appointment.
Highlight any severe or urgent keywords.

The generated summary is associated with the appointment and can be reviewed by the doctor.

Post-Visit Summary

Doctor-provided notes are processed using:

You are a helpful medical assistant.
The doctor has provided the following raw notes from a patient visit:
{doctorNotes}
Create a clear, professional, and well-structured post-visit summary that the patient can read.
Include key takeaways, lifestyle recommendations if mentioned, and next steps.
Do NOT invent medical advice.

The resulting summary is stored with the appointment and made available to the patient.

AI-generated content is intended for summarization and informational purposes. It does not replace professional medical judgment or clinical decision-making.

⸻

Google Calendar Integration

Appointments can be synchronized with a shared Google Calendar using a Google Cloud Service Account.

1. Enable Google Calendar API

Create a project in Google Cloud Console and enable the Google Calendar API.

2. Create a Service Account

Create a Service Account and generate a JSON credentials file.

Place the file at:

backend/src/main/resources/healthcare-appointment-key.json

Do not commit this file to source control.

Add it to .gitignore:

healthcare-appointment-key.json

3. Create a Clinic Calendar

Create a separate calendar in Google Calendar, for example:

Clinic Appointments

4. Share the Calendar

Share the calendar with the Service Account email address.

The email will have a format similar to:

service-account@project-id.iam.gserviceaccount.com

Grant the Service Account:

Make changes to events

permission.

5. Configure Calendar ID

Find the Calendar ID from the calendar settings and configure:

google.calendar.id=YOUR_CALENDAR_ID@group.calendar.google.com

The backend can then create and manage calendar events for appointments.

⸻

Email Relay Architecture

Email delivery is handled separately from the Spring Boot application through a Vercel Serverless Function.

Spring Boot Backend
        │
        │ HTTP Request
        ▼
Vercel Serverless Function
        │
        ▼
   Email Service
        │
        ▼
 Patient / Doctor

Configure the relay endpoint using:

email.relay.url=https://<YOUR_VERCEL_DOMAIN>.vercel.app/api/send-email

⸻

Appointment Workflow

Patient Registration
        │
        ▼
Patient Login
        │
        ▼
Browse Doctors
        │
        ▼
Select Doctor & Date
        │
        ▼
View Available Slots
        │
        ▼
Book Appointment
        │
        ├──────────────► Google Calendar
        │
        └──────────────► Email Relay
                             
        ▼
Doctor Reviews Appointment
        │
        ▼
Doctor Completes Visit
        │
        ├──────────────► AI Post-Visit Summary
        │
        └──────────────► Prescription / Medication
        │
        ▼
Patient Views Visit Summary

⸻

Project Structure

healthcare-appointment/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── .gitignore
└── README.md

⸻

Security

The application implements:

* JWT-based authentication
* Role-based authorization
* Spring Security
* Password-based authentication
* Optimistic locking for appointment updates
* Environment-based configuration for sensitive credentials

The following files should never be committed:

application.properties
.env
healthcare-appointment-key.json

Production secrets should be stored using the deployment platform’s environment-variable or secret-management system.

⸻

Deployment

Frontend

Deploy the React/Vite application to Vercel.

Configure:

VITE_API_URL=<BACKEND_URL>/api

Backend

Deploy the Spring Boot application to Render or another Java-compatible hosting provider.

Configure the required database, JWT, Gemini, Google Calendar, and email relay properties through the deployment platform’s environment variables or configuration system.

⸻

Disclaimer

This project is intended for educational and demonstration purposes.

AI-generated summaries are designed to assist with information organization and should not be used as a substitute for diagnosis, treatment decisions, or professional medical advice.