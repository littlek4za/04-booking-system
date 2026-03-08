import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@features/auth/auth-service';
import { InvitationService } from '@features/invitations/invitation-service';

@Component({
  selector: 'app-booking-dashboard',
  imports: [],
  templateUrl: './booking-dashboard.html',
  styleUrl: './booking-dashboard.css',
})
export class BookingDashboard {

  constructor(private route: ActivatedRoute, private router: Router, private invitationService: InvitationService, private authService: AuthService) { }

  ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token');
    this.validateToken(token);
  }

  validateToken(token: string | null) {
    if (token == null) {
      alert("No Token defined");
      this.router.navigate(['/invitation']);
    }
    if (token) {
      this.invitationService.validateInvitation(token).subscribe({
        next: (res) => {
          if (res.valid == false) {
            alert(res.reason);
          }
          if (res.requiredLogin == false) {
            this.loadInvitation(token);
          }
          if (res.requiredLogin && !this.authService.hasValidToken()) {
            alert("redirecting to login page");
            const currentUrl = this.router.url;
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: currentUrl }
            });
            return;
          }
          if (res.requiredLogin && this.authService.hasValidToken()) {
            this.loadInvitation(token);
          }
        },
        error: () => {
          alert("Token invalid");
          this.router.navigate(['/invitation']);
        }
      })
    }
  }

  loadInvitation(token: string) {
    throw new Error('Method not implemented.');
  }

}
