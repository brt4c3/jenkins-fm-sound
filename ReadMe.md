# Jenkins FM Sound Plugin

This plugin takes JSON input and generates an FM-modulated sound WAV file from it. The sound is also playable via JavaScript using the Web Audio API.

## JSON Format
```json
{
  "data": ["H", "e", "l", "l", "o"]
}
```

## Build
```sh
mvn clean install
```

## Run Jenkins Locally
```sh
mvn hpi:run
```
