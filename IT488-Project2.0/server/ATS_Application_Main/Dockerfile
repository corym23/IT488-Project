FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src

# Copy project files and restore/build (assumes a .csproj exists in this folder)
COPY . ./

# Attempt to publish any .csproj in this directory - adjust project filename if needed
RUN set -e; \
    for proj in *.csproj; do \
    if [ -f "$proj" ]; then \
    dotnet publish "$proj" -c Release -o /app/publish -r linux-x64 --self-contained false; \
    fi; \
    done || true

FROM mcr.microsoft.com/dotnet/aspnet:6.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish ./

EXPOSE 80
CMD ["sh", "-c", "dotnet \"$(ls /app/*.dll | head -n1)\""]
