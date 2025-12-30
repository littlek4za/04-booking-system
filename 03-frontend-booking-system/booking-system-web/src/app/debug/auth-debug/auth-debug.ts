import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-auth-debug',
  imports: [],
  templateUrl: './auth-debug.html',
  styleUrl: './auth-debug.css',
})
export class AuthDebug implements OnInit, OnDestroy {

  hasToken: boolean = false;

  constructor(private router: Router, public authService:AuthService) { }
  ngOnInit(): void {
    this.hasToken = !!localStorage.getItem('authToken');
  }

  ngOnDestroy(): void {
  }

}
