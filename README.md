# Fuel Hunter - Backend

## Build

1. Build docker image
```docker
docker build -t sashjakk/fuel-hunter:<VERSION> .
```
2. Tag docker image with version
```docker
docker tag sashjakk/fuel-hunter:<VERSION> sashjakk/fuel-hunter:0.0.1
```
3. Push to hub
```docker
docker push sashjakk/fuel-hunter:<VERSION>
```
4. Run image as daemon
```docker
docker run -d -p 50051:50051 --name fuel-hunter sashjakk/fuel-hunter:<VERSION>
```
5. Check logs
```docker
docker logs -f fuel-hunter
```