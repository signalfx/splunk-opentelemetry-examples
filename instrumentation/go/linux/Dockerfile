FROM golang:1.22.9

WORKDIR /app

COPY go.mod go.sum ./

RUN go mod download

COPY *.go ./

RUN CGO_ENABLED=0 GOOS=linux go build -o /helloworld-go

EXPOSE 8080

CMD [ "/helloworld-go" ]
