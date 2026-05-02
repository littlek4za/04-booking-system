import { TokenType } from "../model/token-type";

export class GuestAccessTokenDto {

    accessToken!: string;
    expiresAt!: string;
    tokenType!: TokenType;
}
