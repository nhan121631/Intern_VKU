# Use Python 3.11 slim image
FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    default-libmysqlclient-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application files
# Copy application file
COPY gemini.py .

# NOTE: Do NOT copy `.env` or private keys into the image. Use bind-mounts or `--env-file` at runtime
# to provide sensitive files (see docker-compose.yml which mounts .env and my-backend-key.pem).

# Expose port
EXPOSE 5001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:5001')" || exit 1

# Run the application
CMD ["python", "-u", "gemini.py"]
