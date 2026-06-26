# Booking System — Full-Stack Scheduling Platform

A full-stack booking platform that supports **three distinct scheduling models**: recurring business hours, flexible availability windows, and fixed-time events. Built and deployed across separate managed services.

🔗 **Live Demo:** [04-booking-system.vercel.app](https://04-booking-system.vercel.app)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [How It Works](#how-it-works)
- [Event Types](#event-types)
- [Architecture & Stack](#architecture--stack)
- [Security](#security)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)

---

## Overview

This platform allows **organizers** to create events, define bookable slots, and share invitation links — while **attendees** (registered or guest) can browse available times, book a slot, and manage their reservations. Automated email notifications keep both parties in sync throughout.

---

## Features

### Organizer (requires account)
- Create events with location (with map), booking rules, and event type
- Build slots using one of three scheduling models: **Business**, **Flexible**, or **Fixed**
- Generate shareable invitation links with one or more slots attached
- Receive email notifications on bookings and cancellations

### Attendee (registered users and public guests)
- Open an invitation link and view available slots in their local timezone
- Book, manage, or cancel a reservation
- Receive email confirmations and cancellation notices

---

## How It Works

The system is built from four connected layers:

```
Event  →  Slot  →  Invitation  →  Booking
```

| Layer | Description |
|---|---|
| **Event** | The top-level container. Holds event info, location, booking rules, and a **type** that determines how slots behave. |
| **Slot** | Defines *when* bookings can happen. Shape and configuration depend on the event type. |
| **Invitation** | A shareable link tied to an event, with one or more slots attached. Sent to attendees to book against. |
| **Booking** | Stores all booking records, traceable by both organizer and attendee. |

An invitation can attach **multiple slots** within the same event.

---

## Event Types

### 🏪 Business
Recurring weekly availability — ideal for clinics, salons, and support slots.

- Define weekly business hours (e.g. Mon–Fri 9am–5pm)
- Set a **frequency** (e.g. every 5 minutes) and **duration** per booking (e.g. 1 hour)
- Attendee picks a start time → booking blocks that window automatically
- *Example: walk-in consultation slots*

### 🎛 Flexible
Custom availability windows — ideal for tutoring, freelance calls, and interviews.

- Define one or multiple date + time ranges (e.g. Date A 8am–3pm, Date B 11am–12pm)
- Same **frequency** and **duration** controls as Business
- One slot can be sent to multiple invitees — each picks their own time independently
- *Example: 1-on-1 tuition shared across 5 students from a single slot pool*

### 📌 Fixed
A single, fixed start and end time — ideal for webinars, group sessions, and set appointments.

- Organizer sets one fixed start and end time
- Attendees book directly against that exact slot
- No frequency or duration calculation involved
- *Example: a live group workshop at a set date and time*

---

## Architecture & Stack

| Layer | Technology | Hosting |
|---|---|---|
| Frontend | Angular | Vercel |
| Backend | Spring Boot (Java) | Render |
| Database | PostgreSQL | Neon |
| Cache | Redis | Upstash |

### Backend

- **Spring Boot** as the core framework
- **Spring Data JPA** for database access
- **Redis** caching layer for read-heavy endpoints, with event-scoped cache invalidation on writes
- **JWT authentication** for registered users
- Sensitive public endpoints protected with short-lived **temporary JWT tokens**
- Every service method protected via `@PreAuthorize`
- Global **error-handling interceptor** for consistent API responses
- **Database-level locking** with `@Lock(PESSIMISTIC_WRITE)` to prevent slot overbooking and ensure consistency during concurrent booking operations
- `@Transactional` boundaries to ensure correctness after execution
- **Bean Validation** (`@Valid`) and **custom validators** to prevent invalid data from reaching the service layer
- **Three-layer booking limits** — organizers can cap bookings per identity at the Event, Slot, and Invitation level independently

### Frontend

- **Angular** with a component-based structure mirroring the domain model
- **FullCalendar** for slot and booking visualization
- **Leaflet** for event location maps
- **Bootstrap** + **Font Awesome** for UI and icons
- Modern **Angular Signals** and classic lifecycle hooks for a reactive UI experience
- **Auth interceptor** that attaches the appropriate JWT token per request based on route context
- **Route guards** for authentication and authorization control
- **HTTP error interceptor** for consistent error handling
- **Centralized API service layer** for structured backend communication
- **Centralized error mapping service** that normalizes backend HTTP errors into a unified format
- **Standardized error code contract** shared between backend and frontend for system-wide consistency
- **Global loading service** to improve UI experience
- **Structured logging** with multiple severity levels (INFO, WARN, ERROR, DEBUG)

---

## Security

| Mechanism | Details |
|---|---|
| JWT Authentication | Protects organizer endpoints |
| Temporary JWT Tokens | Short-lived tokens for sensitive public-facing actions (view/create/delete bookings) |
| `@PreAuthorize` | Method-level authorization on every service method |
| IP-based Bouncer Filter | Filters requests based on IP reputation |
| Risk Scoring Service | Evaluates failed attempts and dynamically triggers reCAPTCHA challenges |
| Pessimistic DB Locking | Prevents overbooking and race conditions during concurrent reservations |
| Rate Limiting | Applied to public-facing endpoints |

---

## Database Schema

The schema is initialised via `01-starter-file/db-script.sql`. Key tables:

| Table | Purpose |
|---|---|
| `users` | Registered users and guest attendees |
| `roles` / `users_roles` | Role-based access (`ROLE_ADMIN`, `ROLE_ORGANIZER`, `ROLE_ATTENDEE`) |
| `events` | Top-level event container with type (`BUSINESS`, `FLEXIBLE`, `FIXED`) |
| `slots` | Bookable time windows; structure varies by event type (JSONB for business/flexible hours) |
| `invitations` | Shareable links tied to an event, with expiry, usage limits, and access token |
| `invitation_slots` | Many-to-many join between invitations and slots |
| `invitation_usages` | Per-identity usage tracking for invitation-level booking limits |
| `bookings` | Booking records with soft-delete support; stores a snapshot of event/slot info at time of booking |

---

## Project Structure

```
04-booking-system/
├── 01-starter-file/
│   └── db-script/                            # SQL script to initialise the database schema
├── 02-backend-booking-system/                # Spring Boot backend
│   └── src/main/resources/
│       ├── application.properties            # Local development config
│       └── application-prod.properties       # Production config (reads from env vars)
└── 03-frontend-booking-system/
    └── booking-system-web/                   # Angular frontend
        └── src/environments/
            ├── environment.ts                # Production build config
            └── environment.development.ts    # Local development config
```

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL instance (local or [Neon](https://neon.tech))
- Redis instance (local or [Upstash](https://upstash.com))
- Gmail account (for SMTP) or [Resend](https://resend.com) API key
- Google reCAPTCHA v2 site key + secret key

---

### 1. Database Setup

Run the provided SQL script to create all tables and seed the default admin user:

```bash
psql -U postgres -d your_database_name -f 01-starter-file/db-script.sql
```

This creates the following tables: `users`, `roles`, `users_roles`, `events`, `slots`, `invitations`, `bookings`, `invitation_slots`, `invitation_usages` — and inserts the 3 default roles (`ROLE_ADMIN`, `ROLE_ORGANIZER`, `ROLE_ATTENDEE`) plus a default admin account.

> **Default admin credentials (for local testing only):**
> Email: `admin@testcom.testcom` / Password: `admin` *(change this before any real deployment)*

---

### 2. Backend Configuration

The backend uses two property files:

| File | Used when |
|---|---|
| `application.properties` | Local development (points to localhost DB & Redis) |
| `application-prod.properties` | Production (reads all secrets from environment variables) |

**File location:** `02-backend-booking-system/src/main/resources/`

#### `application.properties` (local development)

```properties
spring.application.name=booking-system
server.port=8080

# --- Database (local PostgreSQL) ---
spring.datasource.url=jdbc:postgresql://localhost:5432/booking-system-db
spring.datasource.username=postgres
spring.datasource.password=YOUR_LOCAL_DB_PASSWORD

# --- Redis (local) ---
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=500ms
spring.data.redis.connect-timeout=500ms

# --- JPA ---
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# --- CORS ---
allowed.origins=http://localhost:4300
app.frontend.url=http://localhost:4300

# --- JWT ---
# Generate with: openssl rand -hex 64
security.jwt.token.secret-key=YOUR_JWT_SECRET
security.jwt.issuer=booking-system

# --- Email (Gmail SMTP) ---
# Use a Gmail App Password, not your normal password.
# Google Account → Security → 2-Step Verification → App Passwords
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# --- Resend (optional alternative mail provider) ---
# The project supports Resend as an alternative to Gmail SMTP.
# To switch, update the mail sending logic in the email event service to use the Resend API instead.
# resend.api.key=YOUR_RESEND_API_KEY

# --- Google reCAPTCHA ---
# Get keys at: https://www.google.com/recaptcha/admin
recaptcha.secret-key=YOUR_RECAPTCHA_SECRET_KEY
```

#### `application-prod.properties` (production — uses environment variables)

```properties
spring.application.name=booking-system
server.port=8080

# --- Database ---
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# --- Redis ---
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD}
spring.data.redis.ssl.enabled=true
spring.data.redis.timeout=500ms
spring.data.redis.connect-timeout=500ms

# --- JPA ---
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# --- CORS ---
allowed.origins=${ALLOWED_ORIGINS}
app.frontend.url=${FRONTEND_URL}

# --- JWT ---
security.jwt.token.secret-key=${JWT_SECRET}
security.jwt.issuer=booking-system

# --- Email ---
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# --- Resend (optional alternative mail provider) ---
# resend.api.key=${RESEND_API}

# --- reCAPTCHA ---
recaptcha.secret-key=${RECAPTCHA_SECRET}
```

#### Production environment variables (set in Render dashboard)

| Variable | Description |
|---|---|
| `DATABASE_URL` | Full JDBC URL, e.g. `jdbc:postgresql://host/db?sslmode=require` |
| `DATABASE_USERNAME` | Database username |
| `DATABASE_PASSWORD` | Database password |
| `REDIS_HOST` | Upstash Redis hostname |
| `REDIS_PORT` | Upstash Redis port (usually `6379`) |
| `REDIS_PASSWORD` | Upstash Redis password |
| `JWT_SECRET` | Long random string — generate with `openssl rand -hex 64` |
| `ALLOWED_ORIGINS` | Your frontend URL, e.g. `https://your-app.vercel.app` |
| `FRONTEND_URL` | Same as above (used for email links) |
| `MAIL_USERNAME` | Gmail address used for sending emails |
| `MAIL_PASSWORD` | Gmail App Password (not your normal Gmail password) |
| `RESEND_API` | *(Optional)* API key from [resend.com](https://resend.com) — only needed if switching from Gmail SMTP to Resend as the mail provider |
| `RECAPTCHA_SECRET` | Secret key from Google reCAPTCHA Admin Console |

#### Running the backend locally

```bash
cd 02-backend-booking-system
./mvnw spring-boot:run
# Backend starts at http://localhost:8080
```

To run with the prod profile locally (e.g. to test against Neon + Upstash):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

### 3. Frontend Configuration

**File location:** `03-frontend-booking-system/booking-system-web/src/environments/`

#### `environment.development.ts` (local development)

```typescript
import { LogLevel } from "@core/model/log-level";

export const environment = {
    production: false,
    backendApiUrl: "http://localhost:8080/api",
    logLevel: LogLevel.Debug,
    captchaSiteKey: 'YOUR_RECAPTCHA_SITE_KEY',
};
```

#### `environment.ts` (production build)

```typescript
import { LogLevel } from "@core/model/log-level";

export const environment = {
    production: true,
    backendApiUrl: "https://your-backend.onrender.com/api",
    logLevel: LogLevel.Error,
    captchaSiteKey: 'YOUR_RECAPTCHA_SITE_KEY',
};
```

> `captchaSiteKey` is the **public** site key from [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin). It is safe to commit — only the secret key (used on the backend) must be kept private.

#### Running the frontend locally

```bash
cd 03-frontend-booking-system/booking-system-web
npm install
ng serve
# Frontend starts at http://localhost:4300
```

---

## Email Notifications

| Event | Recipient |
|---|---|
| Attendee books a slot | Organizer notified |
| Attendee cancels a booking | Organizer notified |
| Booking confirmed | Attendee receives booking details |
| Organizer cancels a booking | Attendee notified |