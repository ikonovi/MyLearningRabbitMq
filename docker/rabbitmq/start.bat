REM docker run -d --rm -p 5672:5672 -p 15672:15672 --hostname rabbit-srv --name my-rabbit rabbitmq:3.8.19-management-alpine

docker run -d ^
    -p 5672:5672 -p 15672:15672 ^
    --hostname rabbit-srv ^
    --name my-rabbit ^
    rabbitmq:3.8.19-management-alpine
REM    bitnami/rabbitmq

REM docker start my-rabbit