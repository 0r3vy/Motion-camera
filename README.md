# Motion Camera (Pearl) 1.5.1 - Fabric 1.21.11

Source: https://github.com/0r3vy/Motion-camera

Camera chỉ mượt sau khi bạn ném ender pearl (lúc bay + lúc dịch chuyển), ngoài ra là vanilla.
Có bảo vệ cho PvP: khi đánh nhau camera vẫn mượt một phần nhưng bám sát vị trí thật để tâm ngắm chính xác.
Chỉ là hiệu ứng nhìn phía client, không đổi gì phía server.

## Build
```
.\gradlew.bat build
```
Jar nằm ở `build\libs\motion-camera-pearl-1.5.1.jar` (không dùng file `-sources`).
Cần: Fabric Loader 0.19.5+, Fabric API 0.141.6+1.21.11, Java 21.

## Lệnh
`/motioncamera toggle | status | reload | combat | preset pvp|smooth | speed <v> | fpspeed <v> | duration <ms> | maxoffset <blocks> | combatsmooth <0-1> | combatoffset <blocks>`

## Config (`.minecraft\config\motioncamera.json`)
| Key | Mặc định | Ý nghĩa |
|---|---|---|
| enabled | true | Bật/tắt mod |
| speed | 0.4 | Độ mượt góc nhìn thứ 3 (0.02 rất trễ .. 1.0 không mượt) |
| firstPersonSpeed | 0.5 | Độ mượt góc nhìn thứ 1 |
| exceptionDuration | 900 | Giữ hiệu ứng bao lâu (ms) sau khi pearl hết bay |
| maxOffset | 6.0 | Camera không trễ quá N block (0 = không giới hạn) |
| combatCancel | true | Bật xử lý riêng khi combat |
| combatSmooth | 0.4 | Giữ lại bao nhiêu độ mượt khi combat (0 tắt, 1 như thường) |
| combatMaxOffset | 1.5 | Camera không trễ quá N block khi combat |

## License
MIT.
