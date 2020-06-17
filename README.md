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

## REST API

Maintain & control supported collections

### Collections

#### Companies

All collections are using same model for get & update operations

```json
{
  "items": [...]
}
```

To update collection using curl:

```curl
curl -d "@/path/to/json/file.txt" -X POST http://<SERVER_URL>:8000/<COLLECTION_NAME> -H "Content-Type: application/json"
```

##### Endpoint 

`/companies`

|Method|Description|
|---|---|
| GET | List of supported companies
| POST | Update companies

##### Model 

```json
{
   "name":"Neste",
   "order":1,
   "hidden":false,
   "description":{
      "en":"In Riga - prices from the website, in the rest of Latvia - prices from waze",
      "lv":"Rīgā - cenas no mājaslapas, pārējā Latvijā - cenas no waze",
      "ru":"В Риге - цены с сайта, в остальной части Латвии - цены от waze",
      "lg":"Rīgā - cenas no mājaslapas, pārējā Latvijā - cenas no waze"
   },
   "homepage":"https://www.neste.lv/lv/content/degvielas-cenas",
   "logo":{
      "2x":"http://162.243.16.251/fuel_hunter/neste/neste_logo@2x.png",
      "3x":"http://162.243.16.251/fuel_hunter/neste/neste_logo@3x.png"
   },
   "largeLogo":{
      "2x":"http://162.243.16.251/fuel_hunter/neste/neste_big_logo@2x.png",
      "3x":"http://162.243.16.251/fuel_hunter/neste/neste_big_logo@3x.png"
   },
   "mapLogo":{
      "2x":"http://162.243.16.251/fuel_hunter/neste/neste_map_logo@2x.png",
      "3x":"http://162.243.16.251/fuel_hunter/neste/neste_map_logo@3x.png"
   },
   "mapGrayLogo":{
      "2x":"http://162.243.16.251/fuel_hunter/neste/neste_map_gray_logo@2x.png",
      "3x":"http://162.243.16.251/fuel_hunter/neste/neste_map_gray_logo@3x.png"
   }
}
```

#### Stations

##### Endpoint 

`/stations`

|Method|Description|
|---|---|
| GET | List of supported stations
| POST | Update stations

##### Model 

```json
{
    "id":1,
    "company":"Circle K",
    "latitude":56.9622428,
    "longitude":23.7244566,
    "city":"Jūrmala",
    "address":"Kalēju iela 4",
    "name":"Circle K Automāts Jūrmala"
}
```