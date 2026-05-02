import { UserAccessTokenDto } from "./user-access-token-dto";
import { UserDto } from "./user-dto";

export class LoginResponseDto {
    userDto!: UserDto;
    userAccessTokenDto!: UserAccessTokenDto;
}
