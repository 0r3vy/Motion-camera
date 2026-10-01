# Motion Camera (Pearl) 1.5.0 - Fabric 1.21.11

Source: https://github.com/0r3vy/Motion-camera

Camera chỉ mượt sau khi bạn ném ender pearl (lúc bay + lúc dịch chuyển), ngoài ra là vanilla.
Có bảo vệ cho PvP: khi đánh nhau camera giảm trễ để tâm ngắm vẫn chính xác.

Build: .\gradlew.bat build -> build\libs\motion-camera-pearl-1.5.0.jar (không dùng file -sources)
Cần: Fabric Loader 0.19.5+, Fabric API 0.141.6+1.21.11, Java 21.

Lệnh: /motioncamera toggle | status | reload | combat | preset pvp|smooth
      | speed <v> | fpspeed <v> | duration <ms> | maxoffset <blocks> | combatsmooth <0-1> | combatoffset <blocks>
Config: .minecraft\config\motioncamera.json
