# device tree de j2lte — LineageOS 20 (Android 13)

Samsung Galaxy J2 (SM-J200F/G/GU/M/BT/Y) — Exynos3475, ARMv7 32-bit.

**Estado: FASE 0 (skeleton).** Estructura LOS20 creada; los HALs aún no compilan
contra T. Ver `ANALISIS_LINEAGE17_J2.md` y `ANALISIS_LINEAGE20_REFERENCIAS.md`
en la raíz del proyecto para el plan completo por fases.

## Pendiente por fase

| Fase | Contenido |
|---|---|
| 1 | Kernel 3.10.9 + clang + backports (eBPF, binder freezer) |
| 2 | Gráficos (gralloc/HWC SLSI exynos5 + adapters) |
| 3 | VINTF/manifest ajustes + HALs básicas |
| 4 | Audio HAL 7.x, sensores, luces, power |
| 5 | RIL (radio@1.x estilo Exynos7420 contra blob tss310) |
| 6 | Wi-Fi/BT/cámara runtime |

## Dependencias externas (ramas verificadas ago-2026)

- `LineageOS/android_hardware_samsung` → **lineage-20 existe**
- `LineageOS/android_hardware_samsung_slsi_{exynos,exynos5,openmax}` → máx.
  **lineage-19.1**: harán falta forks actualizados a T (marcados `optional`).
- Kernel: `Exynos3475/android_kernel_samsung_exynos3475` → `kernel/samsung/exynos3475`
  (inconsistencia del 17.1 corregida en lineage.dependencies).

## Blobs marcados para investigación

Ver `proprietary-files.txt`: varios blobs provienen de dispositivos Exynos7580
(SM-A310F/A510F stock) reutilizados por compatibilidad binaria; están anotados
con `[7580-origin]`.
