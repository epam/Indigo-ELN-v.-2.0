#build eln-service (transitively builds eln-core)
cd backend && ./gradlew :eln:eln-service:quarkusBuild -Dquarkus-profile=devtest

#docker compose
cd ..
cd deployment-compose
docker compose -f docker-compose.yml up --build

