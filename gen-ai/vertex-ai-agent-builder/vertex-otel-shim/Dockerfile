FROM python:3.12-slim

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PORT=8080

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY converter.py main.py ./

# Cloud Run uses PORT env var
CMD exec gunicorn --bind :$PORT --workers 2 --threads 4 --timeout 30 main:app