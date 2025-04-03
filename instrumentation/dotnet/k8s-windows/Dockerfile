FROM mcr.microsoft.com/dotnet/framework/sdk:4.8 AS build
WORKDIR /app

# copy csproj and restore as distinct layers
COPY *.sln .
COPY aspnetapp/*.csproj ./aspnetapp/
COPY aspnetapp/*.config ./aspnetapp/
RUN nuget restore

# copy everything else and build app
COPY aspnetapp/. ./aspnetapp/
WORKDIR /app/aspnetapp
RUN msbuild /p:Configuration=Release -r:False


FROM mcr.microsoft.com/dotnet/framework/aspnet:4.8 AS runtime
WORKDIR /inetpub/wwwroot
COPY --from=build /app/aspnetapp/. ./

RUN REG ADD HKLM\SOFTWARE\Microsoft\.NETFramework /v LoaderOptimization /t REG_DWORD /d 1

RUN powershell.exe -Command \
  $module_url = 'https://github.com/signalfx/splunk-otel-dotnet/releases/latest/download/Splunk.OTel.DotNet.psm1'; \
  $download_path = Join-Path $env:temp 'Splunk.OTel.DotNet.psm1'; \
  Invoke-WebRequest -Uri $module_url -OutFile $download_path ; \
  Import-Module $download_path ; \
  Install-OpenTelemetryCore ; \
  Register-OpenTelemetryForIIS ; \
  $env:OTEL_RESOURCE_ATTRIBUTES='deployment.environment=test'