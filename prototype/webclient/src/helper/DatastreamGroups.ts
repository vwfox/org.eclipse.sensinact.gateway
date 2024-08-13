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


import {Datastream, Datastreams, Thing} from "../../openapi/client";
import config from '@/config/mqtt.json';
export default function groupByName(datastreams:Datastreams){
  const ret:any = {}

  datastreams.value?.forEach((datastream:Datastream)=>{
    if(datastream.name) {
      if (!ret[datastream.name]) {
        ret[datastream.name] = [];
      }
      ret[datastream.name].push(datastream);
    }
  })
  return ret;
}

export function groupByCategory(datastreams:Array<Datastream>|Array<Thing>|undefined){
  const ret:any = {}
  let type = "uncategorized";
  console.log(datastreams)
  if(datastreams){
        datastreams.forEach((datastream:Datastream|Thing)=>{
      //@ts-ignore
          if(datastream.properties && datastream.properties['sensorthings.datastream.type']){
            //@ts-ignore
            type = datastream.properties["sensorthings.datastream.type"].toString();
          }
          if(datastream['@iot.id'] && datastream['@iot.id']?.toString().split('~').length>2){
            type = datastream['@iot.id']?.toString().split('~')[2];
          }
          if(datastream.name && !config.DatastreamExclude.includes(type)) {
            if (!ret[type]) {
              ret[type] = [];
            }

              ret[type].push(datastream);


          }


        })
  }
  return ret;
}
export function groupByCategoryAndThing(datastreams:Array<Datastream>,things?:Array<Thing>){
  const ret:any = {}
  let type = "uncategorized";
  console.log(datastreams)
  if(datastreams){
    datastreams.forEach((datastream:Datastream)=>{
      //@ts-ignore
      if(datastream.properties && datastream.properties['sensorthings.datastream.type']){
        //@ts-ignore
        type = datastream.properties["sensorthings.datastream.type"].toString();
      }
      if(datastream['@iot.id'] && datastream['@iot.id']?.toString().split('~').length>2){
        type = datastream['@iot.id']?.toString().split('~')[2];
      }
      if(datastream.name && !config.DatastreamExclude.includes(type)) {
        if (!ret[type]) {
          ret[type] = [];
        }

        ret[type].push(datastream);


      }


    })
  }
  return ret;
}
