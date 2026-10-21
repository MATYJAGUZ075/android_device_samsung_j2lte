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
#include <hidl/HidlTransportSupport.h>

#include "Light.h"

using android::hardware::configureRpcThreadpool;
using android::hardware::joinRpcThreadpool;
using android::hardware::light::V2_0::ILight;
using android::hardware::light::V2_0::implementation::Light;
using android::sp;
using android::status_t;
using android::OK;

int main() {
    // Se registra la implementacion propia directamente.
    //
    // Antes se usaba defaultPassthroughServiceImplementation<ILight>(), que
    // delega en el HAL legacy del vendor. Pero en lineage-20 ese servicio
    // legacy no existe (hardware/samsung no tiene el directorio lights) y el
    // passthrough solo busca el "default service", asi que no habia nada que
    // delegar. La clase Light de este mismo arbol (Light.cpp) es la que
    // escribe el brillo en el sysfs del panel, asi que se registra ella misma.
    configureRpcThreadpool(1, true);

    sp<ILight> light = new Light;
    status_t status = light->registerAsService();
    LOG_ALWAYS_FATAL_IF(status != OK, "No se pudo registrar ILight/default");

    joinRpcThreadpool();
    return 0;
}
