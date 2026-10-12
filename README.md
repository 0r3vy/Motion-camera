# Motion Camera (Pearl)

A client-side Fabric mod for **Minecraft 1.21.11** that smooths the camera **only after you throw an ender pearl** (while it flies and when you teleport). Everything else stays vanilla.

The camera never lags more than `maxOffset` blocks behind your real position, and it returns to vanilla as soon as the pearl has landed and the camera has caught up, so your crosshair stays accurate (important for crystal PvP). It is a visual effect only and changes nothing on the server.

Source: https://github.com/0r3vy/Motion-camera

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.5+
- Fabric API 0.141.6+1.21.11
- Java 21

## Build

```
.\gradlew.bat build
```

The jar is created at `build/libs/motion-camera-pearl-1.6.0.jar` (do not use the `-sources` jar).

## Commands

```
/motioncamera toggle
/motioncamera status
/motioncamera reload
/motioncamera speed <0.02-1>
/motioncamera fpspeed <0.02-1>
/motioncamera duration <100-10000>
/motioncamera maxoffset <0-64>
```

## Config

File: `.minecraft/config/motioncamera.json`

| Key | Default | Description |
|---|---|---|
| `enabled` | `true` | Turns the mod on or off |
| `speed` | `0.25` | Third-person smoothing (0.02 = very laggy, 1.0 = no smoothing) |
| `firstPersonSpeed` | `0.3` | First-person smoothing |
| `exceptionDuration` | `1500` | How long (ms) the effect stays active after your pearl stops flying |
| `maxOffset` | `6.0` | Max distance in blocks the camera may lag behind your real position (0 = unlimited). Lower values keep your crosshair closer to where you really are |

## License

MIT
