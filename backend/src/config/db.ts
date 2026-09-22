import { Pool } from 'pg';
import dotenv from 'dotenv';

dotenv.config();

export const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

// Log connection details on startup
pool.connect((err, client, release) => {
  if (err) {
    return console.error('❌ Database connection error:', err.stack);
  }
  client?.query('SELECT current_database(), current_user', (err, result) => {
    release();
    if (err) {
      return console.error('❌ Error executing query', err.stack);
    }
    console.log(`🐘 PostgreSQL Connected to Database: [${result.rows[0].current_database}] as User: [${result.rows[0].current_user}]`);
  });
});

// For backward compatibility with the user's existing imports, though it's not Prisma anymore
export const db = {
  query: (text: string, params?: any[]) => pool.query(text, params),
};

console.log('🐘 PostgreSQL Pool initialized');
