import dotenv from 'dotenv';
dotenv.config();

function required(name: string): string {
  const v = process.env[name];
  if (!v || v.length < 16) {
    console.error(`❌ FATAL: ${name} must be set in backend/.env (16+ chars)`);
    process.exit(1);
  }
  return v;
}

export const JWT_ACCESS_SECRET = required('JWT_ACCESS_SECRET');
export const JWT_REFRESH_SECRET = required('JWT_REFRESH_SECRET');
