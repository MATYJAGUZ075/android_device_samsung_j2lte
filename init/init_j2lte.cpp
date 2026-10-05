/*
   Copyright (c) 2016, The CyanogenMod Project. All rights reserved.
   Copyright (c) 2017-2020, The LineageOS Project. All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
   NON-INFRINGEMENT ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <cstdlib>
#include <cstring>
#include <vector>

#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/properties.h>
#include <android-base/strings.h>

#include "property_service.h"
#include "vendor_init.h"

using android::base::GetProperty;
using android::base::ReadFileToString;
using android::base::Trim;

// Orden de fuentes de ro.product.* (copiado de build/tools/releasetools,
// igual que en los portes legacy a LOS 20).
std::vector<std::string> ro_product_props_default_source_order = {
    "",
    "odm.",
    "vendor.",
    "system.",
    "system_ext.",
    "product.",
};

void property_override(char const prop[], char const value[], bool add = true)
{
    auto pi = (prop_info *) __system_property_find(prop);

    if (pi != nullptr) {
        __system_property_update(pi, value, strlen(value));
    } else if (add) {
        __system_property_add(prop, strlen(prop), value, strlen(value));
    }
}

void property_override_dual(char const system_prop[],
        char const vendor_prop[], char const value[])
{
    property_override(system_prop, value);
    property_override(vendor_prop, value);
}

void set_sim_info()
{
    const char *simslot_count_path = "/proc/simslot_count";
    std::string simslot_count;

    if (ReadFileToString(simslot_count_path, &simslot_count)) {
        simslot_count = Trim(simslot_count); // strip newline
        property_override("ro.vendor.multisim.simslotcount", simslot_count.c_str());

        /*
         * persist.radio.multisim.config must always be set. rild reads it to
         * decide how many modems to bring up, and on Android 13 it does not
         * start at all when the property is absent: no ril.RildInit, no
         * gsm.version.baseband, and the telephony stack never registers.
         *
         * It used to be assigned only inside the dual-SIM branch, so on a
         * single-SIM device it was never assigned at all. Set the single-SIM
         * default first and let the dual-SIM case override it.
         *
         * The simslotcount property also goes through the vendor namespace,
         * which is where the framework looks for vendor-prefixed properties.
         */
        property_override("persist.radio.multisim.config", "ss");
        if (simslot_count.compare("2") == 0) {
            property_override("rild.libpath2", "/system/lib/libsec-ril-dsds.so");
            property_override("persist.radio.multisim.config", "dsds");
        }
    } else {
        LOG(ERROR) << "Could not open '" << simslot_count_path << "'";
    }
}

void vendor_load_properties()
{
    std::string bootloader = GetProperty("ro.bootloader", "");
    std::string device;

    // Detectar variante por bootloader: SM-J200F/G/GU/M/BT/Y.
    if (bootloader.find("J200F") != std::string::npos) {
        /* SM-J200F */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200F");
    } else if (bootloader.find("J200G") != std::string::npos) {
        /* SM-J200G */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200G");
    } else if (bootloader.find("J200GU") != std::string::npos) {
        /* SM-J200GU */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200GU");
    } else if (bootloader.find("J200M") != std::string::npos) {
        /* SM-J200M */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200M");
    } else if (bootloader.find("J200BT") != std::string::npos) {
        /* SM-J200BT */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200BT");
    } else if (bootloader.find("J200Y") != std::string::npos) {
        /* SM-J200Y */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200Y");
    } else {
        /* Fallback: SM-J200M (variante objetivo de este porte) */
        property_override_dual("ro.product.model", "ro.vendor.product.model", "SM-J200M");
    }

    // El fingerprint NO se sobreescribe aqui a proposito. Antes se fijaba al de
    // stock (5.1.1) para conservar la identidad de stock, pero ART lo usa como
    // clave de la base de datos de core platform API y un 5.1.1 no existe en
    // A13. El fingerprint coherente lo define BUILD_FINGERPRINT en
    // lineage_j2lte.mk. Este modulo de init solo selecciona el modelo de
    // hardware por variante (J200F/G/GU/M/BT/Y).

    set_sim_info();

    device = GetProperty("ro.product.device", "");
    LOG(ERROR) << "Found bootloader id '" << bootloader
               << "' setting build properties for '" << device << "' device";
}
