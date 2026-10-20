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
#include <hidl/MQDescriptor.h>
#include <map>

namespace android::hardware::light::V2_0::implementation {

class Light : public ILight {
  public:
    Return<Status> setLight(Type type, const LightState& state) override;
    Return<void> getSupportedTypes(Output<vec<Type>> types) override;
};

}  // namespace android::hardware::light::V2_0::implementation
