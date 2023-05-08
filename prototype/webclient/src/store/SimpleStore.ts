



/*export interface simpleStoreIF{
    toolname: ToolnameStore,
    toolImage: ToolImageStore,
    strokes: StokesStore,
    counters: CounterStore,
    headImage: HeadImageStore
}*/
import ObsStore from "@/store/ObsStore";

export interface simpleStoreIF{
    [index: string]: SimpleStore;
}



export const simpleStore:simpleStoreIF = {
    obs:new ObsStore(),

};

export default {
    install(Vue:any, options:any) {
        Vue.prototype.$sstore = simpleStore;
    },
    update(){
      for (let key in simpleStore){
          simpleStore[key].update();
      }
    }

};


export interface SimpleStore {
    state: any

    update():any

}
