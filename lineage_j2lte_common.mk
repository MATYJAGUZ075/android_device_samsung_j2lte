#
# Copyright (C) 2018 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := device/samsung/j2lte

# Inherit from those products. Most specific first.
$(call inherit-product, $(LOCAL_PATH)/device.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# The J2 shipped with Android 5.1.1 (L-MR1, API 22).
# product_launched_with_l_mr1.mk still exists in AOSP 13 and sets the
# shipping API level correctly for VINTF/sepolicy purposes.
$(call inherit-product, $(SRC_TARGET_DIR)/product/product_launched_with_l_mr1.mk)

# Inherit common Lineage phone.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

# Set those variables here to overwrite the inherited values.
# Los tres modelos del J2 (j2lte LTE, j2ltedd y j2ltedx dual SIM) comparten
# el mismo SoC, el mismo device tree y el mismo vendor, asi que el producto se
# parametriza por TARGET_DEVICE en vez de estar fijado a j2lte.
#
# ro.product.model lo ajusta libinit_j2lte en el arranque segun la variante de
# hardware (J200F/G/GU/M/BT/Y), asi que PRODUCT_MODEL se deja como el del
# modelo base.
# PRODUCT_NAME y PRODUCT_DEVICE los fija cada wrapper (lineage_j2lte.mk,
# lineage_j2ltedd.mk, lineage_j2ltedx.mk): el core de Android los necesita
# resueltos en tiempo de parseo.
PRODUCT_NAME := lineage_j2lte
PRODUCT_DEVICE := j2lte
PRODUCT_MODEL := SM-J200M
PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung
PRODUCT_GMS_CLIENTID_BASE := android-samsung

# NOTE: ro.product.model is overridden per hardware variant (J200F/G/GU/M/BT/Y)
# at boot by libinit_j2lte (see init/init_j2lte.cpp).
#
# ro.build.fingerprint: antes se fijaba al de stock, samsung/j2ltejv/j2lte:5.1.1
# (Android 5.1.1, 2014), solo para conservar la "identidad de stock". El problema
# es que ART usa el fingerprint como clave para buscar la base de datos de core
# platform API, y un 5.1.1 no puede existir en una base de datos de A13. Se
# observa en el arranque que zygote muere justo despues de loguear
#   I zygote: Core platform API reporting enabled, enforcing=false
# que es justo la linea anterior a esa busqueda.
#
# El nivel de API de envio para VINTF/sepolicy no depende del fingerprint: lo
# fija product_launched_with_l_mr1.mk, mas abajo. Asi que declarar un
# fingerprint coherente con la API real (13) no cambia la compatibilidad VINTF.
PRODUCT_BUILD_PROP_OVERRIDES += \
    PRODUCT_NAME=j2ltejv \
    PRIVATE_BUILD_DESC="j2ltejv-user 13 TQ3A.230901.001 userdebug test-keys"

BUILD_FINGERPRINT := lineage/$(TARGET_DEVICE)/$(TARGET_DEVICE):13/TQ3A.230901.001/20260926:userdebug/test-keys
