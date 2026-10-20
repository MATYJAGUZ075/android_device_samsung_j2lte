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

# Un .mk por cada lunch choice: el core de Android exige que cada producto
# listado en COMMON_LUNCH_CHOICES tenga su propio fichero, si no se queja con
# "COMMON_LUNCH_CHOICES contains products(s) not defined in this file".
PRODUCT_MAKEFILES := \
    $(LOCAL_PATH)/lineage_j2lte.mk \
    $(LOCAL_PATH)/lineage_j2ltedd.mk \
    $(LOCAL_PATH)/lineage_j2ltedx.mk

# Los tres modelos del J2: j2lte (LTE), j2ltedd y j2ltedx (dual SIM).
# Mismo device tree, mismo vendor y mismo kernel; cambia TARGET_DEVICE, que
# el producto usa para el nombre, el fingerprint y ro.product.device.
COMMON_LUNCH_CHOICES := \
    lineage_j2lte-user \
    lineage_j2lte-userdebug \
    lineage_j2lte-eng \
    lineage_j2ltedd-user \
    lineage_j2ltedd-userdebug \
    lineage_j2ltedd-eng \
    lineage_j2ltedx-user \
    lineage_j2ltedx-userdebug \
    lineage_j2ltedx-eng
