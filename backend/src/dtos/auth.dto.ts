export interface UserDto {
  id: string;
  name: string;
  email: string;
  role: string;
}

export interface AuthResponseDto {
  success: boolean;
  accessToken: string;
  refreshToken: string;
  user: UserDto;
}

export function toUserDto(row: any): UserDto {
  return {
    id: row.id,
    name: row.name,
    email: row.email,
    role: row.role || 'VIEWER'
  };
}
