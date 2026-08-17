# Booking System — Full-Stack Scheduling Platform

A full-stack booking platform where organizers create events, configure schedules, and invite attendees to book available time slots. Events can be constructed in three ways: recurring business hours, flexible availability windows, or fixed-time events.

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
- [Deployment Guide](#deployment-guide)

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
| **Event** | The container for everything. Holds event info, location, booking rules, and a **type** that determines how slots behave. |
| **Slot** | Created inside an event, slot defines *when* bookings can happen. Holds basic slot info, slot booking rules, slot bookable time. Their shape depends entirely on the chosen event type. |
| **Invitation** | A shareable link tied to an event, with one or more slots attached. This is what gets sent to attendees to book against. |
| **Booking** | Storage for all booking info, where booking info can be traced back by organizer and attendee. |

---

## Event Types

### 🏪 Business
Child slot will be set as recurring weekly availability — ideal for clinics, salons, support slots etc.

- Set your weekly business hours (e.g. Mon–Fri 9am–5pm)
- Define a **frequency** (e.g. every 5 minutes) 
- Defines a **duration** per booking (e.g. 1 hour)
- Attendee picks a start time → booking blocks that window automatically (e.g. attendee pick 4:15pm, booking blocks time frame for 4.15pm-5.15pm)
- *Example: walk-in consultation slots*

### 🎛 Flexible
Child slot will be set as custom availability windows — ideal for tutoring, freelance calls, interviews etc.

- Define one or multiple date + time ranges (e.g. Date A 8am–3pm, Date B 11am–12pm)
- Same **frequency** and **duration** controls as Business
- One slot can be sent to multiple invitees — each picks their own time independently
- *Example: 1-on-1 tuition shared across 5 students from a single slot pool*

### 📌 Fixed
Child slot will be set with a single, fixed start and end time — ideal for webinars, group sessions, and set appointments.

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
- **Redis** caching layer for read-heavy endpoints
  - Event-scoped cache invalidation on writes
- **JWT authentication** for registered users
- **Temporary JWT tokens** for sensitive public endpoints
  - View booking
  - Create booking
  - Delete booking
- **Method-level authorization** using `@PreAuthorize`
- **Global error handling** for consistent API responses
- **IP-based security bouncer filter** for request filtering
- **Risk scoring service** to evaluate failed attempts and dynamically trigger **reCAPTCHA** challenges
- **Transactional boundaries** using `@Transactional` to ensure data consistency
- **Database-level locking** using `@Lock(PESSIMISTIC_WRITE)`
  - Prevents slot overbooking
  - Prevents exceeding invitation limits
  - Maintains consistency during concurrent booking operations
- **Bean Validation** using `@Valid` and **custom validators**
  - Prevents invalid data from reaching the service layer

### Frontend

- **Angular** with a component-based structure mirroring the UI
- **FullCalendar** for slot and booking visualization
- **Leaflet** for event location maps
- **Bootstrap** + **Font Awesome** for UI components and icons
- **Angular Signals** and **Angular lifecycle hooks** for a reactive UI experience
- **Global HTTP error interceptor** for consistent error handling
- **Auth interceptor** to attach the appropriate JWT token based on route context
- **Route guards** for authentication and authorization control
- **Conditional reCAPTCHA rendering** based on backend risk signals
- **Centralized API service layer** for structured backend communication
- **Global loading service** to improve the user experience
- **Structured logging system** with multiple severity levels
  - `INFO`
  - `WARN`
  - `ERROR`
  - `DEBUG`
- **Centralized error mapping service** to normalize backend HTTP errors and map standardized error codes to user-friendly messages
- **Standardized error code contract** shared between backend and frontend for consistent system-wide error handling

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

## Deployment Guide

Deploy the services in this order: **PostgreSQL -> Redis -> backend -> frontend**.
This order makes configuration straightforward because the backend needs the
database and Redis connection details, while the frontend needs the deployed
backend URL.

### Prerequisites

- Java 21+
- Node.js 18+
- GitHub repository
- Neon PostgreSQL account
- Upstash account
- Render account
- Vercel account
- Google reCAPTCHA v2 site key and secret key
- Gmail account with an App Password, if using Gmail SMTP

---

### 1. Deploy the database (Neon)

Create a PostgreSQL database in [Neon](https://neon.tech), then run [`01-create-tables.sql`](01-starter-file/db-script/01-create-tables.sql) in Neon SQL editor. The script creates the application schema and seeds the three default roles: `ROLE_ADMIN`, `ROLE_ORGANIZER`, and `ROLE_ATTENDEE`.

Copy these Neon connection details for the backend deployment:

| Backend variable | Value from Neon |
|---|---|
| `DATABASE_URL` | JDBC PostgreSQL connection URL |
| `DATABASE_USERNAME` | Database username |
| `DATABASE_PASSWORD` | Database password |

> The seed script also includes a default administrator account for testing.
Change or remove this account before using the application in a real
environment.

<details>
<summary>Optional: local PostgreSQL</summary>

Create a local database named `booking-system-db`, run the same SQL script, and configure `application.properties` with your local database credentials.

</details>

---

### 2. Deploy the cache (Upstash)

Create a Redis database in [Upstash](https://upstash.com). Redis supports read-heavy caching plus short-lived risk and security counters; production connections use TLS.

Copy these values into the backend deployment:

| Backend variable | Value from Upstash |
|---|---|
| `REDIS_HOST` | Redis hostname |
| `REDIS_PORT` | Redis port |
| `REDIS_PASSWORD` | Redis password |

The application continues to serve core functionality if Redis is unavailable, though cache- and counter-dependent behaviour is temporarily unavailable.

<details>
<summary>Optional: local Redis</summary>

Run Redis on `localhost:6379` (for example, with Docker) and keep the local Redis values in `application.properties`.

</details>

---

### 3. Deploy the backend (Render)

1. Create a new **Web Service** in [Render](https://render.com) from this repository.
2. Set its root directory to `02-backend-booking-system` and use Java 21.
3. Add the environment variables below, then deploy the service.
4. Copy the deployed backend URL; it is needed for the frontend configuration.

The backend is built with **Spring Boot** and uses separate configuration
profiles for local development and production.

**Configuration location:**

`02-backend-booking-system/src/main/resources/`

| File | Purpose |
|---|---|
| `application-prod.properties` | Production configuration using environment variables |
| `application.properties` | Optional local development configuration |

> Local configuration is covered in the optional section at the end of this backend guide.

#### Production configuration (`application-prod.properties`)

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

#### Render environment variables

The following environment variables are configured in the **Render**
deployment:

| Variable | Description |
|---|---|
| `DATABASE_URL` | JDBC connection URL for the production PostgreSQL database |
| `DATABASE_USERNAME` | Production PostgreSQL username |
| `DATABASE_PASSWORD` | Production PostgreSQL password |
| `REDIS_HOST` | Upstash Redis hostname |
| `REDIS_PORT` | Upstash Redis port |
| `REDIS_PASSWORD` | Upstash Redis password |
| `JWT_SECRET` | Long random string — generate with `openssl rand -hex 64` |
| `ALLOWED_ORIGINS` | Your frontend URL, e.g. `https://your-app.vercel.app` |
| `FRONTEND_URL` | Same as above (used for email links) |
| `MAIL_USERNAME` | Gmail address used for sending emails |
| `MAIL_PASSWORD` | Gmail App Password (not your normal Gmail password) |
| `RESEND_API` | *(Optional)* API key from [resend.com](https://resend.com) — only needed if switching from Gmail SMTP to Resend as the mail provider, further configuration is needed inside the service |
| `RECAPTCHA_SECRET` | Secret key from Google reCAPTCHA Admin Console |

<details>
<summary>Optional: run the backend locally</summary>

For local development, `application.properties` points to:

- Local PostgreSQL
- Redis running on `localhost:6379`
- Angular frontend running on `localhost:4300`

Start the backend with:

```bash
cd 02-backend-booking-system
./mvnw spring-boot:run
# Backend starts at http://localhost:8080
```

To run with the production profile locally (e.g. to test against Neon + Upstash):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

</details>

---

### 4. Deploy the frontend (Vercel)

The frontend is built with **Angular** and deployed to **Vercel**.

1. Update `environment.ts` with the deployed Render API URL and your public reCAPTCHA site key.
2. Import this repository into Vercel and set the root directory shown below.
3. Deploy the application.
4. Copy the Vercel URL into the backend's `ALLOWED_ORIGINS` and `FRONTEND_URL` environment variables, then redeploy the backend.

**Project location:**

`03-frontend-booking-system/booking-system-web/`

**Configuration location:** `03-frontend-booking-system/booking-system-web/src/environments/`

The production Angular environment is configured in `environment.ts`:

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

<details>
<summary>Optional: run the frontend locally</summary>

Set `backendApiUrl` in `environment.development.ts` to `http://localhost:8080/api`, then run:

```bash
cd 03-frontend-booking-system/booking-system-web
npm install
ng serve --port 4300
# Frontend starts at http://localhost:4300
```

</details>

---

## Email Notifications

> **Note:** Email notifications are not available on the current free-tier
> deployment because outbound SMTP connections on port 587 are blocked.
> The application uses Gmail SMTP for email delivery.

| Event | Recipient |
|---|---|
| Attendee books a slot | Organizer notified |
| Attendee cancels a booking | Organizer notified |
| Booking confirmed | Attendee receives booking details |
| Organizer cancels a booking | Attendee notified |
