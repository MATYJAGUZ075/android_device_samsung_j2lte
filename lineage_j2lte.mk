# Producto j2lte del J2.
#
# El contenido comun esta en lineage_j2lte_common.mk. Este fichero solo fija
# el nombre del producto y el device, que el build necesita poder resolver
# en tiempo de parseo: con un unico .mk parametrizado por TARGET_DEVICE, el
# core de Android se quejaba con
#   error: COMMON_LUNCH_CHOICES contains products(s) not defined in this file
#
# Los tres modelos comparten SoC (Exynos3475), device tree, kernel y vendor.
# ro.product.model lo ajusta libinit_j2lte en el arranque segun la variante
# de hardware, asi que aqui se deja el del modelo base.

$(call inherit-product, $(SRC_TARGET_DIR)/device/samsung/j2lte/lineage_j2lte_common.mk)

PRODUCT_NAME := lineage_j2lte
PRODUCT_DEVICE := j2lte
