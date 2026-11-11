# For Live Demo:
Please check on: https://demo.essra.net


# For Development:
POST http://localhost:8080/api/v1/auth/login

{
  "username": "superadmin",
  "password": "$PoS12345"
}

{
  "username": "admin",
  "password": "$PoS12345"
}

{
  "username": "cashier",
  "password": "$PoS12345"
}

GET http://localhost:8080/api/v1/auth/me
    Header: Authorization: Bearer <token>
    Header (opsional): X-Store-Id: 1 (untuk admin/cashier)

POST http://localhost:8080/api/v1/auth/logout

## Dev Phase:
# Run App:
$ mvn flyway:migrate


## Build Package:
$ mvn clean package -DskipTests 

## Upload JAR:
$ scp -P [port] target/*.jar [user]@[ip_address]:/[path]/openjdk/target/application.jar

## Upload SQL files:
$ scp -P [port] src/main/resources/db/migration/*.sql [user]@[ip_address]:/[path]/db/migration/

## Restore DB:
$ psql -U [user] -d [db_name] < [filename].sql

## Backup DB:
$ pg_dump -U [user] -d [db_name] -f [filename].sql

## Execute DDL from outside container:
$ docker exec -i [container_name] psql -U [user] -d [db_name] < V9.sql

## Deployment JAR:
$ docker cp openjdk/target/application.jar [container_name]:/application.jar

## Clear container logs:
$ sh -c 'truncate -s 0 /var/lib/docker/containers//-json.log'
