# device tree de j2lte — LineageOS 20 (Android 13)

Samsung Galaxy J2 (SM-J200F/G/GU/M/BT/Y) — Exynos3475, ARMv7 32-bit.

**Estado: adaptación avanzada.** La estructura principal ya fue adaptada a
LineageOS 20 / Android 13 y actualmente el árbol se encuentra en etapa de
compilación y pruebas sobre el J2.

Ver `ANALISIS_LINEAGE17_J2.md` y `ANALISIS_LINEAGE20_REFERENCIAS.md` en la raíz
del proyecto para el historial y las referencias utilizadas durante la
adaptación.

## Estado de los componentes

| Componente                       | Estado                                                                          |
| -------------------------------- | ------------------------------------------------------------------------------- |
| Kernel 3.10.9 + adaptación Clang | Adaptado y preparado para la compilación LOS20                                  |
| Device tree                      | Adaptado a la estructura de LineageOS 20                                        |
| Gráficos                         | Adaptación realizada; pendiente de validación final                             |
| VINTF / manifest                 | Adaptados para Android 13                                                       |
| HALs                             | Adaptados según las necesidades del árbol                                       |
| Audio / sensores / luces / power | Adaptados desde la base existente                                               |
| RIL                              | Integrado sobre la base existente; pendiente de validación final                |
| Wi-Fi / Bluetooth / cámara       | Integrados desde la base disponible; pendientes de validación en el dispositivo |

## Dependencias externas

* `LineageOS/android_hardware_samsung` → **lineage-20 existe**
* `LineageOS/android_hardware_samsung_slsi_{exynos,exynos5,openmax}` → máximo
  `lineage-19.1` en sus ramas originales; se utilizan forks/adaptaciones
  cuando es necesario para LOS20.
* Kernel: `Exynos3475/android_kernel_samsung_exynos3475` →
  `kernel/samsung/exynos3475`.

## Blobs

El archivo `proprietary-files.txt` fue reconciliado tomando como base el
inventario verificado del vendor de j2lte y los archivos comprobados del árbol
CM14.1 del J2.

Las entradas que provenían de dispositivos Exynos7580 (SM-A310F/A510F) fueron
eliminadas al no corresponder al hardware del J200. También se eliminaron las
entradas de NFC, ya que ese hardware no está presente en el J200.

Los blobs que todavía requieren una fuente verificable están marcados como
`PENDIENTE-DUMP` y permanecen comentados hasta disponer de un dump stock del
SM-J200M.
