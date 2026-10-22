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

#pragma once

#include <android/hardware/light/2.0/ILight.h>

#include <hidl/HidlSupport.h>
#include <hidl/MQDescriptor.h>
#include <hidl/Status.h>
#include <map>

namespace android::hardware::light::V2_0::implementation {

// En A13 los tipos HIDL (hidl_vec, hidl_string, hidl_handle) viven en
// android::hardware dentro de <hidl/HidlSupport.h>. No existen los headers
// <hidl/Vec.h> ni <hidl/String.h> (en A11 si), por eso el include de
// arriba y no esos.
using ::android::hardware::hidl_handle;
using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::light::V2_0::ILight;
using ::android::hardware::light::V2_0::LightState;
using ::android::hardware::light::V2_0::Status;
using ::android::hardware::light::V2_0::Type;
using ::android::hardware::Return;
using ::android::hardware::Void;

class Light : public ILight {
  public:
    Return<Status> setLight(Type type, const LightState& state) override;
    Return<void> getSupportedTypes(getSupportedTypes_cb _hidl_cb) override;
    Return<void> debug(const hidl_handle& handle, const hidl_vec<hidl_string>& options) override;
};

}  // namespace android::hardware::light::V2_0::implementation

// Punto de entrada del passthrough.
//
// El simbolo tiene que estar FUERA del namespace y con extern "C", que es como
// lo declara la plataforma (hardware/interfaces/light/2.0/default/Light.h).
// Si se define dentro del namespace, el nombre exportado queda mangled
// (_ZN7android8hardware5light4V2_014implementation17HIDL_FETCH_ILightEPKc) y el
// passthrough, que lo busca con dlsym por el nombre plano, no lo encuentra:
//
//   E HidlServiceManagement: Passthrough lookup opened
//     android.hardware.light@2.0-impl.samsung.so but could not find symbol
//     HIDL_FETCH_ILight: undefined symbol: HIDL_FETCH_ILight. Keeping library
//     open.
//   E LegacySupport: Could not get passthrough implementation for
//     android.hardware.light@2.0::ILight/default.
extern "C" android::hardware::light::V2_0::ILight* HIDL_FETCH_ILight(const char* /* name */);
