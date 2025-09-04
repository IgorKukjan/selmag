docker run --name catalogue-db -p 5433:5432 -e POSTGRES_DB=catalogue -e POSTGRES_USER=catalogue -e POSTGRES_PASSWORD=catalogue postgres:16

docker run --name selmag-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm

docker run --name selmag-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v /D/IntelliJProjects/selski/basic-web-application/config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm


docker run --name feedback-db -p 27017:27017  mongo:7

docker exec -it feedback-db mongosh feedback

db.productReview.find()
db.favouriteProduct.find()