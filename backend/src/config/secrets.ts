import dotenv from 'dotenv';
dotenv.config();

function required(name: string): string {
  const v = process.env[name];
  if (!v || v.length < 16 || v.startsWith('your_')) {
    console.error(`❌ FATAL: ${name} must be set in backend/.env (16+ chars and not starting with "your_").`);
    console.error('Generate a random key by running: node -e "console.log(require(\'crypto\').randomBytes(32).toString(\'hex\'))"');
    process.exit(1);
  }
  return v;
}

export const JWT_ACCESS_SECRET = required('JWT_ACCESS_SECRET');
export const JWT_REFRESH_SECRET = required('JWT_REFRESH_SECRET');
