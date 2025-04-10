FROM python:3.12
ADD app.py .
ADD requirements.txt .
RUN pip3 install -r requirements.txt
EXPOSE 8090
# ensure our flask server is visible from the network (not just localhost)
CMD ["opentelemetry-instrument", "flask", "run", "--host", "0.0.0.0", "-p", "8090"]
