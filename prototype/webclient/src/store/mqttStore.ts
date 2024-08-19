import Vue, { ref, computed } from 'vue'
//@ts-ignore
import mqtt, {MqttClient} from 'mqtt';

import conf from '@/config/mqtt.json';
import {SimpleStore} from "@/store/SimpleStore";
export default class MqttStore implements SimpleStore {

  private client?:MqttClient = undefined;
  private timer?:number = undefined;
  update() {
      throw new Error('Method not implemented.');
  }


  public state = {
    features: {} as any,
    config:{}as any,
    connected:false,

  };

  async connect(){
    console.log('starting connection...')
    this.client =  mqtt.connect(conf.url,{port:(conf.port as number),username:conf.user,password:conf.password});
    const prom = new Promise((resolve)=>{
      this.client?.on("connect", () => {
        console.log('connected')
        resolve(null)
      });
    });
    await prom;
  }
  async subscribe(){
    console.log('subscription')
    await new Promise((resolve, reject)=>{
      this.client?.subscribe(conf.topic,(err:any)=>{
        this.setCleaner();
        if(!err)resolve(null)
        reject(err)
      });
    });

    console.log('subscripted');

    this.client?.on('message',(toptic:string,msg:Buffer)=>{
      const topic_arr = toptic.split('/');
      const id = topic_arr[topic_arr.length-1];
      const type = topic_arr[topic_arr.length-2];
      const msgJ = JSON.parse(msg.toString());
      //console.log(msgJ)

      if(msgJ.gpsCoordinates && msgJ.gpsCoordinates[0] && msgJ.gpsCoordinates[0].longitude && msgJ.gpsCoordinates[0].latitude && msgJ.speed){
        this.state.features[id]={
          "type":"Feature",
          "properties":{
            "type":type,
            "inTime":new Date().getTime(),
            "heading" : msgJ.gpsCoordinates[0].heading,
            "speed" : msgJ.speed,
            "id":id,
            "classId" : type
          },
          "geometry":{
            "type":"Point",
            "coordinates":
              [
                msgJ.gpsCoordinates[0].longitude,
                msgJ.gpsCoordinates[0].latitude
              ]

          }
        };
        //this.state.features = {...this.state.features}
      }


    })


  }
  setCleaner(){
    if(!this.timer){
      this.timer = window.setInterval(()=>{this.cleanUp()},conf.cleanIterval)
    }
  }

  cleanUp(){
    for (const id in this.state.features){

      if(this.state.features[id].properties.inTime + conf.cleanIterval < new Date().getTime()){
        delete  this.state.features[id];
      }
    }
    //this.state.features = {...this.state.features}
  }

}
