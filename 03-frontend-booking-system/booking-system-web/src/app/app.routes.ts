import { Routes } from '@angular/router';
import { LoginComponent } from './components/login-component/login-component';
import { WelcomeComponent } from './components/welcome-component/welcome-component';
import { NoPageComponent } from './components/no-page-component/no-page-component';
import { RegisterComponent } from './components/register-component/register-component';
import { RoleSelectComponent } from './components/role-select-component/role-select-component';
import { AuthGuard } from './guard/auth-guard';
import { GuestGuard } from './guard/guest-guard';
import { AuthDebug } from './debug/auth-debug/auth-debug';
import { EventDashboardComponent } from './components/event-dashboard-component/event-dashboard-component';
import { InvitationManagerComponent } from './components/invitation-manager-component/invitation-manager-component';
import { AddEventWizard } from './components/add-event-wizard/add-event-wizard';
import { AddSlotWizard } from './components/add-slot-wizard/add-slot-wizard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [GuestGuard] },
    { path: 'roleSelect', component: RoleSelectComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'welcome', component: WelcomeComponent },
    { path: 'authDebug', component: AuthDebug },
    { path: 'eventDashboard', component: EventDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'invitation', component: InvitationManagerComponent},
    { path: 'addEvent', component: AddEventWizard, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: 'addSlot', component: AddSlotWizard, canActivate: [AuthGuard], data: { roles: ['ROLE_ORGANIZER', 'ROLE_ATTENDEE', 'ROLE_ADMIN'] } },
    { path: '', component: WelcomeComponent },
    { path: '**', component: NoPageComponent }
];
