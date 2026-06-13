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

    public static String invitationByToken(String invitationToken){
        return "INVITATION:" + invitationToken;
    }

    public static String groupInvitationTokens(Long eventId){
        return "EVENT:" + eventId + ":INVITATIONS";
    }

    // KEY: INVITATION:abc123
    // VALUE: {...json of invitationResponseDto...}
    // TTL: 5 min

    // KEY: EVENT:3:INVITATION
    // VALUE: SET containing {"abc123", "xyz789", ...}
    // TTL: none (unless set)

}
