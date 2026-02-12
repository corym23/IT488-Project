# Packaging & Run (Docker)

This repository can be packaged into Docker images and run via `docker-compose`.

Build and run locally (recommended):

1. Build and start all services:

```bash
docker-compose build --parallel
docker-compose up -d
```

2. Open the client at http://localhost:5173/home
   The web API will be available at http://localhost:4000

Notes:

- The client build stage accepts a build-arg `VITE_API_BASE`; the compose file sets that to `http://api:4000` so the built client will talk to the API service inside compose.
- If your .NET component is a desktop Windows Forms app, you should publish it with `dotnet publish -c Release -r win-x64 --self-contained true` and distribute the resulting installer/exe (Windows Forms cannot be reliably run in Linux containers).
- The `server/ATS_Application_Main/Dockerfile` attempts to `dotnet publish` any `*.csproj` found — adjust as needed for project name or runtime.

Publishing images:

Tag and push images to your registry of choice (Docker Hub / ACR):

```bash
docker build -t myorg/ats-client:1.0 ./client
docker build -t myorg/ats-api:1.0 ./server/web-api
docker build -t myorg/ats-core:1.0 ./server/ATS_Application_Main

docker push myorg/ats-client:1.0
docker push myorg/ats-api:1.0
docker push myorg/ats-core:1.0
```

CI suggestion:

- Add workflow that builds each image on push/tag, pushes to registry, and creates GitHub Release attaching generated test artifacts (Surefire XML + PDF summary).
