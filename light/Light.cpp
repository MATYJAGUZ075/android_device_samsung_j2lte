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

#include "Light.h"

#include <fstream>
#include <string>

#include <log/log.h>

namespace android::hardware::light::V2_0::implementation {

namespace {

// El panel de este equipo expone el backlight por sysfs:
//   /sys/class/backlight/panel/brightness   (0 .. max_brightness)
// El rango del panel no es 0..255 sino 0..max_brightness (355 en este
// equipo), asi que el valor del framework se escala.
constexpr const char* kBacklightPath = "/sys/class/backlight/panel/brightness";
constexpr const char* kMaxBrightnessPath = "/sys/class/backlight/panel/max_brightness";

int readInt(const char* path, int fallback) {
    std::ifstream in(path);
    if (!in) return fallback;
    int value = fallback;
    in >> value;
    if (!in) return fallback;
    return value;
}

// El framework manda el brillo como color ARGB. La convencion de las luces
// heredadas replica el valor en los tres canales (dumpsys lights muestra
// "color=ff3e3e3e" para un brillo de 62) y la formula de referencia es
//   ((77*R) + (150*G) + (29*B)) >> 8
int colorToBrightness(uint32_t color) {
    unsigned int r = (color >> 16) & 0xff;
    unsigned int g = (color >> 8) & 0xff;
    unsigned int b = color & 0xff;
    return static_cast<int>((77 * r + 150 * g + 29 * b) >> 8);
}

bool writeBacklight(int brightness) {
    int maxBrightness = readInt(kMaxBrightnessPath, 255);
    if (maxBrightness <= 0) maxBrightness = 255;
    if (brightness < 0) brightness = 0;
    if (brightness > 255) brightness = 255;

    const int value = (brightness * maxBrightness) / 255;

    std::ofstream out(kBacklightPath);
    if (!out) {
        ALOGE("no se pudo abrir %s", kBacklightPath);
        return false;
    }
    out << value << "\n";
    out.flush();
    if (!out) {
        ALOGE("no se pudo escribir el brillo en %s", kBacklightPath);
        return false;
    }
    ALOGI("brillo %d/255 -> %d/%d", brightness, value, maxBrightness);
    return true;
}

}  // namespace

Return<Status> Light::setLight(Type type, const LightState& state) {
    // Solo se implementa la luz de retroiluminacion de la pantalla. Las
    // demas (notificaciones, teclado, botones) no existen en este equipo:
    // el vendor no trae ningun modulo de luces.
    if (type != Type::BACKLIGHT) {
        return Status::LIGHT_NOT_SUPPORTED;
    }
    if (!writeBacklight(colorToBrightness(state.color))) {
        return Status::UNKNOWN;
    }
    return Status::SUCCESS;
}

Return<void> Light::getSupportedTypes(getSupportedTypes_cb _hidl_cb) {
    // En A13 hidl_vec no tiene push_back(): se rellena un array y se pasa
    // con setToExternal, igual que hace la implementacion de referencia.
    Type *types = new Type[1];
    types[0] = Type::BACKLIGHT;
    {
        hidl_vec<Type> hidl_types{};
        hidl_types.setToExternal(types, 1);
        _hidl_cb(hidl_types);
    }
    delete[] types;
    return Void();
}

Return<void> Light::debug(const hidl_handle& /*handle*/,
                           const hidl_vec<hidl_string>& /*options*/) {
    return Void();
}

}  // namespace android::hardware::light::V2_0::implementation

namespace android::hardware::light::V2_0::implementation {

// Punto de entrada del passthrough: el servicio android.hardware.light@2.0-service
// lo busca con este simbolo.
ILight* HIDL_FETCH_ILight(const char* /* name */) {
    return new Light();
}

}  // namespace android::hardware::light::V2_0::implementation
