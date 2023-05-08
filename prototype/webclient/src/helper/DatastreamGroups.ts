import {Datastream, Datastreams} from "../../openapi/client";

export default function groupByName(datastreams:Datastreams){
  let ret:any = {}

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

export function groupByCategory(datastreams:Datastreams){
  let ret:any = {}

  datastreams.value?.forEach((datastream:Datastream)=>{
    //@ts-ignore
    let type = datastream.properties['sensorthings.datastream.type'];
    if(datastream.properties && type && datastream.name) {
      if (!ret[type]) {
        ret[type] = [];
      }
      ret[type].push(datastream);
    }
  })
  return ret;
}
