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

import {SimpleStore} from "@/store/SimpleStore";
import {Vue} from "vue-property-decorator";



export default class ObsStore implements SimpleStore {

  private worker = new Worker(new URL("@/worker/obs.ts", import.meta.url));
  constructor(...args:any) {
    this.worker.postMessage({command:'start'})
    this.worker.onmessage = (ev)=>{
      //console.log('msg in')
      this.state.obs = ev.data.obs;
    }
  }
  public state = Vue.observable({
    obs: {},
  });





  setPoints(points:any){
    this.worker.postMessage({command:'setPoints',payload:points})
  }

  update(): any {
  }

}
