
//@ts-ignore
import mqtt, {MqttClient} from 'mqtt';
//@ts-ignore
import conf from '@/config/mqtt.json';


let client:MqttClient|undefined = undefined;
let timer:number|undefined  = undefined;
let timerPusher:number|undefined  = undefined;


const state = {
    features: {} as any,
    configs:{} as any,
    cams:{} as any,

  };


self.addEventListener("message", evt => {
    if(evt.data == 'connect') connect();
    if(evt.data == 'subscribe') subscribe();
});
   const connect=async ()=>{
    console.log('starting connection...')
    client =  mqtt.connect(conf.url,{port:(conf.port as number),username:conf.user,password:conf.password});
    const prom = new Promise((resolve)=>{
      client?.on("connect", () => {
        console.log('connected')
        resolve(null)
      });
    });
    await prom;
  }

   const subscribe=async ()=>{
    console.log('subscription')
    await new Promise((resolve, reject)=>{
      client?.subscribe(conf.topic,(err:any)=>{
        setCleaner();
        setPusher();
        if(!err)resolve(null)
        reject(err)
      });
    });

    console.log('subscripted');

    client?.on('message',(toptic:string,msg:Buffer)=>{
      const topic_arr = toptic.split('/');
      const id = topic_arr[topic_arr.length-1];
      const type = topic_arr[topic_arr.length-2];
      const thing = topic_arr[topic_arr.length-3];
      const msgJ = JSON.parse(msg.toString());
      //console.log(msgJ)
      if(topic_arr[topic_arr.length-1] == 'retained' && topic_arr[topic_arr.length-4] == 'config'){
        if(msgJ.scene && msgJ.scene.leftTop.longitude && msgJ.scene.leftTop.latitude){
          if(!state.configs[type]){
            state.configs[type] = {
              type: "FeatureCollection",
              properties:{
                thing:type,
              },
              features: {}
            };
          }
          state.configs[type].features[type]={
            "type":"Feature",
              "properties":{
              "type":type,
                "inTime":new Date().getTime(),
                "id":topic_arr[topic_arr.length-3],
                "classId" : type
            },
            "geometry":{
              "type":"Polygon",
                "coordinates":
              [[
                [msgJ.scene.leftTop.longitude,
                  msgJ.scene.leftTop.latitude],
                [msgJ.scene.rightTop.longitude,
                  msgJ.scene.rightTop.latitude],
                [msgJ.scene.rightBottom.longitude,
                  msgJ.scene.rightBottom.latitude],
                [msgJ.scene.leftBottom.longitude,
                  msgJ.scene.leftBottom.latitude],


              ]]

            }
          };
        }
        if(msgJ.location && msgJ.location.latitude && msgJ.location.longitude){
          state.cams[topic_arr[topic_arr.length-3]]={
            "type":"Feature",
            "properties":{
              "type":type,
              "inTime":new Date().getTime(),
              "id":topic_arr[topic_arr.length-3],
              "classId" : type
            },
            "geometry":{
              "type":"Point",
              "coordinates":
                [
                  msgJ.location.longitude,
                  msgJ.location.latitude
                ]

            }
          };
          console.log(state.cams)
          //this.state.features = {...this.state.features}
        }
      }

      if(msgJ.gpsCoordinates && msgJ.gpsCoordinates[0] && msgJ.gpsCoordinates[0].longitude && msgJ.gpsCoordinates[0].latitude && msgJ.speed){
        if(!state.features[thing]){
          state.features[thing] = {
            type: "FeatureCollection",
            properties:{
              thing:thing,
            },
            features: {}
          };
        }
        state.features[thing].features[id]={
          "type":"Feature",
          "properties":{
            "type":type,
            "inTime":new Date().getTime(),
            "heading" : msgJ.gpsCoordinates[0].heading,
            "speed" : msgJ.speed,
            "id":id,
            "thing":thing,
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
  const setCleaner = ()=>{
    if(!timer){
      timer = self.setInterval(()=>{cleanUp()},conf.cleanIterval)
    }
  }

const cleanUp=()=>{
  for (const things in state.features) {
    for (const id in state.features[things].features) {

      if (state.features[things].features[id].properties.inTime + conf.cleanIterval < new Date().getTime()) {
        delete state.features[things][id];
      }
    }
  }
    //this.state.features = {...this.state.features}
  }
const setPusher = ()=>{
  if(!timerPusher){
    timerPusher = self.setInterval(()=>{
      postMessage(state);
    },conf.renderIntervall)
  }
}

