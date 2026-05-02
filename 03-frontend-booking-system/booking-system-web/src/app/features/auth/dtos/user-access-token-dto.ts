import { TokenType } from "../model/token-type";

export class UserAccessTokenDto {
    accessToken!: string;
    expiresAt!: string;
    tokenType!: TokenType; 
}
