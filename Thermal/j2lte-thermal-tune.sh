#!/system/bin/sh
# Limita la frecuencia maxima de la CPU y ajusta el governor interactive.
#
# El Exynos 3475 con cooling pasivo se recalienta mucho: se pasa de 70
# grados y el PMIC acaba reiniciando el equipo por proteccion termica
# (apagones en mitad de una carga, o al cargar con la pantalla encendida).
#
# Con el techo a 897000 la temperatura se queda entre 55 y 62 grados y
# desaparecen los reinicios por temperatura, a cambio de algo menos de
# velocidad punta.
#
# Se aplica a todas las CPUs: el SoC tiene mas de un cluster y cada uno
# tiene su propio dominio de frecuencia.
MAX_FREQ=897000

for p in /sys/devices/system/cpu/cpu*/cpufreq; do
    [ -d "$p" ] || continue

    # cpufreq_max_limit es el techo duro (no lo puede subir nadie mas alla).
    echo "$MAX_FREQ" > "$p/cpufreq_max_limit" 2>/dev/null
    echo "$MAX_FREQ" > "$p/scaling_max_freq" 2>/dev/null

    [ -d "$p/interactive" ] || continue
    echo 507000 > "$p/interactive/hispeed_freq" 2>/dev/null
    echo 95 > "$p/interactive/go_hispeed_load" 2>/dev/null
    echo "85 507000:95 897000:99" > "$p/interactive/target_loads" 2>/dev/null
    echo 80000 > "$p/interactive/above_hispeed_delay" 2>/dev/null
done
