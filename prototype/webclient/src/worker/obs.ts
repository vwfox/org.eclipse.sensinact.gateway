import {Configuration, Observation, Observations, ObservationsApi} from "../../openapi/client";
import obserservation_karl from "@/Mock/observation_karl.json";
import {AxiosResponse} from "axios";
import {getBaseUrl} from "@/config/base";
import conf from '@/config/mqtt.json';


let points:any = [];
const state = {
  obs: {} as any,
};
let timer:number|null = null;

const setPoints = (apoints:any)=>{
  points = apoints;
}

self.addEventListener("message", evt => {
  if(evt.data &&  evt.data.command == 'setPoints') setPoints(evt.data.payload);
  if(evt.data &&  evt.data.command == 'start') start();
  if(evt.data &&  evt.data.command == 'stop') stop();
});

const getDataForPoints =async ()=>{
  console.log('getData')
  const proms:Promise<Observations>[] = []
  points?.forEach((point:any)=> {

    if (point["@iot.id"] === 'karl') {
      proms.push(new Promise((res,rej)=>{
        res({
          value: [
            {
              "@iot.id": "karl",
              "result": obserservation_karl
            }
          ]
        } as unknown as Observations);
      }));
    } else{

      //@ts-ignore
      proms.push(
        new Promise(async (res, rej) => {
          try {
            //@ts-ignore
            const result: AxiosResponse<> = await new ObservationsApi(new Configuration({basePath: getBaseUrl()})).v11ObservationsEntityIdDatastreamObservationsGet(point["@iot.id"]);
            if (result.data && result.data.value && result.data.value[0]) {
              //@ts-ignore
              (result.data.value[0] as LocationsPlus)["@iot.id"] = point["@iot.id"];
              /*if( point["@iot.id"] == 'FelsenkellerRadAuto~conflict~conflict'){
                result.data.value[0].result = true;
              }*/
              res(result.data);
            }
            else{
              //res( {value:[{result:true,"@iot.id":point["@iot.id"]}]}as Observations);
              rej(null)
            }

          } catch (e) {
            rej(e)
          }
        }));
    }
  })
  const promsSettled = await Promise.allSettled(proms);
  //this.obs= new Map();
  promsSettled.forEach((obj:any) => {
    if(obj.value  && obj.value.value && obj.value.value[0]){
      const value:string = obj.value.value[0]["@iot.id"] as string
      state.obs[value]= obj.value.value[0];
    }

  });
  postMessage(state);


}
const start = ()=>{
  if(!timer){
    timer = self.setInterval(()=>getDataForPoints(),conf.obsRefreshIntervall)
  }
}
const stop = ()=>{
  if(timer) {
    clearInterval(timer)
    timer = null;
  }
}
