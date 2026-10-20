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

#include <hidl/Status.h>
#include <hidl/types.h>

namespace android::hardware::light::V2_0::implementation {

using ::android::hardware::Return;
using ::android::hardware::Void;
using ::android::hardware::hidl_vec;

class Light : public ILight {
  public:
    Return<Status> setLight(Type type, const LightState& state) override;
    // El .hal declara "getSupportedTypes() generates (vec<Type> types)", y el
    // HIDL de A13 lo genera con la forma de callback (no con Output<>), que es
    // lo que exige la interfaz pura virtual.
    Return<void> getSupportedTypes(getSupportedTypes_cb _hidl_cb) override;
};

}  // namespace android::hardware::light::V2_0::implementation
