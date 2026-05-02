export class AuthTokenPayload {
    sub!: string;
    firstName!: string;
    lastName!: string;
    email!: string;
    roles!: string[];
    iss!: string;
    iat!: number;
    exp!: number;
    tokenType!: string;
}
