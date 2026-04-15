export class AuthSession {
    token!: string;
    type!: 'USER' | 'GUEST';
    expiry!: number;
}
