/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "android.hardware.light@2.0-service.samsung"

#include <android/hardware/light/2.0/ILight.h>

#include <hidl/LegacySupport.h>

using android::hardware::defaultPassthroughServiceImplementation;
using android::hardware::light::V2_0::ILight;

int main() {
    // El passthrough busca el simbolo HIDL_FETCH_ILight en la libreria
    // android.hardware.light@2.0-impl.samsung.so, que es la que implementa
    // el brillo sobre el sysfs del panel (Light.cpp).
    //
    // Ojo: el simbolo tiene que estar declarado FUERA del namespace y con
    // extern "C", si no el passthrough no lo encuentra por dlsym:
    //
    //   E HidlServiceManagement: Passthrough lookup opened
    //     android.hardware.light@2.0-impl.samsung.so but could not find symbol
    //     HIDL_FETCH_ILight: undefined symbol: HIDL_FETCH_ILight.
    //   E LegacySupport: Could not get passthrough implementation for
    //     android.hardware.light@2.0::ILight/default.
    //
    // Y el framework espera este servicio de forma sincrona (por ejemplo al
    // arrancar el NotificationManagerService), asi que si el passthrough falla
    // el boot se queda colgado ahi.
    return defaultPassthroughServiceImplementation<ILight>();
}
