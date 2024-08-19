/*********************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Markus Hochstein
 **********************************************************************/

import {
  mdiBattery40, mdiCamcorder,
  mdiCloudPercent,
  mdiGauge,
  mdiGrain, mdiHazardLights,
  mdiThermometerLow,
  mdiTrafficLight,
  mdiWeatherDust
} from "@mdi/js";
import {mdiCamera} from "@mdi/js/commonjs/mdi";

export function getPath(id: string) {
  switch (id) {
    case 'tlc':
      return mdiTrafficLight
    case 'color':
      return mdiTrafficLight
    case 'battery':
      return mdiBattery40
    case 'PM_2_5':
    case 'PM_10':
    case 'PM_1':
      return mdiGrain
    case 'air_pressure':
      return mdiGauge
    case 'air_humidity':
      return mdiCloudPercent
    case 'air_qi':
      return mdiWeatherDust
    case 'air_temprature':
      return mdiThermometerLow
    case 'temperature':
      return mdiThermometerLow
    case 'traficam':
      return mdiCamera
    case 'traficam_observedArea':
      return mdiCamera
    case 'viewport':
      return mdiCamera
    case 'conflict':
      return mdiHazardLights
    case 'TrafiCam':
      return mdiCamera
    default:
      return null
  }
}
