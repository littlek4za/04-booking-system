import { Routes } from '@angular/router';
import { LoginComponent } from './components/login-component/login-component';
import { WelcomeComponent } from './components/welcome-component/welcome-component';
import { NoPageComponent } from './components/no-page-component/no-page-component';
import { RegisterComponent } from './components/register-component/register-component';
import { RoleSelectComponent } from './components/role-select-component/role-select-component';
import { AuthGuard } from './guards/auth-guard';
import { GuestGuard } from './guards/guest-guard';
import { AuthDebug } from './debug/auth-debug/auth-debug';
import { EventDashboardComponent } from './components/event-dashboard-component/event-dashboard-component';
import { InvitationManagerComponent } from './components/invitation-manager-component/invitation-manager-component';
import { AddSlotWizard } from './components/add-slot-wizard/add-slot-wizard';
import { LeafletMapSelection } from './components/leaflet-map-selection/leaflet-map-selection';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [GuestGuard] },
    { path: 'roleSelect', component: RoleSelectComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'welcome', component: WelcomeComponent },
    { path: 'authDebug', component: AuthDebug },
    { path: 'eventDashboard', component: EventDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'invitation', component: InvitationManagerComponent},
    { path: 'mapSelection', component: LeafletMapSelection},
    { path: 'addSlot', component: AddSlotWizard, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: '', component: WelcomeComponent },
    { path: '**', component: NoPageComponent }
];
