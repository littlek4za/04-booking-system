import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login-component/login-component';
import { WelcomeComponent } from './shared/components/welcome-component/welcome-component';
import { NoPageComponent } from './shared/components/no-page-component/no-page-component';
import { RegisterComponent } from './features/auth/register-component/register-component';
import { RoleSelectComponent } from './shared/components/role-select-component/role-select-component';
import { AuthGuard } from './core/guards/auth-guard';
import { GuestGuard } from './core/guards/guest-guard';
import { AuthDebug } from './debug/auth-debug/auth-debug';
import { EventDashboardComponent } from './features/events/event-dashboard-component/event-dashboard-component';
import { LeafletMapSelection } from './shared/components/leaflet-map-selection/leaflet-map-selection';
import { SlotDashboardComponent } from '@features/slots/slot-dashboard-component/slot-dashboard-component';
import { InvitationAccessComponent } from '@features/invitations/invitation-access-component/invitation-access-component';
import { BookingDashboard } from '@features/booking/booking-dashboard/booking-dashboard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [GuestGuard] },
    { path: 'roleSelect', component: RoleSelectComponent },
    { path: 'welcome', component: WelcomeComponent },
    { path: 'authDebug', component: AuthDebug },
    { path: 'invitation', component: InvitationAccessComponent },
    { path: 'invitation/:token', component: InvitationAccessComponent },
    { path: 'eventDashboard', component: EventDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'mapSelection', component: LeafletMapSelection },
    { path: 'eventDashboard/:id/slots', component: SlotDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'booking/:token', component: BookingDashboard, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: '', component: WelcomeComponent },
    { path: '**', component: NoPageComponent }
];
