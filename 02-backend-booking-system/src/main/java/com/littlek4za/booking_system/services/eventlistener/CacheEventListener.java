package com.littlek4za.booking_system.services.eventlistener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.littlek4za.booking_system.models.CacheKeys;
import com.littlek4za.booking_system.services.RedisCacheService;
import com.littlek4za.booking_system.services.event.EventServiceEvent;
import com.littlek4za.booking_system.services.event.InvitationServiceEvent;
import com.littlek4za.booking_system.services.event.SlotServiceEvent;

@Service
public class CacheEventListener {

    private final RedisCacheService redisCacheService;

    public CacheEventListener(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEventChanged(EventServiceEvent eventInfo){

        redisCacheService.delete(CacheKeys.eventWithSlotCountList(eventInfo.getUserId()));

        if(eventInfo.getEventId() != null) {
            redisCacheService.delete(CacheKeys.eventById(eventInfo.getUserId(), eventInfo.getEventId()));
            redisCacheService.delete(CacheKeys.invitationListByEventId(eventInfo.getUserId(), eventInfo.getEventId()));
            redisCacheService.deleteByPatternScan(CacheKeys.invitationListBySlotIdPattern(eventInfo.getUserId(), eventInfo.getEventId()));
            redisCacheService.deleteByGroup(CacheKeys.groupInvitationTokens(eventInfo.getEventId()), token-> CacheKeys.invitationByToken(token));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSlotChanged(SlotServiceEvent eventInfo) {
        redisCacheService.delete(CacheKeys.eventWithSlotCountList(eventInfo.getUserId()));
        redisCacheService.delete(CacheKeys.slotListByEventId(eventInfo.getUserId(), eventInfo.getEventId()));
        redisCacheService.delete(CacheKeys.invitationListByEventId(eventInfo.getUserId(), eventInfo.getEventId()));
        redisCacheService.deleteByPatternScan(CacheKeys.invitationListBySlotIdPattern(eventInfo.getUserId(), eventInfo.getEventId()));
        redisCacheService.deleteByGroup(CacheKeys.groupInvitationTokens(eventInfo.getEventId()), token-> CacheKeys.invitationByToken(token));

        if(eventInfo.getSlotId() != null) {
            redisCacheService.delete(CacheKeys.slotById(eventInfo.getUserId(), eventInfo.getEventId(), eventInfo.getSlotId()));
            redisCacheService.delete(CacheKeys.invitationListBySlotId(eventInfo.getUserId(), eventInfo.getEventId(), eventInfo.getSlotId()));
        }

    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvitation(InvitationServiceEvent eventInfo) {
        redisCacheService.delete(CacheKeys.invitationListByEventId(eventInfo.getUserId(), eventInfo.getEventId()));

        if(!eventInfo.getSlotIds().isEmpty()){
            for(Long slotId : eventInfo.getSlotIds()){
                redisCacheService.delete(CacheKeys.invitationListBySlotId(eventInfo.getUserId(), eventInfo.getEventId(), slotId));
            }
        }

        if(eventInfo.getInvitationToken() != null){
            redisCacheService.delete(CacheKeys.invitationByToken(eventInfo.getInvitationToken()));
            redisCacheService.removeSetValueFromGroup(CacheKeys.groupInvitationTokens(eventInfo.getEventId()),eventInfo.getInvitationToken());
        }
    }

}
