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
import { InvitationManagerComponent } from './features/invitations/invitation-manager-component/invitation-manager-component';
import { SlotEditWizard } from './features/slots/slot-edit-wizard/slot-edit-wizard';
import { LeafletMapSelection } from './shared/components/leaflet-map-selection/leaflet-map-selection';
import { SlotDashboardComponent } from '@features/slots/slot-dashboard-component/slot-dashboard-component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [GuestGuard] },
    { path: 'roleSelect', component: RoleSelectComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'welcome', component: WelcomeComponent },
    { path: 'authDebug', component: AuthDebug },
    { path: 'eventDashboard', component: EventDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'invitation', component: InvitationManagerComponent},
    { path: 'mapSelection', component: LeafletMapSelection},
    { path: 'eventDashboard/:id/slots', component: SlotDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: '', component: WelcomeComponent },
    { path: '**', component: NoPageComponent }
];
