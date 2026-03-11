

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

CREATE TABLE users (
	id BIGSERIAL PRIMARY KEY,
	username VARCHAR(50) UNIQUE, 
	password VARCHAR(80),
	email VARCHAR(255) NOT NULL UNIQUE,
	first_name VARCHAR(255),
	last_name VARCHAR(255),
	guest BOOLEAN NOT NULL, -- NEW
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
	id BIGSERIAL PRIMARY KEY,
	role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users_roles (
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
	PRIMARY KEY (user_id, role_id)
);

CREATE TABLE events (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	event_name TEXT NOT NULL,
	event_description TEXT,
	event_location_address TEXT NOT NULL,
	include_position BOOLEAN NOT NULL DEFAULT FALSE, 
	latitude DOUBLE PRECISION,
	longitude DOUBLE PRECISION,
	event_type VARCHAR(20) NOT NULL, -- 'FIXED' or 'FLEXIBLE' or 'BUSINESS'
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL 
);

CREATE TABLE slots (
	id BIGSERIAL PRIMARY KEY,
	event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
	slot_name TEXT NOT NULL,
	slot_description TEXT,
	slot_start_time TIMESTAMPTZ,
	slot_end_time TIMESTAMPTZ,
	max_book_per_interval INT NOT NULL DEFAULT 1,
	slot_interval_minutes INT,
	slot_frequency_interval_minutes INT,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL,
	business_days_hours JSONB,
	flexible_days_hours JSONB,
	CONSTRAINT unique_event_slot_name UNIQUE (event_id, id)
);

CREATE TABLE invitations (
	id BIGSERIAL PRIMARY KEY,
	event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE, -- MODIFY
	created_by BIGINT NOT NULL REFERENCES users(id),
	expires_at TIMESTAMPTZ NOT NULL,
	max_usage INT,
	used_count INT DEFAULT 0,
	access_token VARCHAR(6) NOT NULL UNIQUE, -- NEW
	include_mode VARCHAR(20) NOT NULL, -- 'ALL_AND_FUTURE' or 'ALL_CURRENT' or 'SELECTED' -- NEW
	required_login BOOLEAN NOT NULL DEFAULT TRUE,
	max_usage_per_user INT, -- NULL means UNLIMITED USER -- NEW
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW() -- NEW
);

CREATE TABLE bookings (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	guest_first_name VARCHAR(255),
	guest_last_name VARCHAR(255),
	slot_id BIGINT NOT NULL REFERENCES slots(id) ON DELETE CASCADE,
	invitation_id BIGINT NOT NULL REFERENCES invitations(id) ON DELETE CASCADE,
	booked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	booked_start_time TIMESTAMPTZ NOT NULL,
	booked_end_time TIMESTAMPTZ NOT NULL,
	booking_token VARCHAR(6) NOT NULL UNIQUE, 
	is_deleted BOOLEAN NOT NULL DEFAULT false,
	deleted_at TIMESTAMPTZ,
	deleted_by VARCHAR(20)
);

CREATE TABLE invitation_slots (
	invitation_id BIGINT NOT NULL REFERENCES invitations(id) ON DELETE CASCADE, -- NEW
	slot_id BIGINT NOT NULL REFERENCES slots(id) ON DELETE CASCADE, -- NEW
	PRIMARY KEY(invitation_id, slot_id) -- NEW
);

CREATE TABLE invitation_usages ( -- NEW
	invitation_id BIGINT NOT NULL REFERENCES invitations(id) ON DELETE CASCADE,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, 
	usage_count INT NOT NULL DEFAULT 0, 
	PRIMARY KEY(invitation_id, user_id) 
);

INSERT INTO roles (id, role_name)
VALUES 
	(1,'ROLE_ADMIN'),
	(2,'ROLE_ORGANIZER'),
	(3,'ROLE_ATTENDEE');

INSERT INTO users (username, password, email, first_name,last_name, guest)
VALUES 
	('admin','$2a$12$m6uSzgzScOBsJW/4G3ueUePUpvMKjVXeOq/ETtbn.1fDytaxGScmO','admin@test.com','booking','admin', false),
	('organizer','$2a$12$m6uSzgzScOBsJW/4G3ueUePUpvMKjVXeOq/ETtbn.1fDytaxGScmO','organizer@test.com','booking','organizer', false),
	('attendee','$2a$12$m6uSzgzScOBsJW/4G3ueUePUpvMKjVXeOq/ETtbn.1fDytaxGScmO','attendee@test.com','booking','attendee', false);

INSERT INTO users_roles (user_id, role_id)
VALUES
	(1,1),
	(2,2),
	(3,3);


