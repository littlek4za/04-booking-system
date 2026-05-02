## 📌 Create Booking

**Endpoint**  
POST /api/v1/slots/{slotId}/bookings

**Description**  
Create a booking for a specific slot under an invitation.

Supports:  
- Guest booking (via token)
- Logged-in user booking

---

### 🔐 Authorization
@PreAuthorize("@authz.isGuestBookingCreate() or (@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ATTENDEE'))")

Allowed:
- Guest with `GUEST_BOOKING_CREATE` token
- User with `ROLE_ADMIN` or `ROLE_ATTENDEE`

Denied:
- Unauthenticated -> 401
- Wrong role / token -> 403

---

### 📥 Request

**Path Variable**
- slotId (Long)

**Body**
```json
{
  "invitationId": 1001,
  "bookedStartTime": "2026-05-01T10:00:00",
  "email": "guest@example.com",
  "firstName": "John",
  "lastName": "Doe"
}

```

**Notes**
- `email`,`firstName`,`lastName` required for guest
- may differ for user (handled internally)

---

### 📥 Response (201 Created)
```json
{
  "id": 123,
  "username": "john123",
  "firstName": "John",
  "lastName": "Doe",
  "guestFirstName": "John",
  "guestLastName": "Doe",
  "isGuest": true,
  "email": "guest@example.com",
  "bookedStartTime": "2026-05-01T10:00:00Z",
  "bookedEndTime": "2026-05-01T11:00:00Z",
  "bookingToken": "ABC123",
  "bookingStatus": "UPCOMING"
}
```

---

### ❌ Error Responses

| Status | Code                     | Description                   |
| ------ | ------------------------ | ----------------------------- |
| 401    | UNAUTHORIZED             | Not authenticated             |
| 403    | FORBIDDEN                | Invalid role or token         |
| 404    | SLOT_NOT_FOUND           | Slot does not exist           |
| 404    | INVITATION_NOT_FOUND     | Invitation not found          |
| 400    | SLOT_INVITATION_MISMATCH | Slot not linked to invitation |
| 400    | VALIDATION_FAILED        | Invalid booking request       |

---

### 🧠 Business Rules
- Slot must belong to the invitation
- Invitation must allow access for the user/guest
- Booking info must match event constraints
- Guest activity may be tracked (IP/email risk control)

---

### 🔒 Security Notes
- Guest requests are monitored via IP and email
- Repeated invalid attempts may trigger restrictions
- Successful booking resets risk counters

---