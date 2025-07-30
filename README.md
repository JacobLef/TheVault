# TheVault

## Docker Configuration: Build and Run
````bash
    docker build -t banking-database .    
````
```bash
    docker run -p 8080:8080 banking-database
```

#### To use docker compose instead:
```bash
    docker compose up
```
#### To use docker compose started in the background:
````bash
    docker compose -d up
````