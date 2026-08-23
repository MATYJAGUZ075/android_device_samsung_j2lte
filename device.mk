#
# Copyright (C) 2018-2026 The LineageOS Project
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

# Device overlay (static). TODO(fase 3+): migrar overlays compatibles a
# rro_overlays/ siguiendo el patrón de los portes Exynos legacy en LOS 20.
DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

# Audio
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/audio/mixer_paths.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_0.xml \
    $(LOCAL_PATH)/configs/audio/audio_effects.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_effects.xml

# Stack HIDL audio estilo LOS20 (referencia: universal7420-common).
# TODO(fase 4): audio.primary.universal3475 debe compilar contra la HAL 7.x
# de hardware/samsung/audio; audio_policy_configuration.xml requiere regen a
# version 7.0 (actualmente sigue siendo 1.0/3.0 del árbol 17.1).
PRODUCT_PACKAGES += \
    audio.primary.universal3475 \
    audio.r_submix.default \
    audio.usb.default \
    tinymix \
    android.hardware.audio.service \
    android.hardware.audio@7.1-impl:32 \
    android.hardware.audio.effect@7.0-impl:32

# Boot animation
TARGET_BOOTANIMATION_PRELOAD := true
TARGET_BOOTANIMATION_TEXTURE_CACHE := true
TARGET_SCREEN_HEIGHT := 960
TARGET_SCREEN_WIDTH := 540

# Bluetooth
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/bluetooth/bt_vendor.conf:system/etc/bluetooth/bt_vendor.conf

# HAL BT HIDL 1.0 custom (impl vive en universal3475-common/hardware/bluetooth).
# Enfoque validado por el porte Exynos7420 en LOS 20/21 (HIDL 1.0 sigue vivo).
PRODUCT_PACKAGES += \
    android.hardware.bluetooth@1.0-service

# Camera
# Wrapper legacy (CameraWrapper.cpp en common) contra camera.provider HIDL.
# TODO(fase 3): evaluar subida de provider 2.4 -> 2.5 (instancia legacy/0,
# como hace universal7420-common).
# TODO: app de cámara — Snap fue eliminada en T; evaluar Aperture de Lineage.
PRODUCT_PACKAGES += \
    camera.universal3475

# Graphics
# Device uses high-density artwork where available
PRODUCT_AAPT_CONFIG := large
PRODUCT_AAPT_PREF_CONFIG := hdpi
# A list of dpis to select prebuilt apk, in precedence order.
PRODUCT_AAPT_PREBUILT_DPI := hdpi xhdpi mdpi

# Keylayouts
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/keylayout/gpio_keys.kl:system/usr/keylayout/gpio_keys.kl \
    $(LOCAL_PATH)/keylayout/sec_touchkey.kl:system/usr/keylayout/sec_touchkey.kl

# Touch features — FIX-026: entrada retirada TEMPORALMENTE. Soong analiza
# hardware/samsung/hidl/touch/Android.bp (presente en .module_paths) pero NO
# emite el módulo al late-mk (0 menciones vs 32 de composer, run 32665631574).
# Causa raíz bajo investigación (N2). Revertir cuando se resuelva.

# Permissions
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.bluetooth_le.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.bluetooth_le.xml \
    frameworks/native/data/etc/android.hardware.camera.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.xml \
    frameworks/native/data/etc/android.hardware.camera.flash-autofocus.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.flash-autofocus.xml \
    frameworks/native/data/etc/android.hardware.sensor.accelerometer.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.accelerometer.xml \
    frameworks/native/data/etc/android.hardware.sensor.proximity.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.proximity.xml \
    frameworks/native/data/etc/android.hardware.telephony.gsm.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.telephony.gsm.xml \
    frameworks/native/data/etc/android.software.midi.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.software.midi.xml \
    frameworks/native/data/etc/handheld_core_hardware.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/handheld_core_hardware.xml \
    frameworks/native/data/etc/android.hardware.ethernet.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.ethernet.xml

# Ramdisk
PRODUCT_PACKAGES += \
    init.target.rc

# Radio
# TODO(fase 5): bloque RIL completo pendiente de re-trabajo. En T desapareció
# TARGET_PROCESS_SDK_VERSION_OVERRIDE; el modelo objetivo es el del porte
# Exynos7420: radio@1.2-1.4 + config + TARGET_USES_VND_SECRIL contra el blob
# libsec-ril.so (tss310).
# C-1 (22/08): bloque Q neutralizado para el primer build.
# android.hardware.radio.deprecated@1.0 fue eliminado de AOSP en R y puede no
# existir como modulo en LOS 20 -> riesgo "missing module".
PRODUCT_PACKAGES += \
    libprotobuf-cpp-full \
    modemloader \
    libxml2

# PRODUCT_COPY_FILES rild.rc se restaura en fase 5 junto al nuevo bloque RIL.

# Vendor security patch level (vendor blobs from G550FYXXU1CRF1)
# Declarado via VENDOR_SECURITY_PATCH en BoardConfig.mk.

# Wi-Fi
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/wifi/cred.conf:system/etc/wifi/cred.conf \
    $(LOCAL_PATH)/configs/wifi/wpa_supplicant_overlay.conf:$(TARGET_COPY_OUT_VENDOR)/etc/wifi/wpa_supplicant_overlay.conf \
    $(LOCAL_PATH)/configs/wifi/p2p_supplicant_overlay.conf:$(TARGET_COPY_OUT_VENDOR)/etc/wifi/p2p_supplicant_overlay.conf \
    $(LOCAL_PATH)/configs/wifi/filter_ie:system/etc/wifi/filter_ie

# Inherit from universal3475-common
$(call inherit-product, device/samsung/universal3475-common/device-common.mk)

# Call the proprietary setup
$(call inherit-product, vendor/samsung/j2lte/j2lte-vendor.mk)
