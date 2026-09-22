import { Pool, PoolClient } from 'pg';
import dotenv from 'dotenv';

dotenv.config();

const rawDbUrl = process.env.DATABASE_URL;

if (!rawDbUrl || (!rawDbUrl.startsWith('postgresql://') && !rawDbUrl.startsWith('postgres://'))) {
  console.error('❌ FATAL: DATABASE_URL is missing or invalid in backend/.env!');
  console.error('Expected format: postgresql://username:password@localhost:5432/database_name');
  process.exit(1);
}

// Mask password in logs
const safeDbUrlLog = rawDbUrl.replace(/:([^:@]+)@/, ':****@');
console.log(`🐘 Initializing PostgreSQL connection pool to: ${safeDbUrlLog}`);

export const pool = new Pool({
  connectionString: rawDbUrl,
});

pool.on('error', (err) => {
  console.error('❌ Unexpected PostgreSQL pool error:', err.message);
});

// Test connection on startup
pool.connect((err, client, release) => {
  if (err) {
    console.error('❌ Database connection failed:', err.message);
    release();
    return;
  }
  client?.query('SELECT current_database(), current_user, version()', (qErr, result) => {
    release();
    if (qErr) {
      console.error('❌ Database verification query failed:', qErr.message);
      return;
    }
    const dbName = result.rows[0].current_database;
    const dbUser = result.rows[0].current_user;
    console.log(`✅ PostgreSQL Connected successfully to Database: [${dbName}] as User: [${dbUser}]`);
  });
});

/**
 * Execute operations inside a database transaction block (BEGIN -> COMMIT / ROLLBACK)
 */
export async function withTransaction<T>(callback: (client: PoolClient) => Promise<T>): Promise<T> {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const result = await callback(client);
    await client.query('COMMIT');
    return result;
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

export const db = {
  query: (text: string, params?: any[]) => pool.query(text, params),
};
