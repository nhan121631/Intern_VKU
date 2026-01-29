import psycopg2

conn = psycopg2.connect(
    host="aws-1-ap-southeast-1.pooler.supabase.com",
    port=5432,
    dbname="postgres",
    user="postgres.qzfrjxmavmkjgahsnmti",
    password="Nhan@12163@",
    sslmode="require"
)

cur = conn.cursor()
cur.execute("SELECT version();")
print("Connected:", cur.fetchone())

cur.close()
conn.close()
