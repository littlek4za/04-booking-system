package com.littlek4za.booking_system.models;

public class CacheKeys {

    public static String eventWithSlotCountList(Long userId) {
        return "EVENT_LIST:" + userId;
    }

    public static String eventById(Long userId, Long eventId) {
        return "EVENT:" + userId + ":" + eventId;
    }

    public static String slotListByEventId(Long userId, Long eventId) {
        return "SLOT_LIST:" + userId + ":" + eventId;
    }

    public static String slotById(Long userId, Long eventId, Long slotId) {
        return "SLOT:" + userId + ":" + eventId + ":" + slotId;
    }

    public static String invitationListByEventId(Long userId, Long eventId) {
        return "INVITATION_LIST:" + userId + ":" + eventId;
    }

    public static String invitationListBySlotId(Long userId, Long eventId, Long slotId) {
        return "INVITATION_LIST:" + userId + ":" + eventId + ":" + slotId;
    }

    public static String invitationListBySlotIdPattern(Long userId, Long eventId) {
        return "INVITATION_LIST:" + userId + ":" + eventId + ":*";
    }

    public static String invitationByToken(String invitationToken) {
        return "INVITATION:" + invitationToken;
    }

    public static String groupInvitationTokens(Long eventId) {
        return "EVENT:" + eventId + ":INVITATIONS";
    }

    // KEY: INVITATION:abc123
    // VALUE: {...json of invitationResponseDto...}
    // TTL: 5 min

    // KEY: EVENT:3:INVITATION
    // VALUE: SET containing {"abc123", "xyz789", ...}
    // TTL: none (unless set)

    // RiskService Caching
    public static String emailIpAttemptCounterForBookingView(String email, String ip) {
        return "risk:bookingView:" + email + ":" + ip;
    }

    public static String ipAttemptCounterForBookingView(String ip) {
        return "risk:bookingView:" + ip;
    }

    public static String emailIpAttemptCounterForBookingCreate(String email, String ip) {
        return "risk:bookingCreate:" + email + ":" + ip;
    }

    public static String ipAttemptCounterForBookingCreate(String ip) {
        return "risk:bookingCreate:" + ip;
    }

    public static String ipCreateSuccessCounter(String ip) {
        return "risk:bookingCreateSuccess:" + ip;
    }

    // RiskService Login Attemp
    public static String usernameIpAttemptCounterForLogin(String ip, String username) {
        return "risk:login:" + ip + username.toLowerCase();
    }

    public static String usernameAttemptCounterForLogin(String username) {
        return "risk:login:" + username.toLowerCase();
    }

    public static String ipAttemptCounterForLogin(String ip){
        return "risk:login:" + ip;
    }

    public static String ipBanForLogin(String ip){
        return "risk:login:ban:" + ip;
    }
}
