FROM python:3.12
ADD app.py .
ADD requirements.txt .
RUN pip3 install -r requirements.txt \
 && pip3 install --no-deps splunk-opentelemetry==2.13.3
EXPOSE 8090
# ensure our flask server is visible from the network (not just localhost)
CMD ["opentelemetry-instrument", "flask", "run", "--host", "0.0.0.0", "-p", "8090"]
